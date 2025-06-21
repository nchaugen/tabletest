package io.github.nchaugen.tabletest.junit.features;

import io.github.nchaugen.tabletest.junit.Scenario;
import io.github.nchaugen.tabletest.junit.TableTest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.nchaugen.tabletest.junit.TableTestExceptionAssertions.assertThrowsWhenNullSpecifiedForPrimitiveType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JavaNullValueTest {

    @TableTest("""
        Scenario            | String | Integer | List | Map | Set
        Blank               |        |         |      |     |
        Empty single quoted | ''     | ''      | ''   | ''  | ''
        Empty double quoted | ""     | ""      | ""   | ""  | ""
        """)
    void blank_is_null_and_empty_is_null_for_non_string(
        @Scenario String scenario,
        String string,
        Integer integer,
        List<?> list,
        Map<String, ?> map,
        Set<?> set
    ) {
        if ("Blank".equals(scenario)) assertNull(string);
        else assertEquals("", string);
        assertNull(integer);
        assertNull(list);
        assertNull(map);
        assertNull(set);
    }

    @TableTest("""
        table value | parameter type
                    | boolean
        ''          | int
        """)
    void fails_when_null_value_specified_for_primitive_parameter_type(String value, Class<?> type) {
        assertThrowsWhenNullSpecifiedForPrimitiveType(value, type);
    }

}
