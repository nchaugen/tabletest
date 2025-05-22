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

import org.junit.jupiter.params.converter.DefaultArgumentConverter;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.nchaugen.tabletest.junit.ParameterUtil.contextOf;

/**
 * A utility class that handles conversion of parsed table values to the appropriate parameter types
 * for JUnit tests using the {@link TableTest} annotation.
 * <p>
 * This converter handles various data structures, including:
 * <ul>
 *   <li>Basic scalar values (using JUnit's DefaultArgumentConverter)</li>
 *   <li>Lists of values with automatic element type conversion</li>
 *   <li>Maps with automatic value type conversion</li>
 *   <li>Nested collections with complex generic type parameters</li>
 * </ul>
 * <p>
 * The class works recursively to handle deeply nested data structures like lists of lists
 * or maps containing lists, ensuring each element is properly converted to the expected type.
 */
public class ParameterTypeConverter {

    private ParameterTypeConverter() {}

    /**
     * Converts a parsed TableFormat value to the appropriate parameter type. This method handles
     * various data structures and performs recursive conversion as needed.
     *
     * @param value The value from parsed table data that needs to be converted
     * @param parameter The method parameter that defines the target type
     * @return The converted value matching the parameter's expected type
     */
    public static Object convertValue(Object value, Parameter parameter) {
        return convertValue(value, NestedTypes.of(parameter), parameter);
    }

    /**
     * Recursively converts values based on their parsed type and the expected parameter type.
     * Handles Lists, Maps, and scalar values differently.
     *
     * @param value The parsed value to convert
     * @param nestedTypes Information about the nested types in parameterized types
     * @param parameter The original method parameter providing context
     * @return The converted value
     */
    private static Object convertValue(
        Object value,
        NestedTypes nestedTypes,
        Parameter parameter
    ) {
        return switch (value) {
            case List<?> list -> convertList(list, nestedTypes.skipNext(), parameter);
            case Set<?> set -> convertSet(set, nestedTypes.skipNext(), parameter);
            case Map<?, ?> map -> convertMap(map, nestedTypes.skipNext(), parameter);
            default -> convertSingleValue(value, nestedTypes, parameter);
        };
    }

    /**
     * Converts a single scalar value using JUnit's DefaultArgumentConverter if needed.
     *
     * @param value The parsed value to convert
     * @param nestedTypes Information about the nested types in parameterized types
     * @param parameter The original method parameter providing context
     * @return The converted scalar value
     */
    @SuppressWarnings("DataFlowIssue")
    private static Object convertSingleValue(Object value, NestedTypes nestedTypes, Parameter parameter) {
        return nestedTypes.hasNext()
               ? DefaultArgumentConverter.INSTANCE.convert(value, nestedTypes.next(), contextOf(parameter))
               : value;
    }

    /**
     * Converts each element in a list to the appropriate type based on the parameter's
     * generic type information.
     *
     * @param list The parsed list containing values to convert
     * @param nestedTypes Information about nested types for list elements
     * @param parameter The original method parameter providing context
     * @return A new list with converted elements
     */
    private static List<?> convertList(
        List<?> list,
        NestedTypes nestedTypes,
        Parameter parameter
    ) {
        return list.stream()
            .map(it -> convertValue(it, nestedTypes, parameter))
            .collect(Collectors.toList());
    }

    /**
     * Converts each element in a set to the appropriate type based on the parameter's
     * generic type information.
     *
     * @param set The parsed set containing values to convert
     * @param nestedTypes Information about nested types for set elements
     * @param parameter The original method parameter providing context
     * @return A new set with converted elements
     */
    private static Set<?> convertSet(
        Set<?> set,
        NestedTypes nestedTypes,
        Parameter parameter
    ) {
        return set.stream()
            .map(it -> convertValue(it, nestedTypes, parameter))
            .collect(Collectors.toSet());
    }

    /**
     * Converts each value in a map to the appropriate type based on the parameter's
     * generic type information (keys remain unchanged).
     *
     * @param map The parsed map containing keys and values
     * @param nestedTypes Information about nested types for map values
     * @param parameter The original method parameter providing context
     * @return A new map with converted values
     */
    private static Map<?, ?> convertMap(
        Map<?, ?> map,
        NestedTypes nestedTypes,
        Parameter parameter
    ) {
        return map.entrySet().stream()
            .map(entry -> Map.entry(
                entry.getKey(),
                convertValue(
                    entry.getValue(),
                    nestedTypes,
                    parameter
                )
            ))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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
                   : new NestedTypes(ParameterUtil.nestedElementTypesOf(parameter));
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
    }
}
