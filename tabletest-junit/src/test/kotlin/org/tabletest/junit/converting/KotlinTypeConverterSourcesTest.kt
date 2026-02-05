package org.tabletest.junit.converting

import org.tabletest.junit.TableTest
import org.tabletest.junit.TypeConverter
import org.tabletest.junit.TypeConverterSources
import org.tabletest.junit.javatypeconverters.FirstTierTypeConverterSource
import org.tabletest.junit.javatypeconverters.SecondTierTypeConverterSource
import org.tabletest.junit.javadomain.Age
import org.tabletest.junit.javadomain.Ages
import org.tabletest.junit.kotlintypeconverters.KotlinTypeConverterSourceForNestedClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import java.time.LocalDate

@TypeConverterSources(FirstTierTypeConverterSource::class, SecondTierTypeConverterSource::class)
class KotlinTypeConverterSourcesTest {

    @TableTest(
        """
        Int | List | Set  | AVS  | Map       | Nested           | Ages
        16  | [16] | {16} | {16} | [age: 16] | [ages: [16, 16]] | [ages: [16, 16]]
        """
    )
    fun using_type_converters_in_converter_source(
        fromInt: Age,
        inList: List<Age>,
        inSet: Set<Age>,
        fromValueSet: Age,
        inMap: Map<String, Age>,
        inNested: Map<String, List<Age>>,
        inOtherTypeConverter: Ages
    ) {
        val expected = Age(16)
        assertEquals(expected, fromInt)
        assertEquals(listOf(expected), inList)
        assertEquals(setOf(expected), inSet)
        assertEquals(expected, fromValueSet)
        assertEquals(mapOf("age" to expected), inMap)
        assertEquals(mapOf("ages" to listOf(expected, expected)), inNested)
        assertEquals(Ages(listOf<Age?>(expected, expected)), inOtherTypeConverter)
    }

    @TableTest(
        """
        This Date  | Other Date | Is Before?
        today      | tomorrow   | true
        today      | yesterday  | false
        2024-02-29 | 2024-03-01 | true
        """
    )
    fun overriding_converter_sources(thisDate: LocalDate, otherDate: LocalDate?, expectedIsBefore: Boolean) {
        assertEquals(expectedIsBefore, thisDate.isBefore(otherDate))
    }

    companion object {
        @TypeConverter
        @Suppress("unused")
        @JvmStatic
        fun parseLocalDate(input: String): LocalDate {
            return when (input) {
                "yesterday" -> LocalDate.parse("2025-06-06")
                "today" -> LocalDate.parse("2025-06-07")
                "tomorrow" -> LocalDate.parse("2025-06-08")
                else -> LocalDate.parse(input)
            }
        }
    }

    @TypeConverterSources(KotlinTypeConverterSourceForNestedClass::class)
    @Nested
    inner class NestedTestClass {
        @TableTest(
            """
        Ages             | Expected
        [ages: [16, 16]] | 26
            """
        )
        fun converter_source_in_nested_class_takes_precedence_over_outer_converter_source(
            fromTypeConverterInAnnotationClass: Ages,
            expectedAgeAfterConversion: Int
        ) {
            val expected = Age(expectedAgeAfterConversion)
            assertEquals(Ages(listOf(expected, expected)), fromTypeConverterInAnnotationClass)
        }

        @TableTest(
            """
        This Date | Other Date | Is Before?
        today     | tomorrow   | true
        today     | yesterday  | false
            """
        )
        fun type_converter_in_outer_class_takes_precedence_over_converter_source(
            thisDate: LocalDate,
            otherDate: LocalDate,
            expectedIsBefore: Boolean
        ) {
            assertEquals(expectedIsBefore, thisDate.isBefore(otherDate))
        }


        @Nested
        inner class DeeplyNestedTestClass {

            @TableTest(
                """
        Ages             | Expected
        [ages: [16, 16]] | 26
                """
            )
            fun direct_outer_converter_source_takes_precedence_over_outermost_converter_source(
                fromTypeConverterInAnnotationClass: Ages,
                expectedAgeAfterConversion: Int
            ) {
                val expected = Age(expectedAgeAfterConversion)
                assertEquals(Ages(listOf(expected, expected)), fromTypeConverterInAnnotationClass)
            }

        }
    }
}
