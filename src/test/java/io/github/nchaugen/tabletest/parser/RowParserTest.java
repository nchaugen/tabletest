package io.github.nchaugen.tabletest.parser;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.nchaugen.tabletest.parser.CaptureParser.capture;
import static io.github.nchaugen.tabletest.parser.CombinationParser.atLeast;
import static io.github.nchaugen.tabletest.parser.CombinationParser.sequence;
import static io.github.nchaugen.tabletest.parser.ParseResult.failure;
import static io.github.nchaugen.tabletest.parser.StringParser.character;
import static io.github.nchaugen.tabletest.parser.StringParser.characterExceptNonEscaped;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static io.github.nchaugen.tabletest.parser.RowParser.cell;
import static io.github.nchaugen.tabletest.parser.RowParser.listValue;
import static io.github.nchaugen.tabletest.parser.RowParser.mapValue;

class RowParserTest {

    @Test
    void shouldCaptureListValueElement() {
        Map.of(
            "[]", List.of(),
            "[a, b, c]", List.of("a", "b", "c"),
            "[[a], [b], [c]]", List.of(List.of("a"), List.of("b"), List.of("c")),
            "[]]", List.of(),
            "[[],a]", List.of(List.of(), "a"),
            "[[a:b],[c]]", List.of(Map.of("a", "b"), List.of("c")),
            "[\"a, b\", c]", List.of("a, b", "c")
        ).forEach(
            (input, expected) -> assertEquals(List.of(expected), listValue().parse(input).captures())
        );

        List.of("", "[", "]", "[[]").forEach(
            input -> assertFalse(listValue().parse(input).isSuccess())
        );
    }

    @Test
    void shouldCaptureMapValueElement() {
        Map.of(
            "[:]", Map.of(),
            "[a: 1, b: 2, c: 3]", Map.of("a", "1", "b", "2", "c", "3"),
            "[a: [1], b: [2]]", Map.of("a", List.of("1"), "b", List.of("2")),
            "[a: [x:1], b: [y:2]]", Map.of("a", Map.of("x", "1"), "b", Map.of("y", "2")),
            "[:]]", Map.of()
        ).forEach(
            (input, expected) -> assertEquals(List.of(expected), mapValue().parse(input).captures())
        );

        List.of("", "[:", ":]", "[[:]").forEach(
            input -> assertFalse(mapValue().parse(input).isSuccess())
        );
    }

    @Test
    void shouldCaptureSingleValueElement() {
        Map.of(
            "", "",
            "abc", "abc",
            "a b c", "a b c",
            "a, b, c", "a, b, c",
            "\"abc\"", "abc",
            "\"\"", "",
            "\"", "\"",
            "\"|\"", "|"
        ).forEach(
            (input, expected) ->
                assertEquals(List.of(expected), RowParser.singleValue().parse(input).captures())
        );
    }

    @Test
    void shouldCaptureCell() {
        Map.of(
            "0", "0",
            "1.23", "1.23",
            "100_000", "100_000",
            "a b c", "a b c",
            "\"abc\"", "abc",
            "'abc'", "abc",
            "[]", List.of(),
            "[a, 'b', \"c\"]", List.of("a", "b", "c"),
            "[:]", Map.of(),
            "[a: 1, b: '2', c: \"3\"]", Map.of("a", "1", "b", "2", "c", "3")
        ).forEach(
            (input, expected) ->
                assertEquals(List.of(expected), cell().parse(input).captures())
        );
    }

    @Test
    void shouldCaptureRow() {
        Map.of(
            "a  | b     | c    ", List.of("a", "b", "c"),
            "[] | [:]   | s t r", List.of(List.of(), Map.of(), "s t r"),
            "4  | 5.5   | 6_000", List.of("4", "5.5", "6_000"),
            "1  | \"2\" | '3'", List.of("1", "2", "3")
        ).forEach(
            (input, expected) ->
                assertEquals(
                    expected,
                    RowParser.row().parse(input)
                        .captures().stream()
                        .map(it -> (it instanceof String s) ? s.trim() : it)
                        .toList()
                )
        );
    }

    @Test
    void shouldMatchTableRow() {
        Parser tableRow = sequence(
            capture(atLeast(1, characterExceptNonEscaped('|'))),
            atLeast(
                0, sequence(
                    character('|'),
                    capture(atLeast(0, characterExceptNonEscaped('|')))
                )
            )
        );

        assertEquals(failure(), tableRow.parse(""));
        assertEquals(List.of("a"), tableRow.parse("a").captures());
        assertEquals(List.of("a ", ""), tableRow.parse("a |").captures());
        assertEquals(List.of("a ", " b"), tableRow.parse("a | b").captures());
        assertEquals(List.of("a ", " ", " c"), tableRow.parse("a | | c").captures());
        assertEquals(List.of("a \\| b ", " c"), tableRow.parse("a \\| b | c").captures());
    }

}
