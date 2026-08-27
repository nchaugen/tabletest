package org.tabletest.junit.features;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Value sets")
@Description("""
        A cell in {curly braces} lists several example values that share one
        expectation. A scalar parameter expands the row into one invocation per
        value. A Set parameter receives the whole set as one argument.

        A value in a set is a converted value, not the text in the cell. The last two
        rules say what follows from that. Read them before you write a value set for
        several spellings of one value.
        """)
class JavaValueSetTest {

    @DisplayName("Runs a row once per value, unless the parameter is a Set")
    @Description("""
            The "Adding any of" column binds to a scalar parameter, so the row expands into one
            invocation per value. The "To set" column binds to a Set parameter, which receives
            the whole set as one argument.
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
            Each row runs once per combination of values. Three x values and two y values give
            six invocations per row.
            """)
    @TableTest("""
        Scenario       | x         | y       | Is sum even?
        Even plus even | {2, 4, 6} | {8, 10} | true
        Odd plus even  | {1, 3, 5} | {6, 8}  | false
        """)
    void value_set_combinations(int x, int y, boolean expectedEvenSum) {
        assertEquals(expectedEvenSum, (x + y) % 2 == 0);
    }

    @DisplayName("Passes the whole set to any parameter that can hold one")
    @Description("""
            A row expands only when the set cannot be the value of the parameter. A Set
            parameter receives the whole set. An Object parameter receives the whole set too,
            because a Set is assignable to Object. A method with such a parameter runs once, and
            that one invocation holds every member.

            The two input columns hold the same cell. One column binds to an Object parameter and
            one to a Set parameter. The counts show that the Object parameter receives the set,
            and not one member of it.
            """)
    @TableTest("""
        Scenario      | Bound to Object | Bound to Set | Members via Object? | Members via Set?
        Three members | {1, 2, 3}       | {1, 2, 3}    | 3                   | 3
        Two members   | {4, 5}          | {4, 5}       | 2                   | 2
        """)
    void whole_set_reaches_a_parameter_that_can_hold_one(
        Object asObject,
        Set<Integer> asSet,
        int expectedViaObject,
        int expectedViaSet
    ) {
        assertEquals(expectedViaObject, ((Set<?>) asObject).size());
        assertEquals(expectedViaSet, asSet.size());
    }

    @DisplayName("Counts a value once however it is spelled")
    @Description("""
            A set holds converted values. Two cells that convert to the same object are one
            member, and the row expands once. The hexadecimal, decimal and octal spellings of
            fifteen are one value.

            Do not write a value set to show that TableTest accepts several spellings. The set keeps
            one spelling and drops the others, and a dropped spelling never runs. Give each
            spelling a row.

            A value set states that the outcome is the same for several values. That is a
            different claim.
            """)
    @TableTest("""
        Scenario                         | Spellings      | Members after conversion?
        Three spellings of one number    | {0xF, 15, 017} | 1
        Three numbers that stay distinct | {1, 2, 3}      | 3
        """)
    void value_set_holds_converted_values(Set<Integer> spellings, int expectedMembers) {
        assertEquals(expectedMembers, spellings.size());
    }

    @DisplayName("Groups values sharing an expectation, repeating fixed columns")
    @Description("""
            A value set is a claim that the outcome is the same whichever value is chosen. Every value
            in the set must therefore lead to the same result. A column that is not a set, here the
            pass mark, keeps its single value across the whole expansion.
            """)
    @TableTest("""
        Scenario          | Score (any of) | Pass mark | Passes?
        Scores above mark | {50, 75, 100}  | 50        | true
        Scores below mark | {10, 30, 49}   | 50        | false
        """)
    void value_set_shares_one_expectation(int score, int passMark, boolean passes) {
        assertEquals(passes, score >= passMark);
    }

}
