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
    @Description("""
            A leading pipe, a trailing pipe, or two pipes together leave a field with no text.
            The parser captures that field as null.

            Spacing around a pipe changes nothing. One row carries both spellings of the same
            three fields, in place of two rows with one expectation between them.
            """)
    @TableTest("""
        Scenario                     | Row                            | Fields?
        No pipe at all               | a                              | "[a]"
        One pipe                     | "a | b"                        | "[a, b]"
        Spaced or unspaced pipes     | {"a | b | c", "a|b|c"}         | "[a, b, c]"
        A trailing pipe              | "a | b |"                      | "[a, b, null]"
        A leading pipe               | "| a | b"                      | "[null, a, b]"
        An empty field between pipes | "a | | c"                      | "[a, null, c]"
        Nothing but pipes            | "|||"                          | "[null, null, null, null]"
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
