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

import io.github.nchaugen.tabletest.junit.ConfiguredAsciidocStyle;
import io.github.nchaugen.tabletest.junit.Description;
import io.github.nchaugen.tabletest.junit.InputResolver;
import io.github.nchaugen.tabletest.junit.JunitTableMetadata;
import io.github.nchaugen.tabletest.junit.TableFileIndex;
import io.github.nchaugen.tabletest.junit.TableTest;
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
import org.junit.jupiter.api.extension.TestWatcher;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

public class TablePublisher implements TestWatcher, AfterAllCallback {

    private static final ExtensionContext.Namespace NAMESPACE =
        ExtensionContext.Namespace.create(TablePublisher.class);

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
        publishTable(context, tableTest, table, List.of());
    }

    public static void publishTable(ExtensionContext context, TableTest tableTest, Table table, List<RowResult> rowResults) {
        Format format = getPublisherFormat(context);
        TableMetadata metadata = new JunitTableMetadata(context, table, rowResults);

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
    public void testSuccessful(ExtensionContext context) {
        recordInvocationResult(context, true, null);
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        recordInvocationResult(context, false, cause);
    }

    private void recordInvocationResult(ExtensionContext context, boolean passed, Throwable cause) {
        // Get the parent context (test template context for parameterized tests)
        context.getParent().ifPresent(parentContext -> {
            parentContext.getTestMethod().ifPresent(method -> {
                TableTest tableTest = method.getAnnotation(TableTest.class);
                if (tableTest != null) {
                    // Mark that this class has TableTest methods
                    getClassStore(parentContext).put("hasTableTests", true);

                    // Get invocation index
                    int rowIndex = getInvocationIndex(context);

                    // Store the row result
                    storeRowResult(parentContext, new RowResult(
                        rowIndex, passed, cause, context.getDisplayName()
                    ));

                    // Store the table and annotation for later republishing
                    ExtensionContext.Store store = getTestMethodStore(parentContext);
                    if (store.get("tableTest") == null) {
                        store.put("tableTest", tableTest);
                        String input = InputResolver.resolveInput(parentContext, tableTest);
                        Table table = io.github.nchaugen.tabletest.parser.TableParser.parse(input);
                        store.put("table", table);

                        // Add this method context to the class-level list for republishing
                        addMethodContextForRepublishing(parentContext);
                    }
                }
            });
        });
    }

    private static int getInvocationIndex(ExtensionContext context) {
        // Extract invocation index from the unique ID
        // Format: [engine:junit-jupiter]/[class:...]/[test-template:...]/[test-template-invocation:#N]
        String uniqueId = context.getUniqueId();
        int start = uniqueId.lastIndexOf("#");
        if (start >= 0 && start < uniqueId.length() - 2) {
            int end = uniqueId.indexOf("]", start);
            if (end > start) {
                try {
                    return Integer.parseInt(uniqueId.substring(start + 1, end));
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    private static void storeRowResult(ExtensionContext context, RowResult result) {
        ExtensionContext.Store store = getTestMethodStore(context);
        @SuppressWarnings("unchecked")
        List<RowResult> results = (List<RowResult>) store.getOrComputeIfAbsent(
            "rowResults",
            key -> new java.util.ArrayList<RowResult>()
        );
        results.add(result);
    }

    private static ExtensionContext.Store getTestMethodStore(ExtensionContext context) {
        return context.getStore(NAMESPACE);
    }

    private static ExtensionContext.Store getClassStore(ExtensionContext context) {
        return context.getRoot().getStore(ExtensionContext.Namespace.create(
            context.getRequiredTestClass()
        ));
    }

    private static void addMethodContextForRepublishing(ExtensionContext methodContext) {
        ExtensionContext.Store classStore = getClassStore(methodContext);
        @SuppressWarnings("unchecked")
        List<ExtensionContext> contexts = (List<ExtensionContext>) classStore.getOrComputeIfAbsent(
            "methodContexts",
            key -> new java.util.ArrayList<ExtensionContext>()
        );
        contexts.add(methodContext);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        // Republish all tables with collected row results
        republishTablesWithResults(context);

        // Publish test index
        publishTestIndex(context);
    }

    private void republishTablesWithResults(ExtensionContext context) {
        ExtensionContext.Store classStore = getClassStore(context);
        @SuppressWarnings("unchecked")
        List<ExtensionContext> methodContexts = (List<ExtensionContext>) classStore.get("methodContexts");

        if (methodContexts != null) {
            for (ExtensionContext methodContext : methodContexts) {
                ExtensionContext.Store methodStore = getTestMethodStore(methodContext);
                TableTest tableTest = (TableTest) methodStore.get("tableTest");
                Table table = (Table) methodStore.get("table");
                @SuppressWarnings("unchecked")
                List<RowResult> rowResults = (List<RowResult>) methodStore.get("rowResults");

                if (tableTest != null && table != null && rowResults != null) {
                    publishTable(methodContext, tableTest, table, rowResults);
                }
            }
        }
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
