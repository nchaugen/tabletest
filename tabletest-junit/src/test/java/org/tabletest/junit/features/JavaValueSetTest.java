package org.tabletest.junit.features;

import org.tabletest.junit.TableTest;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JavaValueSetTest {

    @TableTest("""
        Scenario                  | adding any of | to set    | makes size? | is set null? | contains null?
        Adding existing values    | {1, 2, 3}     | {1, 2, 3} | 3           | false        | false
        Adding other values       | {4, 5, 6}     | {1, 2, 3} | 4           | false        | false
        Adding no values          | {}            | {1, 2, 3} | 4           | false        | true
        Adding nothing to nothing |               |           |             | true         | false
        """)
    void value_sets(
        Integer a,
        Set<Integer> b,
        Integer expectedSize,
        boolean expectedNull,
        boolean containsNull
    ) {
        if (expectedNull) {
            assertNull(b);
        }
        else {
            Set<Integer> result = new HashSet<>(b);
            result.add(a);
            assertEquals(expectedSize, result.size());
            assertEquals(containsNull, result.contains(null));
        }
    }

    @TableTest("""
        Scenario                      | a                | b | c | d         | e
        Anything multiplied by 0 is 0 | {-1, 0, 1, 1000} | 0 | 0 | {1, 2, 3} | 3
        """)
    void testValueSet(int a, int b, int c, Set<Integer> d, int e) {
        assertEquals(c, a * b);
        assertEquals(e, d.size());
    }

}
