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
 *   <li>Custom types, either implicitly through converter methods in test class
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

        if (value == null
            || isBlankForNonStringType(value, targetType)
            || isEmptyApplicableValueSet(value, targetType)
        ) {
            if (targetType.isPrimitive()) {
                throw new ConversionException(
                    "Cannot convert null to primitive value of type " + targetType.getTypeName());
            }
            return null;
        }

        // Let JUnit handle explicit converters
        if (parameter.isAnnotationPresent(ConvertWith.class)) {
            return value;
        }

        return convert(value, NestedTypes.of(parameter), testClass);
    }

    /**
     * Determines if the cell was blank for a non-string type
     *
     * @param value      converted cell value
     * @param targetType type of parameter
     */
    private static boolean isBlankForNonStringType(Object value, Class<?> targetType) {
        return value.toString().isBlank() && !targetType.isAssignableFrom(String.class);
    }

    /**
     * Determines if the cell is an applicable value set with no values
     *
     * @param value      converted cell value
     * @param targetType type of parameter
     */
    private static boolean isEmptyApplicableValueSet(Object value, Class<?> targetType) {
        return value instanceof Set<?> set && set.isEmpty() && !targetType.isAssignableFrom(Set.class);
    }

    /**
     * Recursively converts values based on their parsed type and the expected parameter type.
     *
     * @param value       The parsed value to convert
     * @param nestedTypes Information about the nested types in parameterized types
     * @param testClass   The test class to search for custom converters
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

        // Types don't match - look for a converter
        return findConverter(testClass, targetType)
            .map(converter -> invokeConverter(
                converter,
                // Convert value to match converter input
                convert(value, NestedTypes.of(converter.getParameters()[0]), testClass),
                targetType
            ))

            // Fallback to JUnit conversion
            .orElseGet(() -> ConversionSupport.convert(
                value.toString(),
                targetType,
                testClass.getClassLoader()
            ));
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
               // for applicable value sets the target will be the element type
               || Set.class.isAssignableFrom(value.getClass());
    }

    /**
     * Converts each element in a list to the appropriate type based on the parameter's
     * generic type information.
     *
     * @param list        The parsed list containing values to convert
     * @param nestedTypes Information about nested types for list elements
     * @param testClass   The test class to search for custom converters
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
     * @param testClass   The test class to search for custom converters
     * @return A new set with converted elements
     */
    private static Set<?> convertSet(
        Set<?> set,
        NestedTypes nestedTypes,
        Class<?> testClass
    ) {
        // if this is an applicable value set, the target type will be the element type
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
     * @param testClass   The test class to search for custom converters
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
     * Searches for a custom converter method for mapping a parsed value to the parameter type.
     * <p>
     * The converter method must be static and accessible and must take a single parameter.
     * The return type of the converter method must be the target type.
     * <p>
     * Searches in priority order and stops at the first suitable method found:
     * 1. Methods in test class
     * 2. Methods in classes listed in @TableTestConverters annotation for test class (in listed order)
     * 3. Methods in classes listed in @TableTestConverters for enclosing classes (in inside-out order)
     *
     * @param testClass The test class to search for custom converters
     * @param toType    The target type of the conversion
     * @return An Optional with the converter method if found, otherwise an empty Optional
     */
    static Optional<Method> findConverter(Class<?> testClass, Class<?> toType) {
        return Stream.concat(
                Stream.of(testClass, kotlinTestFile(testClass)),
                tableTestConverters(testClass))
            .filter(Objects::nonNull)
            .map(it -> findMatchingConverterInClass(it, toType))
            .filter(Optional::isPresent)
            .flatMap(Optional::stream)
            .findFirst();
    }

    //TODO find converter functions in enclosing test classes

    /**
     * Helper method to load the Kotlin file class holding top-level, static functions
     * @param testClass the current test class
     * @return the Kotlin file class, or null if not found
     */
    private static Class<?> kotlinTestFile(Class<?> testClass) {
        try {
            return testClass.getClassLoader().loadClass(testClass.getTypeName() + "Kt");
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Recursive method to create stream of classes listed in TableTestConverters annotation
     * for test class and any enclosing classes of a nested test class
     * @param testClass the test class with possible TableTestConverters annotation
     * @return stream of classes listed in found annotations
     */
    private static Stream<Class<?>> tableTestConverters(Class<?> testClass) {
        if (testClass == null) return Stream.empty();

        Stream<Class<?>> converters =
            testClass.isAnnotationPresent(TableTestConverters.class)
            ? Arrays.stream(testClass.getAnnotation(TableTestConverters.class).value())
            : Stream.empty();

        return Stream.concat(
            converters,
            tableTestConverters(testClass.getEnclosingClass())
        );
    }

    /**
     * Helper method to find a converter in a class
     */
    private static Optional<Method> findMatchingConverterInClass(Class<?> clazz, Class<?> toType) {
        return Arrays.stream(clazz.getDeclaredMethods())
            .filter(it -> Modifier.isStatic(it.getModifiers()))
            .filter(it -> it.canAccess(null))
            .filter(it -> it.getParameterCount() == 1)
            .filter(it -> toType.isAssignableFrom(it.getReturnType()))
            .findFirst();
    }

    /**
     * Invokes a custom converter method to convert a parsed value to the parameter type.
     *
     * @param converter  The converter method to invoke
     * @param value      The parsed value to convert
     * @param targetType The target type of the conversion
     * @return The converted value
     */
    private static Object invokeConverter(Method converter, Object value, Class<?> targetType) {
        try {
            return converter.invoke(null, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ConversionException(
                String.format(
                    "Failed to convert %s \"%s\" to type %s",
                    value.getClass().getTypeName(),
                    value,
                    targetType.getTypeName()
                ), e
            );
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
         * <p>
         * If the parameter has a @ConvertWith annotation, returns an empty list
         * since conversion will be handled by the custom converter.
         * Otherwise, extracts the nested element types from the parameter.
         *
         * @param parameter The method parameter to analyze
         * @return A NestedTypes instance containing type information
         */
        static NestedTypes of(Parameter parameter) {
            return parameter.isAnnotationPresent(org.junit.jupiter.params.converter.ConvertWith.class)
                   ? new NestedTypes(List.of())
                   : new NestedTypes(nestedElementTypesOf(parameter));
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
