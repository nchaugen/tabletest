package io.github.nchaugen.tabletest.renderer;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfiguredAsciidocStyleTest {

    @Test
    void shouldUseDefaultConfigIfNoOverrides() {
        ConfiguredAsciidocStyle defaultConfig = new ConfiguredAsciidocStyle(new StubExtensionContext());

        assertEquals(AsciidocListFormat.ordered(), defaultConfig.listFormat());
        assertEquals(AsciidocListFormat.unordered(), defaultConfig.setFormat());
        assertEquals(AsciidocListFormat.description(), defaultConfig.mapFormat());
    }

    @Test
    void shouldAllowListTypeToBeOverridden() {
        assertEquals(
            AsciidocListFormat.unordered(),
            new ConfiguredAsciidocStyle(new StubExtensionContext(
                Map.of("tabletest.publisher.asciidoc.list.type", "unordered")
            )).listFormat()
        );
    }

    @Test
    void shouldUseDefaultListTypeIfOverrideUnknown() {
        assertEquals(
            AsciidocListFormat.ordered(),
            new ConfiguredAsciidocStyle(new StubExtensionContext(
                Map.of("tabletest.publisher.asciidoc.list.type", "bullet")
            )).listFormat()
        );
    }

    @Test
    void shouldAllowSetTypeToBeOverridden() {
        assertEquals(
            AsciidocListFormat.ordered(),
            new ConfiguredAsciidocStyle(new StubExtensionContext(
                Map.of("tabletest.publisher.asciidoc.set.type", "ordered")
            )).setFormat()
        );
    }

    @Test
    void shouldUseDefaultSetTypeIfOverrideUnknown() {
        assertEquals(
            AsciidocListFormat.unordered(),
            new ConfiguredAsciidocStyle(new StubExtensionContext(
                Map.of("tabletest.publisher.asciidoc.set.type", "numbered")
            )).setFormat()
        );
    }

    @Test
    void shouldAllowListStyleToBeOverridden() {
        assertEquals(
            AsciidocListFormat.unordered("square"),
            new ConfiguredAsciidocStyle(new StubExtensionContext(
                Map.of(
                    "tabletest.publisher.asciidoc.list.type", "unordered",
                    "tabletest.publisher.asciidoc.list.style", "square"
                )
            )).listFormat()
        );
    }

    @Test
    void shouldAllowSetStyleToBeOverridden() {
        assertEquals(
            AsciidocListFormat.unordered("circle", "disc"),
            new ConfiguredAsciidocStyle(new StubExtensionContext(
                Map.of(
                    "tabletest.publisher.asciidoc.set.style", "circle,disc"
                )
            )).setFormat()
        );
    }

    @Test
    void shouldAllowSetTypeAndStyleToBeOverridden() {
        assertEquals(
            AsciidocListFormat.ordered("lowergreek"),
            new ConfiguredAsciidocStyle(new StubExtensionContext(
                Map.of(
                    "tabletest.publisher.asciidoc.set.type", "ordered",
                    "tabletest.publisher.asciidoc.set.style", "lowergreek"
                )
            )).setFormat()
        );
    }
}