package io.github.nchaugen.tabletest.parser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static io.github.nchaugen.tabletest.parser.CaptureParser.capture;
import static io.github.nchaugen.tabletest.parser.CombinationParser.atLeast;
import static io.github.nchaugen.tabletest.parser.CombinationParser.sequence;
import static io.github.nchaugen.tabletest.parser.CombinationParser.zeroOrMore;
import static io.github.nchaugen.tabletest.parser.ParseResult.failure;
import static io.github.nchaugen.tabletest.parser.RowParser.cell;
import static io.github.nchaugen.tabletest.parser.RowParser.listValue;
import static io.github.nchaugen.tabletest.parser.RowParser.mapValue;
import static io.github.nchaugen.tabletest.parser.RowParser.parse;
import static io.github.nchaugen.tabletest.parser.RowParser.row;
import static io.github.nchaugen.tabletest.parser.RowParser.setValue;
import static io.github.nchaugen.tabletest.parser.RowParser.singleValue;
import static io.github.nchaugen.tabletest.parser.StringParser.character;
import static io.github.nchaugen.tabletest.parser.StringParser.characterExcept;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RowParserTest {

    @Test
    void shouldParseBasicInputs() {
        // Test basic row
        assertTrue(parse("a | b | c").isSuccess());

        // Test empty input
        assertTrue(parse("").isSuccess());

        // Test whitespace-only input
        assertTrue(parse("   ").isSuccess());

        // Test comment
        assertTrue(parse("// this is a comment").isSuccess());
        assertFalse(parse("// this is a comment").captures().stream().findAny().isPresent());

        // Test valid row after whitespace
        assertTrue(parse("   a | b | c").isSuccess());
    }

    @Test
    void shouldHandleComments() {
        // Basic comment
        assertTrue(parse("// Comment").isSuccess());
        assertEquals(0, parse("// Comment").captures().size());

        // Comment with special characters
        assertTrue(parse("// This | is [a] comment with: special chars").isSuccess());
        assertEquals(0, parse("// This | is [a] comment with: special chars").captures().size());

        // Comment with leading whitespace
        assertTrue(parse("   // Comment").isSuccess());
        assertEquals(0, parse("   // Comment").captures().size());

        // Double slash within quoted value shouldn't be a comment
        assertTrue(parse("\"//\" | Not a comment").isSuccess());
        assertEquals(2, parse("\"//\" | Not a comment").captures().size());
        assertEquals(
            List.of("//", "Not a comment"),
            parse("\"//\" | Not a comment").captures()
        );
    }

    @Test
    void shouldCaptureListValueElement() {
        // Test empty list
        assertEquals(List.of(List.of()), listValue().parse("[]").captures());

        // Test simple list
        assertEquals(
            List.of(List.of("a", "b", "c")),
            listValue().parse("[a, b, c]").captures()
        );

        // Test nested lists
        assertEquals(
            List.of(List.of(List.of("a"), List.of("b"), List.of("c"))),
            listValue().parse("[[a], [b], [c]]").captures()
        );

        // Test mixed content lists
        assertEquals(
            List.of(List.of(List.of(), "a")),
            listValue().parse("[[],a]").captures()
        );

        // Test list with map
        assertEquals(
            List.of(List.of(Map.of("a", "b"), List.of("c"))),
            listValue().parse("[[a:b],[c]]").captures()
        );

        // Test list with quoted values
        assertEquals(
            List.of(List.of("a, b", "c")),
            listValue().parse("[\"a, b\", c]").captures()
        );

        // Test with trailing character - edge case
        assertEquals(List.of(List.of()), listValue().parse("[]]").captures());

        // Test invalid lists
        List.of("", "[", "]", "[[]").forEach(
            input -> assertFalse(listValue().parse(input).isSuccess())
        );
    }

    @Test
    void shouldCaptureSetValueElement() {
        // Test empty set
        assertEquals(List.of(Set.of()), setValue().parse("{}").captures());

        // Test simple set
        assertEquals(
            List.of(Set.of("a", "b", "c")),
            setValue().parse("{a, b, c, b, a}").captures()
        );

        // Test set of lists
        assertEquals(
            List.of(Set.of(List.of("a"), List.of("b"), List.of("c"))),
            setValue().parse("{[a], [b], [c], [b], [a]}").captures()
        );

        // Test set of sets
        assertEquals(
            List.of(Set.of(Set.of("a"), Set.of("b"), Set.of("c"))),
            setValue().parse("{{a}, {b}, {c}, {b}, {a}}").captures()
        );

        // Test set of maps
        assertEquals(
            List.of(Set.of(Map.of("a", "b"), List.of("c"))),
            setValue().parse("{[a:b], [c], [c], [a:b]}").captures()
        );

        // Test mixed content sets
        assertEquals(
            List.of(Set.of(List.of(), "a")),
            setValue().parse("{[], a, a, a, a, [], []}").captures()
        );

        // Test set with quoted values
        assertEquals(
            List.of(Set.of("a, b", "c")),
            setValue().parse("{\"a, b\", c, 'a, b'}").captures()
        );

        // Test with trailing character - edge case
        assertEquals(List.of(Set.of()), setValue().parse("{}}").captures());

        // Test invalid sets
        List.of("", "{", "}", "{{}").forEach(
            input -> assertFalse(setValue().parse(input).isSuccess())
        );
    }

    @Test
    void shouldCaptureMapValueElement() {
        // Test empty map
        assertEquals(List.of(Map.of()), mapValue().parse("[:]").captures());

        // Test basic map with string values
        assertEquals(
            List.of(Map.of("a", "1", "b", "2", "c", "3")),
            mapValue().parse("[a: 1, b: 2, c: 3]").captures()
        );

        // Test map with list values
        assertEquals(
            List.of(Map.of("a", List.of("1"), "b", List.of("2"))),
            mapValue().parse("[a: [1], b: [2]]").captures()
        );

        // Test map with nested maps
        assertEquals(
            List.of(Map.of("a", Map.of("x", "1"), "b", Map.of("y", "2"))),
            mapValue().parse("[a: [x:1], b: [y:2]]").captures()
        );

        // Test map with trailing character
        assertEquals(List.of(Map.of()), mapValue().parse("[:]]").captures());

        // Test invalid maps
        List.of("", "[:", ":]", "[[:]").forEach(
            input -> assertFalse(mapValue().parse(input).isSuccess())
        );

        // Test maps with whitespace
        assertEquals(
            List.of(Map.of("key", "value")),
            mapValue().parse("[  key  :  value  ]").captures()
        );
    }

    @Test
    void shouldCaptureEmptyUnquotedSingleValueAsNull() {
        assertNull(singleValue().parse("").captures().getFirst());
    }

    @Test
    void shouldCaptureSingleValueElement() {
        Map<String, String> testCases1 = Map.of(
            "abc", "abc",                // Simple text
            "a b c", "a b c",            // Text with spaces
            "a, b, c", "a, b, c",        // Text with commas
            "\"abc\"", "abc",            // Double-quoted text
            "\"\"", "",                  // Empty double quotes
            "\"", "\""                       // Unmatched double quote
        );

        Map<String, String> testCases2 = Map.of(
            "\"|\"", "|",             // Quoted pipe character
            "'abc'", "abc",               // Single-quoted text
            "''", "",                     // Empty single quotes
            "'", "'",                     // Unmatched single quote
            "'|'", "|"                        // Single-quoted pipe
        );

        Stream.of(testCases1, testCases2)
            .flatMap(it -> it.entrySet().stream())
            .forEach(entry ->
                         assertEquals(
                             List.of(entry.getValue()),
                             singleValue().parse(entry.getKey()).captures(),
                             "Failed for input: " + entry.getKey()
                         )
            );
    }

    @Test
    void shouldCaptureCell() {
        Map<String, Object> testCases = Map.of(
            "0", "0",                         // Integer
            "1.23", "1.23",                   // Decimal
            "100_000", "100_000",             // Number with underscores
            "a b c", "a b c",                 // Text with spaces
            "\"abc\"", "abc",                 // Double-quoted text
            "'abc'", "abc",                   // Single-quoted text
            "[]", List.of(),                  // Empty list
            "[a, 'b', \"c\"]", List.of("a", "b", "c"), // List with mixed quotes
            "[:]", Map.of(),                  // Empty map
            "[a: 1, b: '2', c: \"3\"]", Map.of("a", "1", "b", "2", "c", "3") // Map with quoted values
        );

        testCases.forEach((input, expected) ->
                              assertEquals(
                                  List.of(expected), cell().parse(input).captures(),
                                  "Failed for input: " + input
                              )
        );

        // Test whitespace handling
        assertEquals(List.of("abc"), cell().parse("  abc  ").captures());
        assertEquals(List.of(List.of("a", "b")), cell().parse("  [a, b]  ").captures());
    }

    @Test
    void shouldCaptureRow() {
        // Test basic row
        assertEquals(
            List.of("a", "b", "c"),
            trimStringCaptures(row().parse("a | b | c"))
        );

        // Test row with different value types
        assertEquals(
            List.of(List.of(), Map.of(), "s t r"),
            trimStringCaptures(row().parse("[] | [:] | s t r"))
        );

        // Test row with numbers
        assertEquals(
            List.of("4", "5.5", "6_000"),
            trimStringCaptures(row().parse("4 | 5.5 | 6_000"))
        );

        // Test row with quoted values
        assertEquals(
            List.of("1", "2", "3"),
            trimStringCaptures(row().parse("1 | \"2\" | '3'"))
        );

        // Test row with extra whitespace
        assertEquals(
            List.of("a", "b", "c"),
            trimStringCaptures(row().parse("  a  |  b  |  c  "))
        );

        // Test complex row
        assertEquals(
            List.of(
                Map.of("name", "John", "age", "30"),
                List.of("a", "b", "c"),
                "simple text"
            ),
            trimStringCaptures(row().parse("[name: John, age: 30] | [a, b, c] | simple text"))
        );
    }

    private List<Object> trimStringCaptures(ParseResult result) {
        return result.captures().stream()
            .map(it -> (it instanceof String s) ? s.trim() : it)
            .toList();
    }

    @Test
    void shouldHandleNestedStructures() {
        // Test deeply nested structures
        String complexInput = "[a: [x: [1, 2], y: [m: 'n']], b: ['c']]";
        ParseResult result = mapValue().parse(complexInput);

        assertTrue(result.isSuccess());
        assertEquals(1, result.captures().size());

        Object captured = result.captures().getFirst();
        assertInstanceOf(Map.class, captured);

        Map<String, Object> map = (Map<String, Object>) captured;
        assertEquals(2, map.size());
        assertTrue(map.containsKey("a"));
        assertTrue(map.containsKey("b"));

        // Verify nested structures
        assertInstanceOf(Map.class, map.get("a"));
        assertInstanceOf(List.class, map.get("b"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "a | b | c",
        "[a, b] | [c: d] | 'text'",
        "  spaced  |  values  ",
        "// This is a comment",
        "1 | 2 | 3",
        "\"Quoted | Value\" | [a, b]"
    })
    void shouldParseVariousRowFormats(String input) {
        assertTrue(parse(input).isSuccess());
    }

    @Test
    void shouldMatchTableRow() {
        Parser tableRow = sequence(
            capture(atLeast(1, characterExcept('|'))),
            zeroOrMore(
                sequence(
                    character('|'),
                    capture(zeroOrMore(characterExcept('|')))
                )
            )
        );

        // Test empty input
        assertEquals(failure(), tableRow.parse(""));

        // Test single cell
        assertEquals(List.of("a"), tableRow.parse("a").captures());

        // Test two cells
        assertEquals(List.of("a ", ""), tableRow.parse("a |").captures());
        assertEquals(List.of("a ", " b"), tableRow.parse("a | b").captures());

        // Test three cells
        assertEquals(List.of("a ", " ", " c"), tableRow.parse("a | | c").captures());
    }

    @Test
    void shouldHandleEdgeCases() {
        // Empty cells
        assertEquals(
            Arrays.asList(null, null, null),
            trimStringCaptures(row().parse("| |"))
        );

        // Trailing pipe
        assertEquals(
            Arrays.asList("a", "b", null),
            trimStringCaptures(row().parse("a | b |"))
        );

        // Leading pipe
        assertEquals(
            Arrays.asList(null, "a", "b"),
            trimStringCaptures(row().parse("| a | b"))
        );

        // Only pipes
        assertEquals(
            Arrays.asList(null, null, null, null),
            trimStringCaptures(row().parse("|||"))
        );

        // Quoted empty string
        assertEquals(
            List.of("", ""),
            trimStringCaptures(row().parse("\"\" | ''"))
        );
    }
}
