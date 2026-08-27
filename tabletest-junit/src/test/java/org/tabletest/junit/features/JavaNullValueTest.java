package org.tabletest.junit.features;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.tabletest.junit.TableTestExceptionAssertions.assertConversionFails;
import static org.tabletest.junit.TableTestExceptionAssertions.conversionFailureFor;
import static org.tabletest.junit.TableTestExceptionAssertions.searchedLocations;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Null values")
@Description("""
        A blank cell converts to null for any nullable parameter type. Null is
        distinct from the empty string: '' and "" are empty values, not null.
        """)
class JavaNullValueTest {

    @DisplayName("Converts a blank cell to null for any parameter type")
    @Description("""
            A blank cell means the value is absent, whatever type the parameter declares. The
            second row is what makes the first contradictable: the same five columns holding
            values arrive as values, so a conversion that answered null for everything would
            fail it.

            The expectation names the parameters that arrived null, in the order the columns
            are written.
            """)
    @TableTest("""
        Scenario              | String | Integer | List | Map    | Set | Parameters left null?
        Every column is blank |        |         |      |        |     | [String, Integer, List, Map, Set]
        No column is blank    | text   | 1       | [a]  | [a: b] | {a} | []
        """)
    void blank_converts_to_null(
        String string,
        Integer integer,
        List<?> list,
        Map<String, ?> map,
        Set<?> set,
        List<String> expectedNullParameters
    ) {
        Map<String, Object> byParameterName = new LinkedHashMap<>();
        byParameterName.put("String", string);
        byParameterName.put("Integer", integer);
        byParameterName.put("List", list);
        byParameterName.put("Map", map);
        byParameterName.put("Set", set);

        assertEquals(expectedNullParameters, namesOfNullValues(byParameterName));
    }

    private static List<String> namesOfNullValues(Map<String, Object> byParameterName) {
        return byParameterName.entrySet().stream()
            .filter(entry -> entry.getValue() == null)
            .map(Map.Entry::getKey)
            .toList();
    }

    @DisplayName("Fails the row when a primitive parameter gets a blank cell")
    @Description("""
            Primitives cannot hold null — declare the boxed type instead if the
            column can be blank.
            """)
    @TableTest("""
        Scenario           | Input value | Parameter type | Error message?
        A boolean column   |             | boolean        | Blank cell translates to null, but null cannot be assigned to primitive type boolean
        A numeric column   |             | short          | Blank cell translates to null, but null cannot be assigned to primitive type short
        A character column |             | char           | Blank cell translates to null, but null cannot be assigned to primitive type char
        """)
    void blank_fails_for_primitive_parameter_type(String value, Class<?> type, String errorMessage) {
        assertConversionFails(value, type, errorMessage);
    }

    @DisplayName("Refuses an empty string for a type other than String")
    @Description("""
            An empty string is a value, not an absent one, so it reaches conversion where a blank
            cell does not. Only String has a built-in conversion from it; every other type fails
            and asks for a custom type converter. The String row is what the other three are
            measured against — strike it and the rule reads as though nothing accepts an empty
            string.

            A quote style is invisible here. '' and "" are the same value once the cell is parsed,
            so the rows vary the parameter type instead.

            Each failure message closes by naming the classes searched for a type converter. Those
            depend on where the test lives, so the table leaves them out.
            """)
    @TableTest("""
        Scenario         | Input value | Parameter type    | Error message?
        A String         | ''          | java.lang.String  |
        A collection     | ''          | java.util.List    | 'Built-in conversion of value "" to type java.util.List failed. Are you missing a type converter for this conversion?'
        A boxed number   | ''          | java.lang.Integer | 'Built-in conversion of value "" to type java.lang.Integer failed. Are you missing a type converter for this conversion?'
        A primitive      | ''          | boolean           | 'Built-in conversion of value "" to type boolean failed. Are you missing a type converter for this conversion?'
        """)
    void empty_string_for_non_string_types_requires_factory_method(
        String value,
        Class<?> type,
        String errorMessage
    ) {
        assertEquals(fullMessage(errorMessage), conversionFailureFor(value, type));
    }

    /** The whole message including the searched-locations suffix, or null when nothing failed. */
    private static String fullMessage(String errorMessage) {
        return Optional.ofNullable(errorMessage).map(it -> it + searchedLocations()).orElse(null);
    }

}
