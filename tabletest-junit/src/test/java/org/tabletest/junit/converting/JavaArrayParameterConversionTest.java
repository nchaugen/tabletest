package org.tabletest.junit.converting;

import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Array parameters")
@Description("""
        A [bracketed] list converts to an array parameter for any element type —
        object, primitive, nested, or map — and arrays nest inside other
        collection parameters. Every table below reads the same way: the Input
        value column is the text written in the cell, Parameter type is the type
        the test method receives it as, and Converted value is that parameter
        printed back out.
        """)
public class JavaArrayParameterConversionTest {

    @DisplayName("A list converts to a String array, and a blank cell to a null array")
    @TableTest("""
        Scenario       | Input value | Parameter type? | Converted value?
        Single element | [hello]     | String[]        | "[hello]"
        Several        | [a, b, c]   | String[]        | "[a, b, c]"
        Empty list     | []          | String[]        | "[]"
        Blank cell     |             |                 |
        """)
    void converts_to_string_array(String[] values, String expectedType, String expectedValue) {
        assertEquals(expectedType, typeNameOf(values));
        assertEquals(expectedValue, renderedValueOf(values));
    }

    @DisplayName("A list converts to a boxed Integer array")
    @TableTest("""
        Scenario       | Input value | Parameter type? | Converted value?
        Single element | [1]         | Integer[]       | "[1]"
        Several        | [1, 2, 3]   | Integer[]       | "[1, 2, 3]"
        Empty list     | []          | Integer[]       | "[]"
        """)
    void converts_to_integer_array(Integer[] values, String expectedType, String expectedValue) {
        assertEquals(expectedType, typeNameOf(values));
        assertEquals(expectedValue, renderedValueOf(values));
    }

    @DisplayName("A list converts to a primitive int array")
    @TableTest("""
        Scenario       | Input value | Parameter type? | Converted value?
        Single element | [1]         | int[]           | "[1]"
        Several        | [1, 2, 3]   | int[]           | "[1, 2, 3]"
        Empty list     | []          | int[]           | "[]"
        """)
    void converts_to_primitive_int_array(int[] values, String expectedType, String expectedValue) {
        assertEquals(expectedType, typeNameOf(values));
        assertEquals(expectedValue, Arrays.toString(values));
    }

    @DisplayName("A list converts to a primitive long array")
    @TableTest("""
        Scenario       | Input value | Parameter type? | Converted value?
        Single element | [1]         | long[]          | "[1]"
        Several        | [1, 2, 3]   | long[]          | "[1, 2, 3]"
        """)
    void converts_to_primitive_long_array(long[] values, String expectedType, String expectedValue) {
        assertEquals(expectedType, typeNameOf(values));
        assertEquals(expectedValue, Arrays.toString(values));
    }

    @DisplayName("A list converts to a primitive double array")
    @TableTest("""
        Scenario       | Input value     | Parameter type? | Converted value?
        Single element | [1.5]           | double[]        | "[1.5]"
        Several        | [1.5, 2.5, 3.5] | double[]        | "[1.5, 2.5, 3.5]"
        """)
    void converts_to_primitive_double_array(double[] values, String expectedType, String expectedValue) {
        assertEquals(expectedType, typeNameOf(values));
        assertEquals(expectedValue, Arrays.toString(values));
    }

    @DisplayName("Nested lists convert to a two-dimensional array")
    @TableTest("""
        Scenario      | Input value      | Parameter type? | Converted value?
        Nested lists  | [[a, b], [c, d]] | String[][]      | "[[a, b], [c, d]]"
        Empty inner   | [[], [e]]        | String[][]      | "[[], [e]]"
        """)
    void converts_to_nested_string_array(String[][] values, String expectedType, String expectedValue) {
        assertEquals(expectedType, typeNameOf(values));
        assertEquals(expectedValue, renderedValueOf(values));
    }

    @DisplayName("A list of maps converts to an array of maps")
    @TableTest("""
        Scenario   | Input value      | Parameter type? | Converted value?
        Single map | [[a: b]]         | Map[]           | "[{a=b}]"
        Two maps   | [[a: b], [c: d]] | Map[]           | "[{a=b}, {c=d}]"
        """)
    void converts_to_map_array(Map<String, String>[] values, String expectedType, String expectedValue) {
        assertEquals(expectedType, typeNameOf(values));
        assertEquals(expectedValue, renderedValueOf(values));
    }

    @DisplayName("Arrays nest as elements of a list")
    @Description("The parameter is a List, so the element type is the array — shown per element.")
    @TableTest("""
        Scenario      | Input value   | Element type? | Converted value?
        String arrays | [[a, b], [c]] | String[]      | "[[a, b], [c]]"
        """)
    void converts_list_of_string_arrays(List<String[]> values, String expectedElementType, String expectedValue) {
        assertEquals(expectedElementType, typeNameOf(values.get(0)));
        assertEquals(expectedValue, renderedValueOf(values.toArray()));
    }

    private static String typeNameOf(Object value) {
        return value == null ? null : value.getClass().getSimpleName();
    }

    private static String renderedValueOf(Object[] values) {
        return values == null ? null : Arrays.deepToString(values);
    }
}
