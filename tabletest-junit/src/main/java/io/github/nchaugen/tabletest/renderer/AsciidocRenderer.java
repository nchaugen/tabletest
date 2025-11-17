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
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public class AsciidocRenderer implements TableRenderer {

    private static final String CONFIG_PREFIX = "tabletest.publisher.asciidoc.";
    private final ListFormat setFormat;
    private final ListFormat listFormat;
    private final ListFormat mapFormat;

    public AsciidocRenderer(ExtensionContext context) {
        ListType listType = getListTypeOverride(context, "list").orElse(ListType.ORDERED);
        ListType setType = getListTypeOverride(context, "set").orElse(ListType.UNORDERED);
        List<String> listStyle = getListStyleOverride(context, "list").orElse(emptyList());
        List<String> setStyle = getListStyleOverride(context, "set").orElse(emptyList());

        listFormat = new ListFormat(listType, listStyle);
        setFormat = new ListFormat(setType, setStyle);
        mapFormat = new ListFormat(ListType.DESCRIPTION, emptyList());
    }

    @Override
    public String render(Table table) {
        return String.join(
            "\n",
            table.headers().stream().map(AsciidocRenderer::columnSpecifier).collect(ASCIIDOC_ATTRIBUTE_LIST),
            table.headers().stream().map(header -> render(header, isExpectation(header))).collect(ASCIIDOC_HEADER_ROW),
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

    private static String columnSpecifier(String header) {
        return "1";
    }

    private String render(Row row) {
        return row
            .mapWithHeader((header, value) -> render(value, isExpectation(header)))
            .collect(ASCIIDOC_ROW);
    }

    private boolean isExpectation(String header) {
        return header.trim().endsWith("?");
    }

    private String render(Object value, boolean isExpectation) {
        return renderValue(value, isExpectation).replace("|", "\\|");
    }

    private String renderValue(Object value, boolean isExpectation) {
        return renderValue(value, 0, isExpectation);
    }

    private String renderValue(Object value, int nestLevel, boolean isExpectation) {
        return switch (value) {
            case null -> isExpectation ? "[.expectation]" : "";
            case List<?> list -> renderAsList(list, nestLevel, listFormat, isExpectation);
            case Set<?> set -> renderAsList(set, nestLevel, setFormat, isExpectation);
            case Map<?, ?> map -> renderAsDescriptionList(map, nestLevel, mapFormat, isExpectation);
            default -> isExpectation ? "[.expectation]#+" + value + "+#" : "+" + value + "+";
        };
    }

    private String renderAsList(Collection<?> collection, int nestLevel, ListFormat format, boolean isExpectation) {
        return collection.isEmpty()
            ? isExpectation ? "[.expectation]#{empty}#" : "{empty}"
            : format.getStyle(nestLevel) + collection.stream()
            .map(it -> renderValue(it, nestLevel + 1, false))
            .map(it -> renderListElement(it, nestLevel, format))
            .collect(joiningRenderedListElements(nestLevel, isExpectation));
    }

    private String renderAsDescriptionList(Map<?, ?> map, int nestLevel, ListFormat format, boolean isExpectation) {
        return map.isEmpty()
            ? isExpectation ? "[.expectation]#{empty}#" : "{empty}"
            : map.entrySet().stream()
            .map(it -> renderEntryValue(it, nestLevel + 1))
            .map(it -> renderDescriptionListElement(it, nestLevel, format))
            .collect(joiningRenderedListElements(nestLevel, isExpectation));
    }

    private Map.Entry<?, String> renderEntryValue(Map.Entry<?, ?> entry, int nextNestLevel) {
        return Map.entry(entry.getKey(), renderValue(entry.getValue(), nextNestLevel, false));
    }

    private static Collector<CharSequence, ?, String> joiningRenderedListElements(int nestLevel, boolean isExpectation) {
        return Collectors.joining("\n",  (isExpectation ? "[.expectation]\n" : "\n"), nestLevel == 0 ? "\n" : "");
    }

    private static String renderListElement(String renderedValue, int nestLevel, ListFormat format) {
        return indentedBullet(nestLevel, format) + (renderedValue.contains("\n") ? " {empty}" : " ") + renderedValue;
    }

    private static String renderDescriptionListElement(Map.Entry<?, String> entryWithRenderedValue, int nestLevel, ListFormat format) {
        Object key = "+" + entryWithRenderedValue.getKey() + "+";
        String renderedValue = entryWithRenderedValue.getValue();
        String keyValueSeparator = (renderedValue.isEmpty() || renderedValue.startsWith("\n")) ? "" : " ";

        return indentedBullet(nestLevel, format, key) + keyValueSeparator + renderedValue;
    }

    private static String indentedBullet(int nestLevel, ListFormat format) {
        return indentedBullet(nestLevel, format, "");
    }

    private static String indentedBullet(int nestLevel, ListFormat format, Object key) {
        return "  ".repeat(nestLevel) + key + format.getBullet(nestLevel);
    }

    record ListFormat(ListType type, List<String> style) {
        private static final List<String> descriptionDelimiters = List.of("::", ":::", "::::", ";;");

        public String getBullet(int nestLevel) {
            return type == ListType.DESCRIPTION
                ? descriptionDelimiters.get(nestLevel % descriptionDelimiters.size())
                : type.bullet.repeat(nestLevel + 1);
        }

        public String getStyle(int nestLevel) {
            return style.isEmpty() || nestLevel >= style.size() ? "" : "\n[" + style.get(nestLevel) + "]";
        }
    }

    enum ListType {
        ORDERED("."),
        UNORDERED("*"),
        DESCRIPTION("::");

        final String bullet;

        ListType(String bullet) {
            this.bullet = bullet;
        }
    }

    private Optional<ListType> getListTypeOverride(ExtensionContext context, String collectionType) {
        return context.getConfigurationParameter(CONFIG_PREFIX + collectionType + ".type")
            .map(this::parseListType);
    }

    private ListType parseListType(String configValue) {
        return switch (configValue.trim().toLowerCase()) {
            case "ordered" -> ListType.ORDERED;
            case "unordered" -> ListType.UNORDERED;
            default -> null;
        };
    }

    private Optional<List<String>> getListStyleOverride(ExtensionContext context, String collectionType) {
        return context.getConfigurationParameter(CONFIG_PREFIX + collectionType + ".style")
            .map(this::parseListStyle);
    }

    private List<String> parseListStyle(String configValue) {
        return Arrays.asList(configValue.toLowerCase().split("\\s*,\\s*"));
    }
}
