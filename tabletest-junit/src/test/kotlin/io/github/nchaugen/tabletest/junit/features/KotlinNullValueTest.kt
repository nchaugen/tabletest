package io.github.nchaugen.tabletest.junit.features

import io.github.nchaugen.tabletest.junit.TableTest
import io.github.nchaugen.tabletest.junit.TableTestExceptionAssertions.assertThrowsWhenFallbackFails
import io.github.nchaugen.tabletest.junit.TableTestExceptionAssertions.assertThrowsWhenNullSpecifiedForPrimitiveType
import kotlin.test.assertNull

class KotlinNullValueTest {

    @TableTest(
        """    
        String | Integer | List | Map | Set
               |         |      |     |
        """
    )
    fun blank_converts_to_null(
        string: String?,
        integer: Int?,
        list: List<*>?,
        map: Map<String, *>?,
        set: Set<*>?
    ) {
        assertNull(string)
        assertNull(integer)
        assertNull(list)
        assertNull(map)
        assertNull(set)
    }

    @TableTest(
        """    
        table value | parameter type
                    | boolean
                    | short
                    | char
        
        """
    )
    fun blank_fails_for_primitive_parameter_type(value: String?, type: Class<*>?) {
        assertThrowsWhenNullSpecifiedForPrimitiveType(value, type)
    }

    @TableTest(
        """    
        Scenario            | table value | Type
        Empty single quoted | ''          | java.util.List
        Empty double quoted | ""          | java.lang.Integer
        Empty primitive     | ""          | boolean
        
        """
    )
    fun empty_string_for_non_string_types_requires_factory_method(
        value: String?,
        type: Class<*>?
    ) {
        assertThrowsWhenFallbackFails(value, type)
    }

}
