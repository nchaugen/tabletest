package io.github.nchaugen.tabletest.renderer;

import io.github.nchaugen.tabletest.parser.TableParser;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static io.github.nchaugen.tabletest.renderer.ColumnRoles.NO_ROLES;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class YamlTableRendererTest {

    private static final TableMetadata NO_METADATA = new EmptyTableMetadata();
    private final YamlTableRenderer renderer = new YamlTableRenderer();

    @Test
    void shouldRenderTitleAndDescriptionIfPresent() {
        assertEquals(//language=yaml
            """
                "title": "Table Title"
                "description": "This is a description of the __table__.\\n\\nIt can span multiple lines, and include lists and formatting:\\n\\n- List item 1\\n- List item 2\\n"
                "headers":
                - "value": "a"
                - "value": "b"
                "rows":
                - - "value": "1"
                  - "value": "2"
                """,
            renderer.render(
                TableParser.parse("""
                    a | b
                    1 | 2
                    """),
                new EmptyTableMetadata() {
                    @Override
                    public String title() {
                        return "Table Title";
                    }

                    @Override
                    public String description() {
                        return """
                            This is a description of the __table__.
                            
                            It can span multiple lines, and include lists and formatting:
                            
                            - List item 1
                            - List item 2
                            """;
                    }
                }
            )
        );
    }

    @Test
    void shouldAddRoleForExpectationCells() {
        assertEquals(//language=yaml
            """
                "headers":
                - "value": "a?"
                  "role": "expectation"
                - "value": "b?"
                  "role": "expectation"
                - "value": "c?"
                  "role": "expectation"
                - "value": "d?"
                  "role": "expectation"
                - "value": "e?"
                  "role": "expectation"
                "rows":
                - - "value": !!set {
                      }
                    "role": "expectation"
                  - "value":
                    - "1"
                    - "2"
                    - "3"
                    "role": "expectation"
                  - "value": "3"
                    "role": "expectation"
                  - "value": !!null "null"
                    "role": "expectation"
                  - "value":
                      "a": "1"
                      "b": "2"
                      "c": "3"
                    "role": "expectation"
                """,
            renderer.render(
                TableParser.parse("""
                    a? | b?      | c? | d? | e?
                    {} | [1,2,3] | 3  |    | [a:1,b:2,c:3]
                    """),
                new EmptyTableMetadata() {
                    @Override
                    public ColumnRoles columnRoles() {
                        return new ColumnRoles(-1, Set.of(0, 1, 2, 3, 4));
                    }
                }
            )
        );
    }

    @Test
    void shouldAddRoleForScenarioCells() {
        assertEquals(//language=yaml
            """
                "headers":
                - "value": "scenario"
                  "role": "scenario"
                - "value": "input"
                - "value": "output?"
                  "role": "expectation"
                "rows":
                - - "value": "add"
                    "role": "scenario"
                  - "value": "5"
                  - "value": "5"
                    "role": "expectation"
                - - "value": "multiply"
                    "role": "scenario"
                  - "value": "3"
                  - "value": "15"
                    "role": "expectation"
                """,
            renderer.render(
                TableParser.parse("""
                    scenario | input | output?
                    add      | 5     | 5
                    multiply | 3     | 15
                    """),
                new EmptyTableMetadata() {
                    @Override
                    public ColumnRoles columnRoles() {
                        return new ColumnRoles(0, Set.of(2));
                    }
                }
            )
        );
    }

    @Test
    void shouldRenderNullEmptyStringAndExplicitWhitespace() {
        assertEquals(//language=yaml
            """
                "headers":
                - "value": "a"
                - "value": "b"
                - "value": "c d"
                - "value": " e "
                - "value": "f"
                - "value": "g"
                "rows":
                - - "value": !!null "null"
                  - "value": ""
                  - "value": "   "
                  - "value": "a bc  def"
                  - "value": "\\t"
                  - "value": "\\t "
                """,
            renderer.render(
                TableParser.parse("""
                    a | b  | c d   | " e "     | f    | g
                      | "" | "   " | a bc  def | '\t' | '\t '
                    """),
                NO_METADATA
            )
        );
    }

    @Test
    void shouldRenderEscapedPipe() {
        assertEquals(//language=yaml
            """
                "headers":
                - "value": "++"
                - "value": "+"
                - "value": "a|b"
                "rows":
                - - "value": "|"
                  - "value": "|"
                  - "value": "Text with | character"
                """,
            renderer.render(
                TableParser.parse("""
                    ++  | +   | 'a|b'
                    "|" | '|' | "Text with | character"
                    """),
                NO_METADATA
            )
        );
    }

    @Test
    void shouldRenderList() {
        assertEquals(//language=yaml
            """
                "headers":
                - "value": "a"
                - "value": "b"
                - "value": "c"
                "rows":
                - - "value": [
                      ]
                  - "value":
                    - "1"
                    - "2"
                    - "3"
                  - "value":
                    - "|"
                    - "|"
                """,
            renderer.render(
                TableParser.parse("""
                    a  | b         | c
                    [] | [1,2,3] | ['|', "|"]
                    """),
                NO_METADATA
            )
        );
    }

    @Test
    void shouldRenderEmptyListWhenNested() {
        assertEquals(//language=yaml
            """
                "headers":
                - "value": "a"
                - "value": "b"
                - "value": "c"
                "rows":
                - - "value": [
                      ]
                  - "value":
                    - [
                      ]
                  - "value":
                    - - [
                        ]
                """,
            renderer.render(
                TableParser.parse("""
                    a  | b    | c
                    [] | [[]] | [[[]]]
                    """),
                NO_METADATA
            )
        );
    }

    @Test
    void shouldRenderNestedLists() {
        assertEquals(//language=yaml
            """
                "headers":
                - "value": "a"
                "rows":
                - - "value":
                    - - "1"
                      - "2"
                      - "3"
                    - - "a"
                      - "b"
                      - "c"
                    - - "#"
                      - "$"
                      - "%"
                """,
            renderer.render(
                TableParser.parse("""
                    a
                    [[1,2,3],[a,b,c],[#,$,%]]
                    """),
                NO_METADATA
            )
        );
    }

    @Test
    void shouldRenderSet() {
        assertEquals(//language=yaml
            """
                "headers":
                - "value": "a"
                - "value": "b"
                - "value": "c"
                "rows":
                - - "value": !!set {
                      }
                  - "value": !!set
                      "1": !!null "null"
                      "2": !!null "null"
                      "3": !!null "null"
                  - "value": !!set
                      "||": !!null "null"
                """,
            renderer.render(
                TableParser.parse("""
                    a  | b   | c
                    {} | {1,2,3} | {"||"}
                    """),
                NO_METADATA
            )
        );
    }

    @Test
    void shouldRenderNestedSets() {
        assertEquals(//language=yaml
            """
                "headers":
                - "value": "a"
                "rows":
                - - "value": !!set
                      ? !!set
                        "1": !!null "null"
                        "2": !!null "null"
                        "3": !!null "null"
                      : !!null "null"
                      ? !!set
                        "a": !!null "null"
                        "b": !!null "null"
                        "c": !!null "null"
                      : !!null "null"
                      ? !!set
                        "#": !!null "null"
                        "$": !!null "null"
                        "%": !!null "null"
                      : !!null "null"
                """,
            renderer.render(
                TableParser.parse("""
                    a
                    {{1,2,3}, {a,b,c}, {#,$,%}}
                    """),
                NO_METADATA
            )
        );
    }

    @Test
    void shouldRenderMapAsDescriptionList() {
        assertEquals(//language=yaml
            """
                "headers":
                - "value": "a"
                - "value": "b"
                - "value": "c"
                "rows":
                - - "value": {
                      }
                  - "value":
                      "a": "1"
                      "b": "2"
                      "c": "3"
                  - "value":
                      "b": "||"
                """,
            renderer.render(
                TableParser.parse("""
                    a   | b             | c
                    [:] | [a:1,b:2,c:3] | [b: "||"]
                    """),
                NO_METADATA
            )
        );
    }

    @Test
    void shouldRenderNestedMaps() {
        assertEquals(//language=yaml
            """
                "headers":
                - "value": "a"
                - "value": "b"
                "rows":
                - - "value":
                      "a": {
                        }
                      "b": {
                        }
                  - "value":
                      "a":
                        "A": "1"
                      "b":
                        "B": "2"
                """,
            renderer.render(
                TableParser.parse("""
                    a                | b
                    [a: [:], b: [:]] | [a: [A: 1],b: [B: 2]]
                    """),
                NO_METADATA
            )
        );
    }

    @Test
    void shouldRenderNestedMixedCollections() {
        assertEquals(
            """
                "headers":
                - "value": "a"
                - "value": "b"
                "rows":
                - - "value":
                      "a":
                      - "1"
                      - "2"
                      "b": !!set
                        "3": !!null "null"
                        "4": !!null "null"
                      "c": "5"
                  - "value": !!set
                      ? "A": "1"
                      : !!null "null"
                      ? "B": "2"
                      : !!null "null"
                """,
            renderer.render(
                TableParser.parse("""
                    a                            | b
                    [a: [1, 2], b: {3, 4}, c: 5] | {[A: 1], [B: 2]}
                    """),
                NO_METADATA
            )
        );
    }

    private static class EmptyTableMetadata implements TableMetadata {
        @Override
        public ColumnRoles columnRoles() {
            return NO_ROLES;
        }

        @Override
        public String title() {
            return null;
        }

        @Override
        public String description() {
            return null;
        }
    }
}
