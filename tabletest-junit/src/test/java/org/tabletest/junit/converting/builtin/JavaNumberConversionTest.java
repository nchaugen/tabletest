package org.tabletest.junit.converting.builtin;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DisplayName("Numbers")
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
public class JavaNumberConversionTest {

    @DisplayName("byte converts from a whole number in the 8-bit signed range")
    @Description("Decimal, hex, and octal text all convert, to the primitive and its wrapper alike.")
    @TableTest("""
        Scenario            | byte | Byte | Converted value?
        Decimal digits      | 15   | 15   | 15
        Hex literal         | 0xF  | 0xF  | 15
        Octal literal       | 017  | 017  | 15
        Most negative value | -128 | -128 | -128
        Largest value       | 127  | 127  | 127
        """)
    void converts_bytes(byte value, Byte boxedValue, byte expectedValue) {
        assertEquals(expectedValue, value);
        assertEquals(expectedValue, boxedValue.byteValue());
    }

    @DisplayName("short converts from a whole number in the 16-bit signed range")
    @TableTest("""
        Scenario            | short  | Short  | Converted value?
        Decimal digits      | 15     | 15     | 15
        Hex literal         | 0xF    | 0xF    | 15
        Octal literal       | 017    | 017    | 15
        Most negative value | -32768 | -32768 | -32768
        Largest value       | 32767  | 32767  | 32767
        """)
    void converts_shorts(short value, Short boxedValue, short expectedValue) {
        assertEquals(expectedValue, value);
        assertEquals(expectedValue, boxedValue.shortValue());
    }

    @DisplayName("int converts from a whole number in the 32-bit signed range")
    @TableTest("""
        Scenario            | int         | Integer     | Converted value?
        Decimal digits      | 15          | 15          | 15
        Hex literal         | 0xF         | 0xF         | 15
        Octal literal       | 017         | 017         | 15
        Most negative value | -2147483648 | -2147483648 | -2147483648
        Largest value       | 2147483647  | 2147483647  | 2147483647
        """)
    void converts_ints(int value, Integer boxedValue, int expectedValue) {
        assertEquals(expectedValue, value);
        assertEquals(expectedValue, boxedValue.intValue());
    }

    @DisplayName("long converts from a whole number in the 64-bit signed range")
    @TableTest("""
        Scenario            | long                 | Long                 | Converted value?
        Decimal digits      | 15                   | 15                   | 15
        Hex literal         | 0xF                  | 0xF                  | 15
        Octal literal       | 017                  | 017                  | 15
        Most negative value | -9223372036854775808 | -9223372036854775808 | -9223372036854775808
        Largest value       | 9223372036854775807  | 9223372036854775807  | 9223372036854775807
        """)
    void converts_longs(long value, Long boxedValue, long expectedValue) {
        assertEquals(expectedValue, value);
        assertEquals(expectedValue, boxedValue.longValue());
    }

    @DisplayName("float converts from plain or scientific notation")
    @Description("Applies to the primitive and its wrapper alike.")
    @TableTest("""
        Scenario            | float   | Float   | Converted value?
        Plain decimal       | 3.14159 | 3.14159 | 3.14159
        Leading zero        | 0.1     | 0.1     | 0.1
        Scientific notation | 1.23e4  | 1.23e4  | 12300
        No decimal point    | 123     | 123     | 123
        """)
    void converts_floats(float value, Float boxedValue, float expectedValue) {
        assertEquals(expectedValue, value);
        assertEquals(expectedValue, boxedValue.floatValue());
    }

    @DisplayName("double converts from plain or scientific notation")
    @Description("Applies to the primitive and its wrapper alike.")
    @TableTest("""
        Scenario            | double  | Double  | Converted value?
        Plain decimal       | 3.14159 | 3.14159 | 3.14159
        Leading zero        | 0.1     | 0.1     | 0.1
        Scientific notation | 1.23e4  | 1.23e4  | 12300
        No decimal point    | 123     | 123     | 123
        """)
    void converts_doubles(double value, Double boxedValue, double expectedValue) {
        assertEquals(expectedValue, value);
        assertEquals(expectedValue, boxedValue.doubleValue());
    }

    @DisplayName("BigDecimal converts from decimal text, keeping the precision written")
    @Description("""
            Scale is the number of digits after the decimal point — negative when
            scientific notation places the value's precision above the point.
            """)
    @TableTest("""
        Scenario            | Input value | Parameter type?      | BigDecimal as plain number? | BigDecimal scale?
        Plain decimal       | 3.14159     | java.math.BigDecimal | 3.14159                     | 5
        Leading zero        | 0.1         | java.math.BigDecimal | 0.1                         | 1
        Scientific notation | 1.23e4      | java.math.BigDecimal | 12300                       | -2
        No decimal point    | 123         | java.math.BigDecimal | 123                         | 0
        """)
    void converts_big_decimals(
        BigDecimal value,
        Class<?> parameterType,
        String expectedPlainNumber,
        int expectedScale
    ) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedPlainNumber, value.toPlainString());
        assertEquals(expectedScale, value.scale());
    }

    @DisplayName("BigInteger converts from whole numbers beyond the long range")
    @TableTest("""
        Scenario        | Input value                    | Parameter type?      | BigInteger beyond long range?
        Nineteen digits | 1234567890123456789            | java.math.BigInteger | false
        Thirty digits   | 123456789012345678901234567890 | java.math.BigInteger | true
        """)
    void converts_big_integers(BigInteger value, Class<?> parameterType, boolean expectedBeyondLong) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedBeyondLong, value.bitLength() > 63);
    }
}
