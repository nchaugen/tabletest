package io.github.nchaugen.tabletest.junit.converting;

import io.github.nchaugen.tabletest.junit.TableTest;
import io.github.nchaugen.tabletest.junit.FactorySources;
import io.github.nchaugen.tabletest.junit.javafactories.FirstTierFactorySource;
import io.github.nchaugen.tabletest.junit.javafactories.SecondTierFactorySource;
import io.github.nchaugen.tabletest.junit.javadomain.Age;
import io.github.nchaugen.tabletest.junit.javadomain.Ages;
import io.github.nchaugen.tabletest.junit.kotlinfactories.KotlinFactorySourceForNestedClass;
import org.junit.jupiter.api.Nested;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@FactorySources({FirstTierFactorySource.class, SecondTierFactorySource.class})
public class JavaFactorySourcesTest {

    @TableTest("""
        Int | List | Set  | AVS  | Map       | Nested           | Ages
        16  | [16] | {16} | {16} | [age: 16] | [ages: [16, 16]] | [ages: [16, 16]]
        """)
    void using_factory_methods_in_factory_source(
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
    void overriding_factory_source(LocalDate thisDate, LocalDate otherDate, boolean expectedIsBefore) {
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

    @FactorySources(KotlinFactorySourceForNestedClass.class)
    @Nested
    public class NestedTestClass {

        @TableTest("""
            Age | Expected
            16  | 17
            """)
        void factory_method_in_nested_class_takes_precedence_over_factory_source(
            Age fromFactoryMethodInThisClass,
            int expectedAgeAfterConversion
        ) {
            Age expected = new Age(expectedAgeAfterConversion);
            assertEquals(expected, fromFactoryMethodInThisClass);
        }

        @SuppressWarnings("unused")
        public static Age parseAgePlusOne(int age) {
            return new Age(age + 1);
        }

        @TableTest("""
            This Date | Other Date | Is Before?
            today     | tomorrow   | true
            today     | yesterday  | false
            """)
        void factory_method_in_outer_class_takes_precedence_over_factory_source(
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
            void factory_method_in_deeply_nested_class_takes_precedence(
                Age fromFactoryMethodInThisClass,
                int expectedAgeAfterConversion
            ) {
                Age expected = new Age(expectedAgeAfterConversion);
                assertEquals(expected, fromFactoryMethodInThisClass);
            }

            @SuppressWarnings("unused")
            public static Age parseAgePlusTwo(int age) {
                return new Age(age + 2);
            }

            @TableTest("""
                Ages             | Expected
                [ages: [16, 16]] | 28
                """)
            void factory_method_in_closest_outer_factory_source_takes_precedence_over_outermost_factory_source(
                Ages fromFactoryMethodInAnnotationClass,
                int expectedAgeAfterConversion
            ) {
                Age expected = new Age(expectedAgeAfterConversion);
                assertEquals(
                    new Ages(List.of(expected, expected)),
                    fromFactoryMethodInAnnotationClass);
            }

        }
    }
}
