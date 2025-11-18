package io.github.nchaugen.tabletest.renderer;

import io.github.nchaugen.tabletest.parser.TableParser;
import org.junit.jupiter.api.Test;

import static io.github.nchaugen.tabletest.renderer.ColumnRoles.NO_ROLES;
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
            renderer.render(
                TableParser.parse("""
                a | b | c
                1 | 2 | 3
                """),
                NO_ROLES
            )
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
            renderer.render(
                TableParser.parse("""
                a | b | c
                1 |   | 3
                """),
                NO_ROLES
            )
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
            renderer.render(
                TableParser.parse("""
                a   | b   | 'a|b'
                "|" | '|' | "Text with | character"
                """),
                NO_ROLES
            )
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
            renderer.render(
                TableParser.parse("""
                a  | b         | c
                [] | [1,2,3] | ['|', "|"]
                """),
                NO_ROLES
            )
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
            renderer.render(
                TableParser.parse("""
                a  | b   | c
                {} | {1,2,3} | {"||"}
                """),
                NO_ROLES
            )
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
            renderer.render(
                TableParser.parse("""
                a   | b             | c
                [:] | [a:1,b:2,c:3] | [b: "||"]
                """),
                NO_ROLES
            )
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
            renderer.render(
                TableParser.parse("""
                a    | b           | c
                [{}] | [[1],[2,3]] | [[a: '|'], [b: ["|"]]]
                """),
                NO_ROLES
            )
        );
    }
}
