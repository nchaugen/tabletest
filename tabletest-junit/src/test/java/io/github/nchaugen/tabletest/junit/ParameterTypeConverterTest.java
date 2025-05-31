package io.github.nchaugen.tabletest.junit;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.support.conversion.ConversionException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        void converts_according_to_junit_rules(String value, Class<?> type) {
            assertInstanceOf(type, convertValue(value, parameter(type)));
        }

        @TableTest("""
            table value | parameter type
            0.1         | java.lang.Byte
            256         | java.lang.Byte
            abc         | java.lang.Character
            """)
        void fails_conversion_for_values_outside_type_range(
            String value,
            Class<?> type
        ) {
            assertThrows(
                ConversionException.class,
                () -> convertValue(value, parameter(type))
            );
        }

        @TableTest("""
            table value | parameter type
            123         | java.lang.Byte
            123         | java.lang.Float
            123         | java.lang.Double
            """)
        void allows_widening_primitive_conversion(String value, Class<?> type) {
            assertInstanceOf(type, convertValue(value, parameter(type)));
        }

        @TableTest("""
            Scenario     | table value | parameter type
            Empty string | ""          | java.lang.Float
            Blank cell   |             | java.util.List
            """)
        void converts_blank_to_null_for_non_string_parameters(String value, Class<?> type) {
            assertNull(convertValue(value, parameter(type)));
        }

        @TableTest("""
            Scenario     | table value | parameter type
            Empty string | ''          | java.lang.String
            Blank cell   |             | java.lang.String
            """)
        void converts_blank_to_blank_for_string_parameters(String value, Class<?> type) {
            assertEquals("", convertValue(value, parameter(type)));
        }

    }

    @Nested
    class ListValues {

        @TableTest("""
        Empty | Byte | Integer | Long | Double | String | List  | Map
        []    | [1]  | [2]     | [3]  | [4]    | [5]    | [[6]] | [[1:7]]
        """)
        void converts_to_parameterized_parameter_type(
            List<?> emptyList,
            List<Byte> byteList,
            List<Integer> integerList,
            List<Long> longList,
            List<Double> doubleList,
            List<String> stringList,
            List<List<Short>> listList,
            List<Map<?, Long>> mapList
        ) {
            assertEquals(List.of(), emptyList);
            assertEquals(List.of((byte) 1), byteList);
            assertEquals(List.of(2), integerList);
            assertEquals(List.of(3L), longList);
            assertEquals(List.of(4.0), doubleList);
            assertEquals(List.of("5"), stringList);
            assertEquals(List.of(List.of((short) 6)), listList);
            assertEquals(List.of(Map.of("1", 7L)), mapList);
        }

        @Test
        void fails_when_conversion_not_possible() {
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

        @TableTest("""
        Empty | Byte    | Integer | Long    | Double  | String  | List      | Map
        [:]   | [key:1] | [key:2] | [key:3] | [key:4] | [key:5] | [key:[6]] | [key:[1:7]]
        """)
        void converts_to_parameterized_parameter_type(
            Map<?, ?> emptyMap,
            Map<String, Byte> byteMap,
            Map<?, Integer> integerMap,
            Map<?, Long> longMap,
            Map<?, Double> doubleMap,
            Map<?, String> stringMap,
            Map<?, List<Short>> listMap,
            Map<?, Map<?, Long>> mapMap
        ) {
            assertEquals(Map.of(), emptyMap);
            assertEquals(Map.of("key", (byte) 1), byteMap);
            assertEquals(Map.of("key", 2), integerMap);
            assertEquals(Map.of("key", 3L), longMap);
            assertEquals(Map.of("key", 4.0), doubleMap);
            assertEquals(Map.of("key", "5"), stringMap);
            assertEquals(Map.of("key", List.of((short) 6)), listMap);
            assertEquals(Map.of("key", Map.of("1", 7L)), mapMap);
        }

        @Test
        void fails_when_conversion_not_possible() {
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

    @Nested
    class CustomConversion {

        @TableTest("""
            String | List | Set  | Applicable Value Set | Map         | Nested           | Nested Ages
            16     | [16] | {16} | {16}                 | [value: 16] | [value: [16,16]] | [value: [16,16]]
            """)
        void uses_factory_method_in_test_class(
            Age fromString,
            List<Age> inList,
            Set<Age> inSet,
            Age fromApplicableSet,
            Map<String, Age> inMap,
            Map<String, List<Age>> inNested,
            Ages inCustom
        ) {
            Age expected = new Age(16);
            assertEquals(expected, fromString);
            assertEquals(List.of(expected), inList);
            assertEquals(Set.of(expected), inSet);
            assertEquals(expected, fromApplicableSet);
            assertEquals(Map.of("value", expected), inMap);
            assertEquals(Map.of("value", List.of(expected, expected)), inNested);
            assertEquals(new Ages(List.of(expected, expected)), inCustom);
        }

        @SuppressWarnings("unused")
        static Age parseAge(int age) {
            return new Age(age);
        }

        @SuppressWarnings("unused")
        static Ages parseAges(Map<String, List<Age>> age) {
            return new Ages(age.get("value"));
        }

        record Age(int age) {}
        record Ages(List<Age> ages) {}

        @TableTest("""
            JUnit convert | Constructor convert | Factory method convert | List of factory method convert
            2025-05-27    | 2025-05-27          | 2025-05-27             | [2025-05-27]
            """)
        void factory_methods_takes_priority_over_junit_conversion(
            LocalDate date,
            ConstructorDate date1,
            FactoryMethodDate date2,
            List<FactoryMethodDate> list
        ) {
            assertEquals(date, date1.date);
            assertEquals(date, date2.date);
            assertEquals(date, list.getFirst().date);
        }

        record ConstructorDate(LocalDate date) {
            @SuppressWarnings("unused")
            ConstructorDate(String date) {
                this(LocalDate.parse(date));
            }
        }
        record FactoryMethodDate(LocalDate date) {}

        @SuppressWarnings("unused")
        static FactoryMethodDate parseDate(String date) {
            return new FactoryMethodDate(LocalDate.parse(date));
        }
    }
}
