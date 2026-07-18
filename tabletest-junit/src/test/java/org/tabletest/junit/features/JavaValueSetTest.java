package org.tabletest.junit.features;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
            The "Adding any of" column binds to a scalar parameter, so the row
            expands into one invocation per value. The "To set" column binds to
            a Set parameter, so the whole set is passed as one argument.
            """)
    @TableTest("""
        Scenario               | Adding any of | To set    | Makes size?
        Values already present | {1, 2, 3}     | {1, 2, 3} | 3
        New values             | {4, 5, 6}     | {1, 2, 3} | 4
        """)
    void value_sets(Integer value, Set<Integer> set, int expectedSize) {
        Set<Integer> result = new HashSet<>(set);
        result.add(value);
        assertEquals(expectedSize, result.size());
    }

    @DisplayName("Value sets in several columns expand to every combination")
    @Description("""
            Each row runs once per combination of values — three x values times
            two y values give six invocations per row.
            """)
    @TableTest("""
        Scenario       | x         | y       | Is sum even?
        Even plus even | {2, 4, 6} | {8, 10} | true
        Odd plus even  | {1, 3, 5} | {6, 8}  | false
        """)
    void value_set_combinations(int x, int y, boolean expectedEvenSum) {
        assertEquals(expectedEvenSum, (x + y) % 2 == 0);
    }

}
