package io.github.nchaugen.tabletest.junit.features

import io.github.nchaugen.tabletest.junit.Scenario
import io.github.nchaugen.tabletest.junit.TableTest
import io.github.nchaugen.tabletest.junit.TableTestExceptionAssertions.assertThrowsWhenNullSpecifiedForPrimitiveType
import kotlin.test.assertEquals
import kotlin.test.assertNull

class KotlinNullValueTest {

    @TableTest(
        """    
        Scenario            | String | Integer | List | Map | Set
        Blank               |        |         |      |     |
        Empty single quoted | ''     | ''      | ''   | ''  | ''
        Empty double quoted | ""     | ""      | ""   | ""  | ""
        """
    )
    fun blank_is_null_and_empty_is_null_for_non_string(
        @Scenario scenario: String,
        string: String?,
        integer: Int?,
        list: List<*>?,
        map: Map<String, *>?,
        set: Set<*>?
    ) {
        if (scenario == "Blank") assertNull(string) else assertEquals("", string)
        assertNull(integer)
        assertNull(list)
        assertNull(map)
        assertNull(set)
    }

    @TableTest(
        """    
        table value | parameter type
                    | boolean
        ''          | int
        """
    )
    fun fails_when_null_value_specified_for_primitive_parameter_type(value: String?, type: Class<*>) {
        assertThrowsWhenNullSpecifiedForPrimitiveType(value, type)
    }

    @TableTest(
        """    
        blank | empty
              | ''
        """
    )
    fun kotlin_nullable_types_can_be_null(blank: Boolean?, empty: Integer?) {
        assertNull(blank)
        assertNull(empty)
    }

}
