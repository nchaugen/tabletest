package io.github.nchaugen.tabletest.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaValueParsingTest {

    @TableTest("""
        Scenario                                | Input      | Length?
        Tab character processed by compiler     | a\tb       | 3
        Quote marks processed by compiler       | Say \"hi\" | 8
        Quote marks processed by compiler       | Say "hi"   | 8
        Backslash processed by compiler         | path\\file | 9
        Unicode character processed by compiler | \u0041B    | 2
        Octal character processed by compiler   | \101B      | 2
        """)
    void testJavaEscapeSequencesExampleInReadme(String input, int expectedLength) {
        assertEquals(expectedLength, input.length(), "Input: " + input);
    }

    @TableTest(resource = "/parsing/single-values.table")
    void testSingleValueParsing(String input, int expectedLength ) {
        assertEquals(expectedLength, input.length(), "Input: " + input);
    }

    @TableTest("""
    Scenario                     | Input              | Expected Length
    Tab escape sequence          | a\tb               | 3
    Backslash escape sequence    | path\\file         | 9
    Double quote escape sequence | Say \"hi\"         | 8
    Single quote escape sequence | It\\'s fine        | 10
    Unicode escape A             | \u0041B            | 2
    Unicode escape euro          | \u20ACost          | 4
    Unicode escape emoji         | \uD83D\uDE00       | 2
    Octal escape A               | \101B              | 2
    Octal escape max             | \377end            | 4
    Form feed escape             | page1\fpage2       | 11
    Vertical tab escape          | a\u000Bb           | 3
    Backspace escape             | abc\bdef           | 7
    Bell character escape        | alert\u0007end     | 9
    Null character escape        | text\u0000null     | 9
    Escape character escape      | esc\u001Bseq       | 7
    Multiple escape sequences    | a\"\t\'\bb         | 6
    Mixed escape and text        | Hello\tWorldd\b    | 12
    Complex unicode sequence     | \u03B1\u03B2\u03B3 | 3
    """)
    void testJavaEscapeSequences(String input, int expectedLength) {
        assertEquals(expectedLength, input.length(), "Input: " + input);
    }


}
