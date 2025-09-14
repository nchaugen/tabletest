package io.github.nchaugen.tabletest.junit.converting

import io.github.nchaugen.tabletest.junit.FactorySources
import io.github.nchaugen.tabletest.junit.TableTest
import kotlin.test.assertEquals

@FactorySources(JavaPrimitiveConversionTest::class)
class KotlinPrimitiveConversionTest {

    @TableTest(
        """
            A   | B   | A and B
            Yes | Yes | Yes
            No  | Yes | No
            """
    )
    fun primitive_factory_method_(a: Boolean, b: Boolean, expectedResult: Boolean) {
        assertEquals(expectedResult, a && b)
    }

    @TableTest(
        """
        A   | B     | A + B
        ONE | THREE | FOUR
        TWO | TWO   | FOUR
        """
    )
    fun non_primitive_factory_method_(a: Int, b: Int, expectedResult: Int) {
        assertEquals(expectedResult, a + b)
    }

}

@Suppress("unused")
fun parseInteger(value: String): Int =
    when (value) {
        "ONE" -> 1
        "TWO" -> 2
        "THREE" -> 3
        "FOUR" -> 4
        else -> throw IllegalArgumentException("Unsupported value: $value")
    }
