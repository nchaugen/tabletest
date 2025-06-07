package io.github.nchaugen.tabletest.junit.features

import io.github.nchaugen.tabletest.junit.TableTest
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
    fun testBlankIsNullForNonString(
        scenario: String,
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

}
