package io.github.nchaugen.tabletest.renderer;

import io.github.nchaugen.tabletest.parser.TableParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AsciidocRendererTest {

    private final AsciidocRenderer renderer = new AsciidocRenderer();

    @Test
    void shouldRenderBasicTable() {
        assertEquals(
            """
                [%header,cols="1,1,1"]
                |===
                |a
                |b
                |c
                
                a|1
                a|2
                a|3
                |===
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
                [%header,cols="1,1,1"]
                |===
                |a
                |b
                |c
                
                a|1
                a|
                a|3
                |===
                """,
            renderer.render(TableParser.parse("""
                a | b | c
                1 |   | 3
                """))
        );
    }

    @Test
    void shouldRenderEscapedPipe() {
        assertEquals(
            """
                [%header,cols="1,1,1"]
                |===
                |a
                |b
                |a\\|b
                
                a|\\|
                a|\\|
                a|Text with \\| character
                |===
                """,
            renderer.render(TableParser.parse("""
                a   | b   | 'a|b'
                "|" | '|' | "Text with | character"
                """))
        );
    }

    @Test
    void shouldRenderListAsOrderedList() {
        assertEquals(
            """
                [%header,cols="1,1,1"]
                |===
                |a
                |b
                |c
                
                a|
                a|
                . 1
                . 2
                . 3
                
                a|
                . \\|
                . \\|
                
                |===
                """,
            renderer.render(TableParser.parse("""
                a  | b         | c
                [] | [1,2,3] | ['|', "|"]
                """))
        );
    }

    @Test
    void shouldRenderNestedLists() {
        assertEquals(
            """
                [%header,cols="1"]
                |===
                |a
                
                a|
                . {empty}
                  .. 1
                  .. 2
                  .. 3
                . {empty}
                  .. a
                  .. b
                  .. c
                . {empty}
                  .. #
                  .. $
                  .. %
                
                |===
                """,
            renderer.render(TableParser.parse("""
                a
                [[1,2,3],[a,b,c],[#,$,%]]
                """))
        );
    }

    @Test
    void shouldRenderSetAsUnorderedList() {
        assertEquals(
            """
                [%header,cols="1,1,1"]
                |===
                |a
                |b
                |c
                
                a|
                a|
                * 1
                * 2
                * 3
                
                a|
                * \\|\\|
                
                |===
                """,
            renderer.render(TableParser.parse("""
                a  | b   | c
                {} | {1,2,3} | {"||"}
                """))
        );
    }

    @Test
    void shouldRenderNestedSets() {
        assertEquals(
            """
                [%header,cols="1"]
                |===
                |a
                
                a|
                * {empty}
                  ** 1
                  ** 2
                  ** 3
                * {empty}
                  ** a
                  ** b
                  ** c
                * {empty}
                  ** #
                  ** $
                  ** %
                
                |===
                """,
            renderer.render(TableParser.parse("""
                a
                {{1,2,3}, {a,b,c}, {#,$,%}}
                """))
        );
    }

    @Test
    void shouldRenderMapAsDescriptionList() {
        assertEquals(
            """
                [%header,cols="1,1,1"]
                |===
                |a
                |b
                |c
                
                a|
                a|
                a:: 1
                b:: 2
                c:: 3
                
                a|
                b:: \\|\\|
                
                |===
                """,
            renderer.render(TableParser.parse("""
                a   | b             | c
                [:] | [a:1,b:2,c:3] | [b: "||"]
                """))
        );
    }

    @Test
    void shouldRenderNestedMaps() {
        assertEquals(
            """
                [%header,cols="1,1"]
                |===
                |a
                |b
                
                a|
                a::
                b::
                
                a|
                a::
                  A::: 1
                b::
                  B::: 2
                
                |===
                """,
            renderer.render(TableParser.parse("""
                a                | b
                [a: [:], b: [:]] | [a: [A: 1],b: [B: 2]]
                """))
        );
    }

    @Test
    void shouldRenderNestedMixedCollections() {
        assertEquals(
            """
                [%header,cols="1,1"]
                |===
                |a
                |b
                
                a|
                a::
                  .. 1
                  .. 2
                b::
                  ** 3
                  ** 4
                c:: 5
                
                a|
                * {empty}
                  A::: 1
                * {empty}
                  B::: 2
                
                |===
                """,
            renderer.render(TableParser.parse("""
                a                            | b
                [a: [1, 2], b: {3, 4}, c: 5] | {[A: 1], [B: 2]}
                """))
        );
    }
}
