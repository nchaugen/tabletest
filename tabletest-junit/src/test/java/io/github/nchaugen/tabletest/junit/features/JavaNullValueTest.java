package io.github.nchaugen.tabletest.junit.features;

import io.github.nchaugen.tabletest.junit.TableTest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JavaNullValueTest {

    @TableTest("""
        Scenario            | String | Integer | List | Map | Set
        Blank               |        |         |      |     |
        Empty single quoted | ''     | ''      | ''   | ''  | ''
        Empty double quoted | ""     | ""      | ""   | ""  | ""
        """)
    void testBlankIsNullForNonString(
        String scenario,
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

}
