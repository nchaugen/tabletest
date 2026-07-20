package org.tabletest.junit.converting;

import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.junit.jupiter.api.DisplayName;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DisplayName("Parameterized collection element types")
@Description("""
        The generic signature of a List, Set, or Map parameter decides what its
        elements become: the same cell text converts to a different type depending
        on the element type the test method declares. Every table below reads the
        same way — one input column per collection kind, all three holding the same
        values, then the element type the method received and the elements
        themselves, which the input columns must convert to.

        An element type is fixed by the method signature and cannot vary by row, so
        each element type gets its own table. Only a Map's value type takes part in
        conversion: its keys are always the cell text, whatever key type the
        signature declares.
        """)
public class JavaParameterizedTypeConversionTest {

    @DisplayName("Byte elements hold a whole number in the 8-bit signed range")
    @TableTest("""
        Scenario         | List input  | Set input   | Map input          | Element type?  | Converted elements?
        Decimal digits   | [15]        | {15}        | [a: 15]            | java.lang.Byte | [15]
        Hex literal      | [0xF]       | {0xF}       | [a: 0xF]           | java.lang.Byte | [15]
        Several elements | [1, 2, 3]   | {1, 2, 3}   | [a: 1, b: 2, c: 3] | java.lang.Byte | [1, 2, 3]
        Range bounds     | [-128, 127] | {-128, 127} | [a: -128, b: 127]  | java.lang.Byte | [-128, 127]
        """)
    void converts_byte_elements(
        List<Byte> list,
        Set<Byte> set,
        Map<String, Byte> map,
        Class<?> expectedElementType,
        List<Byte> expectedElements
    ) {
        assertElements(expectedElementType, expectedElements, list, set, map.values());
    }

    @DisplayName("Integer elements hold a whole number in the 32-bit signed range")
    @TableTest("""
        Scenario         | List input | Set input | Map input          | Element type?     | Converted elements?
        Decimal digits   | [15]       | {15}      | [a: 15]            | java.lang.Integer | [15]
        Octal literal    | [017]      | {017}     | [a: 017]           | java.lang.Integer | [15]
        Several elements | [1, 2, 3]  | {1, 2, 3} | [a: 1, b: 2, c: 3] | java.lang.Integer | [1, 2, 3]
        """)
    void converts_integer_elements(
        List<Integer> list,
        Set<Integer> set,
        Map<String, Integer> map,
        Class<?> expectedElementType,
        List<Integer> expectedElements
    ) {
        assertElements(expectedElementType, expectedElements, list, set, map.values());
    }

    @DisplayName("Long elements hold whole numbers beyond the int range")
    @TableTest("""
        Scenario         | List input   | Set input    | Map input          | Element type?  | Converted elements?
        Decimal digits   | [15]         | {15}         | [a: 15]            | java.lang.Long | [15]
        Beyond int range | [2147483648] | {2147483648} | [a: 2147483648]    | java.lang.Long | [2147483648]
        Several elements | [1, 2, 3]    | {1, 2, 3}    | [a: 1, b: 2, c: 3] | java.lang.Long | [1, 2, 3]
        """)
    void converts_long_elements(
        List<Long> list,
        Set<Long> set,
        Map<String, Long> map,
        Class<?> expectedElementType,
        List<Long> expectedElements
    ) {
        assertElements(expectedElementType, expectedElements, list, set, map.values());
    }

    @DisplayName("Double elements accept plain and scientific notation")
    @TableTest("""
        Scenario            | List input | Set input  | Map input        | Element type?    | Converted elements?
        Plain decimal       | [1.5]      | {1.5}      | [a: 1.5]         | java.lang.Double | [1.5]
        Whole number        | [4]        | {4}        | [a: 4]           | java.lang.Double | [4.0]
        Scientific notation | [1.23e4]   | {1.23e4}   | [a: 1.23e4]      | java.lang.Double | [12300.0]
        Several elements    | [1.5, 2.5] | {1.5, 2.5} | [a: 1.5, b: 2.5] | java.lang.Double | [1.5, 2.5]
        """)
    void converts_double_elements(
        List<Double> list,
        Set<Double> set,
        Map<String, Double> map,
        Class<?> expectedElementType,
        List<Double> expectedElements
    ) {
        assertElements(expectedElementType, expectedElements, list, set, map.values());
    }

