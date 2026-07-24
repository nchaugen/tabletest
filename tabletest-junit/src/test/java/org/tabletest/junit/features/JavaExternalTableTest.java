package org.tabletest.junit.features;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.junit.Description;
import org.tabletest.junit.InputResolver;
import org.tabletest.junit.TableTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("External table files")
@Description("""
        A table can live in a classpath resource file instead of the annotation:
        @TableTest(resource = "external.table", encoding = "UTF-8"). The first three
        rules below call the loading API with those same arguments, so the tables can
        show which paths resolve, what text the file's bytes become, and how loading
        fails; the rules after them are driven by the very files shown, each row
        coming from a line of the file rather than from the annotation.
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

    @DisplayName("A resource that cannot be read fails with a message naming the file")
    @Description("""
            The two ways loading can fail: the path resolves to nothing, or the file
            resolves but the declared encoding cannot decode it. Both raise the same
            exception type, so the message is what tells the two apart.
            """)
    @TableTest("""
        Scenario                 | Resource path  | Encoding   | Throws?                                | Error message?
        No such file anywhere    | no_such.table  | UTF-8      | org.tabletest.junit.TableTestException | External table file no_such.table not found, searched the classpath relative to org.tabletest.junit.features.JavaExternalTableTest and from the root
        Subfolder file misspelt  | subfolder/x    | UTF-8      | org.tabletest.junit.TableTestException | External table file subfolder/x not found, searched the classpath relative to org.tabletest.junit.features.JavaExternalTableTest and from the root
        Encoding does not exist  | external.table | Latin-42   | org.tabletest.junit.TableTestException | Failed to read table from external file external.table using encoding Latin-42
        """)
    void reports_a_resource_it_cannot_read(
        String resourcePath,
        String encoding,
        Class<? extends Throwable> expectedException,
        String expectedMessage
    ) {
        Throwable thrown = assertThrows(
            expectedException,
            () -> InputResolver.loadResource(resourcePath, encoding, getClass())
        );
        assertEquals(expectedMessage, thrown.getMessage());
    }

    @DisplayName("A table loaded from a resource file runs like an inline table")
    @Description("The rows below are the lines of src/test/resources/external.table.")
    @TableTest(resource = "external.table")
    void table_in_external_file_in_implicit_root(int a, int b, int expectedSum) {
        assertEquals(expectedSum, a + b);
    }

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
