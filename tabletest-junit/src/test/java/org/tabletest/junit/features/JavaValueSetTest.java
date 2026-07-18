package org.tabletest.junit.features;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("spec")
@DisplayName("Value sets")
@Description("""
        A cell in {curly braces} lists several example values that share one
        expectation. Scalar parameters expand the row into one invocation per
        value; a Set-typed parameter receives the whole set as a single argument.
        """)
class JavaValueSetTest {

    @DisplayName("A value set runs the row once per value; a Set parameter gets the set itself")
    @Description("""
            The "adding any of" column expands: each row runs once per value in it.
            The "to set" column binds to a Set parameter, so the whole set is passed
            as one argument. Blank cells pass null, never an empty set.
            """)
    @TableTest("""
        Scenario                  | adding any of | to set    | makes size? | is set null?
        Adding existing values    | {1, 2, 3}     | {1, 2, 3} | 3           | false
        Adding other values       | {4, 5, 6}     | {1, 2, 3} | 4           | false
        Adding nothing to nothing |               |           |             | true
        """)
    void value_sets(
        Integer a,
        Set<Integer> b,
        Integer expectedSize,
        boolean expectedNull
    ) {
        if (expectedNull) {
            assertNull(b);
        }
        else {
            Set<Integer> result = new HashSet<>(b);
            result.add(a);
            assertEquals(expectedSize, result.size());
        }
    }

    @DisplayName("Value sets in several columns expand to every combination")
    @Description("""
            Each row runs once per combination of values — three x values times
            two y values give six invocations per row.
            """)
    @TableTest("""
        Scenario       | x         | y       | is sum even?
        Even plus even | {2, 4, 6} | {8, 10} | true
        Odd plus even  | {1, 3, 5} | {6, 8}  | false
        """)
    void value_set_combinations(int x, int y, boolean expectedEvenSum) {
        assertEquals(expectedEvenSum, (x + y) % 2 == 0);
    }

}
