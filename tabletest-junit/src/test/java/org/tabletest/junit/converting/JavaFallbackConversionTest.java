package org.tabletest.junit.converting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.tabletest.junit.javadomain.ConstructorDate;
import org.tabletest.junit.javadomain.TypeFactoryDate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("spec")
@DisplayName("Built-in conversion")
@Description("""
        Cell text converts automatically to the declared parameter type. Each
        column shows accepted input formats for the type named in its header —
        a passing row means every cell converted.
        """)
public class JavaFallbackConversionTest {

    @DisplayName("Integer types accept decimal, hex, and octal formats")
    @Description("Applies to byte, short, int, and long — primitive or boxed.")
    @TableTest("""
        Format  | Byte | Short | Integer | Long | Expected
        Decimal | 15   | 15    | 15      | 15   | 15
        Hex     | 0xF  | 0xF   | 0xF     | 0xF  | 15
        Octal   | 017  | 017   | 017     | 017  | 15
        """)
    void converts_numeric_formats_to_expected_primitive_value(
        byte byteValue,
        short shortValue,
        int intValue,
        long longValue,
        String expected
    ) {
        assertEquals(expected, String.valueOf(byteValue));
        assertEquals(expected, String.valueOf(shortValue));
        assertEquals(expected, String.valueOf(intValue));
        assertEquals(expected, String.valueOf(longValue));
    }

    // Not published: boxed twin of the table above — renders identically.
    @Tag("unpublished")
    @TableTest("""
        Format  | Byte | Short | Integer | Long | Expected
        Decimal | 15   | 15    | 15      | 15   | 15
        Hex     | 0xF  | 0xF   | 0xF     | 0xF  | 15
        Octal   | 017  | 017   | 017     | 017  | 15
        """)
    void converts_numeric_formats_to_expected_value(
        Byte byteValue,
        Short shortValue,
        Integer intValue,
        Long longValue,
        String expected
    ) {
        assertEquals(expected, String.valueOf(byteValue));
        assertEquals(expected, String.valueOf(shortValue));
        assertEquals(expected, String.valueOf(intValue));
        assertEquals(expected, String.valueOf(longValue));
    }

    @DisplayName("Decimal types accept plain and scientific notation")
    @Description("float and double, primitive or boxed, plus BigDecimal and BigInteger.")
    @TableTest("""
        Float Primitive | Double Primitive | Float   | Double      | BigDecimal  | BigInteger
        3.14159         | 2.718281828      | 3.14159 | 2.718281828 | 123.456e789 | 1234567890123456789
        0.1             | 0.1              | 0.1     | 0.1         | 1           | 15
        1.23e4          | 1.23e4           | 1.23e4  | 1.23e4      | 1.23e4      | 123
        """)
    void converts_decimal_types(
        float floatPrimitive,
        double doublePrimitive,
        Float floatVal,
        Double doubleVal,
        BigDecimal bigDecVal,
        BigInteger bigIntVal
    ) {
        assertNotNull(floatVal);
        assertNotNull(doubleVal);
        assertEquals(floatPrimitive, floatVal);
        assertEquals(doublePrimitive, doubleVal);
        assertNotNull(bigDecVal);
        assertNotNull(bigIntVal);
    }

    @DisplayName("Numeric literals widen to larger primitive types")
    @TableTest("""
        byte | widened byte? | float | widened float? | double | widened double?
        123  | 123           | 123   | 123.0          | 123    | 123.0
        """)
    void allows_widening_primitive_conversion(
        byte byteVal,
        byte widenedByte,
        float floatVal,
        float widenedFloat,
        double doubleVal,
        double widenedDouble
    ) {
        assertEquals(byteVal, widenedByte);
        assertEquals(floatVal, widenedFloat);
        assertEquals(doubleVal, widenedDouble);
    }

    @DisplayName("Chars, strings, enums, and booleans convert directly")
    @Description("Enum values are matched by constant name — TimeUnit in this table.")
    @TableTest("""
        char | Character | String   | TimeUnit | boolean | Boolean
        a    | a         | abc      | SECONDS  | true    | true
        Z    | Z         | "quoted" | MINUTES  | false   | false
        1    | 1         | 'single' | HOURS    | true    | true
        """)
    void converts_basic_types(
        char charPrimitive,
        Character charVal,
        String stringVal,
        TimeUnit timeUnit,
        boolean boolPrimitive,
        Boolean boolVal
    ) {
        assertNotNull(charVal);
        assertNotNull(stringVal);
        assertNotNull(timeUnit);
        assertNotNull(boolVal);
        assertEquals(charPrimitive, charVal);
        assertEquals(boolPrimitive, boolVal);
    }

    @DisplayName("Files, paths, URIs, URLs, and class names convert")
    @TableTest("""
        File            | Path            | URI                 | URL                 | Class Name
        /path/to/file   | /path/to/file   | https://junit.org/  | https://junit.org/  | java.lang.Integer
        C:\\Users\\test | C:\\Users\\test | file:///tmp/test    | https://example.com | java.lang.Thread$State
        ./relative/path | ./relative/path | urn:isbn:1234567890 | file:///tmp/test    | byte
        """)
    void converts_resource_types(
        java.io.File fileVal,
        java.nio.file.Path pathVal,
        java.net.URI uriVal,
        java.net.URL urlVal,
        Class<?> classVal
    ) {
        assertNotNull(fileVal);
        assertNotNull(pathVal);
        assertNotNull(uriVal);
        assertNotNull(urlVal);
        assertNotNull(classVal);
    }

