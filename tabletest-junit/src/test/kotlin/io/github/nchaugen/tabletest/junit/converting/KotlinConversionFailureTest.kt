package io.github.nchaugen.tabletest.junit.converting

import io.github.nchaugen.tabletest.junit.ParameterFixture.parameter
import io.github.nchaugen.tabletest.junit.ParameterTypeConverter
import io.github.nchaugen.tabletest.junit.TableTest
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.platform.commons.support.conversion.ConversionException

class KotlinConversionFailureTest {

    @TableTest(
        """    
        table value | parameter type
        0.1         | java.lang.Byte
        256         | java.lang.Byte
        abc         | java.lang.Character
            """
    )
    fun fails_conversion_for_single_values_outside_type_range(
        value: String?,
        type: Class<*>?
    ) {
        assertThrows(ConversionException::class.java) {
            ParameterTypeConverter.convertValue(value, parameter(type))
        }
    }

    @Test
    fun fails_when_list_conversion_not_possible() {
        mapOf(
            "java.util.List<java.lang.Byte>" to listOf("x")
        ).forEach { (typeName: String, parsedValue: List<*>) ->
            assertThrows(
                ConversionException::class.java, {
                    ParameterTypeConverter.convertValue(
                        parsedValue,
                        parameter(typeName)
                    )
                },
                { "Expected failure for $typeName" }
            )
        }
    }

    @Test
    fun fails_when_set_conversion_not_possible() {
        mapOf(
            "java.util.Set<java.lang.Byte>" to setOf("x")
        ).forEach { (typeName: String, parsedValue: Set<*>) ->
            assertThrows(
                ConversionException::class.java, {
                    ParameterTypeConverter.convertValue(
                        parsedValue,
                        parameter(typeName)
                    )
                },
                { "Expected failure for $typeName" }
            )
        }
    }

    @Test
    fun fails_when_map_conversion_not_possible() {
        mapOf(
            "java.util.Map<?, java.lang.Short>" to mapOf("key" to "x")
        ).forEach { (typeName: String, parsedValue: Map<*, *>) ->
            assertThrows(
                ConversionException::class.java,
                { ParameterTypeConverter.convertValue(parsedValue, parameter(typeName)) },
                { "Expected failure for $typeName" }
            )
        }
    }

}
