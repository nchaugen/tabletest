package io.github.nchaugen.tabletest.junit.converting;

import io.github.nchaugen.tabletest.junit.TableTest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaParameterizedTypeConversionTest {

    @TableTest("""
        Empty | Byte | Integer | Long | Double | String | List  | Map
        []    | [1]  | [2]     | [3]  | [4]    | [5]    | [[6]] | [[1: 7]]
        """)
    void converts_to_parameterized_list_types(
        List<?> emptyList,
        List<Byte> byteList,
        List<Integer> integerList,
        List<Long> longList,
        List<Double> doubleList,
        List<String> stringList,
        List<List<Short>> listList,
        List<Map<?, Long>> mapList
    ) {
        assertEquals(List.of(), emptyList);
        assertEquals(List.of((byte) 1), byteList);
        assertEquals(List.of(2), integerList);
        assertEquals(List.of(3L), longList);
        assertEquals(List.of(4.0), doubleList);
        assertEquals(List.of("5"), stringList);
        assertEquals(List.of(List.of((short) 6)), listList);
        assertEquals(List.of(Map.of("1", 7L)), mapList);
    }

    @TableTest("""
        Empty | Byte     | Integer  | Long     | Double   | String   | List       | Map
        [:]   | [key: 1] | [key: 2] | [key: 3] | [key: 4] | [key: 5] | [key: [6]] | [key: [1: 7]]
        """)
    void converts_to_parameterized_map_types(
        Map<?, ?> emptyMap,
        Map<String, Byte> byteMap,
        Map<?, Integer> integerMap,
        Map<?, Long> longMap,
        Map<?, Double> doubleMap,
        Map<?, String> stringMap,
        Map<?, List<Short>> listMap,
        Map<?, Map<?, Long>> mapMap
    ) {
        assertEquals(Map.of(), emptyMap);
        assertEquals(Map.of("key", (byte) 1), byteMap);
        assertEquals(Map.of("key", 2), integerMap);
        assertEquals(Map.of("key", 3L), longMap);
        assertEquals(Map.of("key", 4.0), doubleMap);
        assertEquals(Map.of("key", "5"), stringMap);
        assertEquals(Map.of("key", List.of((short) 6)), listMap);
        assertEquals(Map.of("key", Map.of("1", 7L)), mapMap);
    }

    @TableTest("""
        Empty | Byte    | Integer | Long    | Double  | String  | List      | Map
        {}    | {1}     | {2}     | {3}     | {4}     | {5}     | {[6]}     | {[1: 7]}
        """)
    void converts_to_parameterized_set_types(
        Set<?> emptySet,
        Set<Byte> byteSet,
        Set<Integer> integerSet,
        Set<Long> longSet,
        Set<Double> doubleSet,
        Set<String> stringSet,
        Set<List<Short>> listSet,
        Set<Map<?, Long>> mapSet
    ) {
        assertEquals(Set.of(), emptySet);
        assertEquals(Set.of((byte) 1), byteSet);
        assertEquals(Set.of(2), integerSet);
        assertEquals(Set.of(3L), longSet);
        assertEquals(Set.of(4.0), doubleSet);
        assertEquals(Set.of("5"), stringSet);
        assertEquals(Set.of(List.of((short) 6)), listSet);
        assertEquals(Set.of(Map.of("1", 7L)), mapSet);
    }

}