    @DisplayName("Charsets, currencies, locales, and UUIDs convert")
    @TableTest("""
        Charset    | Currency | Locale | UUID
        UTF-8      | NOK      | en     | d043e930-7b3b-48e3-bdbe-5a3ccfb833db
        UTF-16     | USD      | en_US  | 550e8400-e29b-41d4-a716-446655440000
        ISO-8859-1 | EUR      | no_NO  | 6ba7b810-9dad-11d1-80b4-00c04fd430c8
        """)
    void converts_locale_types(
        java.nio.charset.Charset charsetVal,
        java.util.Currency currencyVal,
        java.util.Locale localeVal,
        java.util.UUID uuidVal
    ) {
        assertNotNull(charsetVal);
        assertNotNull(currencyVal);
        assertNotNull(localeVal);
        assertNotNull(uuidVal);
    }

    @DisplayName("Durations, periods, years, and month-days use ISO-8601 formats")
    @TableTest("""
        Duration | Period  | Year | YearMonth | MonthDay
        PT3S     | P2M6D   | 2017 | 2017-03   | --03-14
        PT1H30M  | P1Y2M3D | 2025 | 2025-12   | --12-25
        PT0.123S | P0D     | 1999 | 1999-01   | --01-01
        """)
    void converts_temporal_periods(
        Duration durationVal,
        Period periodVal,
        Year yearVal,
        YearMonth yearMonthVal,
        MonthDay monthDayVal
    ) {
        assertNotNull(durationVal);
        assertNotNull(periodVal);
        assertNotNull(yearVal);
        assertNotNull(yearMonthVal);
        assertNotNull(monthDayVal);
    }

    @DisplayName("Local dates and times use ISO-8601 formats")
    @TableTest("""
        LocalDate  | LocalTime    | LocalDateTime           | ZoneId
        2017-03-14 | 12:34:56.789 | 2017-03-14T12:34:56.789 | Europe/Berlin
        2025-12-25 | 00:00:00     | 2025-12-25T23:59:59     | America/New_York
        1999-01-01 | 23:59:59.999 | 1999-01-01T00:00:01     | Asia/Tokyo
        """)
    void converts_local_temporal_types(
        LocalDate localDateVal,
        LocalTime localTimeVal,
        LocalDateTime localDateTimeVal,
        ZoneId zoneIdVal
    ) {
        assertNotNull(localDateVal);
        assertNotNull(localTimeVal);
        assertNotNull(localDateTimeVal);
        assertNotNull(zoneIdVal);
    }

    @DisplayName("Zoned and offset date-times use ISO-8601 formats")
    @TableTest("""
        Instant                  | OffsetDateTime            | OffsetTime     | ZonedDateTime             | ZoneOffset
        1977-08-17T18:19:20Z     | 2017-03-14T12:34:56.789Z  | 12:34:56.789Z  | 2017-03-14T12:34:56.789Z  | +02:30
        2025-01-01T00:00:00Z     | 2025-12-25T00:00:00+01:00 | 23:59:59+05:30 | 2025-12-25T12:00:00+09:00 | -05:00
        2000-01-01T12:00:00.123Z | 2000-06-15T18:30:45-07:00 | 06:15:30-03:00 | 2000-06-15T18:30:45+02:00 | +00:00
        """)
    void converts_zoned_temporal_types(
        Instant instantVal,
        OffsetDateTime offsetDateTimeVal,
        OffsetTime offsetTimeVal,
        ZonedDateTime zonedDateTimeVal,
        ZoneOffset zoneOffsetVal
    ) {
        assertNotNull(instantVal);
        assertNotNull(offsetDateTimeVal);
        assertNotNull(offsetTimeVal);
        assertNotNull(zonedDateTimeVal);
        assertNotNull(zoneOffsetVal);
    }

    @DisplayName("A type with a String constructor or factory method converts automatically")
    @Description("""
            When no built-in conversion exists, TableTest falls back to a
            single-argument constructor or a static factory method on the
            target type — inside collections too.
            """)
    @TableTest("""
        String     | Constructor | Factory method in type | List with fallback
        2025-05-27 | 2025-05-27  | 2025-05-27             | [2025-05-27]
        """)
    void converts_custom_types_with_constructor_or_type_internal_factory(
        LocalDate stringToLocalDate,
        ConstructorDate withConstructor,
        TypeFactoryDate withFactoryMethodInsideType,
        List<TypeFactoryDate> listWithFallbackConversion
    ) {
        assertEquals(stringToLocalDate, withConstructor.date());
        assertEquals(stringToLocalDate, withFactoryMethodInsideType.date());
        assertEquals(stringToLocalDate, listWithFallbackConversion.get(0).date());
    }

}
