package org.tabletest.parser;

import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Table structure")
@Description("""
        The shape a table must have before its values are read: a header row
        followed by at least one data row, non-blank header cells, and the
        lines the parser ignores — comments and blanks.
        """)
public class TableStructureTest {

    @DisplayName("A table is a header row followed by at least one data row")
    @Description("""
            The first row read becomes the header; every row after it is a data
            row, and each row divides into one cell per header. The Input column
            lists the source one line per element.
            """)
    @TableTest("""
        Scenario      | Input                          | Headers?  | Data rows?
        One data row  | ["a | b", "1 | 2"]             | [a, b]    | "[[1, 2]]"
        Two data rows | ["a | b", "1 | 2", "3 | 4"]    | [a, b]    | "[[1, 2], [3, 4]]"
        Single column | [a, 1, 2]                      | [a]       | "[[1], [2]]"
        Three columns | ["a | b | c", "1 | 2 | 3"]     | [a, b, c] | "[[1, 2, 3]]"
        """)
    void shouldParseHeaderRowFollowedByDataRows(
        List<String> inputLines,
        List<String> expectedHeaders,
        String expectedRows
    ) {
        Table result = TableParser.parse(String.join("\n", inputLines));

        assertEquals(expectedHeaders, result.headers());
        assertEquals(expectedHeaders.size(), result.columnCount());
        assertEquals(expectedRows, dataRowsOf(result));
    }

    @DisplayName("Blank lines and whole-line comments are ignored")
    @Description("""
            A line is a comment only when // is the first thing on it — anywhere
            else // is ordinary cell text, and quoting makes even a leading //
            part of the value. The Input column lists the source one line per
            element.
            """)
    @TableTest("""
        Scenario                     | Input                                  | Headers? | Data rows?
        Blank lines around the table | ['', '   ', "a | b", "1 | 2", '  ']    | [a, b]   | "[[1, 2]]"
        Blank line between rows      | ["a | b", "1 | 2", '', "3 | 4"]        | [a, b]   | "[[1, 2], [3, 4]]"
        Comment line above header    | ['// intro', "a | b", "1 | 2"]         | [a, b]   | "[[1, 2]]"
        Comment line between rows    | ["a | b", '// note', "1 | 2"]          | [a, b]   | "[[1, 2]]"
        Comment line holding pipes   | ["a | b", "// 0 | 1", "1 | 2"]         | [a, b]   | "[[1, 2]]"
        Quoted leading slashes       | ["a | b", "'//2' | 3"]                 | [a, b]   | "[[//2, 3]]"
        Slashes after cell text      | ["a | b", "4 // | 5"]                  | [a, b]   | "[[4 //, 5]]"
        Slashes starting a cell      | ["a | b", "6 | // 7"]                  | [a, b]   | "[[6, // 7]]"
        """)
    void shouldIgnoreBlankLinesAndCommentLines(
        List<String> inputLines,
        List<String> expectedHeaders,
        String expectedRows
    ) {
        Table result = TableParser.parse(String.join("\n", inputLines));

        assertEquals(expectedHeaders, result.headers());
        assertEquals(expectedRows, dataRowsOf(result));
    }

    @DisplayName("Input without any data rows is rejected")
    @Description("""
            A table needs a header row and at least one data row. The Input
            column lists the source one line per element.
            """)
    @TableTest("""
        Scenario            | Input                          | Error message?
        Empty input         | ['']                           | Table has no rows: input was empty or contained only blank lines and comments
        Blank input         | ['   ']                        | Table has no rows: input was empty or contained only blank lines and comments
        Comment only        | ['// just a comment']          | Table has no rows: input was empty or contained only blank lines and comments
        Comments and blanks | ['// one', '   ', '// two']    | Table has no rows: input was empty or contained only blank lines and comments
        """)
    void shouldRejectInputWithoutTableRows(List<String> inputLines, String expectedErrorMessage) {
        TableTestParseException actualException = assertThrows(
            TableTestParseException.class,
            () -> TableParser.parse(String.join("\n", inputLines))
        );

        assertEquals(expectedErrorMessage, actualException.getMessage());
    }

    private static String dataRowsOf(Table table) {
        return table.rows().stream().map(Row::values).toList().toString();
    }

    @DisplayName("Blank header cells are rejected, naming the column")
    @TableTest("""
        Scenario            | Header Row  | Error message?
        Blank first header  | " | b | c"  | Header cell in column 1 is blank
        Blank middle header | "a |  | c"  | Header cell in column 2 is blank
        Blank last header   | "a | b | "  | Header cell in column 3 is blank
        """)
    void shouldRejectBlankHeaderCells(String headerRow, String expectedErrorMessage) {
        TableTestParseException actualException = assertThrows(
            TableTestParseException.class,
            () -> TableParser.parse(headerRow + "\n1 | 2 | 3")
        );
        assertEquals(expectedErrorMessage, actualException.getMessage());
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
    void shouldPreserveQuotesForHeaders() {
        Table table = TableParser.parse("\"Scenario\" | 'Other'", true);
        assertEquals("\"Scenario\"", table.header(0));
        assertEquals("'Other'", table.header(1));
    }

}
