package org.tabletest.junit.converting;

import org.tabletest.junit.TableTest;
import org.tabletest.junit.TypeConverter;
import org.tabletest.junit.javadomain.Age;
import org.tabletest.junit.javadomain.Ages;
import org.junit.jupiter.api.Nested;
import org.junit.platform.commons.support.conversion.ConversionException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaFactoryMethodInTestClassConversionTest extends JavaTestSuperClass {

    @TableTest("""
        Int | List | Set  | AVS  | Map       | Nested
        16  | [16] | {16} | {16} | [age: 16] | [ages: [16, 16]]
        """)
    void using_factory_method_in_test_class(
        Age fromInt,
        List<Age> inList,
        Set<Age> inSet,
        Age fromValueSet,
        Map<String, Age> inMap,
        Map<String, List<Age>> inNested
    ) {
        Age expected = new Age(16);
        assertEquals(expected, fromInt);
        assertEquals(List.of(expected), inList);
        assertEquals(Set.of(expected), inSet);
        assertEquals(expected, fromValueSet);
        assertEquals(Map.of("age", expected), inMap);
        assertEquals(Map.of("ages", List.of(expected, expected)), inNested);
    }

    @TypeConverter
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

    @TableTest("""
        Ages
        [ages: [16, 16]]
        """)
    void using_factory_method_in_super_class(Ages inSuperClass) {
        Age expected = new Age(16);
        assertEquals(new Ages(List.of(expected, expected)), inSuperClass);
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

    @TypeConverter
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
            Age | Expected
            16  | 17
            """)
        void factory_method_in_nested_class_takes_precedence(
            Age fromFactoryMethodInThisClass,
            int expectedAgeAfterConversion
        ) {
            Age expected = new Age(expectedAgeAfterConversion);
            assertEquals(expected, fromFactoryMethodInThisClass);
        }

        @TypeConverter
        @SuppressWarnings("unused")
        public static Age parseAgePlusOne(int age) {
            return new Age(age + 1);
        }

        @TableTest("""
            Ages             | Expected
            [ages: [16, 16]] | 17
            """)
        void factory_method_inherited_in_nested_class(
            Ages fromFactoryMethodInheritedByEnclosingClass,
            int expectedAgeAfterConversion
        ) {
            Age expected = new Age(expectedAgeAfterConversion);
            assertEquals(
                new Ages(List.of(expected, expected)),
                fromFactoryMethodInheritedByEnclosingClass
            );
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
                Age | Expected
                16  | 18
                """)
            void factory_method_in_nested_class_takes_precedence(
                Age fromFactoryMethodInThisClass,
                int expectedAgeAfterConversion
            ) {
                Age expected = new Age(expectedAgeAfterConversion);
                assertEquals(expected, fromFactoryMethodInThisClass);
            }

            @TypeConverter
            @SuppressWarnings("unused")
            public static Age parseAgePlusTwo(int age) {
                return new Age(age + 2);
            }

        }
    }
}
