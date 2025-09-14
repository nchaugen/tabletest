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

import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.platform.commons.support.conversion.ConversionException;
import org.junit.platform.commons.support.conversion.ConversionSupport;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static io.github.nchaugen.tabletest.junit.ParameterUtil.nestedElementTypesOf;
import static io.github.nchaugen.tabletest.junit.TableTestException.cannotAcceptNull;
import static io.github.nchaugen.tabletest.junit.TableTestException.factoryMethodFailed;
import static io.github.nchaugen.tabletest.junit.TableTestException.fallbackJUnitConversionFailed;
import static io.github.nchaugen.tabletest.junit.TableTestException.multipleFactoryMethodsFound;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static java.util.stream.Collectors.toUnmodifiableSet;

/**
 * A utility class that handles conversion of parsed table values to the appropriate parameter types
 * for JUnit tests using the {@link TableTest} annotation.
 * <p>
 * This converter handles various data structures, including:
 * <ul>
 *   <li>Null values</li>
 *   <li>Basic scalar values</li>
 *   <li>Lists, sets, and maps</li>
 *   <li>Nested collections with complex generic type parameters</li>
 *   <li>Custom types, either implicitly through either factory methods in test class
 *       or factory methods found via {@link FactorySources} annotation,
 *       or explicitly using the {@link ConvertWith} annotation</li>
 * </ul>
 * <p>
 * The class works recursively to handle deeply nested data structures like lists of lists
 * or maps containing lists, ensuring each element is properly converted to the expected type.
 */
public class ParameterTypeConverter {

    private ParameterTypeConverter() {
    }

    /**
     * Converts a parsed TableFormat value to the appropriate parameter type. This method handles
     * various data structures and performs recursive conversion as needed.
     *
     * @param value     The value from parsed table data that needs to be converted
     * @param parameter The test method parameter that defines the target type
     * @return The converted value matching the parameter's expected type
     */
    public static Object convertValue(Object value, Parameter parameter) {
        Class<?> targetType = parameter.getType();
        Class<?> testClass = parameter.getDeclaringExecutable().getDeclaringClass();

        if (value == null || valueConvertsToNull(targetType, value)
        ) {
            if (targetType.isPrimitive()) {
                throw new TableTestException(cannotAcceptNull(value, targetType));
            }
            return null;
        }

        // Let JUnit handle explicit converters
        if (parameter.isAnnotationPresent(ConvertWith.class)) {
            return value;
        }

        return convert(value, NestedTypes.of(parameter), testClass);
    }

    private static boolean valueConvertsToNull(Class<?> targetType, Object value) {
        return isEmptyForNonStringType(value, targetType)
               || isEmptyValueSet(value, targetType);
    }

    /**
     * Determines if the parsed value was the empty value ("" or '') for a non-string type
     *
     * @param parsedValue parsed value
     * @param targetType  type of parameter
     */
    private static boolean isEmptyForNonStringType(Object parsedValue, Class<?> targetType) {
        return parsedValue.toString().isEmpty() && !targetType.isAssignableFrom(String.class);
    }

    /**
     * Determines if the cell is a value set with no values
     *
     * @param value      converted cell value
     * @param targetType type of parameter
     */
    private static boolean isEmptyValueSet(Object value, Class<?> targetType) {
        return value instanceof Set<?> set && set.isEmpty() && !targetType.isAssignableFrom(Set.class);
    }

    /**
     * Recursively converts values based on their parsed type and the expected parameter type.
     *
     * @param value       The parsed value to convert
     * @param nestedTypes Information about the nested types in parameterized types
     * @param testClass   The test class to search for factory methods
     * @return The converted value
     */
    private static Object convert(
        Object value,
        NestedTypes nestedTypes,
        Class<?> testClass
    ) {
        Class<?> targetType = nestedTypes.next();

        if (isTargetMatchingTypeOfParsedValue(value, targetType)) {
            return switch (value) {
                case List<?> list -> convertList(list, nestedTypes.skipNext(), testClass);
                case Set<?> set -> convertSet(set, nestedTypes, testClass);
                case Map<?, ?> map -> convertMap(map, nestedTypes.skipNext(), testClass);
                default -> value;
            };
        }

        // Types don't match - look for a factory method
        return findFactoryMethod(targetType, factoryMethodSearchPath(testClass))
            .map(factoryMethod -> invokeFactoryMethod(
                factoryMethod,
                // Convert value to match factory method input
                convert(value, NestedTypes.of(factoryMethod.getParameters()[0]), testClass),
                targetType
            ))
            .orElseGet(() -> fallbackToJUnitConversion(value, testClass, targetType));
    }

