package org.tabletest.junit.features

import org.tabletest.junit.TableTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull

class KotlinValueSetTest {

    @TableTest(
        """    
        Scenario                  | adding any of | to set    | makes size? | is set null?
        Adding existing values    | {1, 2, 3}     | {1, 2, 3} | 3           | false
        Adding other values       | {4, 5, 6}     | {1, 2, 3} | 4           | false
        Adding nothing to nothing |               |           |             | true
        """
    )
    fun value_sets(
        a: Int?,
        b: Set<Int?>?,
        expectedSize: Int?,
        expectedNull: Boolean
    ) {
        if (expectedNull) {
            assertNull(b)
        } else {
            val result = b!!.toMutableSet()
            result.add(a)
            assertEquals(expectedSize, result.size)
        }
    }

    @TableTest(
        """
        Scenario       | x         | y       | is sum even?
        Even plus even | {2, 4, 6} | {8, 10} | true
        Odd plus even  | {1, 3, 5} | {6, 8}  | false
        """
    )
    fun value_set_combinations(x: Int, y: Int, expectedEvenSum: Boolean) {
        assertEquals(expectedEvenSum, (x + y) % 2 == 0)
    }

}
