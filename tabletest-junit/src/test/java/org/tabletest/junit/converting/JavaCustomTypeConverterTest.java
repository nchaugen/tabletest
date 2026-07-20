package org.tabletest.junit.converting;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.tabletest.junit.TypeConverter;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DisplayName("Custom type converters")
@Description("""
        A static @TypeConverter method in the test class decides what a cell's text
        becomes, so a table can speak the domain's language instead of programmer
        literals. Three converters serve the tables below: Yes/No becomes a boolean,
        a number word becomes an integer, and a day word becomes a date.

        The rules are written so the conversion itself stays visible. Each table
        pairs the text as written in the cell with the value the test method
        received, printed back as plain text — a String column no converter touches,
        and therefore one that shows what the converter produced instead of
        restating what it was given.
        """)
public class JavaCustomTypeConverterTest {

    @DisplayName("A converter turns cell text into the declared parameter type")
    @Description("Yes and No become booleans, for a primitive and a boxed parameter alike.")
    @TableTest("""
        Scenario           | boolean | Boolean | Converted value as text?
        Affirmative        | Yes     | Yes     | true
        Negative           | No      | No      | false
        Case is ignored    | yES     | yES     | true
        Anything else is No| maybe   | maybe   | false
        """)
    void converts_text_to_booleans(boolean value, Boolean boxedValue, String expectedText) {
        assertEquals(expectedText, String.valueOf(value));
        assertEquals(expectedText, String.valueOf(boxedValue));
    }

    @DisplayName("A converter takes precedence over built-in conversion of the same type")
    @Description("""
            Day words are the dates this converter knows; anything else it hands to
            the built-in LocalDate conversion, so ISO text still works.
            """)
    @TableTest("""
        Scenario         | Input value | Parameter type?     | Converted date as text? | Date day of week?
        Day word         | yesterday   | java.time.LocalDate | 2025-06-06              | FRIDAY
        Another day word | tomorrow    | java.time.LocalDate | 2025-06-08              | SUNDAY
        Unknown to it    | 2024-02-29  | java.time.LocalDate | 2024-02-29              | THURSDAY
        """)
    void converts_day_words_to_dates(
        LocalDate value,
        Class<?> parameterType,
        String expectedText,
        String expectedDayOfWeek
    ) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedText, value.toString());
        assertEquals(expectedDayOfWeek, value.getDayOfWeek().name());
    }

    @DisplayName("Every column converts — inputs and expectations alike")
    @Description("""
            The expected sum is written as a number word too; the digits column shows
            the sum the test method computed from the converted parameters.
            """)
    @TableTest("""
        Scenario      | int | Integer | Sum?  | Sum in digits?
        One and three | one | three   | four  | 4
        Two and two   | two | two     | four  | 4
        Two and three | two | three   | five  | 5
        """)
    void converts_number_words_in_every_column(int a, Integer b, int expectedSum, String expectedDigits) {
        assertEquals(expectedSum, a + b);
        assertEquals(expectedDigits, String.valueOf(a + b));
    }

    @DisplayName("A converter reaches the elements inside a collection")
    @TableTest("""
        Scenario      | Number words    | Element type?     | Sum in digits?
        Two words     | [one, three]    | java.lang.Integer | 4
        Three words   | [one, one, two] | java.lang.Integer | 4
        Repeated word | [two, two, two] | java.lang.Integer | 6
        """)
    void converts_number_words_inside_a_list(
        List<Integer> numbers,
        Class<?> expectedElementType,
        String expectedDigits
    ) {
        numbers.forEach(number -> assertInstanceOf(expectedElementType, number));
        assertEquals(expectedDigits, String.valueOf(numbers.stream().mapToInt(Integer::intValue).sum()));
    }

    @TypeConverter
    @SuppressWarnings("unused")
    public static boolean parseBoolean(String value) {
        return value.equalsIgnoreCase("yes");
    }

    @TypeConverter
    @SuppressWarnings("unused")
    public static Integer parseInteger(String value) {
        return switch (value) {
            case "one" -> 1;
            case "two" -> 2;
            case "three" -> 3;
            case "four" -> 4;
            case "five" -> 5;
            case "six" -> 6;
            default -> throw new IllegalArgumentException("Unsupported value: " + value);
        };
    }

    @TypeConverter
    @SuppressWarnings("unused")
    public static LocalDate parseLocalDate(String value) {
        return switch (value) {
            case "yesterday" -> LocalDate.parse("2025-06-06");
            case "today" -> LocalDate.parse("2025-06-07");
            case "tomorrow" -> LocalDate.parse("2025-06-08");
            default -> LocalDate.parse(value);
        };
    }
}
