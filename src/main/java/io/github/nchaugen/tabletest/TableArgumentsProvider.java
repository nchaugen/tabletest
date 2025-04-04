package io.github.nchaugen.tabletest;

import io.github.nchaugen.tabletest.parser.ParserCombinators;
import io.github.nchaugen.tabletest.parser.TableParser;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.DefaultArgumentConverter;
import org.junit.jupiter.params.provider.AnnotationBasedArgumentsProvider;
import org.junit.jupiter.params.provider.Arguments;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class TableArgumentsProvider extends AnnotationBasedArgumentsProvider<TableTest> {

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

    private static Arguments toArguments(Row row, Parameter[] parameters) {
        return Arguments.of(
            row
                .mapIndexed((index, cell) -> convertValue(cell, parameters[index]))
                .toArray()
        );
    }

    private static Object convertValue(Object value, Parameter parameter) {
        return convertValue(
            value,
            input -> DefaultArgumentConverter.INSTANCE.convert(input, parameter.getType(), contextOf(parameter))
        );
    }

    private static Object convertValue(Object value, Function<Object, Object> converter) {
        return switch (value) {
            case List<?> list -> convertList(list);
            case Map<?, ?> map -> convertMap(map);
            default -> converter.apply(value);
        };
    }

    private static List<?> convertList(List<?> list) {
        return list.stream()
            .map(it -> convertValue(it, TableArgumentsProvider::convertElement))
            .collect(Collectors.toList());
    }

    private static Map<?, ?> convertMap(Map<?, ?> map) {
        return map.entrySet().stream()
            .map(entry -> Map.entry(
                entry.getKey(),
                convertValue(entry.getValue(), TableArgumentsProvider::convertElement)
            ))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Object convertElement(Object value) {
        if (value instanceof String input) {
            if (ParserCombinators.integer().parse(input).isCompleteSuccess()) {
                return Integer.parseInt(input);
            }
            else if (ParserCombinators.decimal().parse(input).isCompleteSuccess()) {
                return Double.parseDouble(input);
            }
        }
        return value;
    }

    private static ParameterContext contextOf(Parameter parameter) {
        return new ParameterContext() {
            @Override
            public Parameter getParameter() {
                return parameter;
            }

            @Override
            public int getIndex() {
                return 0;
            }

            @Override
            public Optional<Object> getTarget() {
                return Optional.empty();
            }
        };
    }
}
