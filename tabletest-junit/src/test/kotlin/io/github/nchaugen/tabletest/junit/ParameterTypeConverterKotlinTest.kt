package io.github.nchaugen.tabletest.junit

import io.github.nchaugen.tabletest.junit.ParameterFixture.parameter
import io.github.nchaugen.tabletest.junit.exampledomain.Age
import io.github.nchaugen.tabletest.junit.exampledomain.Ages
import io.github.nchaugen.tabletest.junit.exampledomain.ConstructorDate
import io.github.nchaugen.tabletest.junit.exampledomain.ExternalFactoryDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.platform.commons.support.conversion.ConversionException
import java.time.LocalDate
import java.util.List
import java.util.Map
import java.util.function.Consumer

class ParameterTypeConverterKotlinTest {

    @Nested
    internal inner class SingleValues {

        @TableTest(
            """    
        table value | parameter type
        0.1         | java.lang.Byte
        256         | java.lang.Byte
        abc         | java.lang.Character
            """
        )
        fun fails_conversion_for_values_outside_type_range(
            value: String?,
            type: Class<*>?
        ) {
            assertThrows(ConversionException::class.java) {
                ParameterTypeConverter.convertValue(value, parameter(type))
            }
        }

    }

    @Nested
    internal inner class ListValues {
        @Test
        fun fails_when_conversion_not_possible() {
            mapOf(
                "java.util.List<java.lang.Byte>" to listOf("x")
            ).forEach { (typeName: String, parsedListValue: kotlin.collections.List<*>) ->
                assertThrows(
                    ConversionException::class.java, {
                        ParameterTypeConverter.convertValue(
                            parsedListValue,
                            parameter(typeName)
                        )
                    },
                    { "Expected failure for $typeName" }
                )
            }
        }

        @TableTest(
            """    
        list | nested list | nested set | nested map
        []   | [[]]        | [{}]       | [[:]]
        """
        )
        fun passes_immutable_lists_to_test(
            list: MutableList<String>,
            nestedList: MutableList<MutableList<String>>,
            nestedSet: MutableList<MutableSet<String>>,
            nestedMap: MutableList<MutableMap<String, String>>
        ) {
            try {
                list.add("x")
                fail<Any?>("modifying collections from the table should fail")
            } catch (_: Exception) {
                // expected
            }
            try {
                nestedList.first().add("x")
                fail<Any?>("modifying collections from the table should fail")
            } catch (_: Exception) {
                // expected
            }
            try {
                nestedSet.first().add("x")
                fail<Any?>("modifying collections from the table should fail")
            } catch (_: Exception) {
                // expected
            }
            try {
                nestedMap.first().put("x", "y")
                fail<Any?>("modifying collections from the table should fail")
            } catch (_: Exception) {
                // expected
            }
        }
    }

    @Nested
    internal inner class MapValues {

        @Test
        fun fails_when_conversion_not_possible() {
            mapOf(
                "java.util.Map<?, java.lang.Short>" to mapOf("key" to "x")
            ).forEach { (typeName: String, parsedMapValue: kotlin.collections.Map<*, *>) ->
                assertThrows(
                    ConversionException::class.java,
                    { ParameterTypeConverter.convertValue(parsedMapValue, parameter(typeName)) },
                    { "Expected failure for $typeName" }
                )
            }
        }

        @TableTest(
            """    
        map | nested list | nested set  | nested map
        [:] | [empty: []] | [empty: {}] | [empty: [:]]
            """
        )
        fun passes_immutable_maps_to_test(
            map: Map<String?, String?>,
            nestedList: Map<String?, List<String?>?>,
            nestedSet: Map<String?, MutableSet<String?>?>,
            nestedMap: Map<String?, Map<String?, String?>?>
        ) {
            try {
                map.put("x", "y")
                fail<Any?>("modifying collections from the table should fail")
            } catch (_: Exception) {
                // expected
            }
            try {
                nestedList.get("empty")!!.add("x")
                fail<Any?>("modifying collections from the table should fail")
            } catch (_: Exception) {
                // expected
            }
            try {
                nestedSet.get("empty")!!.add("x")
                fail<Any?>("modifying collections from the table should fail")
            } catch (_: Exception) {
                // expected
            }
            try {
                nestedMap.get("empty")!!.put("x", "y")
                fail<Any?>("modifying collections from the table should fail")
            } catch (_: Exception) {
                // expected
            }
        }
    }

