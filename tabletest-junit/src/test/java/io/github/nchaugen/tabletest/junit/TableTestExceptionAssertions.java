package io.github.nchaugen.tabletest.junit;

import org.junit.jupiter.api.function.Executable;

import java.util.stream.Stream;

import static io.github.nchaugen.tabletest.junit.ParameterFixture.parameter;
import static io.github.nchaugen.tabletest.junit.ParameterTypeConverter.convertValue;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TableTestExceptionAssertions {

    public static void assertThrowsWhenFallbackFails(Object parsedValue, String typeName) {
        assertThrowsTableTestException(
            () -> convertValue(parsedValue, parameter(typeName)),
            "Fallback JUnit conversion of value \"[^\"]*\" to type .+ " +
            "Are you missing a factory method for this conversion\\? " +
            "Locations searched for public static factory methods: .+"
        );
    }

    public static void assertThrowsWhenMultipleFactoryMethodsFound(Object parsedValue, Class<?> type) {
        assertThrowsTableTestException(
            () -> convertValue(parsedValue, parameter(type)),
            "Multiple factory methods found for type " + type.getTypeName() + " in class .+"
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
