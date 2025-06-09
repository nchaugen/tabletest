package io.github.nchaugen.tabletest.junit.converting;

import io.github.nchaugen.tabletest.junit.TableTest;
import io.github.nchaugen.tabletest.junit.TableTestConverters;
import io.github.nchaugen.tabletest.junit.javadomain.Age;
import io.github.nchaugen.tabletest.junit.javadomain.Ages;
import io.github.nchaugen.tabletest.junit.javaconverters.FirstTierConverters;
import io.github.nchaugen.tabletest.junit.javaconverters.SecondTierConverters;
import org.junit.jupiter.api.Nested;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TableTestConverters({FirstTierConverters.class, SecondTierConverters.class})
public class JavaFactoryMethodInAnnotationClassConversionTest {

    @TableTest("""
        Int | List | Set  | AVS  | Map       | Nested           | Ages
        16  | [16] | {16} | {16} | [age: 16] | [ages: [16, 16]] | [ages: [16, 16]]
        """)
    void using_factory_methods_in_annotation_class(
        Age fromInt,
        List<Age> inList,
        Set<Age> inSet,
        Age fromApplicableSet,
        Map<String, Age> inMap,
        Map<String, List<Age>> inNested,
        Ages inOtherFactoryMethod
    ) {
        Age expected = new Age(16);
        assertEquals(expected, fromInt);
        assertEquals(List.of(expected), inList);
        assertEquals(Set.of(expected), inSet);
        assertEquals(expected, fromApplicableSet);
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
    void overriding_annotation_conversion(LocalDate thisDate, LocalDate otherDate, boolean expectedIsBefore) {
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
        Age | Ages             | Expected
        16  | [ages: [16, 16]] | 17
        """)
        void factory_method_in_nested_class_takes_precedence_over_annotation_method(
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
                Age | Ages             | Expected
                16  | [ages: [16, 16]] | 18
                """)
            void factory_method_in_deeply_nested_class_takes_precedence(
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
