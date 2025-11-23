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
import io.github.nchaugen.tabletest.renderer.ConfiguredAsciidocStyle;
import io.github.nchaugen.tabletest.renderer.AsciidocRenderer;
import io.github.nchaugen.tabletest.renderer.MarkdownRenderer;
import io.github.nchaugen.tabletest.renderer.TableMetadata;
import io.github.nchaugen.tabletest.renderer.TableRenderer;
import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.nio.file.Files;
import java.util.function.BiConsumer;

import static io.github.nchaugen.tabletest.junit.InputResolver.resolveInput;

public class TablePublisher {

    private static final MarkdownRenderer MARKDOWN_RENDERER = new MarkdownRenderer();

    public static void publishTable(ExtensionContext context, TableTest tableTest, Table table) {
        TableMetadata metadata = new JunitTableMetadata(context, table);

        BiConsumer<String, TableRenderer> filePublisher = (String extension, TableRenderer renderer) ->
            context.publishFile(
                context.getDisplayName() + extension,
                MediaType.TEXT_PLAIN_UTF_8,
                path -> Files.writeString(path, renderer.render(table, metadata))
            );

        context.getConfigurationParameter("tabletest.publisher.format")
            .ifPresent(format -> {
                switch (format.strip().toLowerCase()) {
                    case "" -> {} // do nothing if the config parameter is present without value
                    case "tabletest" -> filePublisher.accept(".table", (__, ___) -> resolveInput(context, tableTest));
                    case "markdown" -> filePublisher.accept(".md", MARKDOWN_RENDERER);
                    case "asciidoc" -> filePublisher.accept(".adoc", new AsciidocRenderer(new ConfiguredAsciidocStyle(context)));
                    default -> throw new IllegalArgumentException("`" + format + "` not among supported table publisher formats [tabletest, markdown, asciidoc]");
                }
            });

    }

}
