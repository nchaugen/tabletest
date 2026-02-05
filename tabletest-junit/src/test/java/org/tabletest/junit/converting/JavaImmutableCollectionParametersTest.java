package org.tabletest.junit.converting;

import org.tabletest.junit.TableTest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.fail;

class JavaImmutableCollectionParametersTest {

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
