package io.github.nchaugen.tabletest.junit.converting;

import io.github.nchaugen.tabletest.junit.TableTest;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.support.conversion.ConversionException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.nchaugen.tabletest.junit.ParameterFixture.parameter;
import static io.github.nchaugen.tabletest.junit.ParameterTypeConverter.convertValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JavaConversionFailureTest {

    @TableTest("""
        table value | parameter type
        0.1         | java.lang.Byte
        256         | java.lang.Byte
        abc         | java.lang.Character
        """)
    void fails_conversion_for_single_values_outside_type_range(
        String value,
        Class<?> type
    ) {
        assertThrows(
            ConversionException.class,
            () -> convertValue(value, parameter(type))
        );
    }

    @Test
    void fails_when_list_conversion_not_possible() {
        Map.of(
            "java.util.List<java.lang.Byte>", List.of("x")
        ).forEach((String typeName, List<?> parsedValue) ->
                      assertThrows(
                          ConversionException.class,
                          () -> convertValue(parsedValue, parameter(typeName)),
                          () -> "Expected failure for " + typeName
                      )
        );
    }

    @Test
    void fails_when_set_conversion_not_possible() {
        Map.of(
            "java.util.Set<java.lang.Byte>", Set.of("x")
        ).forEach((String typeName, Set<?> parsedValue) ->
                      assertThrows(
                          ConversionException.class,
                          () -> convertValue(parsedValue, parameter(typeName)),
                          () -> "Expected failure for " + typeName
                      )
        );
    }

    @Test
    void fails_when_map_conversion_not_possible() {
        Map.of(
            "java.util.Map<?, java.lang.Short>", Map.of("key", "x")
        ).forEach((String typeName, Map<?, ?> parsedValue) ->
                      assertThrows(
                          ConversionException.class,
                          () -> convertValue(parsedValue, parameter(typeName)),
                          () -> "Expected failure for " + typeName
                      )
        );
    }

}
