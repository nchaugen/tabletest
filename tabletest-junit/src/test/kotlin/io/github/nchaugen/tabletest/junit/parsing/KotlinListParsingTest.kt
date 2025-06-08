package io.github.nchaugen.tabletest.junit.parsing

import io.github.nchaugen.tabletest.junit.TableTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf

class KotlinListParsingTest {

    @TableTest(resource = "/parsing/list-values.table")
    fun testListValueParsing(list: List<*>, expectedSize: Int, elementType: Class<*>) {
        assertEquals(expectedSize, list.size, "List: $list")
        list.firstOrNull()?.let { assertInstanceOf(elementType, it, "First element type for: $list") }
    }

    @TableTest(
        """    
        Scenario                     | Input                 | First element?
        Unquoted is trimmed          | [unquoted text]       | "unquoted text"
        Double-quoted is not trimmed | ["   quoted text   "] | '   quoted text   '
        Single-quoted is not trimmed | ['   quoted text   '] | "   quoted text   "
        """
    )
    fun testElementTrimming(input: List<String>, expectedFirstElement: String) {
        assertEquals(1, input.size)
        assertEquals(expectedFirstElement, input.first())
    }

    @TableTest(
        """    
        Scenario             | Input              | Size? | Element Type?
        Nested lists         | [[a, b], [c, d]]   | 2     | java.util.List
        Lists of maps        | [[a: 1], [b: 2]]   | 2     | java.util.Map
        Lists of sets        | [{a}, {b, c}]      | 2     | java.util.Set
        Deep nesting         | [[[a]], [[b, c]]]  | 2     | java.util.List
        Mixed compound types | [[a: 1], [b], {c}] | 3     | java.lang.Object
        """
    )
    fun testCompoundListTypes(list: List<*>, expectedSize: Int, expectedElementType: Class<*>) {
        assertEquals(expectedSize, list.size)
        list.firstOrNull()?.let { assertInstanceOf(expectedElementType, it) }
    }

}
