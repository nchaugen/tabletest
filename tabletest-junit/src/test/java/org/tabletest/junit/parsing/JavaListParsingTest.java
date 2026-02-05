package org.tabletest.junit.parsing;

import org.tabletest.junit.TableTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class JavaListParsingTest {

    @TableTest(resource = "/parsing/list-values.table")
    void testListValueParsing(List<?> list, int expectedSize, Class<?> elementType) {
        assertEquals(expectedSize, list.size(), "List: " + list);
        if (!list.isEmpty()) {
            assertInstanceOf(elementType, list.getFirst(), "First element type for: " + list);
        }
    }

    @TableTest("""
        Scenario                     | Input                  | First element?
        Unquoted is trimmed          | [unquoted text]        | "unquoted text"
        Double-quoted is not trimmed | [" \t quoted text \t"] | ' \t quoted text \t'
        Single-quoted is not trimmed | [' \t quoted text \t'] | " \t quoted text \t"
        """)
    void testElementTrimming(
        List<String> input,
        String expectedFirstElement
    ) {
        assertEquals(1, input.size());
        assertEquals(expectedFirstElement, input.getFirst());
    }

    @TableTest("""
        Scenario             | Input              | Size? | Element Type?
        Nested lists         | [[a, b], [c, d]]   | 2     | java.util.List
        Lists of maps        | [[a: 1], [b: 2]]   | 2     | java.util.Map
        Lists of sets        | [{a}, {b, c}]      | 2     | java.util.Set
        Deep nesting         | [[[a]], [[b, c]]]  | 2     | java.util.List
        Mixed compound types | [[a: 1], [b], {c}] | 3     | java.lang.Object
        """)
    void testCompoundListTypes(List<?> list, int expectedSize, Class<?> expectedElementType) {
        assertEquals(expectedSize, list.size());
        if (!list.isEmpty()) {
            assertInstanceOf(expectedElementType, list.getFirst());
        }
    }

}
