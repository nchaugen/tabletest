package org.tabletest.junit.features;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.tabletest.junit.TableTestExceptionAssertions.assertConversionFails;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("spec")
@DisplayName("Null values")
@Description("""
        A blank cell converts to null for any nullable parameter type. Null is
        distinct from the empty string: '' and "" are empty values, not null.
        """)
class JavaNullValueTest {

    @DisplayName("A blank cell converts to null for any parameter type")
    @TableTest("""
        String | Integer | List | Map | Set | all null?
               |         |      |     |     | true
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

    @DisplayName("A blank cell for a primitive parameter fails the row")
    @Description("""
            Primitives cannot hold null — declare the boxed type instead if the
            column can be blank.
            """)
    @TableTest("""
        blank cell | parameter type | Error?
                   | boolean        | null cannot be assigned to primitive type boolean
                   | short          | null cannot be assigned to primitive type short
                   | char           | null cannot be assigned to primitive type char
        """)
    void blank_fails_for_primitive_parameter_type(String value, Class<?> type, String expectedError) {
        assertConversionFails(value, type, expectedError);
    }

    @DisplayName("An empty string is a value, not null, and non-String types cannot convert it")
    @Description("""
            '' and "" convert fine to String parameters, but there is no
            built-in conversion from an empty string to other types — without a
            custom type converter the row fails.
            """)
    @TableTest("""
        Scenario            | table value | parameter type    | Error?
        Empty single quoted | ''          | java.util.List    | 'conversion of value "" to type java.util.List failed'
        Empty double quoted | ""          | java.lang.Integer | 'conversion of value "" to type java.lang.Integer failed'
        Empty primitive     | ""          | boolean           | 'conversion of value "" to type boolean failed'
        """)
    void empty_string_for_non_string_types_requires_factory_method(
        String value,
        Class<?> type,
        String expectedError
    ) {
        assertConversionFails(value, type, expectedError);
    }

}
