package org.tabletest.junit.converting;

import org.junit.jupiter.api.Test;
import org.tabletest.junit.TableTestException;
import org.tabletest.junit.TypeConverter;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeSet;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tabletest.junit.ParameterTypeConverter.convertValue;

/**
 * Tests that collection cells require parameters declared with the interface types
 * List, Set, or Map, failing with a clear error for concrete collection types,
 * while type converters can still produce concrete collection implementations.
 */
public class JavaCollectionParameterTypeTest {

    @Test
    void concrete_set_parameter_type_fails_with_clear_error() {
        assertUnsupportedCollectionType(
            new LinkedHashSet<>(Arrays.asList("a", "b")),
            parameterOf("treeSetParam")
        );
    }

    @Test
    void concrete_list_parameter_type_fails_with_clear_error() {
        assertUnsupportedCollectionType(
            new ArrayList<>(singletonList("a")),
            parameterOf("arrayListParam")
        );
    }

    @Test
    void concrete_map_parameter_type_fails_with_clear_error() {
        LinkedHashMap<Object, Object> parsedMap = new LinkedHashMap<>();
        parsedMap.put("key", "value");
        assertUnsupportedCollectionType(parsedMap, parameterOf("linkedHashMapParam"));
    }

    @Test
    void concrete_element_type_in_nested_collection_fails_with_clear_error() {
        assertUnsupportedCollectionType(
            new ArrayList<>(singletonList(new LinkedHashSet<>(singletonList("a")))),
            parameterOf("listOfTreeSetsParam")
        );
    }

    @Test
    void type_converter_can_still_produce_concrete_collection_type() {
        Object converted = convertValue("a", parameterOf("treeSetParam"));
        assertEquals(new TreeSet<>(singletonList("a")), converted);
    }

    @TypeConverter
    @SuppressWarnings("unused")
    public static TreeSet<String> parseTreeSet(String value) {
        return new TreeSet<>(singletonList(value));
    }

    @SuppressWarnings("unused")
    private void treeSetParam(TreeSet<String> value) {
    }

    @SuppressWarnings("unused")
    private void arrayListParam(ArrayList<String> value) {
    }

    @SuppressWarnings("unused")
    private void linkedHashMapParam(LinkedHashMap<String, String> value) {
    }

    @SuppressWarnings("unused")
    private void listOfTreeSetsParam(List<TreeSet<String>> value) {
    }

    private static void assertUnsupportedCollectionType(Object parsedValue, Parameter parameter) {
        TableTestException exception = assertThrows(
            TableTestException.class,
            () -> convertValue(parsedValue, parameter)
        );
        assertTrue(
            exception.getMessage().contains("not supported for collection values"),
            "Unexpected message: " + exception.getMessage()
        );
    }

    private static Parameter parameterOf(String fixtureMethodName) {
        return Arrays.stream(JavaCollectionParameterTypeTest.class.getDeclaredMethods())
            .filter(method -> method.getName().equals(fixtureMethodName))
            .findFirst()
            .orElseThrow()
            .getParameters()[0];
    }
}
