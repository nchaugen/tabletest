package io.github.nchaugen.tabletest.renderer;

import io.github.nchaugen.tabletest.parser.TableParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MarkdownRendererTest {

    private final MarkdownRenderer renderer = new MarkdownRenderer();

    @Test
    void shouldRenderBasicTable() {
        assertEquals(
            """
                | a | b | c |
                | --- | --- | --- |
                | 1 | 2 | 3 |
                """,
            renderer.render(TableParser.parse("""
                a | b | c
                1 | 2 | 3
                """))
        );
    }

    @Test
    void shouldRenderNull() {
        assertEquals(
            """
                | a | b | c |
                | --- | --- | --- |
                | 1 |  | 3 |
                """,
            renderer.render(TableParser.parse("""
                a | b | c
                1 |   | 3
                """))
        );
    }

    @Test
    void shouldRenderTableWithEscapedPipe() {
        assertEquals(
            """
                | a | b | a\\|b |
                | --- | --- | --- |
                | \\| | \\| | Text with \\| character |
                """,
            renderer.render(TableParser.parse("""
                a   | b   | 'a|b'
                "|" | '|' | "Text with | character"
                """))
        );
    }

    @Test
    void shouldRenderTableWithList() {
        assertEquals(
            """
                | a | b | c |
                | --- | --- | --- |
                | [] | [1, 2, 3] | [\\|, \\|] |
                """,
            renderer.render(TableParser.parse("""
                a  | b         | c
                [] | [1,2,3] | ['|', "|"]
                """))
        );
    }

    @Test
    void shouldRenderTableWithSet() {
        assertEquals(
            """
                | a | b | c |
                | --- | --- | --- |
                | {} | {1, 2, 3} | {\\|\\|} |
                """,
            renderer.render(TableParser.parse("""
                a  | b   | c
                {} | {1,2,3} | {"||"}
                """))
        );
    }

    @Test
    void shouldRenderTableWithMap() {
        assertEquals(
            """
                | a | b | c |
                | --- | --- | --- |
                | [:] | [a: 1, b: 2, c: 3] | [b: \\|\\|] |
                """,
            renderer.render(TableParser.parse("""
                a   | b             | c
                [:] | [a:1,b:2,c:3] | [b: "||"]
                """))
        );
    }

    @Test
    void shouldRenderTableWithNestedList() {
        assertEquals(
            """
                | a | b | c |
                | --- | --- | --- |
                | [{}] | [[1], [2, 3]] | [[a: \\|], [b: [\\|]]] |
                """,
            renderer.render(TableParser.parse("""
                a    | b           | c
                [{}] | [[1],[2,3]] | [[a: '|'], [b: ["|"]]]
                """))
        );
    }
}
