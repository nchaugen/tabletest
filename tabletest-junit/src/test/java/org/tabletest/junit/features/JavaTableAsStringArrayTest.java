package org.tabletest.junit.features;

import org.tabletest.junit.TableTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the array form of a table: the elements of the annotation's string array are joined
 * with a newline, so each element is a line of the table.
 * <p>
 * These stay conformance rather than spec. The rule they demonstrate is the join, and a
 * published table shows the rows a table produced, never the source it was written from. A
 * rule that joined the lines in its own body would restate the production step and verify
 * nothing. See the publish exclusion in tabletest-reporter.yaml.
 */
public class JavaTableAsStringArrayTest {

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
}
