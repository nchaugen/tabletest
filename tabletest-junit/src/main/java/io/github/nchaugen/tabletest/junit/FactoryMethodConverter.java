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
package io.github.nchaugen.tabletest.junit;

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

import static io.github.nchaugen.tabletest.junit.TableTestException.*;

/**
 * Responsible for finding and invoking a factory method to convert a parsed value to the target
 * parameter type, falling back to JUnit conversion if no factory method is found.
 */
public class FactoryMethodConverter {

    /**
     * Converts a parsed value to the target parameter type using a factory method if available,
     * otherwise falls back to JUnit conversion.
     * <p>
     * Factory methods will be looked for in the following order:
     * <ol>
     *     <li>Test class</li>
     *     <li>Any enclosing classes (in inside-out order)</li>
     *     <li>Kotlin top-level, static functions</li>
     *     <li>Any classes listed in @FactorySources annotation for test class (in listed order)</li>
     *     <li>Any classes listed in @FactorySources for enclosing classes (in inside-out order)</li>
     * </ol>
     *
     * @param value                  The parsed value to convert
     * @param targetType             The target type of the conversion
     * @param testClass              The test class to search for factory methods
     * @param convertedValueSupplier A function that supplies the parsed value converted to match
     *                               the factory method's parameter type
     * @return the converted value
     * @throws TableTestException if the conversion fails
     */
    public static Object convert(
        Object value,
        ParameterType targetType,
        Class<?> testClass,
        Function<Parameter, Object> convertedValueSupplier
    ) {
        return findFactoryMethod(targetType, factoryMethodSearchPath(testClass))
            .map(factoryMethod ->
                invokeFactoryMethod(
                    factoryMethod,
                    convertedValueSupplier.apply(factoryMethod.getParameters()[0]),
                    targetType
                ))
            .orElseGet(() -> fallbackToJUnitConversion(value, targetType, testClass));
    }

    /**
     * Searches the provided search path for a factory method for mapping a parsed value to the parameter type.
     * <p>
     * The factory method must be static and public and must take a single parameter.
     * The return type of the factory method must be the target type.
     *
     * @param targetType              The target type of the conversion
     * @param factoryMethodSearchPath The ordered stream of classes to search for an applicable factory method
     * @return An Optional with the factory method if found, otherwise an empty Optional
     */
    private static Optional<Method> findFactoryMethod(
        ParameterType targetType,
        Stream<Class<?>> factoryMethodSearchPath
    ) {
        return factoryMethodSearchPath
            .map(it -> findMatchingFactoryMethodInClass(it, targetType))
            .filter(Optional::isPresent)
            .flatMap(Optional::stream)
            .findFirst();
    }

    /**
     * Creates an ordered stream of classes to search for factory methods.
     * <p>
     * The stream will contain the following classes in this order:
     * 1. Test class
     * 2. Any enclosing classes (in inside-out order)
     * 3. Kotlin top-level, static functions
     * 4. Any classes listed in @FactorySources annotation for test class (in listed order)
     * 5. Any classes listed in @FactorySources for enclosing classes (in inside-out order)
     *
     * @param testClass The current test class
     * @return A stream of classes to search for an applicable factory method
     */
    private static Stream<Class<?>> factoryMethodSearchPath(Class<?> testClass) {
        return Stream.concat(
                Stream.concat(
                    testClasses(testClass),
                    kotlinTestFile(testClass)
                ),
                factorySources(testClass)
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
     * Recursive method to create a stream of classes listed in FactorySources annotation
     * for test class and any enclosing classes of a nested test class
     *
     * @param testClass the test class with possible FactorySources annotation
     * @return stream of classes listed in found annotations
     */
    private static Stream<Class<?>> factorySources(Class<?> testClass) {
        if (testClass == null) return Stream.empty();

        Stream<Class<?>> factorySources = testClass.isAnnotationPresent(FactorySources.class)
            ? Arrays.stream(testClass.getAnnotation(FactorySources.class).value())
            : Stream.empty();

        return Stream.concat(
            factorySources,
            factorySources(testClass.getEnclosingClass())
        );
    }

    /**
     * Find applicable factory method in class if any
     */
    private static Optional<Method> findMatchingFactoryMethodInClass(
        Class<?> factoryMethodClass,
        ParameterType targetType
    ) {
        List<Method> matchingMethods = Arrays.stream(factoryMethodClass.getMethods())
            .filter(FactoryMethodConverter::isFactoryMethod)
            .filter(it -> targetType.isAssignableFrom(it.getReturnType()))
            .toList();

        if (matchingMethods.size() > 1) {
            throw new TableTestException(multipleFactoryMethodsFound(factoryMethodClass, targetType));
        }

        return matchingMethods.stream().findFirst();
    }

    /**
     * Decides if a method can be used for type conversion
     *
     * @return true if public, static, and takes a single parameter; false otherwise
     */
    private static boolean isFactoryMethod(Method method) {
        return Modifier.isStatic(method.getModifiers()) && method.canAccess(null) && method.getParameterCount() == 1;
    }

    /**
     * Invokes a factory method to convert a parsed value to the parameter type.
     *
     * @param factoryMethod The factory method to invoke
     * @param value         The value to convert
     * @param targetType    The target type of the conversion
     * @return The converted value
     */
    private static Object invokeFactoryMethod(
        Method factoryMethod,
        Object value,
        ParameterType targetType
    ) {
        try {
            return factoryMethod.invoke(null, value);
        } catch (IllegalAccessException | InvocationTargetException cause) {
            throw new TableTestException(factoryMethodFailed(factoryMethod, value, targetType), cause);
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
                fallbackJUnitConversionFailed(value, targetType, factoryMethodSearchPath(testClass)),
                cause
            );
        }
    }
}