    private static Object fallbackToJUnitConversion(Object value, Class<?> testClass, Class<?> targetType) {
        try {
            return ConversionSupport.convert(
                value.toString(),
                targetType,
                testClass.getClassLoader()
            );
        } catch (ConversionException cause) {
            throw new TableTestException(
                fallbackJUnitConversionFailed(value, targetType, factoryMethodSearchPath(testClass)),
                cause
            );
        }
    }

    /**
     * Checks if the given parsed value is matching the specified target type.
     *
     * @param value      The parsed value to be checked.
     * @param targetType The class representing the target type to check against.
     * @return true if the value is matching the target type; false otherwise.
     */
    private static boolean isTargetMatchingTypeOfParsedValue(Object value, Class<?> targetType) {
        return targetType == null
               || targetType.isAssignableFrom(value.getClass())
               // for value sets the target will be the element type
               || Set.class.isAssignableFrom(value.getClass());
    }

    /**
     * Converts each element in a list to the appropriate type based on the parameter's
     * generic type information.
     *
     * @param list        The parsed list containing values to convert
     * @param nestedTypes Information about nested types for list elements
     * @param testClass   The test class to search for factory methods
     * @return A new list with converted elements
     */
    private static List<?> convertList(
        List<?> list,
        NestedTypes nestedTypes,
        Class<?> testClass
    ) {
        return list.stream()
            .map(it -> convert(it, nestedTypes, testClass))
            .toList();
    }

    /**
     * Converts each element in a set to the appropriate type based on the parameter's
     * generic type information.
     *
     * @param set         The parsed set containing values to convert
     * @param nestedTypes Information about nested types for set elements
     * @param testClass   The test class to search for factory methods
     * @return A new set with converted elements
     */
    private static Set<?> convertSet(
        Set<?> set,
        NestedTypes nestedTypes,
        Class<?> testClass
    ) {
        // if this is a value set, the target type will be the element type
        NestedTypes types = nestedTypes.nextIsSet() ? nestedTypes.skipNext() : nestedTypes;

        return set.stream()
            .map(it -> convert(it, types, testClass))
            .collect(toUnmodifiableSet());
    }

