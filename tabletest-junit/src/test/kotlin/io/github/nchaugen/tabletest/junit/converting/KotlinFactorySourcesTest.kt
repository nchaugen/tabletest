package io.github.nchaugen.tabletest.junit.converting

import io.github.nchaugen.tabletest.junit.TableTest
import io.github.nchaugen.tabletest.junit.FactorySources
import io.github.nchaugen.tabletest.junit.javafactories.FirstTierFactorySource
import io.github.nchaugen.tabletest.junit.javafactories.SecondTierFactorySource
import io.github.nchaugen.tabletest.junit.javadomain.Age
import io.github.nchaugen.tabletest.junit.javadomain.Ages
import io.github.nchaugen.tabletest.junit.kotlinfactories.KotlinFactorySourceForNestedClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import java.time.LocalDate

@FactorySources(FirstTierFactorySource::class, SecondTierFactorySource::class)
class KotlinFactorySourcesTest {

    @TableTest(
        """    
        Int | List | Set  | AVS  | Map       | Nested           | Ages
        16  | [16] | {16} | {16} | [age: 16] | [ages: [16, 16]] | [ages: [16, 16]]
        """
    )
    fun using_factory_methods_in_factory_source(
        fromInt: Age,
        inList: List<Age>,
        inSet: Set<Age>,
        fromValueSet: Age,
        inMap: Map<String, Age>,
        inNested: Map<String, List<Age>>,
        inOtherFactoryMethod: Ages
    ) {
        val expected = Age(16)
        assertEquals(expected, fromInt)
        assertEquals(listOf(expected), inList)
        assertEquals(setOf(expected), inSet)
        assertEquals(expected, fromValueSet)
        assertEquals(mapOf("age" to expected), inMap)
        assertEquals(mapOf("ages" to listOf(expected, expected)), inNested)
        assertEquals(Ages(listOf<Age?>(expected, expected)), inOtherFactoryMethod)
    }

    @TableTest(
        """    
        This Date  | Other Date | Is Before?
        today      | tomorrow   | true
        today      | yesterday  | false
        2024-02-29 | 2024-03-01 | true
        """
    )
    fun overriding_factory_sources(thisDate: LocalDate, otherDate: LocalDate?, expectedIsBefore: Boolean) {
        assertEquals(expectedIsBefore, thisDate.isBefore(otherDate))
    }

    companion object {
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

    @FactorySources(KotlinFactorySourceForNestedClass::class)
    @Nested
    inner class NestedTestClass {
        @TableTest(
            """    
        Ages             | Expected
        [ages: [16, 16]] | 26
            """
        )
        fun factory_source_in_nested_class_takes_precedence_over_outer_factory_source(
            fromFactoryMethodInAnnotationClass: Ages,
            expectedAgeAfterConversion: Int
        ) {
            val expected = Age(expectedAgeAfterConversion)
            assertEquals(Ages(listOf(expected, expected)), fromFactoryMethodInAnnotationClass)
        }

        @TableTest(
            """    
        This Date | Other Date | Is Before?
        today     | tomorrow   | true
        today     | yesterday  | false
            """
        )
        fun factory_method_in_outer_class_takes_precedence_over_factory_source(
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
            fun direct_outer_factory_sourcea_takes_precedence_over_outermost_factory_source(
                fromFactoryMethodInAnnotationClass: Ages,
                expectedAgeAfterConversion: Int
            ) {
                val expected = Age(expectedAgeAfterConversion)
                assertEquals(Ages(listOf(expected, expected)), fromFactoryMethodInAnnotationClass)
            }

        }
    }
}
