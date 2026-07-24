package org.tabletest.junit.features

import org.tabletest.junit.TableTest
import kotlin.test.assertEquals

class KotlinExternalTableTest {

    @TableTest(resource = "external.table")
    fun table_in_external_file_in_implicit_root(a: Int, b: Int, expectedSum: Int) {
        assertEquals(expectedSum, a + b)
    }

    @TableTest(resource = "/external.table")
    fun table_in_external_file_in_explicit_root(a: Int, b: Int, expectedSum: Int) {
        assertEquals(expectedSum, a + b)
    }

    @TableTest(resource = "/subfolder/custom_encoding.table", encoding = "ISO-8859-1")
    fun table_in_external_file_in_subfolder_with_custom_encoding(string: String, expectedLength: Int) {
        assertEquals(expectedLength, string.length)
    }

}
