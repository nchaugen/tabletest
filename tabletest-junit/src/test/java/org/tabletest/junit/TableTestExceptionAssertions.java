package org.tabletest.junit;

import org.junit.jupiter.api.function.Executable;

import java.lang.reflect.Parameter;
import java.util.stream.Stream;

import static org.tabletest.junit.ParameterFixture.parameter;
import static org.tabletest.junit.ParameterTypeConverter.convertValue;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TableTestExceptionAssertions {

    public static void assertConversionFails(Object parsedValue, Class<?> type, String expectedMessagePart) {
        Exception exception = assertThrows(
            TableTestException.class,
            () -> convertValue(parsedValue, parameter(type))
        );
        assertTrue(
            exception.getMessage().contains(expectedMessagePart),
            String.format("Message `%s` did not contain `%s`", exception.getMessage(), expectedMessagePart)
        );
    }

    public static void assertThrowsWhenFallbackFails(Object parsedValue, Class<?> type) {
        assertThrowsTableTestException(
            () -> convertValue(parsedValue, parameter(type)),
            "Built-in conversion of value \"[^\"]*\" to type .+ " +
            "Are you missing a type converter for this conversion\\? " +
            "Locations searched for type converters: .+"
        );
    }

    public static void assertThrowsWhenMultipleTypeConvertersFound(Object parsedValue, Class<?> type) {
        assertThrowsTableTestException(
            () -> convertValue(parsedValue, parameter(type)),
            "Multiple type converters found for type " + type.getTypeName() + " in class .+"
        );
    }

    public static void assertThrowsWhenTypeConverterCycleDetected(Object parsedValue, Parameter parameter) {
        assertThrowsTableTestException(
            () -> convertValue(parsedValue, parameter),
            "Type converter cycle detected .+"
        );
    }

    public static void assertThrowsWhenNullSpecifiedForPrimitiveType(Object value, Class<?> type) {
        assertThrowsTableTestException(
            () -> convertValue(value, parameter(type)),
            ".+ translates to null, but null cannot be assigned to primitive type " + type.getTypeName()
        );
    }

    private static void assertThrowsTableTestException(Executable action, String expectedMessageRegex) {
        Exception expectedException = assertThrows(
            TableTestException.class,
            action
        );
        assertLinesMatch(
            Stream.of(expectedMessageRegex),
            Stream.of(expectedException.getMessage())
        );
    }
}
