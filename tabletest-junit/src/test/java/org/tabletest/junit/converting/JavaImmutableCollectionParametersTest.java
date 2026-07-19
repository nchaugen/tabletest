package org.tabletest.junit.converting;

import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("Immutable collection parameters")
@Description("""
        Collections passed from a table — and every collection nested inside them —
        are immutable. Any attempt to add, put, or otherwise modify one throws. Each
        row lists the collection shapes exercised; the rule is that none of them can
        be mutated.
        """)
class JavaImmutableCollectionParametersTest {

    @DisplayName("A List parameter and its nested collections reject modification")
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
            nestedList.get(0).add("x");
            fail("modifying collections from the table should fail");
        } catch (Exception e) {
            // expected
        }
        try {
            nestedSet.get(0).add("x");
            fail("modifying collections from the table should fail");
        } catch (Exception e) {
            // expected
        }
        try {
            nestedMap.get(0).put("x", "y");
            fail("modifying collections from the table should fail");
        } catch (Exception e) {
            // expected
        }
    }

    @DisplayName("A Map parameter and its nested collections reject modification")
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


    @DisplayName("A Set parameter and its nested collections reject modification")
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
