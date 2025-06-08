package io.github.nchaugen.tabletest.junit.parsing;

import io.github.nchaugen.tabletest.junit.TableTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class JavaSetParsingTest {

    @TableTest(resource = "/parsing/set-values.table")
    void testSetValueParsing(Set<?> input, int expectedSize, Class<?> elementType) {
        assertEquals(expectedSize, input.size(), "List: " + input);
        input.stream().findFirst().ifPresent(
            (it) -> assertInstanceOf(elementType, it, "First element type for: " + input)
        );
    }

    @TableTest("""
        Scenario                     | Input                  | First element?
        Unquoted is trimmed          | {unquoted text}        | "unquoted text"
        Double-quoted is not trimmed | {" \t quoted text \t"} | ' \t quoted text \t'
        Single-quoted is not trimmed | {' \t quoted text \t'} | " \t quoted text \t"
        """)
    void testElementTrimming(
        Set<String> input,
        String expectedFirstElement
    ) {
        assertEquals(1, input.size());
        input.stream().findFirst().ifPresent(
            (it) -> assertEquals(expectedFirstElement, it)
        );
    }

    @TableTest("""
        Scenario             | Input              | Size? | Element Type?
        Set of lists         | {[a, b], [c, d]}   | 2     | java.util.List
        Set of maps          | {[a: 1], [b: 2]}   | 2     | java.util.Map
        Nested sets          | {{a}, {b, c}}      | 2     | java.util.Set
        Deep nesting         | {[[a]], [[b, c]]}  | 2     | java.util.List
        Mixed compound types | {[a: 1], [b], {c}} | 3     | java.lang.Object
        """)
    void testCompoundSetTypes(Set<?> input, int expectedSize, Class<?> expectedElementType) {
        assertEquals(expectedSize, input.size());
        input.stream().findFirst().ifPresent(
            (it) -> assertInstanceOf(expectedElementType, it)
        );
    }

}
