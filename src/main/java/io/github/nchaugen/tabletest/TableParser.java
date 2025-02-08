package io.github.nchaugen.tabletest;

import io.github.nchaugen.tabletest.parser.RowParser;

import java.util.Arrays;

public class TableParser {
    private static final String ROW_SEPARATOR = "\\n";

    public static Table parse(String input) {
        return new Table(
            Arrays.stream(input.split(ROW_SEPARATOR))
                .map(TableParser::parseRow)
                .toList()
        );
    }

    private static Row parseRow(String line) {
        return new Row(
            RowParser.parse(line).captures().stream()
                .map( it -> (it instanceof String s) ? s.trim() : it)
                .toList()
        );
    }

}
