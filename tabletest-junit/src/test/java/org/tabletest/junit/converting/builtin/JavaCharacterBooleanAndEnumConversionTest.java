package org.tabletest.junit.converting.builtin;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DisplayName("Characters, booleans, and enums")
@Description("""
        Cell text converts automatically to the declared parameter type. There is no type
        converter to write.

        Every table below reads the same way. An Input value column holds the text as written in
        the cell. A Parameter type column holds the type it converts to. Expectation columns
        state observable properties of the object the test method received, so each row shows
        that the text became a valid object of that type, not merely that it converted.

        A parameter type is fixed by the test method signature and cannot vary by row, so each
        type gets its own short table.

        A primitive type shares its table with its wrapper. The two are one type in two forms,
        and a primitive's type cannot be observed once the value is boxed. There the type is the
        value column's own header rather than a column of its own.
        """)
public class JavaCharacterBooleanAndEnumConversionTest {

    @DisplayName("Converts a single character to char")
    @Description("Primitive and boxed alike; a digit cell becomes the digit character, not a number.")
    @TableTest("""
        Scenario         | char | Character | Code point?
        Lowercase letter | a    | a         | 97
        Uppercase letter | Z    | Z         | 90
        Digit            | 1    | 1         | 49
        """)
    void converts_chars(char charPrimitive, Character charBoxed, int expectedCodePoint) {
        assertEquals(expectedCodePoint, charPrimitive);
        assertEquals(expectedCodePoint, charBoxed.charValue());
    }

    @DisplayName("Converts true and false to boolean")
    @Description("""
            Case does not matter, so each word gets a row in lower case and a row in upper case.
            A value set would not do here: it holds converted values, so every spelling of true
            would collapse to one member and only one of them would run.

            No other text converts. A cell holding yes or 1 fails rather than quietly becoming
            false, which Conversion failures shows.
            """)
    @TableTest("""
        Scenario                    | boolean | Boolean | Negated?
        True written in lower case  | true    | true    | false
        True written in upper case  | TRUE    | TRUE    | false
        False written in lower case | false   | false   | true
        False written in upper case | FALSE   | FALSE   | true
        """)
    void converts_booleans(boolean boolPrimitive, Boolean boolBoxed, boolean expectedNegated) {
        assertEquals(expectedNegated, !boolPrimitive);
        assertEquals(expectedNegated, !boolBoxed);
    }

    @DisplayName("Converts a constant name to an enum")
    @Description("The parameter type decides which enum to search — TimeUnit in this table.")
    @TableTest("""
        Scenario | Input value | Parameter type?               | TimeUnit seconds per unit?
        Second   | SECONDS     | java.util.concurrent.TimeUnit | 1
        Minute   | MINUTES     | java.util.concurrent.TimeUnit | 60
        Hour     | HOURS       | java.util.concurrent.TimeUnit | 3600
        """)
    void converts_enums(TimeUnit value, Class<?> parameterType, long expectedSeconds) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedSeconds, value.toSeconds(1));
    }
}
