package io.github.nchaugen.tabletest.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Parser for converting TableTest format text into Table object.
 */
public class TableParser {
    private static final String ROW_SEPARATOR = "\\n";

    /**
     * Parses input string in TableTest format into a Table representation.
     *
     * @param input string to parse
     * @return Table with parsed rows (first valid row as header)
     * @throws NullPointerException if input is null
     */
    public static Table parse(String input) {
        return new Table(
            Arrays.stream(input.split(ROW_SEPARATOR))
                .filter(it -> !it.isBlank())
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
