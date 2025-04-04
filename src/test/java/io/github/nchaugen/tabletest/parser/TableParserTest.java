package io.github.nchaugen.tabletest.parser;

import io.github.nchaugen.tabletest.Table;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TableParserTest {

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
        assertEquals(List.of("1", "2", "3"), result.row(0).cells());
        assertEquals(List.of("4", "5", "6"), result.row(1).cells());
    }

    @Test
    void shouldParseTableWithQuotedPipes() {
        //language=TableTest
        String input = """
            Name    | Character | Usage
            Pipe    | "|"       | Separates columns
            Pipe    | '|'       | Separates columns
            Newline | "\\n"     | Separates rows
            Newline | '\\n'     | Separates rows
            Newline | \\n       | Separates rows
            """;

        Table result = TableParser.parse(input);

        assertEquals(5, result.rowCount());
        assertEquals(3, result.columnCount());

        assertEquals("|", result.row(0).cell(1));
        assertEquals("|", result.row(1).cell(1));
        assertEquals("\\n", result.row(2).cell(1));
        assertEquals("\\n", result.row(3).cell(1));
        assertEquals("\\n", result.row(4).cell(1));
    }

}
