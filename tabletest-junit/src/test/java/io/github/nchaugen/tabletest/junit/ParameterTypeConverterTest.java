package io.github.nchaugen.tabletest.junit;

import io.github.nchaugen.tabletest.junit.exampledomain.Age;
import io.github.nchaugen.tabletest.junit.exampledomain.Ages;
import io.github.nchaugen.tabletest.junit.exampledomain.ConstructorDate;
import io.github.nchaugen.tabletest.junit.exampledomain.FactoryMethodDate;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.support.conversion.ConversionException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.nchaugen.tabletest.junit.ParameterFixture.parameter;
import static io.github.nchaugen.tabletest.junit.ParameterTypeConverter.convertValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

@TableTestConverters(ExampleDomainConverters.class)
class ParameterTypeConverterTest {

    @Nested
    class SingleValues {

        @TableTest("""
            table value | parameter type
            0.1         | java.lang.Byte
            256         | java.lang.Byte
            abc         | java.lang.Character
            """)
        void fails_conversion_for_values_outside_type_range(
            String value,
            Class<?> type
        ) {
            assertThrows(
                ConversionException.class,
                () -> convertValue(value, parameter(type))
            );
        }

    }

    @Nested
    class ListValues {

        @Test
        void fails_when_conversion_not_possible() {
            Map.of(
                "java.util.List<java.lang.Byte>", List.of("x")
            ).forEach((String typeName, List<?> parsedListValue) ->
                          assertThrows(
                              ConversionException.class,
                              () -> convertValue(parsedListValue, parameter(typeName)),
                              () -> "Expected failure for " + typeName
                          )
            );
        }

        @TableTest("""
        list | nested list | nested set | nested map
        []   | [[]]        | [{}]       | [[:]]
        """)
        void passes_immutable_lists_to_test(
            List<String> list,
            List<List<String>> nestedList,
            List<Set<String>> nestedSet,
            List<Map<String, String>> nestedMap
        ) {
            try {
                list.add("x");
                fail("modifying collections from the table should fail");
            } catch (Exception e) {
                // expected
            }
            try {
                nestedList.getFirst().add("x");
                fail("modifying collections from the table should fail");
            } catch (Exception e) {
                // expected
            }
            try {
                nestedSet.getFirst().add("x");
                fail("modifying collections from the table should fail");
            } catch (Exception e) {
                // expected
            }
            try {
                nestedMap.getFirst().put("x", "y");
                fail("modifying collections from the table should fail");
            } catch (Exception e) {
                // expected
            }
        }

    }

    @Nested
    class MapValues {

        @Test
        void fails_when_conversion_not_possible() {
            Map.of(
                "java.util.Map<?, java.lang.Short>", Map.of("key", "x")
            ).forEach((String typeName, Map<?, ?> parsedMapValue) ->
                          assertThrows(
                              ConversionException.class,
                              () -> convertValue(parsedMapValue, parameter(typeName)),
                              () -> "Expected failure for " + typeName
                          )
            );
        }

        @TableTest("""
        map | nested list | nested set  | nested map
        [:] | [empty: []] | [empty: {}] | [empty: [:]]
        """)
        void passes_immutable_maps_to_test(
            Map<String, String> map,
            Map<String, List<String>> nestedList,
            Map<String, Set<String>> nestedSet,
            Map<String, Map<String, String>> nestedMap
        ) {
            try {
                map.put("x", "y");
                fail("modifying collections from the table should fail");
            } catch (Exception e) {
                // expected
            }
            try {
                nestedList.get("empty").add("x");
                fail("modifying collections from the table should fail");
            } catch (Exception e) {
                // expected
            }
            try {
                nestedSet.get("empty").add("x");
                fail("modifying collections from the table should fail");
            } catch (Exception e) {
                // expected
            }
            try {
                nestedMap.get("empty").put("x", "y");
                fail("modifying collections from the table should fail");
            } catch (Exception e) {
                // expected
            }
        }


    }

    @Nested
    class SetValues {

        @TableTest("""
        Scenario                  | adding any of | to set    | makes size? | is set null? | contains null?
        Adding existing values    | {1, 2, 3}     | {1, 2, 3} | 3           | false        | false
        Adding other values       | {4, 5, 6}     | {1, 2, 3} | 4           | false        | false
        Adding no values          | {}            | {1, 2, 3} | 4           | false        | true
        Adding nothing to nothing |               |           |             | true         | false
        """)
        void applicable_value_sets(
            Integer a,
            Set<Integer> b,
            Integer expectedSize,
            boolean expectedNull,
            boolean containsNull
        ) {
            if (expectedNull) {
                assertNull(b);
            } else {
                Set<Integer> result = new HashSet<>(b);
                result.add(a);
                assertEquals(expectedSize, result.size());
                assertEquals(containsNull, result.contains(null));
            }
        }

        @TableTest("""
        set | nested list | nested set | nested map
        {}  | {[]}        | {{}}       | {[:]}
        """)
        void passes_immutable_sets_to_test(
            Set<String> set,
            Set<List<String>> nestedList,
            Set<Set<String>> nestedSet,
            Set<Map<String, String>> nestedMap
        ) {
            try {
                set.add("x");
                fail("modifying collections from the table should fail");
            } catch (Exception e) {
                // expected
            }
            try {
                nestedList.forEach(it -> it.add("x"));
                fail("modifying collections from the table should fail");
            } catch (Exception e) {
                // expected
            }
            try {
                nestedSet.forEach(it -> it.add("x"));
                fail("modifying collections from the table should fail");
            } catch (Exception e) {
                // expected
            }
            try {
                nestedMap.forEach(it -> it.put("x", "y"));
                fail("modifying collections from the table should fail");
            } catch (Exception e) {
                // expected
            }
        }

    }

    @Nested
    class CustomConversion {

        @TableTest("""
            String | List | Set  | Applicable Value Set | Map         | Nested           | Nested Ages
            16     | [16] | {16} | {16}                 | [value: 16] | [value: [16,16]] | [value: [16,16]]
            """)
        void uses_factory_method_in_test_class(
            Age fromString,
            List<Age> inList,
            Set<Age> inSet,
            Age fromApplicableSet,
            Map<String, Age> inMap,
            Map<String, List<Age>> inNested,
            Ages inCustom
        ) {
            Age expected = new Age(16);
            assertEquals(expected, fromString);
            assertEquals(List.of(expected), inList);
            assertEquals(Set.of(expected), inSet);
            assertEquals(expected, fromApplicableSet);
            assertEquals(Map.of("value", expected), inMap);
            assertEquals(Map.of("value", List.of(expected, expected)), inNested);
            assertEquals(new Ages(List.of(expected, expected)), inCustom);
        }

        @SuppressWarnings("unused")
        static Age parseAge(int age) {
            return new Age(age);
        }

        @SuppressWarnings("unused")
        static Ages parseAges(Map<String, List<Age>> age) {
            return new Ages(age.get("value"));
        }

        @TableTest("""
            JUnit convert | Constructor convert | Factory method convert | List of factory method convert
            2025-05-27    | 2025-05-27          | 2025-05-27             | [2025-05-27]
            """)
        void factory_methods_takes_priority_over_junit_conversion(
            LocalDate date,
            ConstructorDate date1,
            FactoryMethodDate date2,
            List<FactoryMethodDate> list
        ) {
            assertEquals(date, date1.date());
            assertEquals(date, date2.date());
            assertEquals(date, list.getFirst().date());
        }

    }

}