    /**
     * Converts each value in a map to the appropriate type based on the parameter's
     * generic type information (keys remain unchanged).
     *
     * @param map         The parsed map containing keys and values
     * @param nestedTypes Information about nested types for map values
     * @param testClass   The test class to search for factory methods
     * @return A new map with converted values
     */
    private static Map<?, ?> convertMap(
        Map<?, ?> map,
        NestedTypes nestedTypes,
        Class<?> testClass
    ) {
        return map.entrySet().stream()
            .map(entry -> Map.entry(
                entry.getKey(),
                convert(entry.getValue(), nestedTypes, testClass)
            ))
            .collect(toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Searches the provided search path for a factory method for mapping a parsed value to the parameter type.
     * <p>
     * The factory method must be static and public and must take a single parameter.
     * The return type of the factory method must be the target type.
     *
     * @param toType                  The target type of the conversion
     * @param factoryMethodSearchPath The ordered stream of classes to search for an applicable factory method
     * @return An Optional with the factory method if found, otherwise an empty Optional
     */
    private static Optional<Method> findFactoryMethod(Class<?> toType, Stream<Class<?>> factoryMethodSearchPath) {
        return factoryMethodSearchPath
            .map(it -> findMatchingFactoryMethodInClass(it, toType))
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
     * 2. Any classes listed in @FactorySources annotation for test class (in listed order)
     * 3. Any classes listed in @FactorySources for enclosing classes (in inside-out order)
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

        Stream<Class<?>> factorySources =
            testClass.isAnnotationPresent(FactorySources.class)
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
    private static Optional<Method> findMatchingFactoryMethodInClass(Class<?> factoryMethodClass, Class<?> targetType) {
        List<Method> matchingMethods = Arrays.stream(factoryMethodClass.getMethods())
            .filter(ParameterTypeConverter::isFactoryMethod)
            .filter(it -> isConvertingToTargetType(targetType, it.getReturnType()))
            .toList();

        if (matchingMethods.size() > 1) {
            throw new TableTestException(multipleFactoryMethodsFound(factoryMethodClass, targetType));
        }

        return matchingMethods.stream().findFirst();
    }

    /**
     * Decides if a method can be used for type conversion
     * @return true if public, static, and takes a single parameter; false otherwise
     */
    private static boolean isFactoryMethod(Method method) {
        return Modifier.isStatic(method.getModifiers()) && method.canAccess(null) && method.getParameterCount() == 1;
    }

    /**
     * Decides if return type of factory method can be assigned to parameter of target type.
     *
     * @param targetType of parameter to convert to
     * @param returnType of factory method
     * @return true if returnType can be assigned to targetType, false otherwise
     */
    private static boolean isConvertingToTargetType(Class<?> targetType, Class<?> returnType) {
        return targetType.isAssignableFrom(returnType)
                || isAssignableFromBoxed(targetType, returnType)
                || isAssignableFromUnboxed(targetType, returnType);
    }

    private static boolean isAssignableFromUnboxed(Class<?> targetType, Class<?> returnType) {
        return returnType.equals(PRIMITIVE_TO_WRAPPER.get(targetType));
    }

    private static boolean isAssignableFromBoxed(Class<?> targetType, Class<?> returnType) {
        return targetType.equals(PRIMITIVE_TO_WRAPPER.get(returnType));
    }

    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            char.class, Character.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class,
            void.class, Void.class
    );

    /**
     * Invokes a factory method to convert a parsed value to the parameter type.
     *
     * @param factoryMethod The factory method to invoke
     * @param value         The parsed value to convert
     * @param targetType    The target type of the conversion
     * @return The converted value
     */
    private static Object invokeFactoryMethod(Method factoryMethod, Object value, Class<?> targetType) {
        try {
            return factoryMethod.invoke(null, value);
        } catch (IllegalAccessException | InvocationTargetException cause) {
            throw new TableTestException(factoryMethodFailed(factoryMethod, value, targetType), cause);
        }
    }

    /**
     * A record that manages information about nested types within a parameterized type.
     * <p>
     * This helps track the type hierarchy in complex generic types like List&lt;Map&lt;String, Integer&gt;&gt;
     * to ensure proper conversion at each level.
     */
    private record NestedTypes(
        List<? extends Class<?>> elementTypes
    ) {
        /**
         * Creates a NestedTypes instance from a parameter.
         *
         * @param parameter The method parameter to analyse
         * @return A NestedTypes instance containing type information
         */
        static NestedTypes of(Parameter parameter) {
            return new NestedTypes(nestedElementTypesOf(parameter));
        }

        /**
         * Checks if there are more nested types in the hierarchy.
         *
         * @return true if there are more nested types, false otherwise
         */
        boolean hasNext() {
            return !elementTypes.isEmpty();
        }

        /**
         * Returns the next type in the nested type hierarchy.
         *
         * @return The next class in the hierarchy, or null if none exists
         */
        Class<?> next() {
            return elementTypes.isEmpty() ? null : elementTypes.getFirst();
        }

        /**
         * Returns a new NestedTypes instance with the first element removed.
         * <p>
         * This is used when moving down the type hierarchy during recursive conversions.
         *
         * @return A new NestedTypes instance with the first element removed
         */
        NestedTypes skipNext() {
            return elementTypes.isEmpty()
                   ? this
                   : new NestedTypes(elementTypes.subList(1, elementTypes.size()));
        }

        @SuppressWarnings("DataFlowIssue")
        boolean nextIsSet() {
            return hasNext() && Set.class.isAssignableFrom(next());
        }
    }
}
