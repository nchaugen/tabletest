package org.tabletest.parser;

import org.tabletest.junit.Description;
import org.tabletest.junit.Scenario;
import org.tabletest.junit.TableTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Values")
@Description("""
        The grammar of a single cell value: strings and quoting, the three
        collection forms — lists, sets, and maps — and the parse errors a
        malformed value raises.
        """)
public class ValueGrammarTest {

    @DisplayName("Lists in [square brackets] hold values, nested collections, and quoted strings")
    @Description("A blank cell captures null; unquoted elements are trimmed.")
    @TableTest("""
        Scenario               | Input               | Captured?
        Null list              | ''                  |
        Empty list             | '[]'                | []
        Basic list             | '[a, b, c]'         | [a, b, c]
        Nested list            | '[[a], [b], [c]]'   | [[a], [b], [c]]
        Mixed content list     | '[[],a]'            | [[], a]
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

    @DisplayName("A value starting with [ must be a well-formed list")
    @Description("""
            Values that do not open a list — quoted brackets, a stray closing
            bracket, a letter before the bracket — capture as plain strings.
            """)
    @TableTest("""
        Scenario                      | Input    | Parsed type?     | Error message?
        Quoted brackets               | '"[]"'   | java.lang.String |
        Only closing bracket          | ']'      | java.lang.String |
        Unexpected leading character  | 'a[]'    | java.lang.String |
        Only opening bracket          | '['      |                  | Failed to parse `[`
        Closed by the wrong delimiter | '[a, b}' |                  | Failed to parse `[a, b}`
        Double opening bracket        | '[[]'    |                  | Failed to parse `[[]`
        Double closing bracket        | '[]]'    |                  | Failed to parse `]`
        Unexpected trailing character | '[]a'    |                  | Failed to parse `a`
        Missing element               | '[a,]'   |                  | Failed to parse `[a,]`
        """)
    void shouldHandleInvalidListSyntax(
        String input,
        Class<?> parsedType,
        String errorMessage
    ) {
        assertEquals(parsedType, parsedTypeOf(input));
        assertEquals(errorMessage, parseErrorFor(input));
    }

    @DisplayName("Sets in {curly braces} keep each distinct value once")
    @TableTest("""
        Scenario              | Input               | Captured?
        Null set              | ''                  |
        Empty set             | '{}'                | {}
        Basic set             | '{a, b, a, a, c}'   | {a, b, c}
        Nested set            | '{{a}, {b}, {b}}'   | {{a}, {b}}
        Mixed content set     | '{{},a,a,{}}'       | {{}, a}
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

    @DisplayName("A value starting with { must be a well-formed set")
    @Description("Values that do not open a set capture as plain strings.")
    @TableTest("""
        Scenario                      | Input    | Parsed type?     | Error message?
        Quoted braces                 | '"{}"'   | java.lang.String |
        Missing opening brace         | '}'      | java.lang.String |
        Unexpected leading character  | 'a{}'    | java.lang.String |
        Missing closing brace         | '{'      |                  | Failed to parse `{`
        Closed by the wrong delimiter | '{a, b]' |                  | Failed to parse `{a, b]`
        Double opening brace          | '{{}'    |                  | Failed to parse `{{}`
        Double closing brace          | '{}}'    |                  | Failed to parse `}`
        Unexpected trailing character | '{}a'    |                  | Failed to parse `a`
        Missing element               | '{a,}'   |                  | Failed to parse `{a,}`
        """)
    void shouldHandleInvalidSetSyntax(
        String input,
        Class<?> parsedType,
        String errorMessage
    ) {
        assertEquals(parsedType, parsedTypeOf(input));
        assertEquals(errorMessage, parseErrorFor(input));
    }

    @DisplayName("Maps in [key: value] form support nesting and quoted keys")
    @TableTest("""
        Scenario              | Input                          | Captured?
        Null map              | ''                             |
        Empty map             | '[:]'                          | [:]
        Basic map             | '[a:b, c:d]'                   | [a: b, c: d]
        Nested map            | '[A:[a:1], B:[b:2], C:[c:3]]'  | [A: [a: 1], B: [b: 2], C: [c: 3]]
        Mixed content map     | '[m:[:], s:a]'                 | [m: [:], s: a]
        Map with set          | '[s:{a,b}, t:{c}]'             | [s: {a, b}, t: {c}]
        Map with list         | '[l:[a,b], i:[c]]'             | [l: [a, b], i: [c]]
        Map with quoted value | '[q:"a,b", u:c]'               | [q: "a,b", u: c]
        Extra whitespace      | '  [   a   :  a   ]  '         | [a: a]
        Double quoted key     | '["key with spaces": value]'   | ["key with spaces": value]
        Single quoted key     | "['key:colon': value]"         | ['key:colon': value]
        Mixed quoted keys     | '["a,b": value, plain: other]' | ["a,b": value, plain: other]
        Empty quoted key      | '["": value]'                  | ["": value]
        """)
    void shouldCaptureMaps(String input, Map<String, Object> expectedCaptures) {
        assertEquals(
            expectedCaptures,
            TableParser.parse("Scenario | Input\nMap value | " + input).row(0).value(1)
        );
    }

    @DisplayName("A malformed map or a duplicate key fails parsing")
    @Description("Values that do not open a map capture as plain strings.")
    @TableTest("""
        Scenario                       | Input                          | Parsed type?     | Error message?
        Quoted empty map               | '"[:]"'                        | java.lang.String |
        Missing opening bracket        | ':]'                           | java.lang.String |
        Unexpected leading character   | 'a[:]'                         | java.lang.String |
        Missing closing bracket        | '[:'                           |                  | Failed to parse `[:`
        Closed by the wrong delimiter  | '[a: b}'                       |                  | Failed to parse `[a: b}`
        Double opening bracket         | '[[:]'                         |                  | Failed to parse `[[:]`
        Double closing bracket         | '[:]]'                         |                  | Failed to parse `]`
        Unexpected trailing character  | '[:]a'                         |                  | Failed to parse `a`
        Missing element                | '[a:b,]'                       |                  | Failed to parse `[a:b,]`
        Duplicate keys                 | '[a:b, a:c]'                   |                  | Duplicate key `a` in map `[a:b, a:c]`
        Duplicate quoted key           | '[a:b, "a":c]'                 |                  | 'Duplicate key `a` in map `[a:b, "a":c]`'
        Same key different value types | '[a:b, a:[b], a:{b}, a:[b:c]]' |                  | 'Duplicate key `a` in map `[a:b, a:[b], a:{b}, a:[b:c]]`'
        """)
    void shouldHandleInvalidMapSyntax(
        String input,
        Class<?> parsedType,
        String errorMessage
    ) {
        assertEquals(parsedType, parsedTypeOf(input));
        assertEquals(errorMessage, parseErrorFor(input));
    }

    /** The type the value captures as, or null when it does not parse. */
    private static Class<?> parsedTypeOf(String input) {
        try {
            return TableParser.parse("Scenario | Input\nInvalid | " + input)
                    .row(0)
                    .value(1)
                    .getClass();
        } catch (TableTestParseException e) {
            return null;
        }
    }

    /** The start of the message the value fails to parse with, or null when it parses. */
    private static String parseErrorFor(String input) {
        try {
            TableParser.parse("Scenario | Input\nInvalid | " + input);
            return null;
        } catch (TableTestParseException e) {
            return e.getMessage().split(" in row ")[0];
        }
    }

    @DisplayName("Quotes delimit values and are discarded from the captured value")
    @Description("""
            Unquoted values are trimmed; quoted values keep their whitespace.
            Either quote style protects pipes, brackets, and braces. A blank
            cell captures null.
            """)
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
        Unterminated quote is literal     | "'abc"            | "'abc"          | java.lang.String
        Quotes inside unquoted value      | a'b"c             | a'b"c           | java.lang.String
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
        Map with double quoted key        | '["k": v]'        | [k: v]          | java.util.Map
        Map with single quoted key        | "['k': v]"        | [k: v]          | java.util.Map
        """)
    void shouldCaptureStringsDiscardingQuotes(String input, Object expectedValue, Class expectedType) {
        Object actualValue = TableParser.parse("Scenario | Input\nString value | " + input).row(0).value(1);
        if (expectedType != null) assertInstanceOf(expectedType, actualValue);
        assertEquals(expectedValue, actualValue);
    }

    @DisplayName("In quote-preserving mode values capture with their quotes intact")
    @Description("""
            The mode used by tools that rewrite tables, such as the formatter,
            so the original quoting survives a round trip.
            """)
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
        Map with double quoted key        | '["k": v]'        | ['"k"': v]        | java.util.Map
        Map with single quoted key        | "['k': v]"        | ["'k'": v]        | java.util.Map
        """)
    void shouldCaptureStringsKeepingQuotes(String input, Object expectedValue, Class expectedType) {
        Object actualValue = TableParser.parse("Scenario | Input\nString value | " + input, true).row(0).value(1);
        if (expectedType != null) assertInstanceOf(expectedType, actualValue);
        assertEquals(expectedValue, actualValue);
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

    @DisplayName("A stray quote fails, and the error names the row it is in")
    @Description("""
            A quote that opens nothing is a parse failure like any unbalanced delimiter. Every
            parse error also names the row it happened in, quoting the row as written, so a
            table with many rows says which one to look at. The unbalanced-bracket cases
            themselves are the well-formed list and set rules above.
            """)
    @TableTest("""
        Scenario             | Input  | Error message?
        Triple single quotes | "'''"  | "Failed to parse `'` in row `Triple single quotes | '''`"
        Triple double quotes | '\"""' | 'Failed to parse `"` in row `Triple double quotes | \"""`'
        """)
    void shouldHandleInvalidQuotedStringSyntax(@Scenario String scenario, String input, String errorMessage) {
        String table = "Scenario | Input\n" + scenario + " | " + input;
        TableTestParseException actualException = assertThrows(
            TableTestParseException.class,
            () -> TableParser.parse(table)
        );
        assertEquals(errorMessage, actualException.getMessage());
    }

}
