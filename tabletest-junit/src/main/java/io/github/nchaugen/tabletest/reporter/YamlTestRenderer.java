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
package io.github.nchaugen.tabletest.reporter;

import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;

import java.util.LinkedHashMap;
import java.util.List;

public class YamlTestRenderer implements TestIndexRenderer {
    private final Dump yaml;

    public YamlTestRenderer() {
        DumpSettings settings = DumpSettings.builder()
            .setDefaultFlowStyle(FlowStyle.BLOCK)
            .setIndent(2)
            .setDefaultScalarStyle(ScalarStyle.DOUBLE_QUOTED)
            .setSplitLines(false)
            .setDereferenceAliases(true)
            .setMultiLineFlow(true)
            .setUseUnicodeEncoding(true)
            .build();

        yaml = new Dump(settings);
    }

    @Override
    public String render(String title, String description, List<TableFileEntry> tableFileEntries) {
        LinkedHashMap content = new LinkedHashMap();
        if (title != null) content.put("title", title);
        if (description != null) content.put("description", description);
        LinkedHashMap tables = new LinkedHashMap();
        tableFileEntries.forEach(it -> tables.put(it.title(), it.path().toString()));
        content.put("tables", tables);
        return yaml.dumpToString(content);
    }
}
