package org.tabletest.junit.examples;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Value formats")
@Description("""
        The basic value formats a table cell can hold: plain values converted to the
        parameter type, quoted strings, lists, and maps.
        """)
public class BasicExamplesTest {

    @DisplayName("Plain values convert to the parameter type")
    @TableTest("""
        Scenario           | Augend | Addend | Sum?
        Two positive terms | 2      | 3      | 5
        Both zero          | 0      | 0      | 0
        Both one           | 1      | 1      | 2
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
        Scenario                   | Value          | Length?
        Plain text needs no quotes | Hello world    | 11
        A comma needs quotes       | "World, hello" | 12
        A pipe needs quotes        | '|'            | 1
        An empty string            | ""             | 0
        """)
    void testString(String value, int expectedLength) {
        assertEquals(expectedLength, value.length());
    }

    @DisplayName("Lists nest values, lists, and maps")
    @TableTest("""
        Scenario                   | List             | Size?
        Two plain values           | [Hello, World]   | 2
        A comma inside one element | ["World, Hello"] | 1
        Both quote styles at once  | ['|', ",", abc]  | 3
        Lists inside a list        | [[1, 2], [3, 4]] | 2
        Maps inside a list         | [[a: 4], [b: 5]] | 2
        No elements                | []               | 0
        """)
    void testList(List<Object> list, int expectedSize) {
        assertEquals(expectedSize, list.size());
    }

    @DisplayName("Maps hold mixed value types")
    @TableTest("""
        Scenario                    | Map                                      | Size?
        Digits as keys              | [1: Hello, 2: World]                     | 2
        Values needing either quote | [pipe: "|", comma: ',']                  | 2
        A value per kind            | [string: abc, list: [1, 2], map: [a: 4]] | 3
        No entries                  | [:]                                      | 0
        """)
    void testMap(Map<String, Object> map, int expectedSize) {
        assertEquals(expectedSize, map.size());
    }

    @DisplayName("Comment lines annotate or disable rows")
    @Description("""
            Lines starting with // are ignored: use them to explain rows or park a row
            without deleting it. This table has a disabled row and two comment lines
            between its two live rows.
            """)
    @TableTest("""
        Scenario        | String      | Length?
        An ordinary row | Hello world | 11
        // The next row is currently disabled
        // A comma        | "World, hello" | 12
        //
        // Special characters must be quoted
        A quoted pipe   | '|'         | 1
        """)
    void testComment(String string, int expectedLength) {
        assertEquals(expectedLength, string.length());
    }

}
