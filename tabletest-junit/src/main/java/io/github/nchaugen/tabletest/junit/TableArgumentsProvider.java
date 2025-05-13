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
import java.util.Objects;
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
     * converts each row into method arguments.
     *
     * @param context The current extension context
     * @param tableTest The TableTest annotation containing the table data
     * @return A stream of Arguments objects, one for each data row in the table
     * @throws IllegalArgumentException if column and parameter counts don't match
     */
    @Override
    protected Stream<? extends Arguments> provideArguments(ExtensionContext context, TableTest tableTest) {
        String input = resolveInput(context, tableTest);
        Table table = TableParser.parse(input);
        Parameter[] parameters = resolveParameters(context, table);

        return table.map(row -> toArguments(row, parameters));
    }

    /**
     * Resolves the table input source based on the annotation.
     * <p>
     * Uses either the inline value from the annotation or loads
     * an external resource if specified.
     *
     * @param context The test extension context
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
     * Verifies that the number of table columns matches the number of method parameters,
     * ensuring data consistency before conversion.
     *
     * @param context The test extension context
     * @param table The parsed table data
     * @return Array of method parameters
     * @throws IllegalArgumentException if column and parameter counts don't match
     */
    private static Parameter[] resolveParameters(ExtensionContext context, Table table) {
        Parameter[] parameters = context.getRequiredTestMethod().getParameters();
        if (table.columnCount() != parameters.length) {
            throw new IllegalArgumentException(
                String.format(
                    "Number of columns in table (%d) does not match number of parameters in test method (%d)",
                    table.columnCount(),
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
     * @param resource Path to the resource containing table data
     * @param encoding Character encoding to use when reading the file
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
     * @param resource Path to the resource file
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
     * Converts a table row into Arguments by mapping each cell to the corresponding parameter type.
     * <p>
     * Uses {@link ParameterTypeConverter} to convert each cell value to the appropriate type
     * based on the method parameter's declared type.
     *
     * @param row The row of data from the table
     * @param parameters The method parameters defining the expected types
     * @return An Arguments instance containing the converted values
     */
    private static Arguments toArguments(Row row, Parameter[] parameters) {
        return Arguments.of(
            row
                .mapIndexed((index, cell) -> convertValue(cell, parameters[index]))
                .toArray()
        );
    }
}
