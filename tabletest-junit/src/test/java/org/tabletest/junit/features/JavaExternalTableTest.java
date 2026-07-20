package org.tabletest.junit.features;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.tabletest.junit.Description;
import org.tabletest.junit.InputResolver;
import org.tabletest.junit.TableTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("External table files")
@Description("""
        A table can live in a classpath resource file instead of the annotation:
        @TableTest(resource = "external.table", encoding = "UTF-8"). The first two
        rules below load those files as data, so the table shows which paths resolve
        and what text the file's bytes become; the rules after them are driven by
        the very files shown, each row coming from a line of the file rather than
        from the annotation.
        """)
public class JavaExternalTableTest {

    @DisplayName("A resource path resolves from the classpath root, with or without a leading slash")
    @Description("Read as UTF-8; the loaded text is shown one file line per element.")
    @TableTest("""
        Scenario            | Resource path           | Lines loaded from the file?
        Path from the root  | external.table          | ["Scenario   | a | b | a + b?", "Zero sum   | 0 | 0 | 0", "Zero right | 2 | 0 | 2", "Two twos   | 2 | 2 | 4"]
        Leading slash       | /external.table         | ["Scenario   | a | b | a + b?", "Zero sum   | 0 | 0 | 0", "Zero right | 2 | 0 | 2", "Two twos   | 2 | 2 | 4"]
        Path in a subfolder | subfolder/nested.table  | ["Scenario  | a | b | a - b?", "Positive  | 3 | 1 | 2", "Negative  | 1 | 3 | -2"]
        """)
    void resolves_resource_paths(String resourcePath, List<String> expectedLines) {
        assertEquals(
            String.join("\n", expectedLines),
            InputResolver.loadResource(resourcePath, "UTF-8", getClass())
        );
    }

    @DisplayName("The declared encoding decides what the file's bytes become")
    @Description("""
            The last row of /subfolder/custom_encoding.table holds the six ISO-8859-1
            letters ÉÜ¥ÆØÅ. Read in another encoding the same bytes decode to other
            characters, so the encoding belongs with the resource path.
            """)
    @TableTest("""
        Scenario                    | Encoding   | Characters in the last row's first cell? | Code points?
        The encoding the file uses  | ISO-8859-1 | 6                                       | [201, 220, 165, 198, 216, 197]
        Read as UTF-8 instead       | UTF-8      | 5                                       | [65533, 1829, 65533, 65533, 65533]
        """)
    void decodes_bytes_with_the_declared_encoding(
        String encoding,
        int expectedCharacterCount,
        List<Integer> expectedCodePoints
    ) {
        String firstCellOfLastRow = firstCellOfLastRow(
            InputResolver.loadResource("/subfolder/custom_encoding.table", encoding, getClass())
        );
        assertEquals(expectedCharacterCount, firstCellOfLastRow.length());
        assertEquals(expectedCodePoints, firstCellOfLastRow.chars().boxed().toList());
    }

    @DisplayName("A table loaded from a resource file runs like an inline table")
    @Description("The rows below are the lines of src/test/resources/external.table.")
    @TableTest(resource = "external.table")
    void table_in_external_file_in_implicit_root(int a, int b, int expectedSum) {
        assertEquals(expectedSum, a + b);
    }

    // Not published: same file as above, differing only in the leading slash of
    // the resource path — the published table would be an exact duplicate.
    @Tag("unpublished")
    @TableTest(resource = "/external.table")
    void table_in_external_file_in_explicit_root(int a, int b, int expectedSum) {
        assertEquals(expectedSum, a + b);
    }

    @DisplayName("Resource files can declare a non-default character encoding")
    @Description("""
            The rows below are the lines of src/test/resources/subfolder/custom_encoding.table,
            read with encoding = "ISO-8859-1" — the last row's letters only count as
            six characters because the file was decoded in the encoding it was
            written in.
            """)
    @TableTest(resource = "/subfolder/custom_encoding.table", encoding = "ISO-8859-1")
    void table_in_external_file_in_subfolder_with_custom_encoding(String string, int expectedLength) {
        assertEquals(expectedLength, string.length());
    }

    private static String firstCellOfLastRow(String table) {
        List<String> lines = List.of(table.split("\n"));
        return lines.get(lines.size() - 1).split("\\|")[0].trim();
    }
}
