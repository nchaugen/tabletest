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

import org.tabletest.parser.Row;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.tabletest.junit.TableTestException.multipleScenarioAnnotations;
import static org.tabletest.junit.ValueSetUtil.isFromValueSet;

public class ScenarioNameUtil {

    /**
     * Returns true if row contains one extra column compared to the number of parameters.
     *
     * @param row        row of data from the table
     * @param parameters test method parameters
     * @return true if the first value in the row is the name of the scenario
     */
    public static boolean hasUndeclaredColumn(Row row, Parameter[] parameters) {
        return row.valueCount() == parameters.length + 1;
    }

    /**
     * Creates the display name for a test based on a scenario name.
     * If the test invocation is generated from a row with value sets, the particular value set values for this
     * test invocation are included in the display name.
     *
     * @param values the converted row values
     * @param row    the parsed row values
     * @return the generated display name
     */
    public static String toDisplayName(List<?> values, Row row, Parameter[] parameters) {
        String scenarioName = getScenarioName(values, row, parameters);
        String currentValueSetValues = currentValueSetValues(values, row);
        return currentValueSetValues.isEmpty()
            ? scenarioName
            : String.format("%s (%s)", scenarioName, currentValueSetValues);
    }

    /**
     * Gets the name of any implicitly or explicitly defined scenario column, or `"null"` if not defined.
     *
     * @param values     converted values
     * @param row        table row
     * @param parameters test method parameters
     * @return the string representation of any scenario column
     */
    private static String getScenarioName(List<?> values, Row row, Parameter[] parameters) {
        return findScenarioValue(values, row, parameters)
            .map(Object::toString)
            .filter(ScenarioNameUtil::isNotBlank)
            .orElse("null");
    }

    /**
     * Finds the value of any implicitly or explicitly defined scenario column.
     *
     * @param values     converted values
     * @param row        table row
     * @param parameters test method parameters
     * @return the value of the scenario column, if any
     */
    private static Optional<?> findScenarioValue(List<?> values, Row row, Parameter[] parameters) {
        return hasUndeclaredColumn(row, parameters)
            ? Optional.ofNullable(row.value(0))
            : findDeclaredScenarioIndex(parameters).map(values::get);
    }

    /**
     * Creates a comma-separated string describing current parameter values from any value sets in the table row.
     * Values described as `<column header> = <current value>`. Parameter values not from a value set are ignored.
     *
     * @param values   parameter values for this test invocation
     * @param tableRow the table row this test invocation is created from
     * @return string describing current values from any value set
     */
    private static String currentValueSetValues(List<?> values, Row tableRow) {
        Row parameterRow = tableRow.skipFirstUnless(values.size() == tableRow.valueCount());

        return IntStream.range(0, values.size())
            .mapToObj(index -> toValueFromValueSetDescription(values, index, parameterRow))
            .filter(Objects::nonNull)
            .collect(Collectors.joining(", "));
    }

    private static String toValueFromValueSetDescription(List<?> values, int index, Row parameterRow) {
        return isFromValueSet(values.get(index), parameterRow.value(index))
            ? String.format("%s = %s", parameterRow.header(index), values.get(index))
            : null;
    }

    /**
     * Returns true if the row contains a scenario name, either implicitly as undeclared first column
     * or explicitly with `@Scenario` parameter annotation.
     *
     * @param row        row of data from the table
     * @param parameters test method parameters
     * @return true if the row contains a scenario name
     */
    public static boolean hasScenarioName(Row row, Parameter[] parameters) {
        return hasUndeclaredColumn(row, parameters) || hasDeclaredScenarioColumn(row, parameters);
    }

    private static Boolean hasDeclaredScenarioColumn(Row row, Parameter[] parameters) {
        return findDeclaredScenarioIndex(parameters)
            .map(index -> isNotBlank(row.value(index)))
            .orElse(false);
    }

    /**
     * Finds the index of the parameter with `@Scenario` annotation, if any.
     *
     * @param parameters test method parameters
     * @return index if present
     */
    private static Optional<Integer> findDeclaredScenarioIndex(Parameter[] parameters) {
        int[] scenarioIndices = IntStream.range(0, parameters.length)
            .filter(index -> parameters[index].isAnnotationPresent(Scenario.class))
            .toArray();

        if (scenarioIndices.length > 1) {
            throw new TableTestException(multipleScenarioAnnotations(parameters[0].getDeclaringExecutable()));
        }

        return Arrays.stream(scenarioIndices).boxed().findFirst();
    }

    /**
     * Tests if the given value is not null and not blank.
     *
     * @param value to test
     * @return true if not null and not blank, false otherwise
     */
    private static boolean isNotBlank(Object value) {
        return value != null && !value.toString().isBlank();
    }

}
