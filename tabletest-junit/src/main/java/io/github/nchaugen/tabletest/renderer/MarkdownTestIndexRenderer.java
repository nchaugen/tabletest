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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MarkdownTestIndexRenderer implements TestIndexRenderer {
    @Override
    public String render(String title, String description, List<TableFileEntry> tableFileEntries) {
        return Stream.concat(
            Stream.of(
                "# " + title + "\n",
                description != null ? description + "\n" : null
            ).filter(Objects::nonNull),
            tableFileEntries.stream().map(it -> "* [" + it.title() + "](" + URLEncoder.encode(it.path().toString(), StandardCharsets.UTF_8) + ")")
        ).collect(Collectors.joining("\n"));
    }
}
