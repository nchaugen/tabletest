package io.github.nchaugen.tabletest.junit.converting

import io.github.nchaugen.tabletest.junit.TableTest
import io.github.nchaugen.tabletest.junit.javadomain.Age
import io.github.nchaugen.tabletest.junit.javadomain.Ages
import io.github.nchaugen.tabletest.junit.javadomain.ExternalFactoryDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.platform.commons.support.conversion.ConversionException
import java.time.LocalDate

class KotlinFactoryMethodInTestClassConversionTest {

    @TableTest(
        """    
        Int | List | Set  | AVS  | Map         | Nested            | Ages
        16  | [16] | {16} | {16} | [value: 16] | [value: [16, 16]] | [value: [16, 16]]
            """
    )
    fun uses_factory_method_in_test_class(
        fromInt: Age,
        inList: java.util.List<Age>,
        inSet: Set<Age>,
        fromValueSet: Age,
        inMap: java.util.Map<String, Age>,
        inNested: java.util.Map<String, List<Age>>,
        inOtherFactoryMethod: Ages
    ) {
        val expected = Age(16)
        assertEquals(expected, fromInt)
        assertEquals(listOf(expected), inList)
        assertEquals(setOf(expected), inSet)
        assertEquals(expected, fromValueSet)
        assertEquals(mapOf("value" to expected), inMap)
        assertEquals(mapOf("value" to listOf(expected, expected)), inNested)
        assertEquals(Ages(listOf(expected, expected)), inOtherFactoryMethod)
    }

    @Suppress("unused")
    fun parseAge(age: String): Age {
        throw ConversionException("Factory method not static, should not be called")
    }

    companion object {
        @JvmStatic
        @Suppress("unused")
        fun parseAge(age: Int): Age {
            return Age(age)
        }

        @JvmStatic
        @Suppress("unused")
        private fun parseAge(age: Long): Age {
            throw ConversionException("Factory method not accessible, should not be called")
        }

        @JvmStatic
        @Suppress("unused")
        fun parseAges(age: Map<String, List<Age>>): Ages {
            return Ages(age["value"])
        }

    }

    @TableTest(
        """    
        This Date  | Other Date | Is Before?
        today      | tomorrow   | true
        today      | yesterday  | false
        2024-02-29 | 2024-03-01 | true
        """
    )
    fun overriding_fallback_conversion(thisDate: LocalDate, otherDate: LocalDate?, expectedIsBefore: Boolean) {
        assertEquals(expectedIsBefore, thisDate.isBefore(otherDate))
    }

    @Nested
    inner class NestedTestClass {

        @TableTest(
            """    
        This Date  | Other Date | Is Before?
        today      | tomorrow   | true
        today      | yesterday  | false
        2024-02-29 | 2024-03-01 | true
            """
        )
        fun finding_factory_method_from_nested_class(
            thisDate: LocalDate,
            otherDate: LocalDate?,
            expectedIsBefore: Boolean
        ) {
            assertEquals(expectedIsBefore, thisDate.isBefore(otherDate))
        }

        @Nested
        inner class DeeplyNestedTestClass {

            @TableTest(
                """    
        This Date  | Other Date | Is Before?
        today      | tomorrow   | true
        today      | yesterday  | false
        2024-02-29 | 2024-03-01 | true
            """
            )
            fun finding_factory_method_from_deeply_nested_class(
                thisDate: LocalDate,
                otherDate: LocalDate?,
                expectedIsBefore: Boolean
            ) {
                assertEquals(expectedIsBefore, thisDate.isBefore(otherDate))
            }
        }
    }

}

@Suppress("unused")
fun parseLocalDate(input: String): LocalDate {
    return when (input) {
        "yesterday" -> LocalDate.parse("2025-06-06")
        "today" -> LocalDate.parse("2025-06-07")
        "tomorrow" -> LocalDate.parse("2025-06-08")
        else -> LocalDate.parse(input)
    }
}

@Suppress("unused")
fun parseDate(date: String): ExternalFactoryDate {
    return ExternalFactoryDate(LocalDate.parse(date))
}
