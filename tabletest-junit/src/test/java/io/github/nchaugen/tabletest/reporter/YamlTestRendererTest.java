package io.github.nchaugen.tabletest.reporter;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class YamlTestRendererTest {

    private static final YamlTestRenderer renderer = new YamlTestRenderer();

    @Test
    void shouldRenderTestIndex() {
        assertEquals(
            """
                "title": "Title of the Test Class"
                "description": "A free-text description explaining what these tables are about."
                "tables":
                  "A Table": "path/to/a_table"
                  "B Table": "path/to/b_table"
                  "C Table": "path/to/c_table"
                """,
            renderer.render("Title of the Test Class",
                "A free-text description explaining what these tables are about.",
                List.of(
                    new TableFileEntry("A Table", Path.of("path/to/a_table")),
                    new TableFileEntry("B Table", Path.of("path/to/b_table")),
                    new TableFileEntry("C Table", Path.of("path/to/c_table"))
                )
            )
        );
    }

}