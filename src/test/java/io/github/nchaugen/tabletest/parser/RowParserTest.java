package io.github.nchaugen.tabletest.parser;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static io.github.nchaugen.tabletest.parser.RowParser.cell;
import static io.github.nchaugen.tabletest.parser.RowParser.list;
import static io.github.nchaugen.tabletest.parser.RowParser.map;

class RowParserTest {

    @Test
    void shouldCaptureListElement() {
        Map.of(
            "[]", List.of(),
            "[a, b, c]", List.of("a", "b", "c"),
            "[[a], [b], [c]]", List.of(List.of("a"), List.of("b"), List.of("c")),
            "[]]", List.of(),
            "[[],a]", List.of(List.of(), "a"),
            "[[a:b],[c]]", List.of(Map.of("a", "b"), List.of("c")),
            "[\"a, b\", c]", List.of("a, b", "c")
        ).forEach(
            (input, expected) -> assertEquals(List.of(expected), list().parse(input).captures())
        );

        List.of("", "[", "]", "[[]").forEach(
            input -> assertFalse(list().parse(input).isSuccess())
        );
    }

    @Test
    void shouldCaptureMapElement() {
        Map.of(
            "[:]", Map.of(),
            "[a: 1, b: 2, c: 3]", Map.of("a", "1", "b", "2", "c", "3"),
            "[a: [1], b: [2]]", Map.of("a", List.of("1"), "b", List.of("2")),
            "[a: [x:1], b: [y:2]]", Map.of("a", Map.of("x", "1"), "b", Map.of("y", "2")),
            "[:]]", Map.of()
        ).forEach(
            (input, expected) -> assertEquals(List.of(expected), map().parse(input).captures())
        );

        List.of("", "[:", ":]", "[[:]").forEach(
            input -> assertFalse(map().parse(input).isSuccess())
        );
    }

    @Test
    void shouldCaptureStringElement() {
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
                assertEquals(List.of(expected), RowParser.string().parse(input).captures())
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
            "[]", List.of(),
            "[a, b, c]", List.of("a", "b", "c"),
            "[:]", Map.of(),
            "[a: 1, b: 2, c: 3]", Map.of("a", "1", "b", "2", "c", "3")
        ).forEach(
            (input, expected) ->
                assertEquals(List.of(expected), cell().parse(input).captures())
        );
    }

    @Test
    void shouldCaptureRow() {
        Map.of(
            "a  | b   | c    ", List.of("a", "b", "c"),
            "[] | [:] | s t r", List.of(List.of(), Map.of(), "s t r"),
            "4  | 5.5 | 6_000", List.of("4", "5.5", "6_000")
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


}
