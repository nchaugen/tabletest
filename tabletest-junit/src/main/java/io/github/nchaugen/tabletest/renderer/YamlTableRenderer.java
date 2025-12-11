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

import io.github.nchaugen.tabletest.parser.Table;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class YamlTableRenderer implements TableRenderer {

    public static final DumpSettings SETTINGS = DumpSettings.builder()
        .setDefaultFlowStyle(FlowStyle.BLOCK)
        .setIndent(2)
        .setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED)
        .setSplitLines(false)
        .setDereferenceAliases(true)
        .setMultiLineFlow(false)
        .setUseUnicodeEncoding(true)
        .build();

    @Override
    public String render(Table table, TableMetadata context) {
        LinkedHashMap content = new LinkedHashMap();
        if (context.title() != null) content.put("title", context.title());
        if (context.description() != null) content.put("description", context.description());

        content.put(
            "headers", IntStream.range(0, table.columnCount())
                .mapToObj(i -> toValueMap(table.header(i), context.columnRoles().roleFor(i)))
                .toList()
        );

        content.put(
            "rows", table.rows().stream()
                .map(row ->
                    IntStream.range(0, table.columnCount())
                    .mapToObj(i -> toValueMap(row.value(i), context.columnRoles().roleFor(i)))
                    .toList()
                )
                .toList()
        );

        if (!context.rowResults().isEmpty()) {
            content.put(
                "rowResults", context.rowResults().stream()
                    .map(this::toRowResultMap)
                    .toList()
            );
        }

        return new Dump(SETTINGS).dumpToString(content);
    }

    private Map<String, Object> toRowResultMap(io.github.nchaugen.tabletest.reporter.RowResult result) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("rowIndex", result.rowIndex());
        map.put("passed", result.passed());
        map.put("displayName", result.displayName());
        if (result.cause() != null) {
            map.put("errorMessage", result.cause().getMessage());
        }
        return map;
    }

    private static Map<String, Object> toValueMap(Object value, CellRole role) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("value", value);
        if (role != CellRole.NORMAL) {
            map.put("role", role.name().toLowerCase());
        }
        return map;
    }

}
