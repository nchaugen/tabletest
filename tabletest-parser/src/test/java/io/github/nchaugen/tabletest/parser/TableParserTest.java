package io.github.nchaugen.tabletest.parser;

import io.github.nchaugen.tabletest.junit.TableTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TableParserTest {

    @Test
    void shouldParseTable() {
        //language=TableTest
        String input = """
            a | b | c
            1 | 2 | 3
            4 | 5 | 6
            """;

        Table result = TableParser.parse(input);

        assertEquals(2, result.rowCount());
        assertEquals(3, result.columnCount());

        assertEquals("a", result.header(0));
        assertEquals("b", result.header(1));
        assertEquals("c", result.header(2));

        assertEquals(List.of("a", "b", "c"), result.headers());
        assertEquals(List.of("1", "2", "3"), result.row(0).values());
        assertEquals(List.of("4", "5", "6"), result.row(1).values());
    }

    @Test
    void shouldIgnoreComments() {
        //language=TableTest
        String input = """
            // this is a TableTest table
            a     | b
            // comment
            // 0 | 1
            '//2' | 3
            4 //  | 5
            6     | // 7
            8     | 9 //
            """;

        Table result = TableParser.parse(input);

        assertEquals(List.of("a", "b"), result.headers());
        assertEquals(List.of("//2", "3"), result.row(0).values());
        assertEquals(List.of("4 //", "5"), result.row(1).values());
        assertEquals(List.of("6", "// 7"), result.row(2).values());
        assertEquals(List.of("8", "9 //"), result.row(3).values());
    }

    @Test
    void shouldIgnoreBlankLines() {
        //language=TableTest
        String input = """
                    \s
             a | b | c
            \s
             1 | 2 | 3
            \s
                      \s
             4 | 5 | 6
                            \s
            \s""";

        Table result = TableParser.parse(input);

        assertEquals(2, result.rowCount());
        assertEquals(List.of("a", "b", "c"), result.headers());
        assertEquals(List.of("1", "2", "3"), result.row(0).values());
        assertEquals(List.of("4", "5", "6"), result.row(1).values());

    }

    @TableTest("""
        Scenario               | Input               | Captured?
        Null list              | ''                  |
        Empty list             | '[]'                | []
        Basic list             | '[a, b, c]'         | [a, b, c]
        Nested list            | '[[a], [b], [c]]'   | [[a], [b], [c]]
        Mixted content list    | '[[],a]'            | [[], a]
        List with set          | '[{a,b}, c]'        | [{a, b}, c]
        List with map          | '[[a:b], c]'        | [[a: b], c]
        List with quoted value | '["a,b", c]'        | ["a,b", c]
        Extra whitespace       | '  [  a  ,  a  ]  ' | [a, a]
        """)
    void shouldCaptureLists(String input, List<Object> expectedCaptures) {
        assertEquals(
            expectedCaptures,
            TableParser.parse("Scenario | Input\nList value | " + input).row(0).value(1)
        );
    }

    @TableTest("""
        Scenario                       | Input    | Success? | Parsed Type?     | Error Message?
        Quoted brackets                | '"[]"'   | true     | java.lang.String |
        Only opening bracket           | '['      | false    |                  | Failed to parse `[`
        Only closing bracket           | ']'      | true     | java.lang.String |
        Opening bracket, closing brace | '[a, b}' | false    |                  | Failed to parse `[a, b}`
        Opening brace, closing bracket | '{a, b]' | false    |                  | Failed to parse `{a, b]`
        Double opening bracket         | '[[]'    | false    |                  | Failed to parse `[[]`
        Double closing bracket         | '[]]'    | false    |                  | Failed to parse `]`
        Unexpected leading character   | 'a[]'    | true     | java.lang.String |
        Unexpected trailing character  | '[]a'    | false    |                  | Failed to parse `a`
        Missing element                | '[a,]'   | false    |                  | Failed to parse `[a,]`
        """)
    void shouldHandleInvalidListSyntax(
        String input,
        boolean expectedSuccess,
        Class expectedType,
        String expectedErrorMessage
    ) {
        String table = "Scenario | Input\nInvalid | " + input;
        if (expectedSuccess) {
            assertInstanceOf(expectedType, TableParser.parse(table).row(0).value(1));
        } else {
            TableTestParseException actualException = assertThrows(
                TableTestParseException.class,
                () -> TableParser.parse(table)
            );
            assertTrue(actualException.getMessage().startsWith(expectedErrorMessage), actualException.getMessage());
        }
    }

    @TableTest("""
        Scenario              | Input               | Captured?
        Null set              | ''                  |
        Empty set             | '{}'                | {}
        Basic set             | '{a, b, a, a, c}'   | {a, b, c}
        Nested set            | '{{a}, {b}, {b}}'   | {{a}, {b}}
        Mixted content set    | '{{},a,a,{}}'       | {{}, a}
        Set with list         | '{[a],[b],[b]}'     | {[a], [b]}
        Set with map          | '{[a:b], [a:b]}'    | {[a: b]}
        Set with quoted value | '{"a,b", c, "a,b"}' | {"a,b", c}
        Extra whitespace      | '  {  a  ,  a  }  ' | {a}
        """)
    void shouldCaptureSets(String input, Set<Object> expectedCaptures) {
        assertEquals(
            expectedCaptures,
            TableParser.parse("Scenario | Input\nSet value | " + input).row(0).value(1)
        );
    }

    @TableTest("""
        Scenario                       | Input    | Success? | Parsed Type?     | Error Message?
        Quoted braces                  | '"{}"'   | true     | java.lang.String |
        Missing closing brace          | '{'      | false    |                  | Failed to parse `{`
        Missing opening brace          | '}'      | true     | java.lang.String |
        Opening bracket, closing brace | '[a, b}' | false    |                  | Failed to parse `[a, b}`
        Opening brace, closing bracket | '{a, b]' | false    |                  | Failed to parse `{a, b]`
        Double opening brace           | '{{}'    | false    |                  | Failed to parse `{{}`
        Double closing brace           | '{}}'    | false    |                  | Failed to parse `}`
        Unexpected leading character   | 'a{}'    | true     | java.lang.String |
        Unexpected trailing character  | '{}a'    | false    |                  | Failed to parse `a`
        Missing element                | '{a,}'   | false    |                  | Failed to parse `{a,}`
        """)
    void shouldHandleInvalidSetSyntax(
        String input,
        boolean expectedSuccess,
        Class expectedType,
        String expectedErrorMessage
    ) {
        String table = "Scenario | Input\nInvalid | " + input;
        if (expectedSuccess) {
            assertInstanceOf(expectedType, TableParser.parse(table).row(0).value(1));
        } else {
            TableTestParseException actualException = assertThrows(
                TableTestParseException.class,
                () -> TableParser.parse(table)
            );
            assertTrue(actualException.getMessage().startsWith(expectedErrorMessage), actualException.getMessage());
        }
    }

    @TableTest("""
        Scenario              | Input                          | Captured?
        Null map              | ''                             |
        Empty map             | '[:]'                          | [:]
        Basic map             | '[a:b, c:d]'                   | [a: b, c: d]
        Nested map            | '[A:[a:1], B:[b:2], C:[c:3]]'  | [A: [a: 1], B: [b: 2], C: [c: 3]]
        Mixted content map    | '[m:[:], s:a]'                 | [m: [:], s: a]
        Map with set          | '[s:{a,b}, t:{c}]'             | [s: {a, b}, t: {c}]
        Map with list         | '[l:[a,b], i:[c]]'             | [l: [a, b], i: [c]]
        Map with quoted value | '[q:"a,b", u:c]'               | [q: "a,b", u: c]
        Same value keys       | '[a:b, a:[b], a:{b}, a:[b:c]]' | [a: [b: c]]
        Extra whitespace      | '  [   a   :  a   ]  '         | [a: a]
        """)
    void shouldCaptureMaps(String input, Map<String, Object> expectedCaptures) {
        assertEquals(
            expectedCaptures,
            TableParser.parse("Scenario | Input\nMap value | " + input).row(0).value(1)
        );
    }

    @TableTest("""
        Scenario                       | Input    | Success? | Parsed Type?     | Error Message?
        Quoted empty map               | '"[:]"'  | true     | java.lang.String |
        Missing closing bracket        | '[:'     | false    |                  | Failed to parse `[:`
        Missing opening bracket        | ':]'     | true     | java.lang.String |
        Opening bracket, closing brace | '[a: b}' | false    |                  | Failed to parse `[a: b}`
        Opening brace, closing bracket | '{a: b]' | false    |                  | Failed to parse `{a: b]`
        Double opening bracket         | '[[:]'   | false    |                  | Failed to parse `[[:]`
        Double closing bracket         | '[:]]'   | false    |                  | Failed to parse `]`
        Unexpected leading character   | 'a[:]'   | true     | java.lang.String |
        Unexpected trailing character  | '[:]a'   | false    |                  | Failed to parse `a`
        Missing element                | '[a:b,]' | false    |                  | Failed to parse `[a:b,]`
        """)
    void shouldHandleInvalidMapSyntax(
        String input,
        boolean expectedSuccess,
        Class expectedType,
        String expectedErrorMessage
    ) {
        String table = "Scenario | Input\nInvalid | " + input;
        if (expectedSuccess) {
            assertInstanceOf(expectedType, TableParser.parse(table).row(0).value(1));
        } else {
            TableTestParseException actualException = assertThrows(
                TableTestParseException.class,
                () -> TableParser.parse(table)
            );
            assertTrue(actualException.getMessage().startsWith(expectedErrorMessage), actualException.getMessage());
        }
    }

    @TableTest("""
        Scenario                          | Input             | Captured Value? | Captured Type?
        Unquoted                          | 'abc'             | abc             | java.lang.String
        Single quoted                     | "'abc'"           | abc             | java.lang.String
        Double quoted                     | '"abc"'           | abc             | java.lang.String
        With spaces                       | 'abc def'         | abc def         | java.lang.String
        Unquoted is trimmed               | ' a b c '         | 'a b c'         | java.lang.String
        Single quoted is not trimmed      | "' a b c '"       | ' a b c '       | java.lang.String
        Double quoted is not trimmed      | '" a b c "'       | ' a b c '       | java.lang.String
        Blank is null                     | ' '               |                 |
        Empty single quoted               | "''"              | ''              | java.lang.String
        Empty double quoted               | '""'              | ''              | java.lang.String
        Unmatched single quote            | "'"               | "'"             | java.lang.String
        Unmatched double quote            | '"'               | '"'             | java.lang.String
        Single quoted pipe                | "'|'"             | '|'             | java.lang.String
        Double quoted pipe                | '"|"'             | '|'             | java.lang.String
        Single quoted opening bracket     | "'['"             | '['             | java.lang.String
        Double quoted opening bracket     | '"["'             | '['             | java.lang.String
        Single quoted opening brace       | "'{'"             | '{'             | java.lang.String
        Double quoted opening brace       | '"{"'             | '{'             | java.lang.String
        Integer                           | '1'               | 1               | java.lang.String
        Decimal                           | '3.14'            | 3.14            | java.lang.String
        Number with underscores           | '1_000_000'       | 1_000_000       | java.lang.String
        List with unquoted string         | '[a]'             | [a]             | java.util.List
        List with double quoted string    | '["a"]'           | [a]             | java.util.List
        List with single quoted string    | "['a']"           | [a]             | java.util.List
        Set with unquoted string          | '{a}'             | {a}             | java.util.Set
        Set with double quoted string     | '{"a"}'           | {a}             | java.util.Set
        Set with single quoted string     | "{'a'}"           | {a}             | java.util.Set
        Map with unquoted string          | '[a: a]'          | [a: a]          | java.util.Map
        Map with double quoted string     | '[a: "a"]'        | [a: a]          | java.util.Map
        Map with single quoted string     | "[a: 'a']"        | [a: a]          | java.util.Map
        Nested with double quoted strings | '[double: ["a"]]' | [double: [a]]   | java.util.Map
        Nested with single quoted strings | "[single: ['a']]" | [single: [a]]   | java.util.Map
        """)
    void shouldCaptureStringsDiscardingQuotes(String input, Object expectedValue, Class expectedType) {
        Object actualValue = TableParser.parse("Scenario | Input\nString value | " + input).row(0).value(1);
        if (expectedType != null) assertInstanceOf(expectedType, actualValue);
        assertEquals(expectedValue, actualValue);
    }

    @TableTest("""
        Scenario                          | Input             | Captured?         | Captured Type?
        Unquoted                          | 'abc'             | abc               | java.lang.String
        Single quoted                     | "'abc'"           | "'abc'"           | java.lang.String
        Double quoted                     | '"abc"'           | '"abc"'           | java.lang.String
        With spaces                       | 'abc def'         | abc def           | java.lang.String
        Unquoted is trimmed               | ' a b c '         | 'a b c'           | java.lang.String
        Single quoted is not trimmed      | "' a b c '"       | "' a b c '"       | java.lang.String
        Double quoted is not trimmed      | '" a b c "'       | '" a b c "'       | java.lang.String
        Blank is null                     | ' '               |                   |
        Empty single quoted               | "''"              | "''"              | java.lang.String
        Empty double quoted               | '""'              | '""'              | java.lang.String
        Unmatched single quote            | "'"               | "'"               | java.lang.String
        Unmatched double quote            | '"'               | '"'               | java.lang.String
        Single quoted pipe                | "'|'"             | "'|'"             | java.lang.String
        Double quoted pipe                | '"|"'             | '"|"'             | java.lang.String
        Single quoted opening bracket     | "'['"             | "'['"             | java.lang.String
        Double quoted opening bracket     | '"["'             | '"["'             | java.lang.String
        Single quoted opening brace       | "'{'"             | "'{'"             | java.lang.String
        Double quoted opening brace       | '"{"'             | '"{"'             | java.lang.String
        Integer                           | '1'               | 1                 | java.lang.String
        Decimal                           | '3.14'            | 3.14              | java.lang.String
        Number with underscores           | '1_000_000'       | 1_000_000         | java.lang.String
        List with unquoted string         | '[a]'             | [a]               | java.util.List
        List with double quoted string    | '["a"]'           | ['"a"']           | java.util.List
        List with single quoted string    | "['a']"           | ["'a'"]           | java.util.List
        Set with unquoted string          | '{a}'             | {a}               | java.util.Set
        Set with double quoted string     | '{"a"}'           | {'"a"'}           | java.util.Set
        Set with single quoted string     | "{'a'}"           | {"'a'"}           | java.util.Set
        Map with unquoted string          | '[a: a]'          | [a: a]            | java.util.Map
        Map with double quoted string     | '[a: "a"]'        | [a: '"a"']        | java.util.Map
        Map with single quoted string     | "[a: 'a']"        | [a: "'a'"]        | java.util.Map
        Nested with double quoted strings | '[double: ["a"]]' | [double: ['"a"']] | java.util.Map
        Nested with single quoted strings | "[single: ['a']]" | [single: ["'a'"]] | java.util.Map
        """)
    void shouldCaptureStringsKeepingQuotes(String input, Object expectedValue, Class expectedType) {
        Object actualValue = TableParser.parse("Scenario | Input\nString value | " + input, true).row(0).value(1);
        if (expectedType != null) assertInstanceOf(expectedType, actualValue);
        assertEquals(expectedValue, actualValue);
    }

    @Test
    void shouldPreserveQuotesForHeaders() {
        Table table = TableParser.parse("\"Scenario\" | 'Other'", true);
        assertEquals("\"Scenario\"", table.header(0));
        assertEquals("'Other'", table.header(1));
    }

    @Test
    void cannotEscapeNewLines() {
        // Double quoted newline
        assertNotEquals("\n", TableParser.parse("Newline | Other\n\"\n\" | abc").row(0).value(0));

        // Single quoted newline
        assertNotEquals("\n", TableParser.parse("Newline | Other\n'\n' | abc").row(0).value(0));
    }

    @Test
    void shouldThrowExceptionOnParsingErrors() {
        // language=TableTest
        String input = """
            Scenario         | Purchase time       | Past purchases        | Count previous 30 days?
            Purchase too old | 2025-09-30T23:59:59 | [2025-08-01T00:00:00] | 0
            """;

        TableTestParseException exception = assertThrows(TableTestParseException.class, () -> TableParser.parse(input));
        assertTrue(exception.getMessage().startsWith("Failed to parse `[2025-08-01T00:00:00] | 0` in row `Purchase too old"), exception.getMessage());
    }
}
