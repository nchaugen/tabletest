package io.github.nchaugen.tabletest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TableTestTest {

    @TableTest("""
        Int | String | Date       | Class
        1   | abc    | 2025-01-20 | java.lang.Integer
        """)
    void testColumnTypes(int integer, String string, LocalDate date, Class<?> type) {
        assertNotEquals(0, integer);
        assertNotNull(string);
        assertNotNull(date);
        assertNotNull(type);
    }

    @TableTest("""
        List                   | size? | sum?
        []                     | 0     | 0
        [1]                    | 1     | 1
        [3, 2, 1]              | 3     | 6
        """)
    void testIntegerListColumn(List<Integer> list, int expectedSize, int expectedSum) {
        assertEquals(expectedSize, list.size());

        assertEquals(expectedSum, list.stream()
            .mapToInt(Integer::intValue)
            .sum());

        if (!list.isEmpty()) assertInstanceOf(Integer.class, list.getFirst());
    }

    @TableTest("""
        List                   | size? | expectedElementType?
        []                     | 0     | java.lang.String
        [one element]          | 1     | java.lang.String
        ["element, one"]       | 1     | java.lang.String
        ["def, abc", ghi]      | 2     | java.lang.String
        [abc, def]             | 2     | java.lang.String
        """)
    void testStringListColumn(List<String> list, int expectedSize, Class<?> expectedElementType) {
        assertEquals(expectedSize, list.size());
        if (!list.isEmpty())
            assertInstanceOf(expectedElementType, list.getFirst());
    }

    @TableTest("""
        List                   | size? | expectedElementType?
        []                     | 0     | java.util.List
        [[1, 2, 3], [a, b, c]] | 2     | java.util.List
        """)
    void testListListColumn(List<Object> list, int expectedSize, Class<?> expectedElementType) {
        assertEquals(expectedSize, list.size());
        if (!list.isEmpty())
            assertInstanceOf(expectedElementType, list.getFirst());
    }

    @TableTest("""
        Map                         | Size?
        [:]                         | 0
        [one: element]              | 1
        [one: "element, one"]       | 1
        [one: "def, abc", two: ghi] | 2
        [one: abc, two: def]        | 2
        [1: one, 2: two, 3: three]  | 3
        """)
    void testMapColumn(Map<String, Object> map, int expectedSize) {
        assertEquals(expectedSize, map.size());
    }

    @TableTest("""
        Person                 | AgeCategory?
        [name: Fred, age: 22]  | ADULT
        [name: Wilma, age: 19] | TEEN
        """)
    void testMapToObject(Map<String, Object> personAttributes, AgeCategory expectedAgeCategory) {
        Person person = new Person(
            (String) personAttributes.getOrDefault("name", "Barry"),
            "Flintstone",
            (Integer) personAttributes.getOrDefault("age", 16)
        );

        assertEquals(expectedAgeCategory, person.ageCategory());
    }

    record Person(String firstName, String lastName, int age) {
        AgeCategory ageCategory() {
            return AgeCategory.of(age);
        }
    }

    enum AgeCategory {
        CHILD, TEEN, ADULT;

        static AgeCategory of(int age) {
            if (age < 13) return AgeCategory.CHILD;
            if (age < 20) return AgeCategory.TEEN;
            return AgeCategory.ADULT;
        }

    }
}
