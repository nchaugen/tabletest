package io.github.nchaugen.tabletest.junit.converting

import io.github.nchaugen.tabletest.junit.TableTest
import io.github.nchaugen.tabletest.junit.TableTestExceptionAssertions.assertThrowsWhenFallbackFails
import io.github.nchaugen.tabletest.junit.TableTestExceptionAssertions.assertThrowsWhenMultipleFactoryMethodsFound

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
        value: String,
        type: Class<*>
    ) {
        assertThrowsWhenFallbackFails(value, type.getTypeName())
    }

    @TableTest(
        """    
        Value     | Parameterized type
        [""]      | java.util.List<java.lang.Byte>
        {""}      | java.util.Set<java.lang.Byte>
        [key: ""] | java.util.Map<?, java.lang.Short>
        """
    )
    fun fails_conversion_for_element_values_outside_type_range(value: Any, parameterizedTypeName: String) {
        assertThrowsWhenFallbackFails(value, parameterizedTypeName)
    }

    @TableTest(
        """    
        table value | parameter type
        [52]        | io.github.nchaugen.tabletest.junit.javadomain.Ages
        """
    )
    fun fails_when_no_factory_method_found(value: Any, type: Class<*>) {
        assertThrowsWhenFallbackFails(value, type.getTypeName())
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

}
