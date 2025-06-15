package io.github.nchaugen.tabletest.junit.converting;

import io.github.nchaugen.tabletest.junit.TableTest;

import java.util.List;

import static io.github.nchaugen.tabletest.junit.TableTestExceptionAssertions.assertThrowsWhenFallbackFails;
import static io.github.nchaugen.tabletest.junit.TableTestExceptionAssertions.assertThrowsWhenMultipleFactoryMethodsFound;

class JavaConversionFailureTest {

    @TableTest("""
        table value | parameter type
        0.1         | java.lang.Byte
        256         | java.lang.Byte
        abc         | java.lang.Character
        """)
    void fails_conversion_for_single_values_outside_type_range(String value, Class<?> type) {
        assertThrowsWhenFallbackFails(value, type.getTypeName());
    }

    @TableTest("""
        Value     | Parameterized type
        [""]      | java.util.List<java.lang.Byte>
        {""}      | java.util.Set<java.lang.Byte>
        [key: ""] | java.util.Map<?, java.lang.Short>
        """)
    void fails_conversion_for_element_values_outside_type_range(Object value, String parameterizedTypeName) {
        assertThrowsWhenFallbackFails(value, parameterizedTypeName);
    }

    @TableTest("""
        table value | parameter type
        [52]        | io.github.nchaugen.tabletest.junit.javadomain.Ages
        """)
    void fails_when_no_factory_method_found(List<String> value, Class<?> type) {
        assertThrowsWhenFallbackFails(value, type.getTypeName());
    }

    @TableTest("""
        table value | parameter type
        52          | io.github.nchaugen.tabletest.junit.javadomain.Age
        """)
    void fails_when_multiple_factory_methods_found(String value, Class<?> type) {
        assertThrowsWhenMultipleFactoryMethodsFound(value, type);
    }

}
