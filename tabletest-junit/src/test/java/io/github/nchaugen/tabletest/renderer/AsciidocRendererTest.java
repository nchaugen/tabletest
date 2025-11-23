package io.github.nchaugen.tabletest.renderer;

import io.github.nchaugen.tabletest.parser.TableParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.*;

import static io.github.nchaugen.tabletest.renderer.ColumnRoles.NO_ROLES;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AsciidocRendererTest {

    private final ExtensionContext context = new StubExtensionContext();
    private final AsciidocRenderer renderer = new AsciidocRenderer(new DefaultAsciidocStyle());

    @Test
    void shouldAddRoleForExpectationCells() {
        assertEquals(
            """
                == ++Display Name++
                
                [%header,cols="1,1,1,1,1"]
                |===
                |[.expectation]#++a?++#
                |[.expectation]#++b?++#
                |[.expectation]#++c?++#
                |[.expectation]#++d?++#
                |[.expectation]#++e?++#
                
                a|[.expectation]#{empty}#
                a|[.expectation]
                . ++1++
                . ++2++
                . ++3++
                
                a|[.expectation]#++3++#
                a|[.expectation]
                a|[.expectation]
                ++a++:: ++1++
                ++b++:: ++2++
                ++c++:: ++3++
                
                |===
                """,
            renderer.render(
                TableParser.parse("""
                    a? | b?      | c? | d? | e?
                    {} | [1,2,3] | 3  |    | [a:1,b:2,c:3]
                    """),
                new ColumnRoles(-1, Set.of(0, 1, 2, 3, 4)),
                context
            )
        );
    }

    @Test
    void shouldAddRoleForScenarioCells() {
        assertEquals(
            """
                == ++Display Name++
                
                [%header,cols="1,1,1"]
                |===
                |[.scenario]#++scenario++#
                |++input++
                |[.expectation]#++output?++#
                
                a|[.scenario]#++add++#
                a|++5++
                a|[.expectation]#++5++#
                
                a|[.scenario]#++multiply++#
                a|++3++
                a|[.expectation]#++15++#
                |===
                """,
            renderer.render(
                TableParser.parse("""
                    scenario | input | output?
                    add      | 5     | 5
                    multiply | 3     | 15
                    """),
                new ColumnRoles(0, Set.of(2)),
                context
            )
        );
    }

    @Test
    void shouldRenderNullEmptyStringAndExplicitWhitespace() {
        assertEquals(
            """
                == ++Display Name++
                
                [%header,cols="1,1,1,1,1,1"]
                |===
                |++a++
                |++b++
                |++c d++
                |&#x2423;++e++&#x2423;
                |++f++
                |++g++
                
                a|
                a|+""+
                a|&#x2423;&#x2423;&#x2423;
                a|++a bc++&#x2423;&#x2423;++def++
                a|&#x21E5;
                a|&#x21E5;&#x2423;
                |===
                """,
            renderer.render(
                TableParser.parse("""
                    a | b  | c d   | " e "     | f    | g
                      | "" | "   " | a bc  def | '\t' | '\t '
                    """),
                NO_ROLES,
                context
            )
        );
    }

    @Test
    void shouldRenderEscapedPipe() {
        assertEquals(
            """
                == ++Display Name++
                
                [%header,cols="1,1,1"]
                |===
                |&#43;&#43;
                |&#43;
                |++a\\|b++
                
                a|++\\|++
                a|++\\|++
                a|++Text with \\| character++
                |===
                """,
            renderer.render(
                TableParser.parse("""
                    ++  | +   | 'a|b'
                    "|" | '|' | "Text with | character"
                    """),
                NO_ROLES,
                context
            )
        );
    }

    @Test
    void shouldRenderListAsOrderedListByDefault() {
        assertEquals(
            """
                == ++Display Name++
                
                [%header,cols="1,1,1"]
                |===
                |++a++
                |++b++
                |++c++
                
                a|{empty}
                a|
                . ++1++
                . ++2++
                . ++3++
                
                a|
                . ++\\|++
                . ++\\|++
                
                |===
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
    void shouldRenderEmptyList() {
        assertEquals(
            """
                == ++Display Name++
                
                [%header,cols="1,1,1"]
                |===
                |++a++
                |++b++
                |++c++
                
                a|{empty}
                a|
                . {empty}
                
                a|
                . {empty}
                  .. {empty}
                
                |===
                """,
            renderer.render(
                TableParser.parse("""
                    a  | b    | c
                    [] | [[]] | [[[]]]
                    """),
                NO_ROLES,
                context
            )
        );
    }

    @Test
    void shouldRenderNestedLists() {
        assertEquals(
            """
                == ++Display Name++
                
                [%header,cols="1"]
                |===
                |++a++
                
                a|
                . {empty}
                  .. ++1++
                  .. ++2++
                  .. ++3++
                . {empty}
                  .. ++a++
                  .. ++b++
                  .. ++c++
                . {empty}
                  .. ++#++
                  .. ++$++
                  .. ++%++
                
                |===
                """,
            renderer.render(
                TableParser.parse("""
                    a
                    [[1,2,3],[a,b,c],[#,$,%]]
                    """),
                NO_ROLES,
                context
            )
        );
    }

    @Test
    void shouldRenderListAsConfiguredListType() {
        AsciidocRenderer configuredRenderer = new AsciidocRenderer(new DefaultAsciidocStyle() {
            @Override
            public AsciidocListFormat listFormat() {
                return AsciidocListFormat.unordered();
            }
        });

        assertEquals(
            """
                == ++Display Name++
                
                [%header,cols="1,1,1"]
                |===
                |++a++
                |++b++
                |++c++
                
                a|{empty}
                a|
                * ++1++
                * ++2++
                * ++3++
                
                a|
                * ++\\|++
                * ++\\|++
                
                |===
                """,
            configuredRenderer.render(
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
    void shouldRenderNestedListsAsConfigured() {
        AsciidocRenderer configuredRenderer = new AsciidocRenderer(new DefaultAsciidocStyle() {
            @Override
            public AsciidocListFormat listFormat() {
                return AsciidocListFormat.unordered("square", "none");
            }
        });

        assertEquals(
            """
                == ++Display Name++
                
                [%header,cols="1"]
                |===
                |++a++
                
                a|
                [square]
                * {empty}
                [none]
                  ** ++1++
                  ** ++2++
                  ** ++3++
                * {empty}
                [none]
                  ** ++a++
                  ** ++b++
                  ** ++c++
                * {empty}
                [none]
                  ** ++#++
                  ** ++$++
                  ** ++%++
                
                |===
                """,
            configuredRenderer.render(
                TableParser.parse("""
                    a
                    [[1,2,3],[a,b,c],[#,$,%]]
                    """),
                NO_ROLES,
                context
            )
        );
    }

    @Test
    void shouldRenderSetAsUnorderedList() {
        assertEquals(
            """
                == ++Display Name++
                
                [%header,cols="1,1,1"]
                |===
                |++a++
                |++b++
                |++c++
                
                a|{empty}
                a|
                * ++1++
                * ++2++
                * ++3++
                
                a|
                * ++\\|\\|++
                
                |===
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
    void shouldRenderNestedSets() {
        assertEquals(
            """
                == ++Display Name++
                
                [%header,cols="1"]
                |===
                |++a++
                
                a|
                * {empty}
                  ** ++1++
                  ** ++2++
                  ** ++3++
                * {empty}
                  ** ++a++
                  ** ++b++
                  ** ++c++
                * {empty}
                  ** ++#++
                  ** ++$++
                  ** ++%++
                
                |===
                """,
            renderer.render(
                TableParser.parse("""
                    a
                    {{1,2,3}, {a,b,c}, {#,$,%}}
                    """),
                NO_ROLES,
                context
            )
        );
    }

    @Test
    void shouldRenderSetAsConfiguredListType() {
        AsciidocRenderer configuredRenderer = new AsciidocRenderer(new DefaultAsciidocStyle() {
            @Override
            public AsciidocListFormat setFormat() {
                return AsciidocListFormat.ordered("lowergreek");
            }
        });

        assertEquals(
            """
                == ++Display Name++
                
                [%header,cols="1,1"]
                |===
                |++a++
                |++b++
                
                a|{empty}
                a|
                [lowergreek]
                . {empty}
                  .. ++1++
                . {empty}
                  .. ++2++
                  .. ++3++
                
                |===
                """,
            configuredRenderer.render(
                TableParser.parse("""
                    a  | b
                    {} | {{1},{2,3}}
                    """),
                NO_ROLES,
                context
            )
        );
    }

    @Test
    void shouldRenderNestedSetsAsConfigured() {
        AsciidocRenderer configuredRenderer = new AsciidocRenderer(new DefaultAsciidocStyle() {
            @Override
            public AsciidocListFormat setFormat() {
                return AsciidocListFormat.unordered("square", "none");
            }
        });

        assertEquals(
            """
                == ++Display Name++
                
                [%header,cols="1"]
                |===
                |++a++
                
                a|
                [square]
                * {empty}
                [none]
                  ** ++1++
                  ** ++2++
                  ** ++3++
                * {empty}
                [none]
                  ** ++a++
                  ** ++b++
                  ** ++c++
                * {empty}
                [none]
                  ** ++#++
                  ** ++$++
                  ** ++%++
                
                |===
                """,
            configuredRenderer.render(
                TableParser.parse("""
                    a
                    {{1,2,3}, {a,b,c}, {#,$,%}}
                    """),
                NO_ROLES,
                context
            )
        );
    }

    @Test
    void shouldRenderMapAsDescriptionList() {
        assertEquals(
            """
                == ++Display Name++
                
                [%header,cols="1,1,1"]
                |===
                |++a++
                |++b++
                |++c++
                
                a|{empty}
                a|
                ++a++:: ++1++
                ++b++:: ++2++
                ++c++:: ++3++
                
                a|
                ++b++:: ++\\|\\|++
                
                |===
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
    void shouldRenderNestedMaps() {
        assertEquals(
            """
                == ++Display Name++
                
                [%header,cols="1,1"]
                |===
                |++a++
                |++b++
                
                a|
                ++a++:: {empty}
                ++b++:: {empty}
                
                a|
                ++a++::
                  ++A++::: ++1++
                ++b++::
                  ++B++::: ++2++
                
                |===
                """,
            renderer.render(
                TableParser.parse("""
                    a                | b
                    [a: [:], b: [:]] | [a: [A: 1],b: [B: 2]]
                    """),
                NO_ROLES,
                context
            )
        );
    }

    @Test
    void shouldRenderNestedMixedCollections() {
        assertEquals(
            """
                == ++Display Name++
                
                [%header,cols="1,1"]
                |===
                |++a++
                |++b++
                
                a|
                ++a++::
                  .. ++1++
                  .. ++2++
                ++b++::
                  ** ++3++
                  ** ++4++
                ++c++:: ++5++
                
                a|
                * {empty}
                  ++A++::: ++1++
                * {empty}
                  ++B++::: ++2++
                
                |===
                """,
            renderer.render(
                TableParser.parse("""
                    a                            | b
                    [a: [1, 2], b: {3, 4}, c: 5] | {[A: 1], [B: 2]}
                    """),
                NO_ROLES,
                context
            )
        );
    }

    @Test
    void shouldRenderNestedMixedCollectionsAsConfigured() {
        AsciidocRenderer configuredRenderer = new AsciidocRenderer(new DefaultAsciidocStyle() {
            @Override
            public AsciidocListFormat listFormat() {
                return AsciidocListFormat.unordered("square");
            }

            @Override
            public AsciidocListFormat setFormat() {
                return AsciidocListFormat.unordered("circle", "disc");
            }
        });

        assertEquals(
            """
                == ++Display Name++
                
                [%header,cols="1,1"]
                |===
                |++a++
                |++b++
                
                a|
                ++a++::
                  ** ++1++
                  ** ++2++
                ++b++::
                [disc]
                  ** ++3++
                  ** ++4++
                ++c++:: ++5++
                
                a|
                [circle]
                * {empty}
                  ++A++::: ++1++
                * {empty}
                  ++B++::: ++2++
                
                |===
                """,
            configuredRenderer.render(
                TableParser.parse("""
                    a                            | b
                    [a: [1, 2], b: {3, 4}, c: 5] | {[A: 1], [B: 2]}
                    """),
                NO_ROLES,
                context
            )
        );
    }

}
