package io.github.nchaugen.tabletest.junit.converting;

import io.github.nchaugen.tabletest.junit.TableTest;
import io.github.nchaugen.tabletest.junit.javadomain.Age;
import io.github.nchaugen.tabletest.junit.javadomain.Ages;
import org.junit.jupiter.api.Nested;
import org.junit.platform.commons.support.conversion.ConversionException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaFactoryMethodInTestClassConversionTest {

    @TableTest("""
        Int | List | Set  | AVS  | Map       | Nested           | Ages
        16  | [16] | {16} | {16} | [age: 16] | [ages: [16, 16]] | [ages: [16, 16]]
        """)
    void using_factory_method_in_test_class(
        Age fromInt,
        List<Age> inList,
        Set<Age> inSet,
        Age fromValueSet,
        Map<String, Age> inMap,
        Map<String, List<Age>> inNested,
        Ages inOtherFactoryMethod
    ) {
        Age expected = new Age(16);
        assertEquals(expected, fromInt);
        assertEquals(List.of(expected), inList);
        assertEquals(Set.of(expected), inSet);
        assertEquals(expected, fromValueSet);
        assertEquals(Map.of("age", expected), inMap);
        assertEquals(Map.of("ages", List.of(expected, expected)), inNested);
        assertEquals(new Ages(List.of(expected, expected)), inOtherFactoryMethod);
    }

    @SuppressWarnings("unused")
    public static Age parseAge(int age) {
        return new Age(age);
    }

    @SuppressWarnings("unused")
    public Age parseAge(Integer age) {
        throw new ConversionException("Factory method not static, should not be called");
    }

    @SuppressWarnings("unused")
    static Age parseAge(Long age) {
        throw new ConversionException("Factory method not accessible, should not be called");
    }

    @SuppressWarnings("unused")
    public static Ages parseAges(Map<String, List<Age>> age) {
        return new Ages(age.get("ages"));
    }

    @TableTest("""
        This Date  | Other Date | Is Before?
        today      | tomorrow   | true
        today      | yesterday  | false
        2024-02-29 | 2024-03-01 | true
        """)
    void overriding_fallback_conversion(LocalDate thisDate, LocalDate otherDate, boolean expectedIsBefore) {
        assertEquals(expectedIsBefore, thisDate.isBefore(otherDate));
    }

    @SuppressWarnings("unused")
    public static LocalDate parseLocalDate(String input) {
        return switch (input) {
            case "yesterday" -> LocalDate.parse("2025-06-06");
            case "today" -> LocalDate.parse("2025-06-07");
            case "tomorrow" -> LocalDate.parse("2025-06-08");
            default -> LocalDate.parse(input);
        };
    }

    @Nested
    public class NestedTestClass {

        @TableTest("""
            This Date  | Other Date | Is Before?
            today      | tomorrow   | true
            today      | yesterday  | false
            2024-02-29 | 2024-03-01 | true
            """)
        void finding_factory_method_from_nested_class(
            LocalDate thisDate,
            LocalDate otherDate,
            boolean expectedIsBefore
        ) {
            assertEquals(expectedIsBefore, thisDate.isBefore(otherDate));
        }

        @TableTest("""
        Age | Ages | Expected
        16  | [ages: [16, 16]] | 17
        """)
        void factory_method_in_nested_class_takes_precedence(
            Age fromFactoryMethodInThisClass,
            Ages fromFactoryMethodInEnclosingClass,
            int expectedAgeAfterConversion
        ) {
            Age expected = new Age(expectedAgeAfterConversion);
            assertEquals(expected, fromFactoryMethodInThisClass);
            assertEquals(
                new Ages(List.of(expected, expected)),
                fromFactoryMethodInEnclosingClass);
        }

        @SuppressWarnings("unused")
        public static Age parseAgePlusOne(int age) {
            return new Age(age + 1);
        }


        @Nested
        public class DeeplyNestedTestClass {

            @TableTest("""
            This Date  | Other Date | Is Before?
            today      | tomorrow   | true
            today      | yesterday  | false
            2024-02-29 | 2024-03-01 | true
            """)
            void finding_factory_method_from_deeply_nested_class(
                LocalDate thisDate,
                LocalDate otherDate,
                boolean expectedIsBefore
            ) {
                assertEquals(expectedIsBefore, thisDate.isBefore(otherDate));
            }

            @TableTest("""
                Age | Ages             | Expected
                16  | [ages: [16, 16]] | 18
                """)
            void factory_method_in_nested_class_takes_precedence(
                Age fromFactoryMethodInThisClass,
                Ages fromFactoryMethodInEnclosingClass,
                int expectedAgeAfterConversion
            ) {
                Age expected = new Age(expectedAgeAfterConversion);
                assertEquals(expected, fromFactoryMethodInThisClass);
                assertEquals(
                    new Ages(List.of(expected, expected)),
                    fromFactoryMethodInEnclosingClass);
            }

            @SuppressWarnings("unused")
            public static Age parseAgePlusTwo(int age) {
                return new Age(age + 2);
            }

        }
    }
}
