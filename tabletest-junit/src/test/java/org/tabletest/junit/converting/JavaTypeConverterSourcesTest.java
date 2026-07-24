package org.tabletest.junit.converting;

import org.tabletest.junit.TableTest;
import org.tabletest.junit.TypeConverter;
import org.tabletest.junit.TypeConverterSources;
import org.tabletest.junit.javatypeconverters.FirstTierTypeConverterSource;
import org.tabletest.junit.javatypeconverters.SecondTierTypeConverterSource;
import org.tabletest.junit.javadomain.Age;
import org.tabletest.junit.javadomain.Ages;
import org.tabletest.junit.kotlintypeconverters.KotlinTypeConverterSourceForNestedClass;
import org.junit.jupiter.api.Nested;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TypeConverterSources({FirstTierTypeConverterSource.class, SecondTierTypeConverterSource.class})
public class JavaTypeConverterSourcesTest {

    @TableTest("""
        Int | List | Set  | AVS  | Map       | Nested           | Ages
        16  | [16] | {16} | {16} | [age: 16] | [ages: [16, 16]] | [ages: [16, 16]]
        """)
    void using_type_converters_in_converter_source(
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

    @TableTest("""
        This Date  | Other Date | Is Before?
        today      | tomorrow   | true
        today      | yesterday  | false
        2024-02-29 | 2024-03-01 | true
        """)
    void overriding_converter_source(LocalDate thisDate, LocalDate otherDate, boolean expectedIsBefore) {
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

    @TypeConverterSources(KotlinTypeConverterSourceForNestedClass.class)
    @Nested
    public class NestedTestClass {

        @TableTest("""
            Age | Expected
            16  | 17
            """)
        void type_converter_in_nested_class_takes_precedence_over_converter_source(
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
            This Date | Other Date | Is Before?
            today     | tomorrow   | true
            today     | yesterday  | false
            """)
        void type_converter_in_outer_class_takes_precedence_over_converter_source(
            LocalDate thisDate,
            LocalDate otherDate,
            boolean expectedIsBefore
        ) {
            assertEquals(expectedIsBefore, thisDate.isBefore(otherDate));
        }


        @Nested
        public class DeeplyNestedTestClass {

            @TableTest("""
                Age | Expected
                16  | 18
                """)
            void type_converter_in_deeply_nested_class_takes_precedence(
                Age fromTypeConverterInThisClass,
                int expectedAgeAfterConversion
            ) {
                Age expected = new Age(expectedAgeAfterConversion);
                assertEquals(expected, fromTypeConverterInThisClass);
            }

            @TypeConverter
            @SuppressWarnings("unused")
            public static Age parseAgePlusTwo(int age) {
                return new Age(age + 2);
            }

            @TableTest("""
                Ages             | Expected
                [ages: [16, 16]] | 28
                """)
            void type_converter_in_closest_outer_converter_source_takes_precedence_over_outermost_converter_source(
                Ages fromTypeConverterInAnnotationClass,
                int expectedAgeAfterConversion
            ) {
                Age expected = new Age(expectedAgeAfterConversion);
                assertEquals(
                    new Ages(List.of(expected, expected)),
                    fromTypeConverterInAnnotationClass);
            }

        }
    }
}
