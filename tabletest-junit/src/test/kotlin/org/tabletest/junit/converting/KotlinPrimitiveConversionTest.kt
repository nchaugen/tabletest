package org.tabletest.junit.converting

import org.tabletest.junit.TypeConverter
import org.tabletest.junit.TypeConverterSources
import org.tabletest.junit.TableTest
import kotlin.test.assertEquals

@TypeConverterSources(JavaPrimitiveConversionTest::class)
class KotlinPrimitiveConversionTest {

    @TableTest(
        """
            A   | B   | A and B
            Yes | Yes | Yes
            No  | Yes | No
            """
    )
    fun primitive_type_converter(a: Boolean, b: Boolean, expectedResult: Boolean) {
        assertEquals(expectedResult, a && b)
    }

    @TableTest(
        """
        A   | B     | A + B
        ONE | THREE | FOUR
        TWO | TWO   | FOUR
        """
    )
    fun non_primitive_type_converter(a: Int, b: Int, expectedResult: Int) {
        assertEquals(expectedResult, a + b)
    }

}

@TypeConverter
@Suppress("unused")
fun parseInteger(value: String): Int =
    when (value) {
        "ONE" -> 1
        "TWO" -> 2
        "THREE" -> 3
        "FOUR" -> 4
        else -> throw IllegalArgumentException("Unsupported value: $value")
    }
