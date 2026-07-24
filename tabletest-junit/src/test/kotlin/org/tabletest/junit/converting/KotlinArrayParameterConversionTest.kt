package org.tabletest.junit.converting

import org.tabletest.junit.TableTest
import kotlin.test.assertEquals

class KotlinArrayParameterConversionTest {

    @TableTest(
        """
        Scenario       | Values    | Expected?
        single element | [hello]   | [hello]
        multiple       | [a, b, c] | [a, b, c]
        empty          | []        | []
        null           |           |
        """
    )
    fun converts_to_string_array(values: Array<String>?, expected: List<String>?) {
        assertEquals(expected, values?.toList())
    }

    @TableTest(
        """
        Scenario       | Values    | Expected?
        single element | [1]       | [1]
        multiple       | [1, 2, 3] | [1, 2, 3]
        empty          | []        | []
        """
    )
    fun converts_to_integer_array(values: Array<Int>, expected: List<Int>) {
        assertEquals(expected, values.toList())
    }

    @TableTest(
        """
        Scenario       | Values    | Expected?
        single element | [1]       | [1]
        multiple       | [1, 2, 3] | [1, 2, 3]
        empty          | []        | []
        """
    )
    fun converts_to_primitive_int_array(values: IntArray, expected: List<Int>) {
        assertEquals(expected, values.toList())
    }

    @TableTest(
        """
        Scenario       | Values    | Expected?
        single element | [1]       | [1]
        multiple       | [1, 2, 3] | [1, 2, 3]
        """
    )
    fun converts_to_primitive_long_array(values: LongArray, expected: List<Long>) {
        assertEquals(expected, values.toList())
    }

    @TableTest(
        """
        Scenario       | Values          | Expected?
        single element | [1.5]           | [1.5]
        multiple       | [1.5, 2.5, 3.5] | [1.5, 2.5, 3.5]
        """
    )
    fun converts_to_primitive_double_array(values: DoubleArray, expected: List<Double>) {
        assertEquals(expected, values.toList())
    }

    @TableTest(
        """
        Scenario     | Values           | Expected?
        nested lists | [[a, b], [c, d]] | [[a, b], [c, d]]
        empty inner  | [[], [e]]        | [[], [e]]
        """
    )
    fun converts_to_nested_string_array(values: Array<Array<String>>, expected: List<List<String>>) {
        assertEquals(expected.size, values.size)
        for (i in values.indices) {
            assertEquals(expected[i], values[i].toList())
        }
    }

    @TableTest(
        """
        Scenario   | Values           | Expected?
        single map | [[a: b]]         | [[a: b]]
        two maps   | [[a: b], [c: d]] | [[a: b], [c: d]]
        """
    )
    fun converts_to_map_array(values: Array<Map<String, String>>, expected: List<Map<String, String>>) {
        assertEquals(expected, values.toList())
    }

    @TableTest(
        """
        Scenario      | Values        | Expected?
        string arrays | [[a, b], [c]] | [[a, b], [c]]
        """
    )
    fun converts_list_of_string_arrays(values: List<Array<String>>, expected: List<List<String>>) {
        assertEquals(expected.size, values.size)
        for (i in values.indices) {
            assertEquals(expected[i], values[i].toList())
        }
    }
}
