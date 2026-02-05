package org.tabletest.junit.features

import org.tabletest.junit.TableTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull

class KotlinValueSetTest {

    @TableTest(
        """    
        Scenario                  | adding any of | to set    | makes size? | is set null? | contains null?
        Adding existing values    | {1, 2, 3}     | {1, 2, 3} | 3           | false        | false
        Adding other values       | {4, 5, 6}     | {1, 2, 3} | 4           | false        | false
        Adding no values          | {}            | {1, 2, 3} | 4           | false        | true
        Adding nothing to nothing |               |           |             | true         | false
        """
    )
    fun value_sets(
        a: Int?,
        b: Set<Int?>?,
        expectedSize: Int?,
        expectedNull: Boolean,
        containsNull: Boolean
    ) {
        if (expectedNull) {
            assertNull(b)
        } else {
            val result = b!!.toMutableSet()
            result.add(a)
            assertEquals(expectedSize, result.size)
            assertEquals(containsNull, result.contains(null))
        }
    }

    @TableTest(
        """    
        Scenario                      | a                | b | c | d         | e
        Anything multiplied by 0 is 0 | {-1, 0, 1, 1000} | 0 | 0 | {1, 2, 3} | 3
        """
    )
    fun testValueSet(a: Int, b: Int, c: Int, d: Set<Int>, e: Int) {
        kotlin.test.assertEquals(c, a * b)
        kotlin.test.assertEquals(e, d.size)
    }

}
