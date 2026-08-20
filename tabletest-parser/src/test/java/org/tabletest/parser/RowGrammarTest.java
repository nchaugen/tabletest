package org.tabletest.parser;

import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Row grammar")
@Description("""
        These rules cover how one line divides into fields. They apply before the parser
        reads a field as a value.

        Each Row below is parsed as the only data row under a one-column header. Fields
        shows the captured field list, printed back out. Fields is text rather than a list
        column because a list cannot hold a null element: writing [a, , c] is itself a parse
        error. A null field is what several of these rules are about.
        """)
public class RowGrammarTest {

    @DisplayName("Divides a row into fields at each unquoted pipe")
    @Description("A missing field — from a leading, trailing, or doubled pipe — is captured as null.")
    @TableTest("""
        Scenario                     | Row         | Fields?
        Single field                 | a           | "[a]"
        Two fields                   | "a | b"     | "[a, b]"
        Three fields                 | "a | b | c" | "[a, b, c]"
        No spaces around pipe        | "a|b|c"     | "[a, b, c]"
        Trailing pipe adds a null    | "a | b |"   | "[a, b, null]"
        Leading pipe adds a null     | "| a | b"   | "[null, a, b]"
        Interior empty field is null | "a | | c"   | "[a, null, c]"
        Only pipes are all null      | "|||"       | "[null, null, null, null]"
        """)
    void shouldDivideRowIntoFields(String row, String fields) {
        assertEquals(fields, TableParser.parse("Field\n" + row).row(0).values().toString());
    }

    @DisplayName("Keeps a quoted pipe inside its field")
    @Description("Either quote style works; the surrounding quotes are discarded from the field.")
    @TableTest("""
        Scenario           | Row                            | Fields?
        Either quote style | {'"a | b" | c', "'a | b' | c"} | "[a | b, c]"
        Whole row quoted   | '"a | b | c"'                  | "[a | b | c]"
        """)
    void shouldKeepPipeInsideQuotedField(String row, String fields) {
        assertEquals(fields, TableParser.parse("Field\n" + row).row(0).values().toString());
    }

    @DisplayName("Refuses an unquoted pipe inside a list, set, or map")
    @Description("""
            Brackets and braces do not protect a pipe. Only quotes do.

            The pipe splits the row inside the collection and leaves an unbalanced fragment. The
            parser therefore names the whole row, not the pipe. To keep a pipe inside a
            collection, quote the element: ['a | b'].
            """)
    @TableTest("""
        Scenario                     | Row          | Error message?
        Unquoted pipe in a list      | "[a | b]"    | "Failed to parse `[a | b]` in row `[a | b]`"
        Unquoted pipe in a set       | "{a | b}"    | "Failed to parse `{a | b}` in row `{a | b}`"
        Unquoted pipe in a map value | "[k: a | b]" | "Failed to parse `[k: a | b]` in row `[k: a | b]`"
        """)
    void shouldRejectUnquotedPipeInsideCollection(String row, String expectedErrorMessage) {
        TableTestParseException actualException = assertThrows(
            TableTestParseException.class,
            () -> TableParser.parse("Field\n" + row)
        );

        assertEquals(expectedErrorMessage, actualException.getMessage());
    }

}
