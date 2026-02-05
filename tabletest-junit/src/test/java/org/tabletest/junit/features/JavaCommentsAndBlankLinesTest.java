package org.tabletest.junit.features;

import org.tabletest.junit.TableTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class JavaCommentsAndBlankLinesTest {

    @TableTest("""
        input     | size?
        []        | 0
        [1]       | 1
        //
        // [1, 2]    | 2
        //
        [1, 2, 3] | 3
        // ending comment
        """)
    void ignoring_comments(List<Integer> input, int expectedSize) {
        assertNotEquals(2, expectedSize);
        assertNotEquals(2, input.size());
    }

    @TableTest("""
        
        input     | size?
        
        []        | 0
        
        [1]       | 1

        [1, 2, 3] | 3
        
        """)
    void ignoring_blank_lines(List<Integer> input, int expectedSize) {
        assertNotEquals(2, expectedSize);
        assertNotEquals(2, input.size());
    }

}
