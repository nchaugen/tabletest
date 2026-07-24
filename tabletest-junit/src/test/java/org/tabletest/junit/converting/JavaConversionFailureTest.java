package org.tabletest.junit.converting;

import org.tabletest.junit.Description;
import org.tabletest.junit.ParameterFixture;
import org.tabletest.junit.TableTest;
import org.tabletest.junit.TableTestException;
import org.tabletest.junit.TypeConverter;
import org.tabletest.junit.javadomain.Age;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Parameter;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tabletest.junit.ParameterFixture.parameter;
import static org.tabletest.junit.ParameterTypeConverter.convertValue;
import static org.tabletest.junit.TableTestExceptionAssertions.*;

@DisplayName("Conversion failures")
public class JavaConversionFailureTest {

    @DisplayName("A value that fits no built-in conversion fails")
    @Description("""
            A number outside the target's range, a malformed date or character, and a
            type with no available converter all raise a parse-time failure. Each
            message closes by naming the classes searched for a type converter; those
            depend on where the test lives, so they are left out of the table.
            """)
    @TableTest("""
        Scenario                          | Input value | Parameter type                      | Error message?
        Decimal for a whole-number type   | 0.1         | java.lang.Byte                      | Built-in conversion of value "0.1" to type java.lang.Byte failed. Are you missing a type converter for this conversion?
        Whole number outside the range    | 256         | java.lang.Byte                      | Built-in conversion of value "256" to type java.lang.Byte failed. Are you missing a type converter for this conversion?
        More than a single character      | abc         | java.lang.Character                 | Built-in conversion of value "abc" to type java.lang.Character failed. Are you missing a type converter for this conversion?
        Malformed date                    | invalid     | java.time.LocalDate                 | Built-in conversion of value "invalid" to type java.time.LocalDate failed. Are you missing a type converter for this conversion?
        Type with no built-in conversion  | 52          | org.tabletest.junit.javadomain.Ages | Built-in conversion of value "52" to type org.tabletest.junit.javadomain.Ages failed. Are you missing a type converter for this conversion?
        """)
    void fails_builtin_conversion(String value, Class<?> type, String expectedMessage) {
        TableTestException exception = assertThrows(
            TableTestException.class,
            () -> convertValue(value, parameter(type))
        );
        assertEquals(expectedMessage + searchedLocations(), exception.getMessage());
    }

    private static String searchedLocations() {
        return " Locations searched for type converters: " + ParameterFixture.class.getTypeName();
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
