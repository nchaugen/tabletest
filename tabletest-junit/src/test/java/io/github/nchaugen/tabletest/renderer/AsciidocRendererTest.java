package io.github.nchaugen.tabletest.renderer;

import io.github.nchaugen.tabletest.parser.TableParser;
import org.junit.jupiter.api.MediaType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExecutableInvoker;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstances;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AsciidocRendererTest {

    private final AsciidocRenderer renderer = new AsciidocRenderer(new StubExtensionContext());

    @Test
    void shouldAddRoleForExpectationCells() {
        assertEquals(
            """
                [%header,cols="1,1,1,1,1"]
                |===
                |[.expectation]#+a?+#
                |[.expectation]#+b?+#
                |[.expectation]#+c?+#
                |[.expectation]#+d?+#
                |[.expectation]#+e?+#
                
                a|[.expectation]#{empty}#
                a|[.expectation]
                . +1+
                . +2+
                . +3+
                
                a|[.expectation]#+3+#
                a|[.expectation]
                a|[.expectation]
                +a+:: +1+
                +b+:: +2+
                +c+:: +3+

                |===
                """,
            renderer.render(TableParser.parse("""
                a? | b?      | c? | d? | e?
                {} | [1,2,3] | 3  |    | [a:1,b:2,c:3]
                """))
        );
    }

    @Test
    void shouldRenderNull() {
        assertEquals(
            """
                [%header,cols="1,1,1"]
                |===
                |+a+
                |+b+
                |+c+
                
                a|+1+
                a|
                a|+3+
                |===
                """,
            renderer.render(TableParser.parse("""
                a | b | c
                1 |   | 3
                """))
        );
    }

    @Test
    void shouldRenderEscapedPipe() {
        assertEquals(
            """
                [%header,cols="1,1,1"]
                |===
                |+a+
                |+b+
                |+a\\|b+
                
                a|+\\|+
                a|+\\|+
                a|+Text with \\| character+
                |===
                """,
            renderer.render(TableParser.parse("""
                a   | b   | 'a|b'
                "|" | '|' | "Text with | character"
                """))
        );
    }

    @Test
    void shouldRenderListAsOrderedList() {
        assertEquals(
            """
                [%header,cols="1,1,1"]
                |===
                |+a+
                |+b+
                |+c+
                
                a|{empty}
                a|
                . +1+
                . +2+
                . +3+
                
                a|
                . +\\|+
                . +\\|+
                
                |===
                """,
            renderer.render(TableParser.parse("""
                a  | b         | c
                [] | [1,2,3] | ['|', "|"]
                """))
        );
    }

    @Test
    void shouldRenderEmptyList() {
        assertEquals(
            """
                [%header,cols="1,1,1"]
                |===
                |+a+
                |+b+
                |+c+
                
                a|{empty}
                a|
                . {empty}
                
                a|
                . {empty}
                  .. {empty}
                
                |===
                """,
            renderer.render(TableParser.parse("""
                a  | b    | c
                [] | [[]] | [[[]]]
                """))
        );
    }

    @Test
    void shouldRenderNestedLists() {
        assertEquals(
            """
                [%header,cols="1"]
                |===
                |+a+
                
                a|
                . {empty}
                  .. +1+
                  .. +2+
                  .. +3+
                . {empty}
                  .. +a+
                  .. +b+
                  .. +c+
                . {empty}
                  .. +#+
                  .. +$+
                  .. +%+
                
                |===
                """,
            renderer.render(TableParser.parse("""
                a
                [[1,2,3],[a,b,c],[#,$,%]]
                """))
        );
    }

    @Test
    void shouldRenderListAsConfiguredListType() {
        AsciidocRenderer configuredRenderer = new AsciidocRenderer(new StubExtensionContext(
            Map.of("tabletest.publisher.asciidoc.list.type", "unordered")
        ));

        assertEquals(
            """
                [%header,cols="1,1,1"]
                |===
                |+a+
                |+b+
                |+c+
                
                a|{empty}
                a|
                * +1+
                * +2+
                * +3+
                
                a|
                * +\\|+
                * +\\|+
                
                |===
                """,
            configuredRenderer.render(TableParser.parse("""
                a  | b         | c
                [] | [1,2,3] | ['|', "|"]
                """))
        );
    }

    @Test
    void shouldRenderListAsDefaultTypeIfConfigValueUnknown() {
        AsciidocRenderer configuredRenderer = new AsciidocRenderer(new StubExtensionContext(
            Map.of("tabletest.publisher.asciidoc.list.type", "bullet")
        ));

        assertEquals(
            """
                [%header,cols="1,1,1"]
                |===
                |+a+
                |+b+
                |+c+
                
                a|{empty}
                a|
                . +1+
                . +2+
                . +3+
                
                a|
                . +\\|+
                . +\\|+
                
                |===
                """,
            configuredRenderer.render(TableParser.parse("""
                a  | b         | c
                [] | [1,2,3] | ['|', "|"]
                """))
        );
    }

    @Test
    void shouldRenderNestedListsAsConfigured() {
        AsciidocRenderer configuredRenderer = new AsciidocRenderer(new StubExtensionContext(
            Map.of(
                "tabletest.publisher.asciidoc.list.type", "unordered",
                "tabletest.publisher.asciidoc.list.style", "square,none"
            )
        ));

        assertEquals(
            """
                [%header,cols="1"]
                |===
                |+a+
                
                a|
                [square]
                * {empty}
                [none]
                  ** +1+
                  ** +2+
                  ** +3+
                * {empty}
                [none]
                  ** +a+
                  ** +b+
                  ** +c+
                * {empty}
                [none]
                  ** +#+
                  ** +$+
                  ** +%+
                
                |===
                """,
            configuredRenderer.render(TableParser.parse("""
                a
                [[1,2,3],[a,b,c],[#,$,%]]
                """))
        );
    }

    @Test
    void shouldRenderSetAsUnorderedList() {
        assertEquals(
            """
                [%header,cols="1,1,1"]
                |===
                |+a+
                |+b+
                |+c+
                
                a|{empty}
                a|
                * +1+
                * +2+
                * +3+
                
                a|
                * +\\|\\|+
                
                |===
                """,
            renderer.render(TableParser.parse("""
                a  | b   | c
                {} | {1,2,3} | {"||"}
                """))
        );
    }

    @Test
    void shouldRenderNestedSets() {
        assertEquals(
            """
                [%header,cols="1"]
                |===
                |+a+
                
                a|
                * {empty}
                  ** +1+
                  ** +2+
                  ** +3+
                * {empty}
                  ** +a+
                  ** +b+
                  ** +c+
                * {empty}
                  ** +#+
                  ** +$+
                  ** +%+
                
                |===
                """,
            renderer.render(TableParser.parse("""
                a
                {{1,2,3}, {a,b,c}, {#,$,%}}
                """))
        );
    }

    @Test
    void shouldRenderSetAsConfiguredListType() {
        AsciidocRenderer configuredRenderer = new AsciidocRenderer(new StubExtensionContext(
            Map.of(
                "tabletest.publisher.asciidoc.set.type", "ordered",
                "tabletest.publisher.asciidoc.set.style", "lowergreek"
            )
        ));

        assertEquals(
            """
                [%header,cols="1,1"]
                |===
                |+a+
                |+b+
                
                a|{empty}
                a|
                [lowergreek]
                . {empty}
                  .. +1+
                . {empty}
                  .. +2+
                  .. +3+
                
                |===
                """,
            configuredRenderer.render(TableParser.parse("""
                a  | b
                {} | {{1},{2,3}}
                """))
        );
    }

    @Test
    void shouldRenderNestedSetsAsConfigured() {
        AsciidocRenderer configuredRenderer = new AsciidocRenderer(new StubExtensionContext(
            Map.of(
                "tabletest.publisher.asciidoc.set.type", "unordered",
                "tabletest.publisher.asciidoc.set.style", "square,none"
            )
        ));

        assertEquals(
            """
                [%header,cols="1"]
                |===
                |+a+
                
                a|
                [square]
                * {empty}
                [none]
                  ** +1+
                  ** +2+
                  ** +3+
                * {empty}
                [none]
                  ** +a+
                  ** +b+
                  ** +c+
                * {empty}
                [none]
                  ** +#+
                  ** +$+
                  ** +%+
                
                |===
                """,
            configuredRenderer.render(TableParser.parse("""
                a
                {{1,2,3}, {a,b,c}, {#,$,%}}
                """))
        );
    }

    @Test
    void shouldRenderMapAsDescriptionList() {
        assertEquals(
            """
                [%header,cols="1,1,1"]
                |===
                |+a+
                |+b+
                |+c+
                
                a|{empty}
                a|
                +a+:: +1+
                +b+:: +2+
                +c+:: +3+
                
                a|
                +b+:: +\\|\\|+
                
                |===
                """,
            renderer.render(TableParser.parse("""
                a   | b             | c
                [:] | [a:1,b:2,c:3] | [b: "||"]
                """))
        );
    }

    @Test
    void shouldRenderNestedMaps() {
        assertEquals(
            """
                [%header,cols="1,1"]
                |===
                |+a+
                |+b+
                
                a|
                +a+:: {empty}
                +b+:: {empty}
                
                a|
                +a+::
                  +A+::: +1+
                +b+::
                  +B+::: +2+
                
                |===
                """,
            renderer.render(TableParser.parse("""
                a                | b
                [a: [:], b: [:]] | [a: [A: 1],b: [B: 2]]
                """))
        );
    }

    @Test
    void shouldRenderNestedMixedCollections() {
        assertEquals(
            """
                [%header,cols="1,1"]
                |===
                |+a+
                |+b+
                
                a|
                +a+::
                  .. +1+
                  .. +2+
                +b+::
                  ** +3+
                  ** +4+
                +c+:: +5+
                
                a|
                * {empty}
                  +A+::: +1+
                * {empty}
                  +B+::: +2+
                
                |===
                """,
            renderer.render(TableParser.parse("""
                a                            | b
                [a: [1, 2], b: {3, 4}, c: 5] | {[A: 1], [B: 2]}
                """))
        );
    }

    @Test
    void shouldRenderNestedMixedCollectionsAsConfigured() {
        AsciidocRenderer configuredRenderer = new AsciidocRenderer(new StubExtensionContext(
            Map.of(
                "tabletest.publisher.asciidoc.list.type", "unordered",
                "tabletest.publisher.asciidoc.list.style", "square",
                "tabletest.publisher.asciidoc.set.style", "circle,disc"
            )
        ));


        assertEquals(
            """
                [%header,cols="1,1"]
                |===
                |+a+
                |+b+
                
                a|
                +a+::
                  ** +1+
                  ** +2+
                +b+::
                [disc]
                  ** +3+
                  ** +4+
                +c+:: +5+
                
                a|
                [circle]
                * {empty}
                  +A+::: +1+
                * {empty}
                  +B+::: +2+
                
                |===
                """,
            configuredRenderer.render(TableParser.parse("""
                a                            | b
                [a: [1, 2], b: {3, 4}, c: 5] | {[A: 1], [B: 2]}
                """))
        );
    }

    @SuppressWarnings("NullableProblems")
    private record StubExtensionContext(Map<String, String> config) implements ExtensionContext {

        public StubExtensionContext() {
            this(Collections.emptyMap());
        }

        @Override
        public Optional<ExtensionContext> getParent() {
            return Optional.empty();
        }

        @Override
        public ExtensionContext getRoot() {
            return null;
        }

        @Override
        public String getUniqueId() {
            return "";
        }

        @Override
        public String getDisplayName() {
            return "";
        }

        @Override
        public Set<String> getTags() {
            return Set.of();
        }

        @Override
        public Optional<AnnotatedElement> getElement() {
            return Optional.empty();
        }

        @Override
        public Optional<Class<?>> getTestClass() {
            return Optional.empty();
        }

        @Override
        public List<Class<?>> getEnclosingTestClasses() {
            return List.of();
        }

        @Override
        public Optional<TestInstance.Lifecycle> getTestInstanceLifecycle() {
            return Optional.empty();
        }

        @Override
        public Optional<Object> getTestInstance() {
            return Optional.empty();
        }

        @Override
        public Optional<TestInstances> getTestInstances() {
            return Optional.empty();
        }

        @Override
        public Optional<Method> getTestMethod() {
            return Optional.empty();
        }

        @Override
        public Optional<Throwable> getExecutionException() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getConfigurationParameter(String key) {
            return Optional.ofNullable(config.get(key));
        }

        @Override
        public <T> Optional<T> getConfigurationParameter(String key, Function<? super String, ? extends T> transformer) {
            return Optional.empty();
        }

        @Override
        public void publishReportEntry(Map<String, String> map) {

        }

        @Override
        public void publishFile(String name, MediaType mediaType, ThrowingConsumer<Path> action) {

        }

        @Override
        public void publishDirectory(String name, ThrowingConsumer<Path> action) {

        }

        @Override
        public Store getStore(Namespace namespace) {
            return null;
        }

        @Override
        public Store getStore(StoreScope scope, Namespace namespace) {
            return null;
        }

        @Override
        public ExecutionMode getExecutionMode() {
            return null;
        }

        @Override
        public ExecutableInvoker getExecutableInvoker() {
            return null;
        }
    }
}
