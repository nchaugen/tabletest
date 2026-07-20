package org.tabletest.junit.converting;

import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.junit.jupiter.api.DisplayName;

import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Immutable collection parameters")
@Description("""
        A collection passed from a table is read-only: adding to a List or Set,
        putting into a Map, or modifying any collection nested inside them throws
        instead of changing the value. A row's data therefore stays as written
        however the test method treats it.
        """)
class JavaImmutableCollectionParametersTest {

    @DisplayName("Modifying a collection parameter throws")
    @Description("The modification attempted is add for a List or Set, and put for a Map.")
    @TableTest("""
        Scenario | Input value | Collection type? | Modification throws?
        A list   | [a, b]      | java.util.List   | java.lang.UnsupportedOperationException
        A set    | {a, b}      | java.util.Set    | java.lang.UnsupportedOperationException
        A map    | [k: a]      | java.util.Map    | java.lang.UnsupportedOperationException
        """)
    void rejects_modifying_a_collection_parameter(
        Object collection,
        Class<?> expectedCollectionType,
        Class<? extends Throwable> expectedException
    ) {
        assertInstanceOf(expectedCollectionType, collection);
        assertThrows(expectedException, () -> modify(collection));
    }

    @DisplayName("Modifying a nested collection throws, whatever collection holds it")
    @Description("""
            Immutability reaches every level of a row's value, so a collection
            reached through another collection rejects modification too.
            """)
    @TableTest("""
        Scenario         | Input value     | Nested collection? | Nested collection type? | Modification throws?
        A list in a list | [[a, b]]        | [a, b]             | java.util.List          | java.lang.UnsupportedOperationException
        A set in a list  | [{a, b}]        | {a, b}             | java.util.Set           | java.lang.UnsupportedOperationException
        A map in a list  | [[k: a]]        | [k: a]             | java.util.Map           | java.lang.UnsupportedOperationException
        A list in a set  | {[a, b]}        | [a, b]             | java.util.List          | java.lang.UnsupportedOperationException
        A set in a set   | {{a, b}}        | {a, b}             | java.util.Set           | java.lang.UnsupportedOperationException
        A map in a set   | {[k: a]}        | [k: a]             | java.util.Map           | java.lang.UnsupportedOperationException
        A list in a map  | [outer: [a, b]] | [a, b]             | java.util.List          | java.lang.UnsupportedOperationException
        A set in a map   | [outer: {a, b}] | {a, b}             | java.util.Set           | java.lang.UnsupportedOperationException
        A map in a map   | [outer: [k: a]] | [k: a]             | java.util.Map           | java.lang.UnsupportedOperationException
        """)
    void rejects_modifying_a_nested_collection(
        Object collection,
        Object expectedNestedCollection,
        Class<?> expectedNestedCollectionType,
        Class<? extends Throwable> expectedException
    ) {
        Object nested = onlyElementOf(collection);
        assertEquals(expectedNestedCollection, nested);
        assertInstanceOf(expectedNestedCollectionType, nested);
        assertThrows(expectedException, () -> modify(nested));
    }

    @SuppressWarnings("unchecked")
    private static void modify(Object collection) {
        if (collection instanceof Map) {
            ((Map<String, String>) collection).put("added key", "added value");
        } else {
            ((Collection<String>) collection).add("added value");
        }
    }

    private static Object onlyElementOf(Object collection) {
        Collection<?> elements = collection instanceof Map<?, ?> map
            ? map.values()
            : (Collection<?>) collection;
        return elements.iterator().next();
    }
}
