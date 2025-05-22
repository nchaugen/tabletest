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
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.AnnotationBasedArgumentsProvider;
import org.junit.jupiter.params.provider.Arguments;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.nchaugen.tabletest.junit.ParameterTypeConverter.convertValue;

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
     * @throws IllegalArgumentException if column and parameter counts don't match
     */
    @Override
    protected Stream<? extends Arguments> provideArguments(ExtensionContext context, TableTest tableTest) {
        String input = resolveInput(context, tableTest);
        Table table = TableParser.parse(input);
        Parameter[] parameters = resolveParameters(context, table.columnCount());

        return table.map(row -> toArguments(row, parameters));
    }

    /**
     * Resolves the table input source based on the annotation.
     * <p>
     * Uses either the inline value from the annotation or loads
     * an external resource if specified.
     *
     * @param context   The test extension context
     * @param tableTest The annotation containing the configuration
     * @return The table data string to parse
     */
    private String resolveInput(ExtensionContext context, TableTest tableTest) {
        return tableTest.resource().isBlank()
               ? tableTest.value()
               : loadResource(tableTest.resource(), tableTest.encoding(), context.getRequiredTestClass());
    }

    /**
     * Resolves and validates method parameters against the table structure.
     * <p>
     * Verifies that the number of table columns matches the number of method parameters.
     * One extra column is allowed, which will be interpreted as the name of the argument set.
     *
     * @param context     The test extension context
     * @param columnCount The number of columns in the table
     * @return Array of method parameters
     * @throws IllegalArgumentException if column and parameter counts don't match
     */
    private static Parameter[] resolveParameters(ExtensionContext context, int columnCount) {
        Parameter[] parameters = context.getRequiredTestMethod().getParameters();
        if (columnCount < parameters.length || columnCount > parameters.length + 1) {
            throw new IllegalArgumentException(
                String.format(
                    "Number of columns in table (%d) does not match number of parameters in test method (%d)",
                    columnCount,
                    parameters.length
                )
            );
        }
        return parameters;
    }

    /**
     * Loads table data from an external resource file.
     * <p>
     * Reads the content of the specified resource using the provided encoding
     * and returns it as a string with normalized line breaks.
     *
     * @param resource  Path to the resource containing table data
     * @param encoding  Character encoding to use when reading the file
     * @param testClass Class to use for resource resolution
     * @return Contents of the resource as a string
     * @throws RuntimeException if an IO error occurs during loading
     */
    private String loadResource(String resource, String encoding, Class<?> testClass) {
        try (InputStream resourceAsStream = resolveResourceStream(resource, testClass)) {
            return new BufferedReader(new InputStreamReader(resourceAsStream, encoding))
                .lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Resolves a resource path to an input stream.
     * <p>
     * Attempts to load the resource both with and without a leading slash
     * to accommodate different resource path styles.
     *
     * @param resource  Path to the resource file
     * @param testClass Class to use for resource resolution
     * @return Input stream for the resource
     * @throws NullPointerException if the resource cannot be found
     */
    private static InputStream resolveResourceStream(String resource, Class<?> testClass) {
        InputStream resourceAsStream = testClass.getResourceAsStream(resource);
        if (resourceAsStream == null) {
            resourceAsStream = testClass.getResourceAsStream("/" + resource);
        }

        return Objects.requireNonNull(resourceAsStream, "Could not load resource " + resource);
    }

    /**
     * Converts a table row into a stream of Arguments instances by mapping each cell to
     * the corresponding parameter type and possibly expanding sets of values to one Arguments
     * instance per value.
     * <p>
     * Uses {@link ParameterTypeConverter} to convert each cell value to the appropriate type
     * based on the method parameter's declared type.
     * <p>
     * If the row has one additional cell compared to the number of parameters, the first cell is
     * assumed to be the name of the argument set.
     *
     * @param row        The row of data from the table
     * @param parameters The method parameters defining the expected types
     * @return An Stream of Arguments containing the converted values
     */
    private static Stream<? extends Arguments> toArguments(Row row, Parameter[] parameters) {
        Optional<String> scenarioName = getScenarioName(row, parameters);

        List<Object> convertedValues = row
            .skipFirstIf(scenarioName.isPresent())
            .mapIndexed((index, cell) -> convertValue(cell, parameters[index]))
            .collect(Collectors.toList());

        return generateValueCombinations(convertedValues, parameters, 0)
            .map(values ->
                     scenarioName.isPresent()
                     ? Arguments.argumentSet(scenarioName.get(), values.toArray())
                     : Arguments.of(values.toArray())
            );
    }

    /**
     * Returns the name of the scenario for the specified row, if present.
     * <p>
     * If the row has one additional cell compared to the number of parameters, the first cell is
     * assumed to be the name of the argument set.
     *
     * @param row row of data from the table
     * @param parameters test method parameters
     */
    private static Optional<String> getScenarioName(Row row, Parameter[] parameters) {
        return isFirstColumnScenarioName(row, parameters)
               ? Optional.of(row.cell(0).toString())
               : Optional.empty();
    }

    /**
     * Returns true if the first cell of the row is the name of the scenario.
     * <p>
     * This is the case if the row has one additional cell compared to the number of parameters.
     *
     * @param row row of data from the table
     * @param parameters test method parameters
     * @return true if the first cell of the row is the name of the scenario
     */
    private static boolean isFirstColumnScenarioName(Row row, Parameter[] parameters) {
        return row.cellCount() == (parameters.length + 1);
    }

    /**
     * Recursively generates all combinations of values by expanding sets that are not
     * declared a Set type in the test method parameter.
     *
     * @param arguments  values from the row
     * @param parameters test method parameters
     * @param position   position to be processed next
     * @return Stream of all possible value combinations
     */
    private static Stream<List<?>> generateValueCombinations(
        List<?> arguments,
        Parameter[] parameters,
        int position
    ) {
        if (position >= arguments.size()) {
            return Stream.of(new ArrayList<>(arguments));
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
     * @param currentArgument value to consider for be expansion
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

}
