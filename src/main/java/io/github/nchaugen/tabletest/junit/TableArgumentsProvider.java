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

class TableArgumentsProvider extends AnnotationBasedArgumentsProvider<TableTest> {

    @Override
    protected Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext, TableTest tableTest) {
        Class<?> testClass = extensionContext.getRequiredTestClass();
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

        return table.map(row -> toArguments(row, parameters, testClass));
    }

    private static Arguments toArguments(Row row, Parameter[] parameters, Class<?> testClass) {
        return Arguments.of(
            row
                .mapIndexed((index, cell) -> convertValue(cell, parameters[index]/*, testClass*/))
                .toArray()
        );
    }

}
