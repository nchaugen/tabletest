package org.tabletest.parser;

import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Table structure")
@Description("""
        The shape a table must have before its values are read: a header row
        followed by at least one data row, non-blank header cells, and the
        lines the parser ignores — comments and blanks.
        """)
public class TableStructureTest {

    @Test
    void shouldParseTable() {
        //language=TableTest
        String input = """
            a | b | c
            1 | 2 | 3
            4 | 5 | 6
            """;

        Table result = TableParser.parse(input);

        assertEquals(2, result.rowCount());
        assertEquals(3, result.columnCount());

        assertEquals("a", result.header(0));
        assertEquals("b", result.header(1));
        assertEquals("c", result.header(2));

        assertEquals(List.of("a", "b", "c"), result.headers());
        assertEquals(List.of("1", "2", "3"), result.row(0).values());
        assertEquals(List.of("4", "5", "6"), result.row(1).values());
    }

    @Test
    void shouldIgnoreComments() {
        //language=TableTest
        String input = """
            // this is a TableTest table
            a     | b
            // comment
            // 0 | 1
            '//2' | 3
            4 //  | 5
            6     | // 7
            8     | 9 //
            """;

        Table result = TableParser.parse(input);

        assertEquals(List.of("a", "b"), result.headers());
        assertEquals(List.of("//2", "3"), result.row(0).values());
        assertEquals(List.of("4 //", "5"), result.row(1).values());
        assertEquals(List.of("6", "// 7"), result.row(2).values());
        assertEquals(List.of("8", "9 //"), result.row(3).values());
    }

    @DisplayName("Input without any data rows is rejected")
    @Description("""
            A table needs a header row and at least one data row. \\n in the
            Input column stands for a line break in the parsed source.
            """)
    @TableTest("""
        Scenario            | Input
        Empty input         | ''
        Blank input         | '   '
        Comment only        | '// just a comment'
        Comments and blanks | '// one\\n   \\n// two'
        """)
    void shouldRejectInputWithoutTableRows(String input) {
        TableTestParseException actualException = assertThrows(
            TableTestParseException.class,
            () -> TableParser.parse(input.replace("\\n", "\n"))
        );
        assertTrue(actualException.getMessage().startsWith("Table has no rows"), actualException.getMessage());
    }

    @DisplayName("Blank header cells are rejected, naming the column")
    @TableTest("""
        Scenario            | Header Row  | Error Message?
        Blank first header  | " | b | c"  | Header cell in column 1 is blank
        Blank middle header | "a |  | c"  | Header cell in column 2 is blank
        Blank last header   | "a | b | "  | Header cell in column 3 is blank
        """)
    void shouldRejectBlankHeaderCells(String headerRow, String expectedErrorMessage) {
        TableTestParseException actualException = assertThrows(
            TableTestParseException.class,
            () -> TableParser.parse(headerRow + "\n1 | 2 | 3")
        );
        assertTrue(actualException.getMessage().startsWith(expectedErrorMessage), actualException.getMessage());
    }

    @Test
    void shouldParseLongCellValuesWithoutStackOverflow() {
        String longValue = "x".repeat(20_000);
        Table result = TableParser.parse("Input\n" + longValue);
        assertEquals(longValue, result.row(0).value(0));
    }

    @Test
    void shouldIgnoreLongCommentsWithoutStackOverflow() {
        String longComment = "// " + "x".repeat(20_000);
        Table result = TableParser.parse("a | b\n" + longComment + "\n1 | 2\n");
        assertEquals(List.of("1", "2"), result.row(0).values());
    }

    @Test
    void shouldIgnoreBlankLines() {
        //language=TableTest
        String input = """
                    \s
             a | b | c
            \s
             1 | 2 | 3
            \s
                      \s
             4 | 5 | 6
                            \s
            \s""";

        Table result = TableParser.parse(input);

        assertEquals(2, result.rowCount());
        assertEquals(List.of("a", "b", "c"), result.headers());
        assertEquals(List.of("1", "2", "3"), result.row(0).values());
        assertEquals(List.of("4", "5", "6"), result.row(1).values());

    }

    @Test
    void shouldPreserveQuotesForHeaders() {
        Table table = TableParser.parse("\"Scenario\" | 'Other'", true);
        assertEquals("\"Scenario\"", table.header(0));
        assertEquals("'Other'", table.header(1));
    }

}
