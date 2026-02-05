package org.tabletest.junit.examples;

import org.tabletest.junit.TableTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BasicExamplesTest {

    @TableTest("""
        Augend | Addend | Sum?
        2      | 3      | 5
        0      | 0      | 0
        1      | 1      | 2
        """)
    void testAddition(int augend, int addend, int sum) {
        assertEquals(sum, augend + addend);
    }

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

    @TableTest("""
        Map                                      | Size?
        [1: Hello, 2: World]                     | 2
        // ["|": 1, ',': 2, abc: 3]                 | 3
        [string: abc, list: [1, 2], map: [a: 4]] | 3
        [:]                                      | 0
        """)
    void testMap(Map<String, Object> map, int expectedSize) {
        assertEquals(expectedSize, map.size());
    }

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
