package io.github.nchaugen.tabletest.junit.converting

import io.github.nchaugen.tabletest.junit.TableTest
import io.github.nchaugen.tabletest.junit.TableTestExceptionAssertions.*
import io.github.nchaugen.tabletest.junit.javadomain.Age
import org.junit.jupiter.api.Test

class KotlinConversionFailureTest {

    @TableTest(
        """    
        table value | parameter type
        0.1         | java.lang.Byte
        256         | java.lang.Byte
        abc         | java.lang.Character
        invalid     | java.time.LocalDate
        52          | io.github.nchaugen.tabletest.junit.javadomain.Ages
            """
    )
    fun `fails conversion for single values outside type range`(value: String, type: Class<*>) {
        assertThrowsWhenFallbackFails(value, type)
    }

    @TableTest(
        """    
        table value | parameter type
        52          | io.github.nchaugen.tabletest.junit.javadomain.Age
        """
    )
    fun fails_when_multiple_factory_methods_found(value: String, type: Class<*>) {
        assertThrowsWhenMultipleFactoryMethodsFound(value, type)
    }

    @Suppress("unused")
    fun parseAge(age: String?): Age {
        throw IllegalStateException("should not be called")
    }

    @Suppress("unused")
    fun anotherParseAge(age: Int?): Age {
        throw IllegalStateException("should not be called")
    }

    @Test
    fun failing_primitive_conversion() {
        assertThrowsWhenNullSpecifiedForPrimitiveType(
            null,
            Boolean::class.javaPrimitiveType
        )
    }

}