package org.tabletest.junit.features;

import org.tabletest.junit.TableTest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaStringArrayValueTest {

    @TableTest({
        "a | b | sum?",
        "1 | 2 | 3   ",
        "",
        "4 | 5 | 9   "
    })
    void empty_array_elements_are_ignored_as_blank_lines(int a, int b, int sum) {
        assertEquals(sum, a + b);
    }

    @TableTest({
        "a | b | sum?            ",
        "1 | 2 | 3               ",
        "// this row is a comment",
        "4 | 5 | 9               "
    })
    void comment_lines_in_array(int a, int b, int sum) {
        assertEquals(sum, a + b);
    }

    @TableTest({"a | b | sum?\n1 | 2 | 3\n4 | 5 | 9"})
    void single_element_with_embedded_newlines(int a, int b, int sum) {
        assertEquals(sum, a + b);
    }

    @TableTest({
        "Scenario | a | b | sum?",
        "Adding   | 1 | 2 | 3   ",
        "Zero sum | 0 | 0 | 0   "
    })
    void string_array_with_scenario_column(int a, int b, int sum) {
        assertEquals(sum, a + b);
    }

    @TableTest({
        "Scenario      | input     | expected?",
        "double quoted | \"hello\" | hello    ",
        "null value    |           |          "
    })
    void string_array_with_quoted_and_null_values(String input, String expected) {
        assertEquals(expected, input);
    }

    @TableTest({
        "Scenario | a         | Expected?",
        "Array    | [a, b, c] | [a, b, c]"
    })
    void array_value_sets_string(String[] a, List<String> expected) {
        assertEquals(expected, Arrays.asList(a));
    }

    @TableTest({
        "Scenario | a         | Expected?",
        "Array    | [1, 2, 3] | [1, 2, 3]"
    })
    void array_value_sets_byte(byte[] a, List<Byte> expected) {
        assertEquals(expected, IntStream.range(0, a.length).mapToObj(i -> a[i]).toList());
    }

    @TableTest({
        "Scenario | a         | Expected?",
        "Array    | [1, 2, 3] | [1, 2, 3]"
    })
    void array_value_sets_short(short[] a, List<Short> expected) {
        assertEquals(expected, IntStream.range(0, a.length).mapToObj(i -> a[i]).toList());
    }

    @TableTest({
        "Scenario | a         | Expected?",
        "Array    | [1, 2, 3] | [1, 2, 3]"
    })
    void array_value_sets_int(int[] a, List<Integer> expected) {
        assertEquals(expected, IntStream.of(a).boxed().toList());
    }

    @TableTest({
        "Scenario | a         | Expected?",
        "Array    | [1, 2, 3] | [1, 2, 3]"
    })
    void array_value_sets_long(long[] a, List<Long> expected) {
        assertEquals(expected, LongStream.of(a).boxed().toList());
    }

    @TableTest({
        "Scenario | a                 | Expected?        ",
        "Array    | [1.25, 2.5, 3.75] | [1.25, 2.5, 3.75]"
    })
    void array_value_sets_float(float[] a, List<Float> expected) {
        assertEquals(expected, IntStream.range(0, a.length).mapToObj(i -> a[i]).toList());
    }

    @TableTest({
        "Scenario | a                 | Expected?        ",
        "Array    | [1.25, 2.5, 3.75] | [1.25, 2.5, 3.75]"
    })
    void array_value_sets_double(double[] a, List<Double> expected) {
        assertEquals(expected, DoubleStream.of(a).boxed().toList());
    }

    @TableTest({
        "Scenario | a         | Expected?",
        "Array    | [a, b, c] | [a, b, c]"
    })
    void array_value_sets_char(char[] a, List<Character> expected) {
        assertEquals(expected, IntStream.range(0, a.length).mapToObj(i -> a[i]).toList());
    }

    @TableTest({
        "Scenario | a                   | Expected?          ",
        "Array    | [true, false, true] | [true, false, true]"
    })
    void array_value_sets_boolean(boolean[] a, List<Boolean> expected) {
        assertEquals(expected, IntStream.range(0, a.length).mapToObj(i -> a[i]).toList());
    }

}