    @Nested
    internal inner class SetValues {
        @TableTest(
            """    
        Scenario                  | adding any of | to set    | makes size? | is set null? | contains null?
        Adding existing values    | {1, 2, 3}     | {1, 2, 3} | 3           | false        | false
        Adding other values       | {4, 5, 6}     | {1, 2, 3} | 4           | false        | false
        Adding no values          | {}            | {1, 2, 3} | 4           | false        | true
        Adding nothing to nothing |               |           |             | true         | false
        """
        )
        fun applicable_value_sets(
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
        set | nested list | nested set | nested map
        {}  | {[]}        | {{}}       | {[:]}
        """
        )
        fun passes_immutable_sets_to_test(
            set: MutableSet<String?>,
            nestedList: MutableSet<List<String?>?>,
            nestedSet: MutableSet<MutableSet<String?>?>,
            nestedMap: MutableSet<Map<String?, String?>?>
        ) {
            try {
                set.add("x")
                fail<Any?>("modifying collections from the table should fail")
            } catch (_: Exception) {
                // expected
            }
            try {
                nestedList.forEach(Consumer { it: List<String?>? -> it!!.add("x") })
                fail<Any?>("modifying collections from the table should fail")
            } catch (_: Exception) {
                // expected
            }
            try {
                nestedSet.forEach(Consumer { it: MutableSet<String?>? -> it!!.add("x") })
                fail<Any?>("modifying collections from the table should fail")
            } catch (_: Exception) {
                // expected
            }
            try {
                nestedMap.forEach(Consumer { it: Map<String?, String?>? -> it!!.put("x", "y") })
                fail<Any?>("modifying collections from the table should fail")
            } catch (_: Exception) {
                // expected
            }
        }
    }

    @TableTest(
        """    
        String | List | Set  | Applicable Value Set | Map         | Nested           | Nested io.github.nchaugen.tabletest.junit.Ages
        16     | [16] | {16} | {16}                 | [value: 16] | [value: [16,16]] | [value: [16,16]]
            """
    )
    fun uses_factory_method_in_test_class(
        fromString: Age,
        inList: List<Age>,
        inSet: Set<Age>,
        fromApplicableSet: Age,
        inMap: Map<String, Age>,
        inNested: Map<String, List<Age>>,
        inCustom: Ages
    ) {
        val expected = Age(16)
        assertEquals(expected, fromString)
        assertEquals(listOf(expected), inList)
        assertEquals(setOf(expected), inSet)
        assertEquals(expected, fromApplicableSet)
        assertEquals(mapOf("value" to expected), inMap)
        assertEquals(mapOf("value" to listOf(expected, expected)), inNested)
        assertEquals(Ages(listOf(expected, expected)), inCustom)
    }

    @TableTest(
        """    
        JUnit convert | Constructor convert | Factory method convert | List of factory method convert
        2025-05-27    | 2025-05-27          | 2025-05-27             | [2025-05-27]
            """
    )
    fun factory_methods_takes_priority_over_junit_conversion(
        date: LocalDate?,
        date1: ConstructorDate,
        date2: ExternalFactoryDate,
        list: List<ExternalFactoryDate>
    ) {
        assertEquals(date, date1.date)
        assertEquals(date, date2.date)
        assertEquals(date, list.first.date)
    }

}

@Suppress("unused")
fun parseAge(age: Int): Age {
    return Age(age)
}

@Suppress("unused")
fun parseAges(age: Map<String, kotlin.collections.List<Age>>): Ages {
    return Ages(age.get("value"))
}

@Suppress("unused")
fun parseDate(date: String): ExternalFactoryDate {
    return ExternalFactoryDate(LocalDate.parse(date))
}
