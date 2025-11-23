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
package io.github.nchaugen.tabletest.junit;

import io.github.nchaugen.tabletest.renderer.AsciidocListFormat;
import io.github.nchaugen.tabletest.renderer.AsciidocListType;
import io.github.nchaugen.tabletest.renderer.AsciidocStyle;
import io.github.nchaugen.tabletest.renderer.DefaultAsciidocStyle;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class ConfiguredAsciidocStyle implements AsciidocStyle {
    private static final AsciidocStyle DEFAULTS = new DefaultAsciidocStyle();
    private static final String CONFIG_PREFIX = "tabletest.publisher.asciidoc.";

    private final AsciidocListFormat setFormat;
    private final AsciidocListFormat listFormat;
    private final AsciidocListFormat mapFormat;

    public ConfiguredAsciidocStyle(ExtensionContext context) {
        listFormat = new AsciidocListFormat(
            getListTypeOverride(context, "list").orElse(DEFAULTS.listFormat().type()),
            getListStyleOverride(context, "list").orElse(DEFAULTS.listFormat().style())
        );
        setFormat = new AsciidocListFormat(
            getListTypeOverride(context, "set").orElse(DEFAULTS.setFormat().type()),
            getListStyleOverride(context, "set").orElse(DEFAULTS.setFormat().style())
        );
        mapFormat = new AsciidocListFormat(
            DEFAULTS.mapFormat().type(),
            DEFAULTS.mapFormat().style()
        );
    }

    @Override
    public AsciidocListFormat listFormat() {
        return listFormat;
    }

    @Override
    public AsciidocListFormat setFormat() {
        return setFormat;
    }

    @Override
    public AsciidocListFormat mapFormat() {
        return mapFormat;
    }

    private Optional<AsciidocListType> getListTypeOverride(ExtensionContext context, String collectionType) {
        return context.getConfigurationParameter(CONFIG_PREFIX + collectionType + ".type")
            .map(this::parseListType);
    }

    private AsciidocListType parseListType(String configValue) {
        return switch (configValue.trim().toLowerCase()) {
            case "ordered" -> AsciidocListType.ORDERED;
            case "unordered" -> AsciidocListType.UNORDERED;
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
