package org.tabletest.junit.parsing

import org.tabletest.junit.TableTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf

class KotlinSetParsingTest {

    @TableTest(resource = "/parsing/set-values.table")
    fun testSetValueParsing(input: Set<*>, expectedSize: Int, elementType: Class<*>) {
        assertEquals(expectedSize, input.size, "Set: $input")
        input.firstOrNull()?.let { assertInstanceOf(elementType, it, "First element type for: $input") }
    }

    @TableTest(
        """    
        Scenario                     | Input                 | First element?
        Unquoted is trimmed          | {unquoted text}       | "unquoted text"
        Double-quoted is not trimmed | {"   quoted text   "} | '   quoted text   '
        Single-quoted is not trimmed | {'   quoted text   '} | "   quoted text   "
        """
    )
    fun testElementTrimming(input: Set<String?>, expectedFirstElement: String?) {
        assertEquals(1, input.size)
        assertEquals(expectedFirstElement, input.first())
    }

    @TableTest(
        """    
        Scenario             | Input              | Size? | Element Type?
        Set of lists         | {[a, b], [c, d]}   | 2     | java.util.List
        Lists of maps        | {[a: 1], [b: 2]}   | 2     | java.util.Map
        Nested sets          | {{a}, {b, c}}      | 2     | java.util.Set
        Deep nesting         | {[[a]], [[b, c]]}  | 2     | java.util.List
        Mixed compound types | {[a: 1], [b], {c}} | 3     | java.lang.Object
        """
    )
    fun testCompoundListTypes(input: Set<*>, expectedSize: Int, expectedElementType: Class<*>) {
        assertEquals(expectedSize, input.size)
        input.firstOrNull()?.let { assertInstanceOf(expectedElementType, it) }
    }

    @TableTest(
        """    
        Set                                                                            | Order?
        {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}                                             | [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
        {-10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0}                                   | [-10, -9, -8, -7, -6, -5, -4, -3, -2, -1, 0]
        {a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z} | [a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z]
        {z, y, x, w, v, u, t, s, r, q, p, o, n, m, l, k, j, i, h, g, f, e, d, c, b, a} | [z, y, x, w, v, u, t, s, r, q, p, o, n, m, l, k, j, i, h, g, f, e, d, c, b, a]
        """
    )
    fun testSetRetainsOrdering(input: Set<*>, expectedOrder: List<*>?) {
        assertEquals(expectedOrder, input.stream().toList())
    }
}
