package io.github.nchaugen.tabletest.junit;

import io.github.nchaugen.tabletest.parser.Row;
import io.github.nchaugen.tabletest.parser.Table;
import io.github.nchaugen.tabletest.parser.TableParser;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.AnnotationBasedArgumentsProvider;
import org.junit.jupiter.params.provider.Arguments;

import java.lang.reflect.Parameter;
import java.util.stream.Stream;

import static io.github.nchaugen.tabletest.junit.ParameterTypeConverter.convertValue;

/**
 * ArgumentsProvider implementation that supplies test arguments from tabular data in
 * TableTest format.
 * <p>
 * This provider processes tables defined in {@link TableTest} annotations,
 * parsing the table format string and converting each row into method arguments.
 * It validates that column counts match parameter counts and ensures type conversion
 * of all values.
 */
class TableArgumentsProvider extends AnnotationBasedArgumentsProvider<TableTest> {

    /**
     * Provides a stream of arguments for parameterized tests from tabular data.
     * <p>
     * The method parses the table string provided in the annotation, validates
     * that the number of columns matches the number of method parameters, and
     * converts each row into method arguments.
     *
     * @param extensionContext The current extension context
     * @param tableTest The TableTest annotation containing the table data
     * @return A stream of Arguments objects, one for each data row in the table
     * @throws IllegalArgumentException if column and parameter counts don't match
     */
    @Override
    protected Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext, TableTest tableTest) {
        Parameter[] parameters = extensionContext.getRequiredTestMethod().getParameters();

        Table table = TableParser.parse(tableTest.value());

        if (table.columnCount() != parameters.length) {
            throw new IllegalArgumentException(
                String.format(
                    "Number of columns in table (%d) does not match number of parameters in test method (%d)",
                    table.columnCount(),
                    parameters.length
                )
            );
        }

        return table.map(row -> toArguments(row, parameters));
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
