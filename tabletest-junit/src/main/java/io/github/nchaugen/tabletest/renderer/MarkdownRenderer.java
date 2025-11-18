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
package io.github.nchaugen.tabletest.renderer;

import io.github.nchaugen.tabletest.parser.Row;
import io.github.nchaugen.tabletest.parser.Table;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class MarkdownRenderer implements TableRenderer {

    @Override
    public String render(Table table, ColumnRoles columnRoles) {
        return String.join("\n",
            table.headers().stream().map(this::render).collect(MARKDOWN_ROW),
            table.headers().stream().map(__ -> "---").collect(MARKDOWN_ROW),
            table.rows().stream().map(this::render).collect(MULTILINE)
        );
    }

    private static final Collector<CharSequence, ?, String>
        MARKDOWN_ROW = Collectors.joining(" | ", "| ", " |");

    private static final Collector<CharSequence, ?, String>
        MULTILINE = Collectors.joining("\n", "", "\n");

    private String render(Row row) {
        return row.values().stream().map(this::render).collect(MARKDOWN_ROW);
    }

    private String render(Object value) {
        return renderValue(value).replace("|", "\\|");
    }

    private String renderValue(Object value) {
        return switch (value) {
            case null -> "";
            case List<?> list -> renderCollection(list, "[", "]");
            case Set<?> set -> renderCollection(set, "{", "}");
            case Map<?, ?> map -> renderDictionary(map);
            default -> value.toString();
        };
    }

    private String renderCollection(Collection<?> collection, String prefix, String suffix) {
        return collection.stream()
            .map(this::renderValue)
            .collect(Collectors.joining(", ", prefix, suffix));
    }

    private String renderDictionary(Map<?, ?> map) {
        return map.isEmpty()
            ? "[:]"
            : map.entrySet().stream()
            .map(it -> renderValue(it.getKey()) + ": " + renderValue(it.getValue()))
            .collect(Collectors.joining(", ", "[", "]"));
    }
}
