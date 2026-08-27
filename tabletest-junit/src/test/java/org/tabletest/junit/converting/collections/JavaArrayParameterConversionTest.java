package org.tabletest.junit.converting.collections;

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

        A family of array types that convert the same text shares one table. There the type is the
        value column's own header rather than a column of its own, and each column holds the list
        that column's type receives. A cell that fails to convert never reaches the test method, so
        a row that runs is a row that converted.

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

    @DisplayName("Converts a list to an array of any whole-number width")
    @TableTest("""
        Scenario       | byte[]    | short[]   | int[]     | long[]    | Converted value?
        Single element | [1]       | [1]       | [1]       | [1]       | "[1]"
        Several        | [1, 2, 3] | [1, 2, 3] | [1, 2, 3] | [1, 2, 3] | "[1, 2, 3]"
        Empty list     | []        | []        | []        | []        | "[]"
        """)
    void converts_to_whole_number_array(
        byte[] bytes,
        short[] shorts,
        int[] ints,
        long[] longs,
        String expectedValue
    ) {
        assertEquals(expectedValue, Arrays.toString(bytes));
        assertEquals(expectedValue, Arrays.toString(shorts));
        assertEquals(expectedValue, Arrays.toString(ints));
        assertEquals(expectedValue, Arrays.toString(longs));
    }

    @DisplayName("Converts a list to an array of either decimal width")
    @TableTest("""
        Scenario       | float[]           | double[]          | Converted value?
        Single element | [1.5]             | [1.5]             | "[1.5]"
        Several        | [1.25, 2.5, 3.75] | [1.25, 2.5, 3.75] | "[1.25, 2.5, 3.75]"
        Empty list     | []                | []                | "[]"
        """)
    void converts_to_decimal_array(float[] floats, double[] doubles, String expectedValue) {
        assertEquals(expectedValue, Arrays.toString(floats));
        assertEquals(expectedValue, Arrays.toString(doubles));
    }

    @DisplayName("Converts a list to a char or a boolean array")
    @Description("A char cell holds the single character itself, not its code point.")
    @TableTest("""
        Scenario       | char[]    | boolean[]           | Chars converted? | Booleans converted?
        Single element | [a]       | [true]              | "[a]"            | "[true]"
        Several        | [a, b, c] | [true, false, true] | "[a, b, c]"      | "[true, false, true]"
        Empty list     | []        | []                  | "[]"             | "[]"
        """)
    void converts_to_char_or_boolean_array(
        char[] chars,
        boolean[] booleans,
        String expectedChars,
        String expectedBooleans
    ) {
        assertEquals(expectedChars, Arrays.toString(chars));
        assertEquals(expectedBooleans, Arrays.toString(booleans));
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
