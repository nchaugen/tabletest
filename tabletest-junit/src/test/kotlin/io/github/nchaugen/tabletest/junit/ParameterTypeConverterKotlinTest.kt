package io.github.nchaugen.tabletest.junit

import io.github.nchaugen.tabletest.junit.ParameterFixture.parameter
import io.github.nchaugen.tabletest.junit.exampledomain.Age
import io.github.nchaugen.tabletest.junit.exampledomain.Ages
import io.github.nchaugen.tabletest.junit.exampledomain.ConstructorDate
import io.github.nchaugen.tabletest.junit.exampledomain.FactoryMethodDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
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
        table value                          | parameter type
        15                                   | java.lang.Byte
        0xF                                  | java.lang.Byte
        017                                  | java.lang.Byte
        15                                   | java.lang.Short
        0xF                                  | java.lang.Short
        017                                  | java.lang.Short
        15                                   | java.lang.Integer
        0xF                                  | java.lang.Integer
        017                                  | java.lang.Integer
        15                                   | java.lang.Long
        0xF                                  | java.lang.Long
        017                                  | java.lang.Long
        15                                   | java.math.BigInteger
        1234567890123456789                  | java.math.BigInteger
        0.1                                  | java.lang.Float
        0.1                                  | java.lang.Double
        1                                    | java.math.BigDecimal
        0.1                                  | java.math.BigDecimal
        123.456e789                          | java.math.BigDecimal
        a                                    | java.lang.Character
        abc                                  | java.lang.String
        SECONDS                              | java.util.concurrent.TimeUnit
        /path/to/file                        | java.io.File
        /path/to/file                        | java.nio.file.Path
        "https://junit.org/"                 | java.net.URI
        "https://junit.org/"                 | java.net.URL
        java.lang.Integer                    | java.lang.Class
        java.lang.Thread${'$'}State             | java.lang.Class
        byte                                 | java.lang.Class
        "char[]"                             | java.lang.Class
        UTF-8                                | java.nio.charset.Charset
        NOK                                  | java.util.Currency
        en                                   | java.util.Locale
        d043e930-7b3b-48e3-bdbe-5a3ccfb833db | java.util.UUID
        PT3S                                 | java.time.Duration
        "1977-08-17T18:19:20Z"               | java.time.Instant
        "2017-03-14T12:34:56.789"            | java.time.LocalDateTime
        2017-03-14                           | java.time.LocalDate
        "12:34:56.789"                       | java.time.LocalTime
        "2017-03-14T12:34:56.789Z"           | java.time.OffsetDateTime
        "12:34:56.789Z"                      | java.time.OffsetTime
        "2017-03-14T12:34:56.789Z"           | java.time.ZonedDateTime
        Europe/Berlin                        | java.time.ZoneId
        "+02:30"                             | java.time.ZoneOffset
        P2M6D                                | java.time.Period
        2017                                 | java.time.Year
        2017-03                              | java.time.YearMonth
        --03-14                              | java.time.MonthDay
            """
        )
        fun converts_according_to_junit_rules(value: String?, type: Class<*>) {
            assertInstanceOf(
                type,
                ParameterTypeConverter.convertValue(value, parameter(type))
            )
        }

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

        @TableTest(
            """    
        table value | parameter type
        123         | java.lang.Byte
        123         | java.lang.Float
        123         | java.lang.Double
            """
        )
        fun allows_widening_primitive_conversion(value: String?, type: Class<*>) {
            assertInstanceOf(
                type,
                ParameterTypeConverter.convertValue(value, parameter(type))
            )
        }

        @TableTest(
            """    
        Scenario     | table value | parameter type
        Empty string | ""          | java.lang.Float
        Blank cell   |             | java.util.List
            """
        )
        fun converts_blank_to_null_for_non_string_parameters(value: String?, type: Class<*>?) {
            assertNull(ParameterTypeConverter.convertValue(value, parameter(type)))
        }

        @TableTest(
            """    
        Scenario     | table value | parameter type
        Empty string | ''          | java.lang.String
        Blank cell   |             | java.lang.String
            """
        )
        fun converts_blank_to_blank_for_string_parameters(value: String?, type: Class<*>?) {
            assertEquals("", ParameterTypeConverter.convertValue(value, parameter(type)))
        }
    }

    @Nested
    internal inner class ListValues {
        @TableTest(
            """    
        Empty | Byte | Integer | Long | Double | String | List  | Map
        []    | [1]  | [2]     | [3]  | [4]    | [5]    | [[6]] | [[1:7]]
        """
        )
        fun converts_to_parameterized_parameter_type(
            emptyList: List<*>,
            byteList: List<Byte>,
            integerList: List<Int>,
            longList: List<Long>,
            doubleList: List<Double>,
            stringList: List<String>,
            listList: List<List<Short>>,
            mapList: List<Map<*, Long>>
        ) {
            assertEquals(emptyList<Any>(), emptyList)
            assertEquals(listOf(1.toByte()), byteList)
            assertEquals(listOf(2), integerList)
            assertEquals(listOf(3L), longList)
            assertEquals(listOf(4.0), doubleList)
            assertEquals(listOf("5"), stringList)
            assertEquals(listOf(listOf(6.toShort())), listList)
            assertEquals(listOf(mapOf("1" to 7L)), mapList)
        }

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
        @TableTest(
            """    
        Empty | Byte    | Integer | Long    | Double  | String  | List      | Map
        [:]   | [key:1] | [key:2] | [key:3] | [key:4] | [key:5] | [key:[6]] | [key:[1:7]]
        """
        )
        fun converts_to_parameterized_parameter_type(
            emptyMap: Map<*, *>,
            byteMap: Map<String, Byte>,
            integerMap: Map<*, Int>,
            longMap: Map<*, Long>,
            doubleMap: Map<*, Double>,
            stringMap: Map<*, String>,
            listMap: Map<*, List<Short>>,
            mapMap: Map<*, Map<*, Long>>
        ) {
            assertEquals(emptyMap<Any?, Any?>(), emptyMap)
            assertEquals(mapOf("key" to 1.toByte()), byteMap)
            assertEquals(mapOf("key" to 2), integerMap)
            assertEquals(mapOf("key" to 3L), longMap)
            assertEquals(mapOf("key" to 4.0), doubleMap)
            assertEquals(mapOf("key" to "5"), stringMap)
            assertEquals(mapOf("key" to listOf(6.toShort())), listMap)
            assertEquals(mapOf("key" to mapOf("1" to 7L)), mapMap)
        }

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
        date2: FactoryMethodDate,
        list: List<FactoryMethodDate>
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
fun parseDate(date: String): FactoryMethodDate {
    return FactoryMethodDate(LocalDate.parse(date))
}
