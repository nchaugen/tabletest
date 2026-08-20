package org.tabletest.junit.converting;

import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Array parameters")
@Description("""
        A [bracketed] list converts to an array parameter for any element type: object,
        primitive, nested, or map. An array also nests inside another collection parameter.

        Every table below reads the same way. Input value is the text as written in the cell,
        quoted so that it stays text. Parameter type is the array type the test method declares.
        Converted value is the array the method received, one entry per element.

        The map rule is the exception. Its input column is unquoted, because that column is the
        array itself, so the published cell shows the parsed value rather than the text. It reports
        the keys and the values in place of a converted value, because Java's map notation is not a
        TableTest format.
        """)
public class JavaArrayParameterConversionTest {

    @DisplayName("Converts a list to a String array, and a blank cell to a null array")
    @TableTest("""
        Scenario       | Input value | Parameter type? | Converted value?
        Single element | "[hello]"   | String[]        | [hello]
        Several        | "[a, b, c]" | String[]        | [a, b, c]
        Empty list     | "[]"        | String[]        | []
        Blank cell     |             |                 |
        """)
    void converts_to_string_array(String inputValue, String expectedType, String[] values) {
        assertEquals(expectedType, typeNameOf(values));
        assertEquals(inputValue, renderedValueOf(values));
    }

    @DisplayName("Converts a list to a boxed Integer array")
    @TableTest("""
        Scenario       | Input value | Parameter type? | Converted value?
        Single element | "[1]"       | Integer[]       | [1]
        Several        | "[1, 2, 3]" | Integer[]       | [1, 2, 3]
        Empty list     | "[]"        | Integer[]       | []
        """)
    void converts_to_integer_array(String inputValue, String expectedType, Integer[] values) {
        assertEquals(expectedType, typeNameOf(values));
        assertEquals(inputValue, renderedValueOf(values));
    }

    @DisplayName("Converts a list to a primitive int array")
    @TableTest("""
        Scenario       | Input value | Parameter type? | Converted value?
        Single element | "[1]"       | int[]           | [1]
        Several        | "[1, 2, 3]" | int[]           | [1, 2, 3]
        Empty list     | "[]"        | int[]           | []
        """)
    void converts_to_primitive_int_array(String inputValue, String expectedType, int[] values) {
        assertEquals(expectedType, typeNameOf(values));
        assertEquals(inputValue, Arrays.toString(values));
    }

    @DisplayName("Converts a list to a primitive long array")
    @TableTest("""
        Scenario       | Input value | Parameter type? | Converted value?
        Single element | "[1]"       | long[]          | [1]
        Several        | "[1, 2, 3]" | long[]          | [1, 2, 3]
        """)
    void converts_to_primitive_long_array(String inputValue, String expectedType, long[] values) {
        assertEquals(expectedType, typeNameOf(values));
        assertEquals(inputValue, Arrays.toString(values));
    }

    @DisplayName("Converts a list to a primitive double array")
    @TableTest("""
        Scenario       | Input value       | Parameter type? | Converted value?
        Single element | "[1.5]"           | double[]        | [1.5]
        Several        | "[1.5, 2.5, 3.5]" | double[]        | [1.5, 2.5, 3.5]
        """)
    void converts_to_primitive_double_array(String inputValue, String expectedType, double[] values) {
        assertEquals(expectedType, typeNameOf(values));
        assertEquals(inputValue, Arrays.toString(values));
    }

    @DisplayName("Converts nested lists to a two-dimensional array")
    @TableTest("""
        Scenario     | Input value        | Parameter type? | Converted value?
        Nested lists | "[[a, b], [c, d]]" | String[][]      | [[a, b], [c, d]]
        Empty inner  | "[[], [e]]"        | String[][]      | [[], [e]]
        """)
    void converts_to_nested_string_array(String inputValue, String expectedType, String[][] values) {
        assertEquals(expectedType, typeNameOf(values));
        assertEquals(inputValue, renderedValueOf(values));
    }

    @DisplayName("Converts a list of maps to an array of maps")
    @Description("""
            Each element is a Map, shown here by the keys and values it holds, in order. The array
            prints in Java's own map notation, which is not a TableTest format.
            """)
    @TableTest("""
        Scenario   | Input value      | Parameter type? | Map keys? | Map values?
        Single map | [[a: b]]         | Map[]           | [a]       | [b]
        Two maps   | [[a: b], [c: d]] | Map[]           | [a, c]    | [b, d]
        """)
    void converts_to_map_array(
        Map<String, String>[] values,
        String expectedType,
        List<String> expectedKeys,
        List<String> expectedValues
    ) {
        assertEquals(expectedType, typeNameOf(values));
        assertEquals(expectedKeys, entriesOf(values, Map::keySet));
        assertEquals(expectedValues, entriesOf(values, Map::values));
    }

    @DisplayName("Converts each element of a list to an array")
    @Description("The parameter is a List, so the element type is the array — shown per element.")
    @TableTest("""
        Scenario      | Input value     | Element type? | Converted value?
        String arrays | "[[a, b], [c]]" | String[]      | [[a, b], [c]]
        """)
    void converts_list_of_string_arrays(String inputValue, String expectedElementType, List<String[]> values) {
        assertEquals(expectedElementType, typeNameOf(values.get(0)));
        assertEquals(inputValue, renderedValueOf(values.toArray()));
    }

    private static String typeNameOf(Object value) {
        return value == null ? null : value.getClass().getSimpleName();
    }

    private static String renderedValueOf(Object[] values) {
        return values == null ? null : Arrays.deepToString(values);
    }

    private static List<String> entriesOf(
        Map<String, String>[] values,
        Function<Map<String, String>, Collection<String>> selector
    ) {
        return Arrays.stream(values).flatMap(map -> selector.apply(map).stream()).toList();
    }
}
