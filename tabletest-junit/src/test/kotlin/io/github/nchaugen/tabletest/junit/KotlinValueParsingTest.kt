package io.github.nchaugen.tabletest.junit

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals

class KotlinValueParsingTest {

    @TableTest(
        """    
        Scenario                                | Input      | Length?
        Tab character processed by compiler     | a\tb       | 4
        Quote marks processed by compiler       | Say \"hi\" | 10
        Backslash processed by compiler         | path\\file | 10
        Unicode character processed by compiler | \u0041B    | 7
        Octal character processed by compiler   | \101B      | 5
    """)
    fun testNonProcessedEscapeSequencesExampleInReadme(input: String, expectedLength: Int) {
        assertEquals(expectedLength, input.length)
    }

    @TableTest(resource = "/parsing/single-values.table")
    fun testSingleValueParsing(input: String, expectedLength: Int) {
        Assertions.assertEquals(expectedLength, input.length, "Input: $input")
    }

}
