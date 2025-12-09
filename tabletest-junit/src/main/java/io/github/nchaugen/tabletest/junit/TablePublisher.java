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

import io.github.nchaugen.tabletest.parser.Table;
import io.github.nchaugen.tabletest.renderer.AsciidocTableRenderer;
import io.github.nchaugen.tabletest.renderer.AsciidocTestIndexRenderer;
import io.github.nchaugen.tabletest.renderer.MarkdownTableRenderer;
import io.github.nchaugen.tabletest.renderer.MarkdownTestIndexRenderer;
import io.github.nchaugen.tabletest.renderer.TableFileEntry;
import io.github.nchaugen.tabletest.renderer.TableMetadata;
import io.github.nchaugen.tabletest.renderer.TableRenderer;
import io.github.nchaugen.tabletest.renderer.TestIndexRenderer;
import io.github.nchaugen.tabletest.renderer.YamlTableRenderer;
import io.github.nchaugen.tabletest.renderer.YamlTestRenderer;
import org.junit.jupiter.api.extension.MediaType;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

public class TablePublisher implements AfterAllCallback {

    private static final TableRenderer MARKDOWN_TABLE_RENDERER = new MarkdownTableRenderer();
    private static final TestIndexRenderer MARKDOWN_TEST_INDEX_RENDERER = new MarkdownTestIndexRenderer();
    private static final TestIndexRenderer ASCIIDOC_TEST_INDEX_RENDERER = new AsciidocTestIndexRenderer();
    private static final TestIndexRenderer YAML_TEST_INDEX_RENDERER = new YamlTestRenderer();
    private static final TableRenderer YAML_TABLE_RENDERER = new YamlTableRenderer();

    enum Format {
        TABLETEST(".table"),
        MARKDOWN(".md"),
        ASCIIDOC(".adoc"),
        YAML(".yaml");


        private final String extension;

        Format(String extension) {
            this.extension = extension;
        }

        void publishFile(ExtensionContext context, String fileName, Function<Path, String> renderer) {
            context.publishFile(
                fileName + extension,
                MediaType.TEXT_PLAIN_UTF_8,
                path -> Files.writeString(path, renderer.apply(path))
            );
        }
    }


    public static void publishTable(ExtensionContext context, TableTest tableTest, Table table) {
        Format format = getPublisherFormat(context);
        TableMetadata metadata = new JunitTableMetadata(context, table);

        TableRenderer renderer = switch (format) {
            case TABLETEST -> (__, ___) -> InputResolver.resolveInput(context, tableTest);
            case MARKDOWN -> MARKDOWN_TABLE_RENDERER;
            case ASCIIDOC -> new AsciidocTableRenderer(new ConfiguredAsciidocStyle(context));
            case YAML -> YAML_TABLE_RENDERER;
        };

        format.publishFile(
            context, metadata.title(), (Path path) -> {
                TableFileIndex.save(metadata.title(), path, context);
                return renderer.render(table, metadata);
            }
        );
    }

    @Override
    public void afterAll(ExtensionContext context) {
        publishTestIndex(context);
    }

    public static void publishTestIndex(ExtensionContext context) {
        Format format = getPublisherFormat(context);
        TestIndexRenderer renderer = switch (format) {
            case TABLETEST -> (__, ___, ____) -> "";
            case MARKDOWN -> MARKDOWN_TEST_INDEX_RENDERER;
            case ASCIIDOC -> ASCIIDOC_TEST_INDEX_RENDERER;
            case YAML -> YAML_TEST_INDEX_RENDERER;
        };

        format.publishFile(
            context, context.getDisplayName(), (Path path) ->
                renderer.render(
                    context.getDisplayName(),
                    findDescription(context),
                    relativizeToIndex(path, TableFileIndex.allForTestClass(context))
                )
        );
    }

    private static Format getPublisherFormat(ExtensionContext context) {
        return context.getConfigurationParameter("tabletest.publisher.format")
            .filter(it -> !it.isBlank())
            .map(it -> it.strip().toUpperCase())
            .map(Format::valueOf)
            .orElse(Format.YAML);
    }

    private static List<TableFileEntry> relativizeToIndex(Path indexPath, List<TableFileEntry> tableFiles) {
        return tableFiles.stream()
            .map(it -> new TableFileEntry(it.title(), indexPath.getParent().relativize(it.path())))
            .toList();
    }

    private static String findDescription(ExtensionContext context) {
        return context.getTestClass()
            .map(it -> it.getAnnotation(Description.class))
            .map(Description::value)
            .orElse(null);
    }

}
