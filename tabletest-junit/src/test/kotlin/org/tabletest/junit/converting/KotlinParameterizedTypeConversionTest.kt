package org.tabletest.junit.converting

import org.tabletest.junit.TableTest
import kotlin.test.assertEquals

class KotlinParameterizedTypeConversionTest {

    @TableTest(
        """    
        Empty | Byte | Integer | Long | Double | String | List  | Map
        []    | [1]  | [2]     | [3]  | [4]    | [5]    | [[6]] | [[1: 7]]
        """
    )
    fun converts_to_parameterized_list_types(
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

    @TableTest(
        """    
        Empty | Byte     | Integer  | Long     | Double   | String   | List       | Map
        [:]   | [key: 1] | [key: 2] | [key: 3] | [key: 4] | [key: 5] | [key: [6]] | [key: [1: 7]]
        """
    )
    fun converts_to_parameterized_map_types(
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

    @TableTest(
        """    
        Empty | Byte    | Integer | Long    | Double  | String  | List      | Map
        {}    | {1}     | {2}     | {3}     | {4}     | {5}     | {[6]}     | {[1: 7]}
        """
    )
    fun converts_to_parameterized_set_types(
        emptySet: Set<*>,
        byteSet: Set<Byte>,
        integerSet: Set<Int>,
        longSet: Set<Long>,
        doubleSet: Set<Double>,
        stringSet: Set<String>,
        listSet: Set<List<Short>>,
        mapSet: Set<Map<*, Long>>
    ) {
        assertEquals(emptySet<Any>(), emptySet)
        assertEquals(setOf(1.toByte()), byteSet)
        assertEquals(setOf(2), integerSet)
        assertEquals(setOf(3L), longSet)
        assertEquals(setOf(4.0), doubleSet)
        assertEquals(setOf("5"), stringSet)
        assertEquals(setOf(listOf(6.toShort())), listSet)
        assertEquals(setOf(mapOf("1" to 7L)), mapSet)
    }

}
