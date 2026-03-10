package org.tabletest.junit.converting;

import org.tabletest.junit.TableTest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaArrayParameterConversionTest {

    @TableTest("""
        Scenario       | Values    | Expected?
        single element | [hello]   | [hello]
        multiple       | [a, b, c] | [a, b, c]
        empty          | []        | []
        null           |           |
        """)
    void converts_to_string_array(String[] values, List<String> expected) {
        assertEquals(expected, values == null ? null : Arrays.asList(values));
    }

    @TableTest("""
        Scenario       | Values    | Expected?
        single element | [1]       | [1]
        multiple       | [1, 2, 3] | [1, 2, 3]
        empty          | []        | []
        """)
    void converts_to_integer_array(Integer[] values, List<Integer> expected) {
        assertEquals(expected, Arrays.asList(values));
    }

    @TableTest("""
        Scenario       | Values    | Expected?
        single element | [1]       | [1]
        multiple       | [1, 2, 3] | [1, 2, 3]
        empty          | []        | []
        """)
    void converts_to_primitive_int_array(int[] values, List<Integer> expected) {
        assertEquals(expected, IntStream.of(values).boxed().toList());
    }

    @TableTest("""
        Scenario       | Values    | Expected?
        single element | [1]       | [1]
        multiple       | [1, 2, 3] | [1, 2, 3]
        """)
    void converts_to_primitive_long_array(long[] values, List<Long> expected) {
        assertEquals(expected, LongStream.of(values).boxed().toList());
    }

    @TableTest("""
        Scenario       | Values          | Expected?
        single element | [1.5]           | [1.5]
        multiple       | [1.5, 2.5, 3.5] | [1.5, 2.5, 3.5]
        """)
    void converts_to_primitive_double_array(double[] values, List<Double> expected) {
        assertEquals(expected, DoubleStream.of(values).boxed().toList());
    }

    @TableTest("""
        Scenario     | Values           | Expected?
        nested lists | [[a, b], [c, d]] | [[a, b], [c, d]]
        empty inner  | [[], [e]]        | [[], [e]]
        """)
    void converts_to_nested_string_array(String[][] values, List<List<String>> expected) {
        assertEquals(expected.size(), values.length);
        for (int i = 0; i < values.length; i++) {
            assertEquals(expected.get(i), Arrays.asList(values[i]));
        }
    }

    @TableTest("""
        Scenario   | Values           | Expected?
        single map | [[a: b]]         | [[a: b]]
        two maps   | [[a: b], [c: d]] | [[a: b], [c: d]]
        """)
    void converts_to_map_array(Map<String, String>[] values, List<Map<String, String>> expected) {
        assertEquals(expected, Arrays.asList(values));
    }

    @TableTest("""
        Scenario      | Values        | Expected?
        string arrays | [[a, b], [c]] | [[a, b], [c]]
        """)
    void converts_list_of_string_arrays(List<String[]> values, List<List<String>> expected) {
        assertEquals(expected.size(), values.size());
        for (int i = 0; i < values.size(); i++) {
            assertEquals(expected.get(i), Arrays.asList(values.get(i)));
        }
    }
}
