package org.tabletest.parser;

import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Row grammar")
@Description("""
        How a single line divides into fields, before any field is read as a
        value: unquoted pipes are the separators, a missing field is null, and
        quotes are the only thing that protects a pipe from splitting the row.
        Each Row below is parsed as the sole data row under a one-column header;
        Fields shows the captured field list.
        """)
public class RowGrammarTest {

    @DisplayName("Unquoted pipes divide a row into fields")
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

    @DisplayName("Quotes protect a pipe so the value stays in one field")
    @Description("Either quote style works; the surrounding quotes are discarded from the field.")
    @TableTest("""
        Scenario                | Row           | Fields?
        Double-quoted pipe      | '"a | b" | c' | "[a | b, c]"
        Single-quoted pipe      | "'a | b' | c" | "[a | b, c]"
        Whole row quoted        | '"a | b | c"' | "[a | b | c]"
        """)
    void shouldKeepPipeInsideQuotedField(String row, String fields) {
        assertEquals(fields, TableParser.parse("Field\n" + row).row(0).values().toString());
    }

    @DisplayName("An unquoted pipe inside a list, set, or map fails to parse")
    @Description("""
            Brackets and braces do not protect a pipe — only quotes do. The pipe
            splits the row mid-collection, leaving an unbalanced fragment, and the
            failure surfaces as a generic parse error naming the whole row rather
            than the pipe. To keep a pipe inside a collection, quote the element:
            ['a | b'].
            """)
    @TableTest("""
        Scenario                   | Row          | Throws?
        Unquoted pipe in a list    | "[a | b]"    | org.tabletest.parser.TableTestParseException
        Unquoted pipe in a set     | "{a | b}"    | org.tabletest.parser.TableTestParseException
        Unquoted pipe in a map value | "[k: a | b]" | org.tabletest.parser.TableTestParseException
        """)
    void shouldRejectUnquotedPipeInsideCollection(String row, Class<? extends Exception> expectedException) {
        assertThrows(expectedException, () -> TableParser.parse("Field\n" + row));
    }

}
