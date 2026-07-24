package org.tabletest.junit.deprecated

import io.github.nchaugen.tabletest.junit.Description
import io.github.nchaugen.tabletest.junit.FactorySources
import io.github.nchaugen.tabletest.junit.Scenario
import io.github.nchaugen.tabletest.junit.TableTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.tabletest.junit.javadomain.Age
import org.tabletest.junit.javadomain.Ages
import org.tabletest.junit.javafactories.FirstTierFactorySource
import org.tabletest.junit.javafactories.SecondTierFactorySource

@DisplayName("Leap Year Rules (Kotlin)")
@Description("The following describes the rules for leap years.")
@FactorySources(FirstTierFactorySource::class, SecondTierFactorySource::class)
class DeprecatedAnnotationsKotlinTest {

    @Description("Testing that deprecated annotations still work in Kotlin")
    @TableTest(
        """
        Scenario | Int | List | Set  | AVS  | Map       | Nested           | Ages
        Example  | 16  | [16] | {16} | {16} | [age: 16] | [ages: [16, 16]] | [ages: [16, 16]]
        """
    )
    fun using_factory_methods_in_factory_source(
        @Scenario scenario: String,
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
        assertEquals(Ages(listOf(expected, expected)), inOtherFactoryMethod)
    }

}
