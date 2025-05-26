package io.github.nchaugen.tabletest.junit;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.support.conversion.ConversionException;

import java.util.List;
import java.util.Map;

import static io.github.nchaugen.tabletest.junit.ParameterFixture.parameter;
import static io.github.nchaugen.tabletest.junit.ParameterTypeConverter.convertValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ParameterTypeConverterTest {

    @Nested
    class SingleValues {

        @TableTest("""
            table value                          | parameter type
            15                                   | java.lang.Byte
            0xF                                  | java.lang.Byte
            017                                  | java.lang.Byte
            15                                   | java.lang.Short
            0xF                                  | java.lang.Short
            017                                  | java.lang.Short
            15                                   | java.lang.Integer
            0xF                                  | java.lang.Integer
            017                                  | java.lang.Integer
            15                                   | java.lang.Long
            0xF                                  | java.lang.Long
            017                                  | java.lang.Long
            15                                   | java.math.BigInteger
            1234567890123456789                  | java.math.BigInteger
            0.1                                  | java.lang.Float
            0.1                                  | java.lang.Double
            1                                    | java.math.BigDecimal
            0.1                                  | java.math.BigDecimal
            123.456e789                          | java.math.BigDecimal
            a                                    | java.lang.Character
            abc                                  | java.lang.String
            SECONDS                              | java.util.concurrent.TimeUnit
            /path/to/file                        | java.io.File
            /path/to/file                        | java.nio.file.Path
            "https://junit.org/"                 | java.net.URI
            "https://junit.org/"                 | java.net.URL
            java.lang.Integer                    | java.lang.Class
            java.lang.Thread$State               | java.lang.Class
            byte                                 | java.lang.Class
            "char[]"                             | java.lang.Class
            UTF-8                                | java.nio.charset.Charset
            NOK                                  | java.util.Currency
            en                                   | java.util.Locale
            d043e930-7b3b-48e3-bdbe-5a3ccfb833db | java.util.UUID
            PT3S                                 | java.time.Duration
            "1977-08-17T18:19:20Z"               | java.time.Instant
            "2017-03-14T12:34:56.789"            | java.time.LocalDateTime
            2017-03-14                           | java.time.LocalDate
            "12:34:56.789"                       | java.time.LocalTime
            "2017-03-14T12:34:56.789Z"           | java.time.OffsetDateTime
            "12:34:56.789Z"                      | java.time.OffsetTime
            "2017-03-14T12:34:56.789Z"           | java.time.ZonedDateTime
            Europe/Berlin                        | java.time.ZoneId
            "+02:30"                             | java.time.ZoneOffset
            P2M6D                                | java.time.Period
            2017                                 | java.time.Year
            2017-03                              | java.time.YearMonth
            --03-14                              | java.time.MonthDay
            """)
        void are_implicitly_converted_according_to_junit_rules(String value, Class<?> type) {
            assertInstanceOf(type, convertValue(value, parameter(type)));
        }

        @TableTest("""
            table value | parameter type
            0.1         | java.lang.Byte
            256         | java.lang.Byte
            abc         | java.lang.Character
            """)
        void fail_implicit_conversion_for_values_outside_type_range(
            String value,
            Class<?> type
        ) {
            assertThrows(
                ConversionException.class,
                () -> convertValue(value, ParameterFixture.parameter(type))
            );
        }

        @TableTest("""
            table value | parameter type
            123         | java.lang.Byte
            123         | java.lang.Float
            123         | java.lang.Double
            """)
        void allow_widening_primitive_conversion(String value, Class<?> type) {
            assertInstanceOf(type, convertValue(value, parameter(type)));
        }

        @TableTest("""
            Scenario     | table value | parameter type
            Empty string | ''          | java.lang.Byte
            Empty string | ""          | java.lang.Float
            Blank cell   |             | java.util.List
            """)
        void convert_blank_to_null_for_non_string_parameters(String value, Class<?> type) {
            assertNull(convertValue(value, parameter(type)));
        }

        @TableTest("""
            Scenario     | table value | parameter type
            Empty string | ''          | java.lang.String
            Empty string | ""          | java.lang.String
            Blank cell   |             | java.lang.String
            """)
        void convert_blank_to_blank_for_string_parameters(String value, Class<?> type) {
            assertEquals("", convertValue(value, parameter(type)));
        }

    }

    @Nested
    class ListValues {

        @Test
        void are_implicitly_converted_according_to_parameterized_parameter_type() {
            List.of(
                List.of(
                    "java.util.List<?>",
                    List.of(),
                    List.of()
                ),
                List.of(
                    "java.util.List<java.lang.Byte>",
                    List.of("1"),
                    List.of((byte) 1)
                ),
                List.of(
                    "java.util.List<java.lang.Integer>",
                    List.of("2"),
                    List.of(2)
                ),
                List.of(
                    "java.util.List<java.lang.Long>",
                    List.of("3"),
                    List.of(3L)
                ),
                List.of(
                    "java.util.List<java.lang.Double>",
                    List.of("4"),
                    List.of(4.0)
                ),
                List.of(
                    "java.util.List<java.lang.String>",
                    List.of("5"),
                    List.of("5")
                ),
                List.of(
                    "java.util.List<java.util.List<java.lang.Short>>",
                    List.of(List.of("6")),
                    List.of(List.of((short) 6))
                ),
                List.of(
                    "java.util.List<java.util.Map<java.lang.String, java.lang.Long>>",
                    List.of(Map.of("1", "7")),
                    List.of(Map.of("1", 7L))
                )
            ).forEach((testCase) -> {
                          String typeName = (String) testCase.get(0);
                          List<?> parsedListValue = (List<?>) testCase.get(1);
                          List<?> expectedValue = (List<?>) testCase.get(2);
                          Object actualValue = convertValue(parsedListValue, parameter(typeName));
                          assertEquals(expectedValue, actualValue, () -> "Failed for " + typeName);
                      }
            );
        }

        @Test
        void fail_when_conversion_according_to_parameterized_parameter_type_not_possible() {
            Map.of(
                "java.util.List<java.lang.Byte>", List.of("x")
            ).forEach((String typeName, List<?> parsedListValue) ->
                          assertThrows(
                              ConversionException.class,
                              () -> convertValue(parsedListValue, parameter(typeName)),
                              () -> "Expected failure for " + typeName
                          )
            );
        }
    }

    @Nested
    class MapValues {

        @Test
        void are_implicitly_converted_according_to_parameterized_parameter_type() {
            List.of(
                List.of(
                    "java.util.Map<?, ?>",
                    Map.of(),
                    Map.of()
                ),
                List.of(
                    "java.util.Map<java.lang.String, java.lang.Byte>",
                    Map.of("key", "1"),
                    Map.of("key", (byte) 1)
                ),
                List.of(
                    "java.util.Map<?, java.lang.Integer>",
                    Map.of("key", "2"),
                    Map.of("key", 2)
                ),
                List.of(
                    "java.util.Map<?, java.lang.Long>",
                    Map.of("key", "3"),
                    Map.of("key", 3L)
                ),
                List.of(
                    "java.util.Map<?, java.lang.Double>",
                    Map.of("key", "4"),
                    Map.of("key", 4.0)
                ),
                List.of(
                    "java.util.Map<?, java.lang.String>",
                    Map.of("key", "5"),
                    Map.of("key", "5")
                ),
                List.of(
                    "java.util.Map<?, java.util.List<java.lang.Short>>",
                    Map.of("key", List.of("6")),
                    Map.of("key", List.of((short) 6))
                ),
                List.of(
                    "java.util.Map<?, java.util.Map<?, java.lang.Long>>",
                    Map.of("key", Map.of("1", "7")),
                    Map.of("key", Map.of("1", 7L))
                )
            ).forEach((testCase) -> {
                          String typeName = (String) testCase.get(0);
                          Map<?, ?> parsedListValue = (Map<?, ?>) testCase.get(1);
                          Map<?, ?> expectedValue = (Map<?, ?>) testCase.get(2);
                          Object actualValue = convertValue(parsedListValue, parameter(typeName));
                          assertEquals(expectedValue, actualValue, () -> "Failed for " + typeName);
                      }
            );
        }

        @Test
        void fail_when_conversion_according_to_parameterized_parameter_type_not_possible() {
            Map.of(
                "java.util.Map<?, java.lang.Short>", Map.of("key", "x")
            ).forEach((String typeName, Map<?, ?> parsedMapValue) ->
                          assertThrows(
                              ConversionException.class,
                              () -> convertValue(parsedMapValue, parameter(typeName)),
                              () -> "Expected failure for " + typeName
                          )
            );
        }
    }

}
