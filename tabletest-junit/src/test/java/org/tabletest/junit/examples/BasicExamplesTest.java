package org.tabletest.junit.examples;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("spec")
@DisplayName("Value formats")
@Description("""
        The basic value formats a table cell can hold: plain values converted to the
        parameter type, quoted strings, lists, and maps.
        """)
public class BasicExamplesTest {

    @DisplayName("Plain values convert to the parameter type")
    @TableTest("""
        Augend | Addend | Sum?
        2      | 3      | 5
        0      | 0      | 0
        1      | 1      | 2
        """)
    void testAddition(int augend, int addend, int sum) {
        assertEquals(sum, augend + addend);
    }

    @DisplayName("Strings are quoted only when they need to be")
    @Description("""
            Quotes are required around values containing pipes or commas in collections;
            either quote style works. "" is the empty string.
            """)
    @TableTest("""
        Value          | Length?
        Hello world    | 11
        "World, hello" | 12
        '|'            | 1
        ""             | 0
        """)
    void testString(String value, int expectedLength) {
        assertEquals(expectedLength, value.length());
    }

    @DisplayName("Lists nest values, lists, and maps")
    @TableTest("""
        List             | Size?
        [Hello, World]   | 2
        ["World, Hello"] | 1
        ['|', ",", abc]  | 3
        [[1, 2], [3, 4]] | 2
        [[a: 4], [b: 5]] | 2
        []               | 0
        """)
    void testList(List<Object> list, int expectedSize) {
        assertEquals(expectedSize, list.size());
    }

    @DisplayName("Maps hold mixed value types")
    @TableTest("""
        Map                                      | Size?
        [1: Hello, 2: World]                     | 2
        [pipe: "|", comma: ',']                  | 2
        [string: abc, list: [1, 2], map: [a: 4]] | 3
        [:]                                      | 0
        """)
    void testMap(Map<String, Object> map, int expectedSize) {
        assertEquals(expectedSize, map.size());
    }

    // Not published: comment lines don't reach the report, so the published table
    // would show two plain rows and none of what it demonstrates.
    @Tag("unpublished")
    @DisplayName("Comment lines annotate or disable rows")
    @Description("""
            Lines starting with // are ignored: use them to explain rows or park a row
            without deleting it. This table has a disabled row and two comment lines
            between its two live rows.
            """)
    @TableTest("""
        String         | Length?
        Hello world    | 11
        // The next row is currently disabled
        // "World, hello" | 12
        //
        // Special characters must be quoted
        '|'            | 1
        """)
    void testComment(String string, int expectedLength) {
        assertEquals(expectedLength, string.length());
    }

}
