package io.github.nchaugen.tabletest.parser;

import io.github.nchaugen.tabletest.Row;
import io.github.nchaugen.tabletest.Table;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class TableParser {
    private static final String ROW_SEPARATOR = "\\n";

    public static Table parse(String input) {
        return new Table(
            Arrays.stream(input.split(ROW_SEPARATOR))
                .map(TableParser::parseRow)
                .filter(Objects::nonNull)
                .toList()
        );
    }

    private static Row parseRow(String line) {
        List<Object> cells = RowParser.parse(line).captures().stream()
            .map(it -> (it instanceof String s) ? s.trim() : it)
            .toList();
        return cells.isEmpty() ? null : new Row(cells);
    }

}
