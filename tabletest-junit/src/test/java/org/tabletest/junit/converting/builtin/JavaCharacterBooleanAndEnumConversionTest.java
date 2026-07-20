package org.tabletest.junit.converting.builtin;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DisplayName("Characters, booleans, and enums")
@Description("""
        Cell text converts automatically to the declared parameter type, with no
        type converter to write. Every table below reads the same way: an Input
        value column holding the text as written in the cell, the parameter type
        it is converted to, and expectation columns stating observable properties
        of the object the test method received — so each row shows that the text
        became a valid object of that type, not merely that it converted.

        A parameter type is fixed by the test method signature and cannot vary by
        row, so each type gets its own short table.

        A primitive type shares its table with its wrapper, since the two are one
        type in two forms and a primitive's type cannot be observed once the value
        is boxed — there the type is the value column's own header rather than a
        column of its own.
        """)
public class JavaCharacterBooleanAndEnumConversionTest {

    @DisplayName("Char cells hold a single character")
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

    @DisplayName("Booleans convert from true and false")
    @TableTest("""
        Scenario | boolean | Boolean | Negated?
        True     | true    | true    | false
        False    | false   | false   | true
        """)
    void converts_booleans(boolean boolPrimitive, Boolean boolBoxed, boolean expectedNegated) {
        assertEquals(expectedNegated, !boolPrimitive);
        assertEquals(expectedNegated, !boolBoxed);
    }

    @DisplayName("Enum values match by constant name")
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
