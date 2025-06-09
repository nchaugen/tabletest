package io.github.nchaugen.tabletest.junit.converting

import io.github.nchaugen.tabletest.junit.TableTest
import org.junit.jupiter.api.Assertions.fail
import java.util.List
import java.util.Map
import java.util.function.Consumer

class KotlinImmutableCollectionParametersTest {

    @TableTest(
        """    
        list | nested list | nested set | nested map
        []   | [[]]        | [{}]       | [[:]]
        """
    )
    fun passes_immutable_lists_to_test(
        list: MutableList<String>,
        nestedList: MutableList<MutableList<String>>,
        nestedSet: MutableList<MutableSet<String>>,
        nestedMap: MutableList<MutableMap<String, String>>
    ) {
        try {
            list.add("x")
            fail<Any?>("modifying collections from the table should fail")
        } catch (_: Exception) {
            // expected
        }
        try {
            nestedList.first().add("x")
            fail<Any?>("modifying collections from the table should fail")
        } catch (_: Exception) {
            // expected
        }
        try {
            nestedSet.first().add("x")
            fail<Any?>("modifying collections from the table should fail")
        } catch (_: Exception) {
            // expected
        }
        try {
            nestedMap.first().put("x", "y")
            fail<Any?>("modifying collections from the table should fail")
        } catch (_: Exception) {
            // expected
        }
    }

    @TableTest(
        """    
        map | nested list | nested set  | nested map
        [:] | [empty: []] | [empty: {}] | [empty: [:]]
            """
    )
    fun passes_immutable_maps_to_test(
        map: Map<String?, String?>,
        nestedList: Map<String?, List<String?>?>,
        nestedSet: Map<String?, MutableSet<String?>?>,
        nestedMap: Map<String?, Map<String?, String?>?>
    ) {
        try {
            map.put("x", "y")
            fail<Any?>("modifying collections from the table should fail")
        } catch (_: Exception) {
            // expected
        }
        try {
            nestedList.get("empty")!!.add("x")
            fail<Any?>("modifying collections from the table should fail")
        } catch (_: Exception) {
            // expected
        }
        try {
            nestedSet.get("empty")!!.add("x")
            fail<Any?>("modifying collections from the table should fail")
        } catch (_: Exception) {
            // expected
        }
        try {
            nestedMap.get("empty")!!.put("x", "y")
            fail<Any?>("modifying collections from the table should fail")
        } catch (_: Exception) {
            // expected
        }
    }

    @TableTest(
        """    
        set | nested list | nested set | nested map
        {}  | {[]}        | {{}}       | {[:]}
        """
    )
    fun passes_immutable_sets_to_test(
        set: MutableSet<String?>,
        nestedList: MutableSet<List<String?>?>,
        nestedSet: MutableSet<MutableSet<String?>?>,
        nestedMap: MutableSet<Map<String?, String?>?>
    ) {
        try {
            set.add("x")
            fail<Any?>("modifying collections from the table should fail")
        } catch (_: Exception) {
            // expected
        }
        try {
            nestedList.forEach(Consumer { it: List<String?>? -> it!!.add("x") })
            fail<Any?>("modifying collections from the table should fail")
        } catch (_: Exception) {
            // expected
        }
        try {
            nestedSet.forEach(Consumer { it: MutableSet<String?>? -> it!!.add("x") })
            fail<Any?>("modifying collections from the table should fail")
        } catch (_: Exception) {
            // expected
        }
        try {
            nestedMap.forEach(Consumer { it: Map<String?, String?>? -> it!!.put("x", "y") })
            fail<Any?>("modifying collections from the table should fail")
        } catch (_: Exception) {
            // expected
        }
    }
}
