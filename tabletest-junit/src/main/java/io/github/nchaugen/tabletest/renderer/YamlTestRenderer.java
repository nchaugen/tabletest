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

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.util.LinkedHashMap;
import java.util.List;

public class YamlTestRenderer implements TestIndexRenderer {
    private final Yaml yaml;

    public YamlTestRenderer() {
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
    public String render(String title, String description, List<TableFileEntry> tableFileEntries) {
        LinkedHashMap content = new LinkedHashMap();
        if (title != null) content.put("title", title);
        if (description != null) content.put("description", description);
        LinkedHashMap tables = new LinkedHashMap();
        tableFileEntries.forEach(it -> tables.put(it.title(), it.path().toString()));
        content.put("tables", tables);
        return yaml.dump(content);
    }
}
