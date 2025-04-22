package io.github.nchaugen.tabletest;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;

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
    void singleValueTypeConversion(int integer, String string, LocalDate date, Class<?> type) {
        assertNotEquals(0, integer);
        assertNotNull(string);
        assertNotNull(date);
        assertNotNull(type);
    }

    @TableTest("""
        List      | size? | sum?
        []        | 0     | 0
        [1]       | 1     | 1
        [3, 2, 1] | 3     | 6
        """)
    void integerListTypeConversion(List<Integer> list, int expectedSize, int expectedSum) {
        assertEquals(expectedSize, list.size());

        assertEquals(
            expectedSum, list.stream()
                .mapToInt(Integer::intValue)
                .sum()
        );

        if (!list.isEmpty()) assertInstanceOf(Integer.class, list.getFirst());
    }

    @TableTest("""
        List              | size? | expectedElementType?
        []                | 0     | java.lang.String
        [one element]     | 1     | java.lang.String
        ["element, one"]  | 1     | java.lang.String
        ['element, one']  | 1     | java.lang.String
        ["def, abc", ghi] | 2     | java.lang.String
        ['def, abc', ghi] | 2     | java.lang.String
        [abc, def]        | 2     | java.lang.String
        """)
    void stringListTypeConversion(List<String> list, int expectedSize, Class<?> expectedElementType) {
        assertEquals(expectedSize, list.size());
        if (!list.isEmpty())
            assertInstanceOf(expectedElementType, list.getFirst());
    }

    @TableTest("""
        List                   | size? | expectedElementType?
        []                     | 0     | java.util.List
        [[1, 2, 3], [a, b, c]] | 2     | java.util.List
        """)
    void nestedListTypeConversion(List<List<?>> list, int expectedSize, Class<?> expectedElementType) {
        assertEquals(expectedSize, list.size());
        if (!list.isEmpty())
            assertInstanceOf(expectedElementType, list.getFirst());
    }

    @TableTest("""
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
        """)
    void mapTypeConversion(Map<String, Object> map, int expectedSize) {
        assertEquals(expectedSize, map.size());
    }

    @TableTest("""
        input     | size?
        []        | 0
        [1]       | 1
        // [1, 2]    | 2
        [1, 2, 3] | 3
        """)
    void testComments(List<Integer> input, int expectedSize) {
        assertNotEquals(2, expectedSize);
        assertNotEquals(2, input.size());
    }

    @TableTest("""
        Person                 | AgeCategory?
        [name: Fred, age: 22]  | ADULT
        [name: Wilma, age: 19] | TEEN
        """)
    void testMapToObject(@ConvertWith(PersonConverter.class) Person person, AgeCategory expectedAgeCategory) {
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

    private static class PersonConverter implements ArgumentConverter {
        @SuppressWarnings("rawtypes")
        @Override
        public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
            if (source instanceof Map attributes) {
                return new Person(
                    (String) attributes.getOrDefault("name", "Fred"),
                    "Flintstone",
                    Integer.parseInt((String) attributes.getOrDefault("age", "16"))
                );
            }
            throw new ArgumentConversionException("Cannot convert " + source.getClass().getSimpleName() + " to Person");
        }
    }
}
