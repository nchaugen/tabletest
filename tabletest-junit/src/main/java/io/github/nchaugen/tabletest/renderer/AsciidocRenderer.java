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

public class AsciidocRenderer implements TableRenderer {

    @Override
    public String render(Table table) {
        return String.join(
            "\n",
            table.headers().stream().map(__ -> "1").collect(ASCIIDOC_ATTRIBUTE_LIST),
            table.headers().stream().map(this::render).collect(ASCIIDOC_HEADER_ROW),
            table.rows().stream().map(this::render).collect(MULTILINE)
        );
    }

    private static final Collector<CharSequence, ?, String>
        ASCIIDOC_ATTRIBUTE_LIST = Collectors.joining(",", "[%header,cols=\"", "\"]\n|===");

    private static final Collector<CharSequence, ?, String>
        ASCIIDOC_HEADER_ROW = Collectors.joining("\n|", "|", "\n");

    private static final Collector<CharSequence, ?, String>
        ASCIIDOC_ROW = Collectors.joining("\na|", "a|", "\n");

    private static final Collector<CharSequence, ?, String>
        MULTILINE = Collectors.joining("\n", "", "|===\n");

    private String render(Row row) {
        return row.values().stream().map(this::render).collect(ASCIIDOC_ROW);
    }

    private String render(Object value) {
        return renderValue(value).replace("|", "\\|");
    }

    private String renderValue(Object value) {
        return renderValue(value, 0);
    }

    private String renderValue(Object value, int nestLevel) {
        return switch (value) {
            case null -> "";
            case List<?> list -> renderAsList(list, nestLevel, BulletStyle.ORDERED);
            case Set<?> set -> renderAsList(set, nestLevel, BulletStyle.UNORDERED);
            case Map<?, ?> map -> renderAsDescriptionList(map, nestLevel);
            default -> value.toString();
        };
    }

    private String renderAsList(Collection<?> collection, int nestLevel, BulletStyle bulletStyle) {
        return collection.isEmpty()
            ? ""
            : collection.stream()
            .map(it -> renderValue(it, nestLevel + 1))
            .map(it -> renderListElement(it, nestLevel, bulletStyle))
            .collect(joiningRenderedListElements(nestLevel));
    }

    private String renderAsDescriptionList(Map<?, ?> map, int nestLevel) {
        return map.isEmpty()
            ? ""
            : map.entrySet().stream()
            .map(it -> renderEntryValue(it, nestLevel + 1))
            .map(it -> renderDescriptionListElement(it, nestLevel))
            .collect(joiningRenderedListElements(nestLevel));
    }

    private Map.Entry<?, String> renderEntryValue(Map.Entry<?, ?> entry, int nextNestLevel) {
        return Map.entry(entry.getKey(), renderValue(entry.getValue(), nextNestLevel));
    }

    private static Collector<CharSequence, ?, String> joiningRenderedListElements(int nestLevel) {
        return Collectors.joining("\n", "\n", nestLevel == 0 ? "\n" : "");
    }

    private static String renderListElement(String renderedValue, int nestLevel, BulletStyle bulletStyle) {
        return indentedBullet(nestLevel, bulletStyle) + (renderedValue.contains("\n") ? " {empty}" : " ") + renderedValue;
    }

    private static String renderDescriptionListElement(Map.Entry<?, String> entryWithRenderedValue, int nestLevel) {
        Object key = entryWithRenderedValue.getKey();
        String renderedValue = entryWithRenderedValue.getValue();
        String keyValueSeparator = (renderedValue.isEmpty() || renderedValue.startsWith("\n")) ? "" : " ";

        return indentedBullet(nestLevel, BulletStyle.DESCRIPTION, key) + keyValueSeparator + renderedValue;
    }

    private static String indentedBullet(int nestLevel, BulletStyle bulletStyle) {
        return indentedBullet(nestLevel, bulletStyle, "");
    }

    private static String indentedBullet(int nestLevel, BulletStyle bulletStyle, Object key) {
        return "  ".repeat(nestLevel) + key + bulletStyle.getBullet(nestLevel);
    }

    enum BulletStyle {
        ORDERED("."),
        UNORDERED("*"),
        DESCRIPTION("::");

        private static final List<String> descriptionDelimiters = List.of("::", ":::", "::::", ";;");

        private final String bullet;

        BulletStyle(String bullet) {
            this.bullet = bullet;
        }

        public String getBullet(int nestLevel) {
            return this == DESCRIPTION
                ? descriptionDelimiters.get(nestLevel % descriptionDelimiters.size())
                : bullet.repeat(nestLevel + 1);
        }
    }
}
