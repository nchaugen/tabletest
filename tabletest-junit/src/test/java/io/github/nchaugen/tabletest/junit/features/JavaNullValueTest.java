package io.github.nchaugen.tabletest.junit.features;

import io.github.nchaugen.tabletest.junit.ExampleDomainConverters;
import io.github.nchaugen.tabletest.junit.TableTest;
import io.github.nchaugen.tabletest.junit.TableTestConverters;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@TableTestConverters(ExampleDomainConverters.class)
class JavaNullValueTest {

    @TableTest("""
        Scenario     | String | Float | List
        Empty string | ""     | ""    | ""
        Blank cell   |        |       |
        """)
    void converts_blank_to_null_for_non_string_parameters(
        String stringVal,
        Float floatVal,
        List<?> listVal
    ) {
        assertNotNull(stringVal);
        assertNull(floatVal);
        assertNull(listVal);
    }

}
