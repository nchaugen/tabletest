/*
 * Copyright 2025-present Nils Christian Haugen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tabletest.junit;

import io.github.nchaugen.tabletest.junit.FactorySources;
import org.jspecify.annotations.NonNull;
import org.junit.platform.commons.support.conversion.ConversionException;
import org.junit.platform.commons.support.conversion.ConversionSupport;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.tabletest.junit.TableTestException.*;

/**
 * Responsible for finding and invoking a type converter method to convert a parsed value to the target
 * parameter type, falling back to JUnit conversion if no type converter is found.
 */
public class TypeConversion {

    /**
     * Converts a parsed value to the target parameter type using a type converter if available,
     * otherwise falls back to JUnit conversion.
     * <p>
     * Type converters will be looked for in the following order:
     * <ol>
     *     <li>Test class</li>
     *     <li>Any enclosing classes (in inside-out order)</li>
     *     <li>Kotlin top-level, static functions</li>
     *     <li>Any classes listed in @TypeConverterSources annotation for test class (in listed order)</li>
     *     <li>Any classes listed in @TypeConverterSources for enclosing classes (in inside-out order)</li>
     * </ol>
     *
     * @param value                  The parsed value to convert
     * @param targetType             The target type of the conversion
     * @param testClass              The test class to search for type converters
     * @param convertedValueSupplier A function that supplies the parsed value converted to match
     *                               the type converter's parameter type
     * @return the converted value
     * @throws TableTestException if the conversion fails
     */
    public static Object convert(
        Object value,
        ParameterType targetType,
        Class<?> testClass,
        Function<Parameter, Object> convertedValueSupplier
    ) {
        return findTypeConverter(targetType, typeConverterSearchPath(testClass))
            .map(converter ->
                invokeTypeConverter(
                    converter,
                    convertedValueSupplier.apply(converter.getParameters()[0]),
                    targetType
                ))
            .orElseGet(() -> fallbackToJUnitConversion(value, targetType, testClass));
    }

    /**
     * Searches the provided search path for a type converter for mapping a parsed value to the parameter type.
     * <p>
     * The type converter must be static and public and must take a single parameter.
     * The return type of the type converter must be the target type.
     *
     * @param targetType              The target type of the conversion
     * @param typeConverterSearchPath The ordered stream of classes to search for an applicable type converter
     * @return An Optional with the type converter if found, otherwise an empty Optional
     */
    private static Optional<Method> findTypeConverter(
        ParameterType targetType,
        Stream<Class<?>> typeConverterSearchPath
    ) {
        return typeConverterSearchPath
            .map(it -> findMatchingConverterInClass(it, targetType))
            .filter(Optional::isPresent)
            .flatMap(Optional::stream)
            .findFirst();
    }

    /**
     * Creates an ordered stream of classes to search for type converters.
     * <p>
     * The stream will contain the following classes in this order:
     * 1. Test class
     * 2. Any enclosing classes (in inside-out order)
     * 3. Kotlin top-level, static functions
     * 4. Any classes listed in @TypeConverterSources annotation for test class (in listed order)
     * 5. Any classes listed in @TypeConverterSources for enclosing classes (in inside-out order)
     *
     * @param testClass The current test class
     * @return A stream of classes to search for an applicable type converter
     */
    private static Stream<Class<?>> typeConverterSearchPath(Class<?> testClass) {
        return Stream.concat(
                Stream.concat(
                    testClasses(testClass),
                    kotlinTestFile(testClass)
                ),
                converterSources(testClass)
            )
            .filter(Objects::nonNull);
    }

    /**
     * Recursive method to create a stream of the test class and any enclosing classes
     * of a nested test class
     *
     * @param testClass the test class
     * @return stream of test class and enclosing classes
     */
    private static Stream<Class<?>> testClasses(Class<?> testClass) {
        if (testClass == null) return Stream.empty();
        return Stream.concat(
            Stream.of(testClass),
            testClasses(testClass.getEnclosingClass())
        );
    }

