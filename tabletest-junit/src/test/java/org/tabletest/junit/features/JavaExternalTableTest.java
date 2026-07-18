package org.tabletest.junit.features;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("spec")
@DisplayName("External table files")
@Description("""
        A table can live in a classpath resource file instead of the annotation:
        @TableTest(resource = "external.table"). Paths resolve from the
        classpath root, with or without a leading slash.
        """)
public class JavaExternalTableTest {

    @DisplayName("A table loaded from a resource file runs like an inline table")
    @Description("This table lives in src/test/resources/external.table.")
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
            This table lives in src/test/resources/subfolder/custom_encoding.table,
            read with encoding = "ISO-8859-1".
            """)
    @TableTest(resource = "/subfolder/custom_encoding.table", encoding = "ISO-8859-1")
    void table_in_external_file_in_subfolder_with_custom_encoding(String string, int expectedLength) {
        assertEquals(expectedLength, string.length());
    }

}
