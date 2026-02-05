package org.tabletest.junit.features;

import org.tabletest.junit.TableTest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.tabletest.junit.TableTestExceptionAssertions.assertThrowsWhenFallbackFails;
import static org.tabletest.junit.TableTestExceptionAssertions.assertThrowsWhenNullSpecifiedForPrimitiveType;
import static org.junit.jupiter.api.Assertions.assertNull;

class JavaNullValueTest {

    @TableTest("""
        String | Integer | List | Map | Set
               |         |      |     |
        """)
    void blank_converts_to_null(
        String string,
        Integer integer,
        List<?> list,
        Map<String, ?> map,
        Set<?> set
    ) {
        assertNull(string);
        assertNull(integer);
        assertNull(list);
        assertNull(map);
        assertNull(set);
    }

    @TableTest("""
        table value | parameter type
                    | boolean
                    | short
                    | char
        """)
    void blank_fails_for_primitive_parameter_type(String value, Class<?> type) {
        assertThrowsWhenNullSpecifiedForPrimitiveType(value, type);
    }

    @TableTest("""
        Scenario            | table value | Type
        Empty single quoted | ''          | java.util.List
        Empty double quoted | ""          | java.lang.Integer
        Empty primitive     | ""          | boolean
        """)
    void empty_string_for_non_string_types_requires_factory_method(
        String value,
        Class<?> type
    ) {
        assertThrowsWhenFallbackFails(value, type);
    }

}
