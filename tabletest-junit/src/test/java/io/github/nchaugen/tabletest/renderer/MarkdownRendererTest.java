package io.github.nchaugen.tabletest.renderer;

import io.github.nchaugen.tabletest.parser.TableParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import static io.github.nchaugen.tabletest.renderer.ColumnRoles.NO_ROLES;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MarkdownRendererTest {

    private final ExtensionContext context = new StubExtensionContext();
    private final MarkdownRenderer renderer = new MarkdownRenderer();

    @Test
    void shouldRenderBasicTable() {
        assertEquals(
            """
                ## Display Name
                
                | a | b | c |
                | --- | --- | --- |
                | 1 | 2 | 3 |
                """,
            renderer.render(
                TableParser.parse("""
                a | b | c
                1 | 2 | 3
                """),
                NO_ROLES,
                context
            )
        );
    }

    @Test
    void shouldRenderNull() {
        assertEquals(
            """
                ## Display Name
                
                | a | b | c |
                | --- | --- | --- |
                | 1 |  | 3 |
                """,
            renderer.render(
                TableParser.parse("""
                a | b | c
                1 |   | 3
                """),
                NO_ROLES,
                context
            )
        );
    }

    @Test
    void shouldRenderTableWithEscapedPipe() {
        assertEquals(
            """
                ## Display Name
                
                | a | b | a\\|b |
                | --- | --- | --- |
                | \\| | \\| | Text with \\| character |
                """,
            renderer.render(
                TableParser.parse("""
                a   | b   | 'a|b'
                "|" | '|' | "Text with | character"
                """),
                NO_ROLES,
                context
            )
        );
    }

    @Test
    void shouldRenderTableWithList() {
        assertEquals(
            """
                ## Display Name
                
                | a | b | c |
                | --- | --- | --- |
                | [] | [1, 2, 3] | [\\|, \\|] |
                """,
            renderer.render(
                TableParser.parse("""
                a  | b         | c
                [] | [1,2,3] | ['|', "|"]
                """),
                NO_ROLES,
                context
            )
        );
    }

    @Test
    void shouldRenderTableWithSet() {
        assertEquals(
            """
                ## Display Name
                
                | a | b | c |
                | --- | --- | --- |
                | {} | {1, 2, 3} | {\\|\\|} |
                """,
            renderer.render(
                TableParser.parse("""
                a  | b   | c
                {} | {1,2,3} | {"||"}
                """),
                NO_ROLES,
                context
            )
        );
    }

    @Test
    void shouldRenderTableWithMap() {
        assertEquals(
            """
                ## Display Name
                
                | a | b | c |
                | --- | --- | --- |
                | [:] | [a: 1, b: 2, c: 3] | [b: \\|\\|] |
                """,
            renderer.render(
                TableParser.parse("""
                a   | b             | c
                [:] | [a:1,b:2,c:3] | [b: "||"]
                """),
                NO_ROLES,
                context
            )
        );
    }

    @Test
    void shouldRenderTableWithNestedList() {
        assertEquals(
            """
                ## Display Name
                
                | a | b | c |
                | --- | --- | --- |
                | [{}] | [[1], [2, 3]] | [[a: \\|], [b: [\\|]]] |
                """,
            renderer.render(
                TableParser.parse("""
                a    | b           | c
                [{}] | [[1],[2,3]] | [[a: '|'], [b: ["|"]]]
                """),
                NO_ROLES,
                context
            )
        );
    }
}
