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

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.nchaugen.tabletest.junit.TableTestException.primitiveTypeDoesNotAllowNull;
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
        // Let JUnit handle explicit converters
        if (parameter.isAnnotationPresent(ConvertWith.class)) {
            return value;
        }

        ParameterType parameterType = ParameterType.of(parameter);
        Class<?> testClass = parameter.getDeclaringExecutable().getDeclaringClass();

        if (value == null) {
            if (parameterType.isPrimitive()) {
                throw new TableTestException(primitiveTypeDoesNotAllowNull(parameterType));
            }
            return null;
        }

        return convert(value, parameterType, testClass);
    }

    /**
     * Converts the parsed value to the expected parameter type.
     *
     * @param value     The parsed value to convert
     * @param parameter The test method parameter value will be assigned to
     * @param testClass The test class to search for factory methods if needed
     * @return The converted value
     */
    public static Object convert(Object value, Parameter parameter, Class<?> testClass) {
        return convert(value, ParameterType.of(parameter), testClass);
    }

    /**
     * Recursively converts values based on their parsed type and the expected parameter type.
     *
     * @param value      The parsed value to convert
     * @param targetType Information about the target parameter type
     * @param testClass  The test class to search for factory methods
     * @return The converted value
     */
    private static Object convert(
        Object value,
        ParameterType targetType,
        Class<?> testClass
    ) {
        if (targetType.isMatching(value.getClass())) {
            return switch (value) {
                case List<?> list -> convertList(list, targetType, testClass);
                case Set<?> set -> convertSet(set, targetType, testClass);
                case Map<?, ?> map -> convertMap(map, targetType, testClass);
                default -> value;
            };
        }

        return FactoryMethodConverter.convert(
            value,
            targetType,
            testClass,
            (Parameter parameter) -> convert(value, parameter, testClass)
        );
    }

    /**
     * Converts each element in a list to the appropriate type based on the parameter's
     * generic type information.
     *
     * @param list          The parsed list containing values to convert
     * @param parameterType Information about nested types for list elements
     * @param testClass     The test class to search for factory methods
     * @return A new list with converted elements
     */
    private static List<?> convertList(
        List<?> list,
        ParameterType parameterType,
        Class<?> testClass
    ) {
        ParameterType elementType = parameterType.elementType();
        return list.stream()
            .map(it -> convert(it, elementType, testClass))
            .toList();
    }

    /**
     * Converts each element in a set to the appropriate type based on the parameter's
     * generic type information.
     *
     * @param set           The parsed set containing values to convert
     * @param parameterType Information about nested types for set elements
     * @param testClass     The test class to search for factory methods
     * @return A new set with converted elements
     */
    private static Set<?> convertSet(
        Set<?> set,
        ParameterType parameterType,
        Class<?> testClass
    ) {
        // if this is a value set, the parameter type will be the element type
        ParameterType elementType = parameterType.isSet() ? parameterType.elementType() : parameterType;

        return set.stream()
            .map(it -> convert(it, elementType, testClass))
            .collect(toUnmodifiableSet());
    }

    /**
     * Converts each value in a map to the appropriate type based on the parameter's
     * generic type information (keys remain unchanged).
     *
     * @param map           The parsed map containing keys and values
     * @param parameterType Information about nested types for map values
     * @param testClass     The test class to search for factory methods
     * @return A new map with converted values
     */
    private static Map<?, ?> convertMap(
        Map<?, ?> map,
        ParameterType parameterType,
        Class<?> testClass
    ) {
        ParameterType elementType = parameterType.elementType();
        return map.entrySet().stream()
            .map(it -> Map.entry(
                it.getKey(),
                convert(it.getValue(), elementType, testClass)
            ))
            .collect(toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

}
