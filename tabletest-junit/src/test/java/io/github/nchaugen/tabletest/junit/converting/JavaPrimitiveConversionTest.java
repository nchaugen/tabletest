package io.github.nchaugen.tabletest.junit.converting;

import io.github.nchaugen.tabletest.junit.TableTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaPrimitiveConversionTest {

    @TableTest("""
        A   | B   | A and B
        Yes | Yes | Yes
        No  | Yes | No
        """)
    void primitive_factory_method_(boolean a, Boolean b, boolean expectedResult) {
        assertEquals(expectedResult, a && b);
    }

    @SuppressWarnings("unused")
    public static boolean parseBoolean(String value) {
        return value.equalsIgnoreCase("yes");
    }

    @TableTest("""
        A   | B     | A + B
        one | three | four
        two | two   | four
        """)
    void non_primitive_factory_method_(int a, Integer b, int expectedResult) {
        assertEquals(expectedResult, a + b);
    }

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