    /**
     * Helper method to load the Kotlin file class holding top-level, static functions
     *
     * @param testClass the current test class
     * @return Stream of the Kotlin file class, or empty Stream if not found
     */
    private static Stream<Class<?>> kotlinTestFile(Class<?> testClass) {
        try {
            return Stream.of(
                testClass.getClassLoader()
                    .loadClass(findOuterClass(testClass).getTypeName() + "Kt")
            );
        } catch (ClassNotFoundException e) {
            return Stream.empty();
        }
    }

    /**
     * Recursive method to find the outermost class of a nested class
     *
     * @param testClass possibly nested testClass
     * @return outermost class, or the given class if not nested
     */
    private static Class<?> findOuterClass(Class<?> testClass) {
        if (testClass == null) return null;
        return testClass.getEnclosingClass() == null
            ? testClass
            : findOuterClass(testClass.getEnclosingClass());
    }

    /**
     * Recursive method to create a stream of classes listed in TypeConverterSources annotation
     * for test class and any enclosing classes of a nested test class
     *
     * @param testClass the test class with possible TypeConverterSources annotation
     * @return stream of classes listed in found annotations
     */
    private static Stream<Class<?>> converterSources(Class<?> testClass) {
        if (testClass == null) return Stream.empty();

        return Stream.concat(
            declaredConverterSources(testClass),
            converterSources(testClass.getEnclosingClass())
        );
    }

    private static @NonNull Stream<Class<?>> declaredConverterSources(Class<?> testClass) {
        return Stream.concat(
            testClass.isAnnotationPresent(TypeConverterSources.class)
                ? Arrays.stream(testClass.getAnnotation(TypeConverterSources.class).value())
                : Stream.empty(),
            testClass.isAnnotationPresent(FactorySources.class)
                ? Arrays.stream(testClass.getAnnotation(FactorySources.class).value())
                : Stream.empty()
        );
    }

    /**
     * Find applicable type converter in class if any
     */
    private static Optional<Method> findMatchingConverterInClass(
        Class<?> converterClass,
        ParameterType targetType
    ) {
        List<Method> matchingMethods = Arrays.stream(converterClass.getMethods())
            .filter(TypeConversion::isTypeConverter)
            .filter(it -> targetType.isAssignableFrom(it.getReturnType()))
            .toList();

        if (matchingMethods.size() > 1) {
            throw new TableTestException(multipleTypeConvertersFound(converterClass, targetType));
        }

        return matchingMethods.stream().findFirst();
    }

    /**
     * Decides if a method can be used for type conversion.
     * <p>
     * A method qualifies as a type converter if it is public, static, and takes a single parameter.
     * Methods should be annotated with {@link TypeConverter} to indicate they are type converters.
     * Non-annotated methods are still accepted for backwards compatibility but will generate a warning.
     *
     * @return true if public, static, and takes a single parameter; false otherwise
     */
    private static boolean isTypeConverter(Method method) {
        boolean isCandidate = Modifier.isStatic(method.getModifiers())
            && method.canAccess(null)
            && method.getParameterCount() == 1;

        if (isCandidate && !method.isAnnotationPresent(TypeConverter.class)) {
            System.err.printf(
                "[TableTest] Warning: Method %s.%s() is used as a type converter but is not annotated with @TypeConverter. " +
                    "Please add @TypeConverter annotation. Non-annotated converters will not be supported in a future version.%n",
                method.getDeclaringClass().getName(),
                method.getName()
            );
        }

        return isCandidate;
    }

    /**
     * Invokes a type converter to convert a parsed value to the parameter type.
     *
     * @param converter  The type converter method to invoke
     * @param value      The value to convert
     * @param targetType The target type of the conversion
     * @return The converted value
     */
    private static Object invokeTypeConverter(
        Method converter,
        Object value,
        ParameterType targetType
    ) {
        try {
            return converter.invoke(null, value);
        } catch (IllegalAccessException | InvocationTargetException cause) {
            throw new TableTestException(typeConverterFailed(converter, value, targetType), cause);
        }
    }

    private static Object fallbackToJUnitConversion(Object value, ParameterType targetType, Class<?> testClass) {
        try {
            return ConversionSupport.convert(
                value.toString(),
                targetType.toClass(),
                testClass.getClassLoader()
            );
        } catch (ConversionException cause) {
            throw new TableTestException(
                fallbackJUnitConversionFailed(value, targetType, typeConverterSearchPath(testClass)),
                cause
            );
        }
    }
}
