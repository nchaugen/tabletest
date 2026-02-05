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

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class ValueSetUtil {

    /**
     * Recursively generates all combinations of values by expanding sets that are not
     * declared a Set type in the test method parameter.
     *
     * @param arguments  values from the row
     * @param parameters test method parameters
     * @param position   position to be processed next
     * @return Stream of all possible value combinations
     */
    public static Stream<List<?>> generateValueCombinations(
        List<?> arguments,
        Parameter[] parameters,
        int position
    ) {
        if (position >= arguments.size()) {
            return Stream.of(arguments);
        }

        Object currentArgument = arguments.get(position);
        Class<?> currentParameterType = parameters[position].getType();
        return isToBeExpanded(currentArgument, currentParameterType)
               ? ((Set<?>) currentArgument).stream()
                   .flatMap(value ->
                                generateValueCombinations(
                                    argumentsWithValueAtPosition(arguments, value, position),
                                    parameters, position + 1
                                ))
               : generateValueCombinations(arguments, parameters, position + 1);

    }

    /**
     * Returns true if the provided value should be expanded into multiple arguments.
     * <p>
     * This is decided if the provided value is a Set and the corresponding test method
     * parameter is not of type Set.
     *
     * @param currentArgument      value to consider for be expansion
     * @param currentParameterType type of the corresponding test method parameter
     */
    private static boolean isToBeExpanded(Object currentArgument, Class<?> currentParameterType) {
        return currentArgument instanceof Set<?> && !currentParameterType.isAssignableFrom(Set.class);
    }

    /**
     * Returns a new list with the value at the specified position replaced with the provided value.
     *
     * @param arguments list of values
     * @param value     value to replace the existing value at the specified position
     * @param pos       position of the value to replace
     * @return new list with the provided value in the specified position
     */
    private static List<?> argumentsWithValueAtPosition(List<?> arguments, Object value, int pos) {
        List<Object> newValues = new ArrayList<>(arguments);
        newValues.set(pos, value);
        return newValues;
    }

    /**
     * Returns true if the current parameter value is selected from a value set in the table row.
     *
     * @param parameterValue parameter value for this test invocation
     * @param rowValue       the value specified in the table row
     * @return true if the value is from a value set, false otherwise
     */
    static boolean isFromValueSet(Object parameterValue, Object rowValue) {
        return parameterValue != null && rowValue != null &&
               !Set.class.isAssignableFrom(parameterValue.getClass()) &&
               Set.class.isAssignableFrom(rowValue.getClass());
    }
}
