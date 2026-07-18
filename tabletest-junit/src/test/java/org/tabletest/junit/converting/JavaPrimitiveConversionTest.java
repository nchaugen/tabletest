package org.tabletest.junit.converting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.tabletest.junit.TypeConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("spec")
@DisplayName("Custom type converters")
@Description("""
        A static @TypeConverter method in the test class converts cell text to
        types the built-in conversion does not cover — letting tables speak
        domain language instead of programmer literals.
        """)
public class JavaPrimitiveConversionTest {

    @DisplayName("A @TypeConverter method converts cell text to the parameter type")
    @Description("""
            A converter in the test class turns Yes/No into booleans; primitive
            and boxed parameters both use it.
            """)
    @TableTest("""
        A   | B   | A and B?
        Yes | Yes | Yes
        No  | Yes | No
        """)
    void primitive_type_converter(boolean a, Boolean b, boolean expectedResult) {
        assertEquals(expectedResult, a && b);
    }

    @TypeConverter
    @SuppressWarnings("unused")
    public static boolean parseBoolean(String value) {
        return value.equalsIgnoreCase("yes");
    }

    @DisplayName("Every column converts — inputs and expectations alike")
    @Description("""
            With a converter mapping number words to integers, the expected sum
            is written as a word too.
            """)
    @TableTest("""
        A   | B     | A + B?
        one | three | four
        two | two   | four
        """)
    void non_primitive_type_converter(int a, Integer b, int expectedResult) {
        assertEquals(expectedResult, a + b);
    }

    @TypeConverter
    @SuppressWarnings("unused")
    public static Integer parseInteger(String value) {
        return switch (value) {
            case "one" -> 1;
            case "two" -> 2;
            case "three" -> 3;
            case "four" -> 4;
            default -> throw new IllegalArgumentException("Unsupported value: " + value);
        };
    }

}
