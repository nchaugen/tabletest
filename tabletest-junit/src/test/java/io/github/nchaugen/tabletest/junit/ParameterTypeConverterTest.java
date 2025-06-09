package io.github.nchaugen.tabletest.junit;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.support.conversion.ConversionException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.nchaugen.tabletest.junit.ParameterFixture.parameter;
import static io.github.nchaugen.tabletest.junit.ParameterTypeConverter.convertValue;
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

}
