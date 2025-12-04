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
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class YamlTableRenderer implements TableRenderer {

    private final Yaml yaml;

    public YamlTableRenderer() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);
        options.setIndent(2);
        options.setSplitLines(false);
        options.setDereferenceAliases(true);
        options.setAllowUnicode(true);
        options.setLineBreak(DumperOptions.LineBreak.getPlatformLineBreak());
        options.setPrettyFlow(true);

        yaml = new Yaml(options);
    }

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

        return yaml.dump(content);
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
