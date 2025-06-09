package io.github.nchaugen.tabletest.junit.features;

import io.github.nchaugen.tabletest.junit.ExampleDomainConverters;
import io.github.nchaugen.tabletest.junit.TableTest;
import io.github.nchaugen.tabletest.junit.TableTestConverters;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@TableTestConverters(ExampleDomainConverters.class)
class JavaApplicableValueSetTest {

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
        }
        else {
            Set<Integer> result = new HashSet<>(b);
            result.add(a);
            assertEquals(expectedSize, result.size());
            assertEquals(containsNull, result.contains(null));
        }
    }

}
