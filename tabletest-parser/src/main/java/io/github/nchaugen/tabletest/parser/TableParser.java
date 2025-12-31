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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Parser for converting TableTest format text into Table object.
 */
public class TableParser {
    private static final String ROW_SEPARATOR = "\\n";

    private TableParser() {
    }

    /**
     * Parses input string in TableTest format into a Table representation.
     * String values are unwrapped from their quotes.
     *
     * @param input string to parse
     * @return Table with parsed rows (first valid row as header)
     * @throws NullPointerException if input is null
     */
    public static Table parse(String input) {
        return parse(input, false);
    }

    /**
     * Parses input string in TableTest format into a Table representation.
     *
     * @param input      string to parse
     * @param keepQuotes if true, preserves original quotes in string values; if false, unwraps quotes
     * @return Table with parsed rows (first valid row as header)
     * @throws NullPointerException if input is null
     */
    public static Table parse(String input, boolean keepQuotes) {
        return new Table(
            Arrays.stream(input.split(ROW_SEPARATOR))
                .filter(it -> !it.isBlank())
                .map(line -> parseRow(line, keepQuotes))
                .filter(Objects::nonNull)
                .toList()
        ).withHeadersInRows();
    }

    private static Row parseRow(String line, boolean keepQuotes) {
        ParseResult parsedRow = RowParser.parse(line);
        if (parsedRow.isIncomplete()) {
            throw new TableTestParseException("Failed to parse `" + parsedRow.rest() + "` in row `" + line + "`");
        }
        List<Object> values = parsedRow.captures().stream()
            .map(v -> unwrapValue(v, keepQuotes))
            .toList();
        return values.isEmpty() ? null : new Row(values);
    }

    private static Object unwrapValue(Object value, boolean keepQuotes) {
        return switch (value) {
            case null -> null;
            case StringValue sv -> keepQuotes ? sv.withQuotes() : sv.value();
            case List<?> list -> unwrapList(list, keepQuotes);
            case Set<?> set -> unwrapSet(set, keepQuotes);
            case Map<?, ?> map -> unwrapMap(map, keepQuotes);
            default -> value;
        };
    }

    private static List<Object> unwrapList(List<?> list, boolean keepQuotes) {
        return list.stream().map(v -> unwrapValue(v, keepQuotes)).toList();
    }

    private static LinkedHashSet<Object> unwrapSet(Set<?> set, boolean keepQuotes) {
        return set.stream()
            .map(v -> unwrapValue(v, keepQuotes))
            .collect(LinkedHashSet::new, Set::add, Set::addAll);
    }

    private static LinkedHashMap<Object, Object> unwrapMap(Map<?, ?> map, boolean keepQuotes) {
        return map.entrySet().stream()
            .map(e -> unwrapEntry(e, keepQuotes))
            .collect(LinkedHashMap::new, TableParser::putEntry, Map::putAll);
    }

    private static Map.Entry<Object, Object> unwrapEntry(Map.Entry<?, ?> entry, boolean keepQuotes) {
        return Map.entry(unwrapValue(entry.getKey(), keepQuotes), unwrapValue(entry.getValue(), keepQuotes));
    }

    private static Object putEntry(LinkedHashMap<Object, Object> m, Map.Entry<Object, Object> e) {
        return m.put(e.getKey(), e.getValue());
    }

}
