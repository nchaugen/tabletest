package org.tabletest.junit.features;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.tabletest.junit.TableTestExceptionAssertions.assertConversionFails;
import static org.tabletest.junit.TableTestExceptionAssertions.searchedLocations;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Null values")
@Description("""
        A blank cell converts to null for any nullable parameter type. Null is
        distinct from the empty string: '' and "" are empty values, not null.
        """)
class JavaNullValueTest {

    @DisplayName("Converts a blank cell to null for any parameter type")
    @TableTest("""
        Scenario              | String | Integer | List | Map | Set | All null?
        Every column is blank |        |         |      |     |     | true
        """)
    void blank_converts_to_null(
        String string,
        Integer integer,
        List<?> list,
        Map<String, ?> map,
        Set<?> set,
        boolean expectedAllNull
    ) {
        assertEquals(
            expectedAllNull,
            string == null && integer == null && list == null && map == null && set == null
        );
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
            '' and "" convert to a String parameter without trouble. There is no built-in conversion
            from an empty string to another type, so the row fails without a custom type converter.

            Each message closes by naming the classes searched for a type converter. Those depend on
            where the test lives, so the table leaves them out.
            """)
    @TableTest("""
        Scenario            | Input value | Parameter type    | Error message?
        Empty single quoted | ''          | java.util.List    | 'Built-in conversion of value "" to type java.util.List failed. Are you missing a type converter for this conversion?'
        Empty double quoted | ""          | java.lang.Integer | 'Built-in conversion of value "" to type java.lang.Integer failed. Are you missing a type converter for this conversion?'
        Empty primitive     | ""          | boolean           | 'Built-in conversion of value "" to type boolean failed. Are you missing a type converter for this conversion?'
        """)
    void empty_string_for_non_string_types_requires_factory_method(
        String value,
        Class<?> type,
        String errorMessage
    ) {
        assertConversionFails(value, type, errorMessage + searchedLocations());
    }

}
