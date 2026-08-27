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
        the cell. A Parameter type column holds the type the text converts to. Expectation
        columns state observable properties of the object the test method receives. Each row
        therefore shows that the text became a valid object of that type, and not merely that
        it converted.

        The test method signature fixes a parameter type, and no row can vary it. Each type
        therefore gets its own short table.

        A primitive type shares its table with its wrapper. The two are one type in two forms,
        and a boxed value does not report its primitive type. The value column's own header names it
        there, in place of a column of its own.
        """)
public class JavaCharacterBooleanAndEnumConversionTest {

    @DisplayName("Converts a single character to char")
    @Description("Primitive and boxed alike. A digit cell becomes the digit character, not a number.")
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
            Case does not matter. Each word therefore gets a row in lower case and a row in upper
            case. A value set does not work here. A set holds converted values, so every spelling
            of true collapses to one member, and only that member runs.

            No other text converts. A cell holding yes or 1 fails, and does not become false.
            Conversion failures shows that row.
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
    @Description("The parameter type decides which enum to search. It is TimeUnit in this table.")
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
