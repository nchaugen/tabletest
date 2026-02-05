package org.tabletest.junit.features;

import org.tabletest.junit.TableTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaExternalTableTest {

    @TableTest(resource = "external.table")
    void table_in_external_file_in_implicit_root(int a, int b, int expectedSum) {
        assertEquals(expectedSum, a + b);
    }

    @TableTest(resource = "/external.table")
    void table_in_external_file_in_explicit_root(int a, int b, int expectedSum) {
        assertEquals(expectedSum, a + b);
    }

    @TableTest(resource = "/subfolder/custom_encoding.table", encoding = "ISO-8859-1")
    void table_in_external_file_in_subfolder_with_custom_encoding(String string, int expectedLength) {
        assertEquals(expectedLength, string.length());
    }

}
