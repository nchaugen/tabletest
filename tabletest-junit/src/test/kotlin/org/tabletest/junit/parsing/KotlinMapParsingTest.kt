package org.tabletest.junit.parsing

import org.tabletest.junit.TableTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf

class KotlinMapParsingTest {

    @TableTest(resource = "/parsing/map-values.table")
    fun testMapValueParsing(map: Map<*, *>, expectedSize: Int, valueType: Class<*>) {
        assertEquals(expectedSize, map.size, "Map: $map")
        if (map.isNotEmpty()) {
            val firstEntry = map.entries.first()
            assertInstanceOf(String::class.java, firstEntry.key, "First key type for: $map")
            assertInstanceOf(valueType, firstEntry.value, "First value type for: $map")
        }
    }

    @TableTest("""    
        Scenario               | Input Map Value          | Size? | First Key?  | First Value?
        Basic alphanumeric key | [abc123: value]          | 1     | abc123      | value
        Key with underscore    | [key_name: value]        | 1     | key_name    | value
        Key with dash          | [key-name: value]        | 1     | key-name    | value
        Key with dot           | [config.host: localhost] | 1     | config.host | localhost
        Numeric key            | [123: value]             | 1     | 123         | value
        Single character key   | [a: value]               | 1     | a           | value
        """)
    fun testValidUnquotedMapKeys(map: Map<String, String>, expectedSize: Int, firstKey: String, firstValue: String) {
        assertEquals(expectedSize, map.size)
        assertEquals(firstValue, map[firstKey])
    }

    @TableTest("""    
        Scenario                         | Input Map Value              | Size? | First Key? | First Value?
        Value with quotes                | [key: "quoted value"]        | 1     | key        | quoted value
        Value with single quotes         | [key: 'single quoted']       | 1     | key        | single quoted
        Value with special chars         | [key: "has, special: chars"] | 1     | key        | has, special: chars
        Empty quoted value               | [key: ""]                    | 1     | key        | ""
        Mixed quotes in different values | [a: "double", b: 'single']   | 2     | a          | double
        """)
    fun testQuotedMapValues(map: Map<String, String>, expectedSize: Int, firstKey: String, firstValue: String) {
        assertEquals(expectedSize, map.size)
        assertEquals(firstValue, map[firstKey])
    }

    @TableTest("""    
        Scenario              | Input                                  | Size? | Value Type?
        Map with list values  | [items: [a, b], tags: [x, y]]          | 2     | java.util.List
        Map with set values   | [active: {true}, ids: {1, 2, 3}]       | 2     | java.util.Set
        Map with nested maps  | [config: [host: local], env: [dev: x]] | 2     | java.util.Map
        Mixed compound values | [list: [a], set: {b}, map: [c: d]]     | 3     | java.lang.Object
        Deep nesting          | [level1: [level2: [level3: value]]]    | 1     | java.util.Map
        """)
    fun testCompoundMapValues(map: Map<String, *>, expectedSize: Int, expectedValueType: Class<*>) {
        assertEquals(expectedSize, map.size)
        if (map.isNotEmpty()) {
            val firstValue = map.values.first()
            assertInstanceOf(expectedValueType, firstValue)
        }
    }
}
