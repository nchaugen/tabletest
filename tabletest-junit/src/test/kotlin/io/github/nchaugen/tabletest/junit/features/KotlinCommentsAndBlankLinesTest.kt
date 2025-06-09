package io.github.nchaugen.tabletest.junit.features

import io.github.nchaugen.tabletest.junit.TableTest
import kotlin.test.assertNotEquals

class KotlinCommentsAndBlankLinesTest {

    @TableTest(
        """    
        input                             | size?
        []                                | 0
        [1]                               | 1
        //
        // [1, 2]    | 2
        //
        [1, 2, 3] | 3
        // ending comment
        """
    )
    fun ignoring_comments(input: List<Integer>, expectedSize: Int) {
        assertNotEquals(2, expectedSize)
        assertNotEquals(2, input.size)
    }

    @TableTest(
        """    
        
        input     | size?

        []        | 0

        [1]       | 1

        [1, 2, 3] | 3
        
        """
    )
    fun ignoring_blank_lines(input: List<Integer>, expectedSize: Int) {
        assertNotEquals(2, expectedSize)
        assertNotEquals(2, input.size)
    }

}
