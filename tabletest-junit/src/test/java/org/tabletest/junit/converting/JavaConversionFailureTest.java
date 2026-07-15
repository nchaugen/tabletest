package org.tabletest.junit.converting;

import org.tabletest.junit.TableTest;
import org.tabletest.junit.TableTestException;
import org.tabletest.junit.TypeConverter;
import org.tabletest.junit.javadomain.Age;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Parameter;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tabletest.junit.ParameterTypeConverter.convertValue;
import static org.tabletest.junit.TableTestExceptionAssertions.*;

public class JavaConversionFailureTest {

    @TableTest("""
        table value | parameter type
        0.1         | java.lang.Byte
        256         | java.lang.Byte
        abc         | java.lang.Character
        invalid     | java.time.LocalDate
        52          | org.tabletest.junit.javadomain.Ages
        """)
    void fails_builtin_conversion(String value, Class<?> type) {
        assertThrowsWhenFallbackFails(value, type);
    }

    @TableTest("""
        table value | parameter type
        52          | org.tabletest.junit.javadomain.Age
        """)
    void fails_when_multiple_type_converters_found(String value, Class<?> type) {
        assertThrowsWhenMultipleTypeConvertersFound(value, type);
    }

    @TypeConverter
    public static Age parseAge(String age) {
        throw new IllegalStateException("should not be called");
    }

    @TypeConverter
    public static Age anotherParseAge(Integer age) {
        throw new IllegalStateException("should not be called");
    }

    @Test
    void failing_primitive_conversion() {
        assertThrowsWhenNullSpecifiedForPrimitiveType(null, boolean.class);
    }

    @Test
    void failing_conversion_lists_searched_locations_separated_by_commas() {
        TableTestException exception = assertThrows(
            TableTestException.class,
            () -> convertValue("invalid", localDateParameterOf(NestedFixture.class))
        );
        String expectedLocations =
            NestedFixture.class.getTypeName() + ", " + JavaConversionFailureTest.class.getTypeName();
        assertTrue(
            exception.getMessage().contains(expectedLocations),
            "Message does not list searched locations `" + expectedLocations + "`: " + exception.getMessage()
        );
    }

    public static class NestedFixture {

        @SuppressWarnings("unused")
        private void params(LocalDate date) {
        }
    }

    private static Parameter localDateParameterOf(Class<?> fixtureClass) {
        try {
            return fixtureClass.getDeclaredMethod("params", LocalDate.class).getParameters()[0];
        } catch (NoSuchMethodException cause) {
            throw new IllegalStateException(cause);
        }
    }

}
