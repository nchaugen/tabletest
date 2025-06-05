package io.github.nchaugen.tabletest.junit

import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.params.converter.ArgumentConversionException
import org.junit.jupiter.params.converter.ArgumentConverter
import org.junit.jupiter.params.converter.ConvertWith
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TableTestKotlinTest {

    @TableTest(
        """    
        Int | String | Date       | Class
        1   | abc    | 2025-01-20 | java.lang.Integer
        """
    )
    fun singleValueTypeConversion(integer: Int, string: String, date: LocalDate, type: Class<*>) {
        assertNotEquals(0, integer)
        assertNotNull(string)
        assertNotNull(date)
        assertNotNull(type)
    }

    @TableTest(
        """
        List      | size? | sum?
        []        | 0     | 0
        [1]       | 1     | 1
        [3, 2, 1] | 3     | 6
        """
    )
    fun integerListTypeConversion(list: List<Int>, expectedSize: Int, expectedSum: Int) {
        assertEquals(expectedSize, list.size)
        assertEquals(expectedSum, list.sum())
        list.forEach { assertInstanceOf(Int::class.javaObjectType, it) }
    }

    @TableTest(
        """    
        List              | size? | expectedElementType?
        []                | 0     | java.lang.String
        [one element]     | 1     | java.lang.String
        ["element, one"]  | 1     | java.lang.String
        ['element, one']  | 1     | java.lang.String
        ["def, abc", ghi] | 2     | java.lang.String
        ['def, abc', ghi] | 2     | java.lang.String
        [abc, def]        | 2     | java.lang.String
        """
    )
    fun stringListTypeConversion(list: List<String>, expectedSize: Int, expectedElementType: Class<*>) {
        assertEquals(expectedSize, list.size)
        list.forEach { assertInstanceOf(expectedElementType, it) }
    }

    @TableTest(
        """
        List                   | size? | expectedElementType?
        []                     | 0     | java.util.List
        [[1, 2, 3], [a, b, c]] | 2     | java.util.List
        """
    )
    fun nestedListTypeConversion(list: List<List<*>>, expectedSize: Int, expectedElementType: Class<*>) {
        assertEquals(expectedSize, list.size)
        list.forEach { assertInstanceOf(expectedElementType, it) }
    }

    @TableTest(
        """
        Map                                 | Size?
        [:]                                 | 0
        [one: element]                      | 1
        [one: "element, one"]               | 1
        [one: 'element, one']               | 1
        [one: "def, abc", two: ghi]         | 2
        [one: 'def, abc', two: ghi]         | 2
        [one: abc, two: def]                | 2
        [one: [abc], two: [def, ghi]]       | 2
        [one: [a: bc], two: [d: ef, g: hi]] | 2
        [1: one, 2: two, 3: three]          | 3
        """
    )
    fun mapTypeConversion(map: Map<String, Object>, expectedSize: Int) {
        assertEquals(expectedSize, map.size)
    }

    @TableTest(
        """
        input     | size?
        []        | 0
        [1]       | 1
        // [1, 2]    | 2
        [1, 2, 3] | 3
        """
    )
    fun testComments(input: List<Integer>, expectedSize: Int) {
        assertNotEquals(2, expectedSize)
        assertNotEquals(2, input.size)
    }

    @TableTest(resource = "external.table")
    fun testExternalTable(a: Int, b: Int, expectedSum: Int) {
        assertEquals(expectedSum, a + b)
    }

    @TableTest(resource = "/subfolder/custom_encoding.table", encoding = "ISO-8859-1")
    fun testExternalTableInSubfolder(string: String, expectedLength: Int) {
        assertEquals(expectedLength, string.length)
    }

    @TableTest(
        """
        Scenario     | a | b
        Zero is zero | 0 | 0
        One is two   | 1 | 2
        Two is four  | 2 | 4
        """
    )
    fun testScenarioName(a: Int, b: Int) {
        assertEquals(b, 2 * a)
    }

    @TableTest(
        """
        Scenario            | String | Integer | List | Map | Set
        Blank               |        |         |      |     |
        Empty single quoted | ''     | ''      | ''   | ''  | ''
        Empty double quoted | ""     | ""      | ""   | ""  | ""
        """
    )
    fun testBlankIsNullForNonString(
        string: String?,
        integer: Int?,
        list: List<*>?,
        map: Map<String, *>?,
        set: Set<*>?
    ) {
        assertEquals("", string)
        assertNull(integer)
        assertNull(list)
        assertNull(map)
        assertNull(set)
    }

    @TableTest(
        """
        Scenario                      | a                | b | c | d         | e
        Anything multiplied by 0 is 0 | {-1, 0, 1, 1000} | 0 | 0 | {1, 2, 3} | 3
        """
    )
    fun testApplicableValueSet(a: Int, b: Int, c: Int, d: Set<Int>, e: Int) {
        assertEquals(c, a * b)
        assertEquals(e, d.size)
    }

    @TableTest(
        """
        Person                 | AgeCategory?
        [name: Fred, age: 22]  | ADULT
        [name: Wilma, age: 19] | TEEN
        """
    )
    fun testConvertWith(@ConvertWith(PersonConverter::class) person: Person, expectedAgeCategory: AgeCategory) {
        assertEquals(expectedAgeCategory, person.ageCategory())
    }

    data class Person(val firstName: String, val lastName: String, val age: Int) {
        fun ageCategory(): AgeCategory {
            return AgeCategory.of(age)
        }
    }

    enum class AgeCategory {
        CHILD, TEEN, ADULT;

        companion object {
            fun of(age: Int): AgeCategory {
                if (age < 13) return CHILD
                if (age < 20) return TEEN
                return ADULT
            }
        }
    }

    class PersonConverter : ArgumentConverter {

        override fun convert(
            source: Any?,
            context: ParameterContext?
        ): Any? {
            if (source is Map<*, *>) {
                return Person(
                    source.getOrDefault("name", "Fred") as String,
                    "Flintstone",
                    (source.getOrDefault("age", 16) as String).toInt()
                )
            }
            throw ArgumentConversionException("Cannot convert " + source?.javaClass?.simpleName + " to Person")
        }
    }

}
