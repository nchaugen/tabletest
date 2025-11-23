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
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public record AsciidocRenderer(AsciidocStyle style) implements TableRenderer {

    private static final String EMPTY = "{empty}";
    private static final String NEWLINE = "\n";
    private static final String NEEDS_ENCODING = "(^ )|( $)|(\t+)|([ \t]{2,})|([+]+)";

    @Override
    public String render(Table table, ColumnRoles columnRoles, ExtensionContext context) {
        return String.join(
            NEWLINE,
            "== " + asLiteral(context.getDisplayName()),
            "",
            table.headers().stream().map(AsciidocRenderer::columnSpecifier).collect(ASCIIDOC_ATTRIBUTE_LIST),
            table.header().mapIndexed((i, header) -> render(header, columnRoles.roleFor(i))).collect(ASCIIDOC_HEADER_ROW),
            table.rows().stream().map(row -> render(row, columnRoles)).collect(MULTILINE)
        );
    }

    private static final Collector<CharSequence, ?, String>
        ASCIIDOC_ATTRIBUTE_LIST = Collectors.joining(",", "[%header,cols=\"", "\"]\n|===");

    private static final Collector<CharSequence, ?, String>
        ASCIIDOC_HEADER_ROW = Collectors.joining("\n|", "|", NEWLINE);

    private static final Collector<CharSequence, ?, String>
        ASCIIDOC_ROW = Collectors.joining("\na|", "a|", NEWLINE);

    private static final Collector<CharSequence, ?, String>
        MULTILINE = Collectors.joining(NEWLINE, "", "|===\n");

    private static String columnSpecifier(String header) {
        return "1";
    }

    private String render(Row row, ColumnRoles columnRoles) {
        return row
            .mapIndexed((i, value) -> render(value, columnRoles.roleFor(i)))
            .collect(ASCIIDOC_ROW);
    }

    private String render(Object value, CellRole role) {
        return renderValue(value, 0, role).replace("|", "\\|");
    }

    private String renderValue(Object value, int nestLevel, CellRole role) {
        return switch (value) {
            case null -> withRole("", role);
            case List<?> list -> renderCollection(list, nestLevel, style.listFormat(), role);
            case Set<?> set -> renderCollection(set, nestLevel, style.setFormat(), role);
            case Map<?, ?> map -> renderDictionary(map, nestLevel, style.mapFormat(), role);
            default -> withRole(asLiteral(value), role);
        };
    }

    private String renderCollection(Collection<?> collection, int nestLevel, AsciidocListFormat format, CellRole role) {
        if (collection.isEmpty()) {
            return withRole(EMPTY, role);
        }

        return format.getStyle(nestLevel) + collection.stream()
            .map(it -> renderValue(it, nestLevel + 1, CellRole.NORMAL))
            .map(it -> renderListElement(it, nestLevel, format))
            .collect(joiningRenderedListElements(nestLevel, role));
    }

    private String renderDictionary(Map<?, ?> map, int nestLevel, AsciidocListFormat format, CellRole role) {
        if (map.isEmpty()) {
            return withRole(EMPTY, role);
        }

        return map.entrySet().stream()
            .map(it -> renderEntryValue(it, nestLevel + 1))
            .map(it -> renderDescriptionListElement(it, nestLevel, format))
            .collect(joiningRenderedListElements(nestLevel, role));
    }

    private Map.Entry<?, String> renderEntryValue(Map.Entry<?, ?> entry, int nextNestLevel) {
        return Map.entry(entry.getKey(), renderValue(entry.getValue(), nextNestLevel, CellRole.NORMAL));
    }

    private static Collector<CharSequence, ?, String> joiningRenderedListElements(int nestLevel, CellRole role) {
        String prefix = role == CellRole.NORMAL ? NEWLINE : roleMarker(role) + NEWLINE;
        return Collectors.joining(NEWLINE, prefix, nestLevel == 0 ? NEWLINE : "");
    }

    private static String renderListElement(String renderedValue, int nestLevel, AsciidocListFormat format) {
        return indentedBullet(nestLevel, format) + (renderedValue.contains(NEWLINE) ? " " + EMPTY : " ") + renderedValue;
    }

    private static String renderDescriptionListElement(Map.Entry<?, String> entryWithRenderedValue, int nestLevel, AsciidocListFormat format) {
        String key = asLiteral(entryWithRenderedValue.getKey());
        String renderedValue = entryWithRenderedValue.getValue();
        String keyValueSeparator = (renderedValue.isEmpty() || renderedValue.startsWith(NEWLINE)) ? "" : " ";

        return indentedBullet(nestLevel, format, key) + keyValueSeparator + renderedValue;
    }

    private static String indentedBullet(int nestLevel, AsciidocListFormat format) {
        return indentedBullet(nestLevel, format, "");
    }

    private static String indentedBullet(int nestLevel, AsciidocListFormat format, Object key) {
        return "  ".repeat(nestLevel) + key + format.getBullet(nestLevel);
    }

    private static String asLiteral(Object value) {
        return asLiteral(value.toString());
    }

    private static String asLiteral(String value) {
        if (value.isEmpty()) return "+\"\"+";

        return Arrays.stream(value.splitWithDelimiters(NEEDS_ENCODING, -1))
            .filter(it -> !it.isEmpty())
            .map(it -> it.matches(NEEDS_ENCODING) ? encodeCharacters(it) : "++" + it + "++")
            .collect(Collectors.joining());
    }

    private static String encodeCharacters(String delimiter) {
        return delimiter
            .replace("+", "&#43;")
            .replace(" ", "&#x2423;")
            .replace("\t", "&#x21E5;");
    }

    private static String withRole(String content, CellRole role) {
        if (role == CellRole.NORMAL) {
            return content;
        }
        if (content.isEmpty()) {
            return roleMarker(role);
        }
        return roleMarker(role) + "#" + content + "#";
    }

    private static String roleMarker(CellRole role) {
        return "[." + role.cssClass() + "]";
    }

}
