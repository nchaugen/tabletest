package org.tabletest.junit.features;

import org.tabletest.junit.TableTest;
import org.junit.jupiter.api.Tag;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

// Unpublished: permanent — duplicates the parser's published "Blank lines and whole-line
// comments are ignored" rule (TableStructureTest), which states the same behaviour as data
// the report can render. These tables demonstrate it self-referentially, in the source of
// the very table being read, which no reporter capability can show.
@Tag("unpublished")
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
