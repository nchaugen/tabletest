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

import io.github.nchaugen.tabletest.parser.Row;
import io.github.nchaugen.tabletest.parser.Table;
import io.github.nchaugen.tabletest.parser.TableParser;
import io.github.nchaugen.tabletest.reporter.TablePublisher;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.AnnotationBasedArgumentsProvider;
import org.junit.jupiter.params.provider.Arguments;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.stream.Stream;

import static io.github.nchaugen.tabletest.junit.ParameterTypeConverter.convertValue;
import static io.github.nchaugen.tabletest.junit.ScenarioNameUtil.hasScenarioName;
import static io.github.nchaugen.tabletest.junit.ScenarioNameUtil.hasUndeclaredColumn;
import static io.github.nchaugen.tabletest.junit.ScenarioNameUtil.toDisplayName;
import static io.github.nchaugen.tabletest.junit.TableTestException.notEnoughTestParameters;
import static io.github.nchaugen.tabletest.junit.ValueSetUtil.generateValueCombinations;

/**
 * Provides arguments for parameterized tests from tabular data defined in {@link TableTest} annotations.
 * <p>
 * Parses table format strings, validates column/parameter count matching, and converts
 * each row into appropriately typed method arguments. Supports both inline tables and
 * external resources.
 */
class TableArgumentsProvider extends AnnotationBasedArgumentsProvider<TableTest> {

    /**
     * Provides a stream of arguments for parameterized tests from tabular data.
     * <p>
     * The method parses the table string provided in the annotation, validates
     * that the number of columns matches the number of method parameters, and
     * converts each row into one or more method arguments.
     * <p>
     * A row becomes multiple instances of method arguments when cells contain sets of
     * values and the corresponding test method parameter is not of type Set. In this
     * situation, method arguments are generated for all set values. If multiple columns
     * contain sets, method arguments are generated for all combinations.
     * <p>
     * NOTE! Be careful with excessive use of expanding sets in the same table, as the
     * number of value combinations can quickly explode and cause long run times.
     *
     * @param context   The current extension context
     * @param tableTest The TableTest annotation containing the table data
     * @return A stream of Arguments objects, one for each data row in the table
     * @throws TableTestException if unable to provide an argument
     */
    @Override
    protected Stream<? extends Arguments> provideArguments(ExtensionContext context, TableTest tableTest) {
        String input = InputResolver.resolveInput(context, tableTest);
        Table table = TableParser.parse(input);
        Parameter[] parameters = resolveParameters(context, table.columnCount());

        TablePublisher.publishTable(context, tableTest, table);

        return table.map(row -> toArguments(row, parameters));
    }

    /**
     * Resolves and validates method parameters against the table structure.
     * <p>
     * Verifies that there are enough method parameters to represent the table columns.
     * One extra column is allowed, which will be interpreted as the name of the argument set.
     *
     * @param context     The test extension context
     * @param columnCount The number of columns in the table
     * @return Array of method parameters
     * @throws TableTestException if there are fewer parameters than columns
     */
    private static Parameter[] resolveParameters(ExtensionContext context, int columnCount) {
        Parameter[] parameters = context.getRequiredTestMethod().getParameters();
        if (parameters.length < columnCount - 1) {
            throw new TableTestException(notEnoughTestParameters(parameters.length, columnCount));
        }
        return parameters;
    }

    /**
     * Converts a table row into a stream of Arguments instances by mapping each cell value to the
     * corresponding parameter type and expanding value sets to one Arguments instance per value.
     * <p>
     * Uses {@link ParameterTypeConverter} to convert cell values to the expected parameter type.
     * <p>
     * If the row has one additional cell compared to the number of parameters, the first cell is
     * assumed to be the name of the argument set. Alternatively, a single parameter with annotation
     * `@Scenario` will be used as the name.
     *
     * @param row        The row of data from the table
     * @param parameters The method parameters defining the expected types
     * @return Stream of Arguments containing the converted values
     */
    private static Stream<? extends Arguments> toArguments(Row row, Parameter[] parameters) {

        List<Object> convertedValues = row
            .skipFirstIf(hasUndeclaredColumn(row, parameters)) // first column is scenario name by convention
            .mapIndexed((index, cell) -> convertValue(cell, parameters[index]))
            .toList();

        return generateValueCombinations(convertedValues, parameters, 0)
            .map(values ->
                hasScenarioName(row, parameters)
                    ? Arguments.argumentSet(toDisplayName(values, row, parameters), values.toArray())
                    : Arguments.of(values.toArray())
            );
    }

}
