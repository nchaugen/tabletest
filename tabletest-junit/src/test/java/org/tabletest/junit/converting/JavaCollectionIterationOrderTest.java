package org.tabletest.junit.converting;

import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Collection iteration order")
@Description("""
        A collection cell decides the order its parameter iterates in: elements come
        out in the order the cell wrote them, never sorted and never in some hash
        order of their own. Order-sensitive assertions against a List, Set, or Map
        parameter are therefore safe to write.

        The elements below are text, but the guarantee is about position rather than
        element type and holds whatever type the signature declares. Each rule shows
        the parameter's iteration as a list, so a row whose input is already in
        ascending order proves nothing on its own — the rows that reverse or shuffle
        the written order are the ones that pin it down.
        """)
public class JavaCollectionIterationOrderTest {

    @DisplayName("A list or set parameter iterates in the order the cell wrote it")
    @Description("Both input columns hold the same elements in the same order.")
    @TableTest("""
        Scenario             | List input | Set input | Iteration order?
        Written ascending    | [1, 2, 3]  | {1, 2, 3} | [1, 2, 3]
        Written descending   | [3, 2, 1]  | {3, 2, 1} | [3, 2, 1]
        Written unsorted     | [3, 1, 2]  | {3, 1, 2} | [3, 1, 2]
        Reverse alphabetical | [c, b, a]  | {c, b, a} | [c, b, a]
        """)
    void iterates_in_written_order(
        List<String> list,
        Set<String> set,
        List<String> expectedOrder
    ) {
        assertEquals(expectedOrder, List.copyOf(list));
        assertEquals(expectedOrder, List.copyOf(set));
    }

    @DisplayName("A map parameter iterates its entries in the order the cell wrote them")
    @Description("Keys and values are read off the same map, entry by entry.")
    @TableTest("""
        Scenario                  | Map input          | Key order? | Value order?
        Written ascending         | [a: 1, b: 2, c: 3] | [a, b, c]  | [1, 2, 3]
        Keys written unsorted     | [c: 1, a: 2, b: 3] | [c, a, b]  | [1, 2, 3]
        Values written descending | [a: 3, b: 2, c: 1] | [a, b, c]  | [3, 2, 1]
        """)
    void iterates_entries_in_written_order(
        Map<String, String> map,
        List<String> expectedKeys,
        List<String> expectedValues
    ) {
        assertEquals(expectedKeys, List.copyOf(map.keySet()));
        assertEquals(expectedValues, List.copyOf(map.values()));
    }
}
