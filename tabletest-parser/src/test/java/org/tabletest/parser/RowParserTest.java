package org.tabletest.parser;

import io.github.nchaugen.tabletest.junit.TableTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tabletest.parser.RowParser.mapValue;
import static org.tabletest.parser.RowParser.parse;
import static org.tabletest.parser.RowParser.row;
import static org.tabletest.parser.StringValue.doubleQuoted;
import static org.tabletest.parser.StringValue.singleQuoted;
import static org.tabletest.parser.StringValue.unquoted;

class RowParserTest {

    @Test
    void shouldCaptureRow() {
        // Test basic row
        assertEquals(
            List.of(unquoted("a"), unquoted("b"), unquoted("c")),
            row().parse("a | b | c").captures()
        );

        // Test row with different value types
        assertEquals(
            List.of(List.of(), Map.of(), unquoted("s t r")),
            row().parse("[] | [:] | s t r").captures()
        );

        // Test row with numbers
        assertEquals(
            List.of(unquoted("4"), unquoted("5.5"), unquoted("6_000")),
            row().parse("4 | 5.5 | 6_000").captures()
        );

        // Test row with quoted values
        assertEquals(
            List.of(unquoted("1"), doubleQuoted("2"), singleQuoted("3")),
            row().parse("1 | \"2\" | '3'").captures()
        );

        // Test row with extra whitespace
        assertEquals(
            List.of(unquoted("a"), unquoted("b"), unquoted("c")),
            row().parse("  a  |  b  |  c  ").captures()
        );

        // Test complex row
        assertEquals(
            List.of(
                Map.of(unquoted("name"), unquoted("John"), unquoted("age"), unquoted("30")),
                List.of(unquoted("a"), unquoted("b"), unquoted("c")),
                unquoted("simple text")
            ),
            row().parse("[name: John, age: 30] | [a, b, c] | simple text").captures()
        );
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

        Map<Object, Object> map = (Map<Object, Object>) captured;
        assertEquals(2, map.size());

        StringValue keyA = unquoted("a");
        StringValue keyB = unquoted("b");
        assertTrue(map.containsKey(keyA));
        assertTrue(map.containsKey(keyB));

        // Verify nested structures
        assertInstanceOf(Map.class, map.get(keyA));
        assertInstanceOf(List.class, map.get(keyB));
    }

    @TableTest("""
        Input
        'a | b | c'
        "[a, b] | [c: d] | 'text'"
        '  spaced  |  values  '
        '// This is a comment'
        '1 | 2 | 3'
        '"Quoted | Value" | [a, b]'
        """)
    void shouldParseVariousRowFormats(String input) {
        assertTrue(parse(input).isSuccess());
    }

    @Test
    void shouldHandleEdgeCases() {
        // Empty values
        assertEquals(Arrays.asList(null, null, null), row().parse("| |").captures());

        // Trailing pipe
        assertEquals(Arrays.asList(unquoted("a"), unquoted("b"), null), row().parse("a | b |").captures());

        // Leading pipe
        assertEquals(Arrays.asList(null, unquoted("a"), unquoted("b")), row().parse("| a | b").captures());

        // Only pipes
        assertEquals(Arrays.asList(null, null, null, null), row().parse("|||").captures());

        // Quoted empty string
        assertEquals(List.of(doubleQuoted(""), singleQuoted("")), row().parse("\"\" | ''").captures());
    }
}
