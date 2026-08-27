package org.tabletest.parser;

import org.tabletest.junit.Description;
import org.tabletest.junit.Scenario;
import org.tabletest.junit.TableTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Values")
@Description("""
        These rules cover one cell value: strings, quoting, the three collection forms, and
        the errors a malformed value raises. The three collection forms are a list, a set,
        and a map.
        """)
public class ValueGrammarTest {

    @DisplayName("Reads a value in square brackets as a list")
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

    @DisplayName("Refuses a value that opens with [ and is not a list")
    @Description("""
            A value that does not open a list captures as a plain string. Quoted brackets, a
            stray closing bracket, and a letter before the bracket are all such values.
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

    @DisplayName("Collects curly braces into a set, each value once")
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

    @DisplayName("Refuses a value that opens with { and is not a set")
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

    @DisplayName("Reads bracketed key: value pairs as a map")
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

    @DisplayName("Refuses a malformed map or a duplicate key")
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

    @DisplayName("Captures a value with its quotes discarded or kept")
    @Description("""
            The parser trims an unquoted value and keeps the whitespace of a quoted one. Either
            quote style protects a pipe, a bracket, and a brace. A blank cell captures null, and
            its Captured Type is blank because nothing was captured.

            The two expectation columns are the same parse in its two modes. Discarding is what a
            test method sees. Keeping is for a tool that rewrites a table, the formatter among
            them, so the original quoting survives the round trip. Where the two columns agree,
            the value carried no delimiting quotes to discard.

            Nothing here converts a number: 1 and 3.14 capture as text, because a parameter type
            is what decides that and the parser never sees one.
            """)
    @TableTest("""
        Scenario                          | Input             | Discarding quotes? | Keeping quotes?   | Captured Type?
        Unquoted                          | 'abc'             | abc                | abc               | java.lang.String
        Single quoted                     | "'abc'"           | abc                | "'abc'"           | java.lang.String
        Double quoted                     | '"abc"'           | abc                | '"abc"'           | java.lang.String
        With spaces                       | 'abc def'         | abc def            | abc def           | java.lang.String
        Unquoted is trimmed               | ' a b c '         | 'a b c'            | 'a b c'           | java.lang.String
        Single quoted is not trimmed      | "' a b c '"       | ' a b c '          | "' a b c '"       | java.lang.String
        Double quoted is not trimmed      | '" a b c "'       | ' a b c '          | '" a b c "'       | java.lang.String
        Blank is null                     | ' '               |                    |                   |
        Empty single quoted               | "''"              | ''                 | "''"              | java.lang.String
        Empty double quoted               | '""'              | ''                 | '""'              | java.lang.String
        Unmatched single quote            | "'"               | "'"                | "'"               | java.lang.String
        Unmatched double quote            | '"'               | '"'                | '"'               | java.lang.String
        Unterminated quote is literal     | "'abc"            | "'abc"             | "'abc"            | java.lang.String
        Quotes inside unquoted value      | a'b"c             | a'b"c              | a'b"c             | java.lang.String
        Single quoted pipe                | "'|'"             | '|'                | "'|'"             | java.lang.String
        Double quoted pipe                | '"|"'             | '|'                | '"|"'             | java.lang.String
        Single quoted opening bracket     | "'['"             | '['                | "'['"             | java.lang.String
        Double quoted opening bracket     | '"["'             | '['                | '"["'             | java.lang.String
        Single quoted opening brace       | "'{'"             | '{'                | "'{'"             | java.lang.String
        Double quoted opening brace       | '"{"'             | '{'                | '"{"'             | java.lang.String
        Integer                           | '1'               | 1                  | 1                 | java.lang.String
        Decimal                           | '3.14'            | 3.14               | 3.14              | java.lang.String
        Number with underscores           | '1_000_000'       | 1_000_000          | 1_000_000         | java.lang.String
        List with unquoted string         | '[a]'             | [a]                | [a]               | java.util.List
        List with double quoted string    | '["a"]'           | [a]                | ['"a"']           | java.util.List
        List with single quoted string    | "['a']"           | [a]                | ["'a'"]           | java.util.List
        Set with unquoted string          | '{a}'             | {a}                | {a}               | java.util.Set
        Set with double quoted string     | '{"a"}'           | {a}                | {'"a"'}           | java.util.Set
        Set with single quoted string     | "{'a'}"           | {a}                | {"'a'"}           | java.util.Set
        Map with unquoted string          | '[a: a]'          | [a: a]             | [a: a]            | java.util.Map
        Map with double quoted string     | '[a: "a"]'        | [a: a]             | [a: '"a"']        | java.util.Map
        Map with single quoted string     | "[a: 'a']"        | [a: a]             | [a: "'a'"]        | java.util.Map
        Nested with double quoted strings | '[double: ["a"]]' | [double: [a]]      | [double: ['"a"']] | java.util.Map
        Nested with single quoted strings | "[single: ['a']]" | [single: [a]]      | [single: ["'a'"]] | java.util.Map
        Map with double quoted key        | '["k": v]'        | [k: v]             | ['"k"': v]        | java.util.Map
        Map with single quoted key        | "['k': v]"        | [k: v]             | ["'k'": v]        | java.util.Map
        """)
    void shouldCaptureValuesWithQuotesDiscardedOrKept(
        String input,
        Object discardingQuotes,
        Object keepingQuotes,
        Class<?> capturedType
    ) {
        Object discarded = capturedFrom(input, false);
        Object kept = capturedFrom(input, true);

        assertEquals(discardingQuotes, discarded);
        assertEquals(keepingQuotes, kept);
        assertTypeOf(capturedType, discarded);
        assertTypeOf(capturedType, kept);
    }

    /** The value the parser captures from this cell text, in the mode asked for. */
    private static Object capturedFrom(String input, boolean keepQuotes) {
        return TableParser.parse("Scenario | Input\nString value | " + input, keepQuotes).row(0).value(1);
    }

    /** Asserts the captured type, where a blank type column means nothing was captured. */
    private static void assertTypeOf(Class<?> expectedType, Object captured) {
        assertEquals(expectedType == null, captured == null, "type column disagrees with the value");
        Optional.ofNullable(expectedType).ifPresent(type -> assertInstanceOf(type, captured));
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

    @DisplayName("Names the row when a stray quote fails the parse")
    @Description("""
            A quote that opens nothing fails like any unbalanced delimiter.

            Every parse error names the row it happened in, and quotes that row as written, so a
            table of many rows says which one to look at. The unbalanced bracket and brace have
            their own rules above.
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