    @DisplayName("String elements keep the cell text unconverted")
    @TableTest("""
        Scenario         | List input | Set input | Map input    | Element type?    | Converted elements?
        Words            | [a, b]     | {a, b}    | [k: a, l: b] | java.lang.String | [a, b]
        Digits stay text | [1, 2]     | {1, 2}    | [k: 1, l: 2] | java.lang.String | ["1", "2"]
        """)
    void converts_string_elements(
        List<String> list,
        Set<String> set,
        Map<String, String> map,
        Class<?> expectedElementType,
        List<String> expectedElements
    ) {
        assertElements(expectedElementType, expectedElements, list, set, map.values());
    }

    @DisplayName("Collection elements convert one level deeper")
    @Description("""
            The element type is itself a collection, so its own element type decides
            what the innermost values become — Short in this table.
            """)
    @TableTest("""
        Scenario             | List input | Set input  | Map input        | Element type?  | Innermost elements?
        A list of lists      | [[6, 7]]   | {[6, 7]}   | [a: [6, 7]]      | java.util.List | [6, 7]
        Several nested lists | [[6], [7]] | {[6], [7]} | [a: [6], b: [7]] | java.util.List | [6, 7]
        """)
    void converts_nested_list_elements(
        List<List<Short>> list,
        Set<List<Short>> set,
        Map<String, List<Short>> map,
        Class<?> expectedElementType,
        List<Short> expectedInnermostElements
    ) {
        assertInstanceOf(expectedElementType, list.get(0));
        assertEquals(expectedInnermostElements, flattened(list));
        assertEquals(expectedInnermostElements, flattened(set));
        assertEquals(expectedInnermostElements, flattened(map.values()));
    }

    @DisplayName("Map elements convert their own values one level deeper")
    @Description("The nested map's declared value type is Long in this table.")
    @TableTest("""
        Scenario            | List input       | Set input        | Map input              | Element type? | Innermost values?
        A list of maps      | [[x: 6, y: 7]]   | {[x: 6, y: 7]}   | [a: [x: 6, y: 7]]      | java.util.Map | [6, 7]
        Several nested maps | [[x: 6], [y: 7]] | {[x: 6], [y: 7]} | [a: [x: 6], b: [y: 7]] | java.util.Map | [6, 7]
        """)
    void converts_nested_map_elements(
        List<Map<String, Long>> list,
        Set<Map<String, Long>> set,
        Map<String, Map<String, Long>> map,
        Class<?> expectedElementType,
        List<Long> expectedInnermostValues
    ) {
        assertInstanceOf(expectedElementType, list.get(0));
        assertEquals(expectedInnermostValues, flattenedValues(list));
        assertEquals(expectedInnermostValues, flattenedValues(set));
        assertEquals(expectedInnermostValues, flattenedValues(map.values()));
    }

    @DisplayName("An empty collection converts to an empty collection of the declared type")
    @TableTest("""
        Scenario       | List input | Set input | Map input | Element count?
        Empty          | []         | {}        | [:]       | 0
        Single element | [a]        | {a}       | [k: a]    | 1
        """)
    void converts_empty_collections(
        List<String> list,
        Set<String> set,
        Map<String, String> map,
        int expectedElementCount
    ) {
        assertEquals(expectedElementCount, list.size());
        assertEquals(expectedElementCount, set.size());
        assertEquals(expectedElementCount, map.size());
    }

    @DisplayName("Map keys stay text whatever key type the signature declares")
    @Description("""
            The parameter in this table is declared Map<Integer, Integer>, yet its
            keys arrive as the cell text: only the value type takes part in
            conversion.
            """)
    @TableTest("""
        Scenario       | Map input      | Key type?        | Keys?      | Value type?       | Values?
        Digits as keys | [1: 10, 2: 20] | java.lang.String | ["1", "2"] | java.lang.Integer | [10, 20]
        Words as keys  | [a: 10, b: 20] | java.lang.String | [a, b]     | java.lang.Integer | [10, 20]
        """)
    void ignores_declared_map_key_type(
        Map<Integer, Integer> map,
        Class<?> expectedKeyType,
        List<String> expectedKeys,
        Class<?> expectedValueType,
        List<Integer> expectedValues
    ) {
        assertElements(expectedKeyType, expectedKeys, map.keySet());
        assertElements(expectedValueType, expectedValues, map.values());
    }

    private static void assertElements(
        Class<?> expectedElementType,
        List<?> expectedElements,
        Collection<?>... collections
    ) {
        for (Collection<?> collection : collections) {
            assertEquals(expectedElements, List.copyOf(collection));
            collection.forEach(element -> assertInstanceOf(expectedElementType, element));
        }
    }

    private static List<Short> flattened(Collection<List<Short>> nested) {
        return nested.stream().flatMap(List::stream).toList();
    }

    private static List<Long> flattenedValues(Collection<Map<String, Long>> nested) {
        return nested.stream().flatMap(map -> map.values().stream()).toList();
    }
}
