/*
 * Copyright 2025-present Nils Christian Haugen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.nchaugen.tabletest.parser;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Parser for converting TableTest format text into Table object.
 */
public class TableParser {
    private static final String ROW_SEPARATOR = "\\n";

    private TableParser() {}

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
