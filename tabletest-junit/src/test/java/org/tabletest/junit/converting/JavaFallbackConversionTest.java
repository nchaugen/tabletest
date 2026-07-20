package org.tabletest.junit.converting;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.tabletest.junit.javadomain.ConstructorDate;
import org.tabletest.junit.javadomain.TypeFactoryDate;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DisplayName("Built-in conversion")
@Description("""
        Cell text converts automatically to the declared parameter type, with no
        type converter to write. Every table reads the same way: an Input value
        column holding the text as written in the cell, the parameter type it is
        converted to, and expectation columns stating observable properties of
        the object the test method received — so each row shows that the text
        became a valid object of that type, not merely that it converted.

        A parameter type is fixed by the test method signature and cannot vary by
        row, so each type gets its own short table. A primitive type shares its
        table with its wrapper, since the two are one type in two forms and a
        primitive's type cannot be observed once the value is boxed — there the
        type is the value column's own header rather than a column of its own.
        """)
public class JavaFallbackConversionTest {

    @DisplayName("byte holds a whole number in the 8-bit signed range")
    @Description("Decimal, hex, and octal text all convert, to the primitive and its wrapper alike.")
    @TableTest("""
        Scenario            | byte | Byte | Converted value?
        Decimal digits      | 15   | 15   | 15
        Hex literal         | 0xF  | 0xF  | 15
        Octal literal       | 017  | 017  | 15
        Most negative value | -128 | -128 | -128
        Largest value       | 127  | 127  | 127
        """)
    void converts_bytes(byte value, Byte boxedValue, byte expectedValue) {
        assertEquals(expectedValue, value);
        assertEquals(expectedValue, boxedValue.byteValue());
    }

    @DisplayName("short holds a whole number in the 16-bit signed range")
    @TableTest("""
        Scenario            | short  | Short  | Converted value?
        Decimal digits      | 15     | 15     | 15
        Hex literal         | 0xF    | 0xF    | 15
        Octal literal       | 017    | 017    | 15
        Most negative value | -32768 | -32768 | -32768
        Largest value       | 32767  | 32767  | 32767
        """)
    void converts_shorts(short value, Short boxedValue, short expectedValue) {
        assertEquals(expectedValue, value);
        assertEquals(expectedValue, boxedValue.shortValue());
    }

    @DisplayName("int holds a whole number in the 32-bit signed range")
    @TableTest("""
        Scenario            | int         | Integer     | Converted value?
        Decimal digits      | 15          | 15          | 15
        Hex literal         | 0xF         | 0xF         | 15
        Octal literal       | 017         | 017         | 15
        Most negative value | -2147483648 | -2147483648 | -2147483648
        Largest value       | 2147483647  | 2147483647  | 2147483647
        """)
    void converts_ints(int value, Integer boxedValue, int expectedValue) {
        assertEquals(expectedValue, value);
        assertEquals(expectedValue, boxedValue.intValue());
    }

    @DisplayName("long holds a whole number in the 64-bit signed range")
    @TableTest("""
        Scenario            | long                 | Long                 | Converted value?
        Decimal digits      | 15                   | 15                   | 15
        Hex literal         | 0xF                  | 0xF                  | 15
        Octal literal       | 017                  | 017                  | 15
        Most negative value | -9223372036854775808 | -9223372036854775808 | -9223372036854775808
        Largest value       | 9223372036854775807  | 9223372036854775807  | 9223372036854775807
        """)
    void converts_longs(long value, Long boxedValue, long expectedValue) {
        assertEquals(expectedValue, value);
        assertEquals(expectedValue, boxedValue.longValue());
    }

    @DisplayName("float accepts plain and scientific notation")
    @Description("Applies to the primitive and its wrapper alike.")
    @TableTest("""
        Scenario            | float   | Float   | Converted value?
        Plain decimal       | 3.14159 | 3.14159 | 3.14159
        Leading zero        | 0.1     | 0.1     | 0.1
        Scientific notation | 1.23e4  | 1.23e4  | 12300
        No decimal point    | 123     | 123     | 123
        """)
    void converts_floats(float value, Float boxedValue, float expectedValue) {
        assertEquals(expectedValue, value);
        assertEquals(expectedValue, boxedValue.floatValue());
    }

    @DisplayName("double accepts plain and scientific notation")
    @Description("Applies to the primitive and its wrapper alike.")
    @TableTest("""
        Scenario            | double  | Double  | Converted value?
        Plain decimal       | 3.14159 | 3.14159 | 3.14159
        Leading zero        | 0.1     | 0.1     | 0.1
        Scientific notation | 1.23e4  | 1.23e4  | 12300
        No decimal point    | 123     | 123     | 123
        """)
    void converts_doubles(double value, Double boxedValue, double expectedValue) {
        assertEquals(expectedValue, value);
        assertEquals(expectedValue, boxedValue.doubleValue());
    }

    @DisplayName("BigDecimal keeps the precision the text was written with")
    @Description("""
            Scale is the number of digits after the decimal point — negative when
            scientific notation places the value's precision above the point.
            """)
    @TableTest("""
        Scenario            | Input value | Parameter type?      | BigDecimal as plain number? | BigDecimal scale?
        Plain decimal       | 3.14159     | java.math.BigDecimal | 3.14159                     | 5
        Leading zero        | 0.1         | java.math.BigDecimal | 0.1                         | 1
        Scientific notation | 1.23e4      | java.math.BigDecimal | 12300                       | -2
        No decimal point    | 123         | java.math.BigDecimal | 123                         | 0
        """)
    void converts_big_decimals(
        BigDecimal value,
        Class<?> parameterType,
        String expectedPlainNumber,
        int expectedScale
    ) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedPlainNumber, value.toPlainString());
        assertEquals(expectedScale, value.scale());
    }

    @DisplayName("BigInteger holds whole numbers beyond the long range")
    @TableTest("""
        Scenario        | Input value                    | Parameter type?      | BigInteger beyond long range?
        Nineteen digits | 1234567890123456789            | java.math.BigInteger | false
        Thirty digits   | 123456789012345678901234567890 | java.math.BigInteger | true
        """)
    void converts_big_integers(BigInteger value, Class<?> parameterType, boolean expectedBeyondLong) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedBeyondLong, value.bitLength() > 63);
    }

    @DisplayName("Char cells hold a single character")
    @Description("Primitive and boxed alike; a digit cell becomes the digit character, not a number.")
    @TableTest("""
        Scenario         | char | Character | Code point?
        Lowercase letter | a    | a         | 97
        Uppercase letter | Z    | Z         | 90
        Digit            | 1    | 1         | 49
        """)
    void converts_chars(char charPrimitive, Character charBoxed, int expectedCodePoint) {
        assertEquals(expectedCodePoint, charPrimitive);
        assertEquals(expectedCodePoint, charBoxed.charValue());
    }

    @DisplayName("Booleans convert from true and false")
    @TableTest("""
        Scenario | boolean | Boolean | Negated?
        True     | true    | true    | false
        False    | false   | false   | true
        """)
    void converts_booleans(boolean boolPrimitive, Boolean boolBoxed, boolean expectedNegated) {
        assertEquals(expectedNegated, !boolPrimitive);
        assertEquals(expectedNegated, !boolBoxed);
    }

    @DisplayName("Enum values match by constant name")
    @Description("The parameter type decides which enum to search — TimeUnit in this table.")
    @TableTest("""
        Scenario | Input value | Parameter type?               | TimeUnit seconds per unit?
        Second   | SECONDS     | java.util.concurrent.TimeUnit | 1
        Minute   | MINUTES     | java.util.concurrent.TimeUnit | 60
        Hour     | HOURS       | java.util.concurrent.TimeUnit | 3600
        """)
    void converts_enums(TimeUnit value, Class<?> parameterType, long expectedSeconds) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedSeconds, value.toSeconds(1));
    }

    @DisplayName("Files convert from path text")
    @TableTest("""
        Scenario      | Input value     | Parameter type? | File name? | File is absolute?
        Absolute path | /path/to/file   | java.io.File    | file       | true
        Relative path | ./relative/path | java.io.File    | path       | false
        """)
    void converts_files(File value, Class<?> parameterType, String expectedName, boolean expectedAbsolute) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedName, value.getName());
        assertEquals(expectedAbsolute, value.isAbsolute());
    }

    @DisplayName("Paths convert from path text")
    @TableTest("""
        Scenario      | Input value     | Parameter type?    | Path file name? | Path is absolute?
        Absolute path | /path/to/file   | java.nio.file.Path | file            | true
        Relative path | ./relative/path | java.nio.file.Path | path            | false
        """)
    void converts_paths(Path value, Class<?> parameterType, String expectedFileName, boolean expectedAbsolute) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedFileName, value.getFileName().toString());
        assertEquals(expectedAbsolute, value.isAbsolute());
    }

    @DisplayName("URIs convert from address text, including non-URL schemes")
    @TableTest("""
        Scenario   | Input value         | Parameter type? | URI scheme?
        Web        | https://junit.org/  | java.net.URI    | https
        Local file | file:///tmp/test    | java.net.URI    | file
        URN        | urn:isbn:0451450523 | java.net.URI    | urn
        """)
    void converts_uris(URI value, Class<?> parameterType, String expectedScheme) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedScheme, value.getScheme());
    }

    @DisplayName("URLs convert from address text")
    @Description("A URL needs a protocol handler, so unlike URI it rejects schemes such as urn:.")
    @TableTest("""
        Scenario   | Input value        | Parameter type? | URL protocol?
        Web        | https://junit.org/ | java.net.URL    | https
        Local file | file:///tmp/test   | java.net.URL    | file
        """)
    void converts_urls(URL value, Class<?> parameterType, String expectedProtocol) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedProtocol, value.getProtocol());
    }

    @DisplayName("Class parameters accept fully qualified, nested, and primitive names")
    @TableTest("""
        Scenario        | Input value            | Parameter type? | Class simple name?
        Top-level class | java.lang.Integer      | java.lang.Class | Integer
        Nested class    | java.lang.Thread$State | java.lang.Class | State
        Primitive type  | byte                   | java.lang.Class | byte
        """)
    void converts_class_names(Class<?> value, Class<?> parameterType, String expectedSimpleName) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedSimpleName, value.getSimpleName());
    }

    @DisplayName("Charsets convert from canonical names and aliases")
    @TableTest("""
        Scenario       | Input value | Parameter type?          | Charset canonical name?
        Canonical name | UTF-8       | java.nio.charset.Charset | UTF-8
        Alias          | utf8        | java.nio.charset.Charset | UTF-8
        Historic alias | latin1      | java.nio.charset.Charset | ISO-8859-1
        """)
    void converts_charsets(
        java.nio.charset.Charset value,
        Class<?> parameterType,
        String expectedCanonicalName
    ) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedCanonicalName, value.name());
    }

    @DisplayName("Currencies convert from ISO 4217 codes")
    @TableTest("""
        Scenario        | Input value | Parameter type?    | Currency decimal places?
        Norwegian krone | NOK         | java.util.Currency | 2
        Japanese yen    | JPY         | java.util.Currency | 0
        Bahraini dinar  | BHD         | java.util.Currency | 3
        """)
    void converts_currencies(java.util.Currency value, Class<?> parameterType, int expectedDecimalPlaces) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedDecimalPlaces, value.getDefaultFractionDigits());
    }

    @DisplayName("Locales convert from IETF BCP 47 language tags")
    @Description("Language and country are separated by a hyphen, not an underscore.")
    @TableTest("""
        Scenario             | Input value | Parameter type?  | Locale language? | Locale country?
        Language only        | en          | java.util.Locale | en               | ''
        Language and country | en-US       | java.util.Locale | en               | US
        Non-English tag      | nb-NO       | java.util.Locale | nb               | NO
        """)
    void converts_locales(
        java.util.Locale value,
        Class<?> parameterType,
        String expectedLanguage,
        String expectedCountry
    ) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedLanguage, value.getLanguage());
        assertEquals(expectedCountry, value.getCountry());
    }

    @DisplayName("UUIDs convert from their standard text form")
    @Description("The first digit of the third group is the UUID version.")
    @TableTest("""
        Scenario        | Input value                          | Parameter type? | UUID version?
        Random UUID     | d043e930-7b3b-48e3-bdbe-5a3ccfb833db | java.util.UUID  | 4
        Time-based UUID | 6ba7b810-9dad-11d1-80b4-00c04fd430c8 | java.util.UUID  | 1
        """)
    void converts_uuids(java.util.UUID value, Class<?> parameterType, int expectedVersion) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedVersion, value.version());
    }

    @DisplayName("Durations use the ISO-8601 duration format")
    @TableTest("""
        Scenario           | Input value | Parameter type?    | Duration in milliseconds?
        Whole seconds      | PT3S        | java.time.Duration | 3000
        Hours and minutes  | PT1H30M     | java.time.Duration | 5400000
        Fractional seconds | PT0.123S    | java.time.Duration | 123
        """)
    void converts_durations(Duration value, Class<?> parameterType, long expectedMilliseconds) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedMilliseconds, value.toMillis());
    }

    @DisplayName("Periods use the ISO-8601 period format")
    @TableTest("""
        Scenario            | Input value | Parameter type?  | Period years? | Period months? | Period days?
        Months and days     | P2M6D       | java.time.Period | 0             | 2              | 6
        Years, months, days | P1Y2M3D     | java.time.Period | 1             | 2              | 3
        """)
    void converts_periods(
        Period value,
        Class<?> parameterType,
        int expectedYears,
        int expectedMonths,
        int expectedDays
    ) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedYears, value.getYears());
        assertEquals(expectedMonths, value.getMonths());
        assertEquals(expectedDays, value.getDays());
    }

    @DisplayName("Years hold a four-digit calendar year")
    @TableTest("""
        Scenario       | Input value | Parameter type? | Year value? | Year is a leap year?
        Pi Day 2017    | 2017        | java.time.Year  | 2017        | false
        Christmas 2024 | 2024        | java.time.Year  | 2024        | true
        """)
    void converts_years(Year value, Class<?> parameterType, int expectedYear, boolean expectedLeapYear) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedYear, value.getValue());
        assertEquals(expectedLeapYear, value.isLeap());
    }

    @DisplayName("Year-months name a month of a specific year")
    @TableTest("""
        Scenario       | Input value | Parameter type?     | YearMonth year? | YearMonth month? | YearMonth length?
        March 2017     | 2017-03     | java.time.YearMonth | 2017            | MARCH            | 31
        December 2025  | 2025-12     | java.time.YearMonth | 2025            | DECEMBER         | 31
        February 2024  | 2024-02     | java.time.YearMonth | 2024            | FEBRUARY         | 29
        """)
    void converts_year_months(
        YearMonth value,
        Class<?> parameterType,
        int expectedYear,
        Month expectedMonth,
        int expectedLength
    ) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedYear, value.getYear());
        assertEquals(expectedMonth, value.getMonth());
        assertEquals(expectedLength, value.lengthOfMonth());
    }

    @DisplayName("Month-days name a day of the year, written with two leading hyphens")
    @TableTest("""
        Scenario       | Input value | Parameter type?    | MonthDay month? | MonthDay day of month?
        Pi Day         | --03-14     | java.time.MonthDay | MARCH           | 14
        Christmas Day  | --12-25     | java.time.MonthDay | DECEMBER        | 25
        """)
    void converts_month_days(
        MonthDay value,
        Class<?> parameterType,
        Month expectedMonth,
        int expectedDayOfMonth
    ) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedMonth, value.getMonth());
        assertEquals(expectedDayOfMonth, value.getDayOfMonth());
    }

    @DisplayName("Local dates use the ISO-8601 date format")
    @TableTest("""
        Scenario        | Input value | Parameter type?     | LocalDate month? | LocalDate day of week?
        Pi Day 2017     | 2017-03-14  | java.time.LocalDate | MARCH            | TUESDAY
        Christmas 2025  | 2025-12-25  | java.time.LocalDate | DECEMBER         | THURSDAY
        """)
    void converts_local_dates(
        LocalDate value,
        Class<?> parameterType,
        Month expectedMonth,
        DayOfWeek expectedDayOfWeek
    ) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedMonth, value.getMonth());
        assertEquals(expectedDayOfWeek, value.getDayOfWeek());
    }

    @DisplayName("Local times use the ISO-8601 time format, with optional fractional seconds")
    @TableTest("""
        Scenario           | Input value  | Parameter type?     | LocalTime hour? | LocalTime minute? | LocalTime nano?
        Fractional seconds | 12:34:56.789 | java.time.LocalTime | 12              | 34                | 789000000
        Whole seconds      | 23:59:59     | java.time.LocalTime | 23              | 59                | 0
        Hours and minutes  | 06:15        | java.time.LocalTime | 6               | 15                | 0
        """)
    void converts_local_times(
        LocalTime value,
        Class<?> parameterType,
        int expectedHour,
        int expectedMinute,
        int expectedNano
    ) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedHour, value.getHour());
        assertEquals(expectedMinute, value.getMinute());
        assertEquals(expectedNano, value.getNano());
    }

    @DisplayName("Local date-times join a date and a time with T")
    @Description("The expectation columns are themselves converted, to a LocalDate and a LocalTime.")
    @TableTest("""
        Scenario        | Input value             | Parameter type?         | LocalDateTime date? | LocalDateTime time?
        Pi Day 2017     | 2017-03-14T12:34:56.789 | java.time.LocalDateTime | 2017-03-14          | 12:34:56.789
        Christmas night | 2025-12-25T23:59:59     | java.time.LocalDateTime | 2025-12-25          | 23:59:59
        """)
    void converts_local_date_times(
        LocalDateTime value,
        Class<?> parameterType,
        LocalDate expectedDate,
        LocalTime expectedTime
    ) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedDate, value.toLocalDate());
        assertEquals(expectedTime, value.toLocalTime());
    }

    @DisplayName("Instants are a point on the UTC timeline, always written with a trailing Z")
    @TableTest("""
        Scenario     | Input value          | Parameter type?    | Instant epoch second?
        Unix epoch   | 1970-01-01T00:00:00Z | java.time.Instant  | 0
        Pi Day 2017  | 2017-03-14T12:00:00Z | java.time.Instant  | 1489492800
        """)
    void converts_instants(Instant value, Class<?> parameterType, long expectedEpochSecond) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedEpochSecond, value.getEpochSecond());
    }

    @DisplayName("Offset date-times carry an offset from UTC")
    @Description("The expectation column is itself converted, to an Instant.")
    @TableTest("""
        Scenario    | Input value               | Parameter type?          | OffsetDateTime as instant in UTC?
        East of UTC | 2017-03-14T13:00:00+01:00 | java.time.OffsetDateTime | 2017-03-14T12:00:00Z
        At UTC      | 2025-12-25T00:00:00Z      | java.time.OffsetDateTime | 2025-12-25T00:00:00Z
        West of UTC | 2000-06-15T18:30:45-07:00 | java.time.OffsetDateTime | 2000-06-16T01:30:45Z
        """)
    void converts_offset_date_times(OffsetDateTime value, Class<?> parameterType, Instant expectedInstant) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedInstant, value.toInstant());
    }

    @DisplayName("Zoned date-times may name a region id in brackets after the offset")
    @TableTest("""
        Scenario     | Input value                              | Parameter type?         | ZonedDateTime zone id? | ZonedDateTime as instant in UTC?
        Region zone  | 2017-03-14T13:00:00+01:00[Europe/Berlin] | java.time.ZonedDateTime | Europe/Berlin          | 2017-03-14T12:00:00Z
        Offset only  | 2000-06-15T18:30:45-07:00                | java.time.ZonedDateTime | -07:00                 | 2000-06-16T01:30:45Z
        At UTC       | 2025-12-25T00:00:00Z                     | java.time.ZonedDateTime | Z                      | 2025-12-25T00:00:00Z
        """)
    void converts_zoned_date_times(
        ZonedDateTime value,
        Class<?> parameterType,
        String expectedZoneId,
        Instant expectedInstant
    ) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedZoneId, value.getZone().getId());
        assertEquals(expectedInstant, value.toInstant());
    }

    @DisplayName("Offset times are a time of day with an offset from UTC")
    @TableTest("""
        Scenario    | Input value    | Parameter type?      | OffsetTime hour? | OffsetTime offset in minutes?
        East of UTC | 12:00:00+02:30 | java.time.OffsetTime | 12               | 150
        West of UTC | 06:15:30-03:00 | java.time.OffsetTime | 6                | -180
        At UTC      | 23:59:59Z      | java.time.OffsetTime | 23               | 0
        """)
    void converts_offset_times(
        OffsetTime value,
        Class<?> parameterType,
        int expectedHour,
        int expectedOffsetMinutes
    ) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedHour, value.getHour());
        assertEquals(expectedOffsetMinutes, value.getOffset().getTotalSeconds() / 60);
    }

    @DisplayName("Zone offsets are an offset from UTC on its own, with Z meaning none")
    @TableTest("""
        Scenario    | Input value | Parameter type?      | ZoneOffset in minutes?
        East of UTC | +02:30      | java.time.ZoneOffset | 150
        West of UTC | -03:00      | java.time.ZoneOffset | -180
        At UTC      | Z           | java.time.ZoneOffset | 0
        """)
    void converts_zone_offsets(ZoneOffset value, Class<?> parameterType, int expectedOffsetMinutes) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedOffsetMinutes, value.getTotalSeconds() / 60);
    }

    @DisplayName("Zone ids accept region names, UTC, and fixed offsets")
    @Description("Region zones carry daylight-saving rules; UTC and plain offsets are fixed.")
    @TableTest("""
        Scenario     | Input value   | Parameter type?  | ZoneId fixed offset?
        Region zone  | Europe/Berlin | java.time.ZoneId | false
        UTC          | UTC           | java.time.ZoneId | true
        Plain offset | +02:00        | java.time.ZoneId | true
        """)
    void converts_zone_ids(ZoneId value, Class<?> parameterType, boolean expectedFixedOffset) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedFixedOffset, value.getRules().isFixedOffset());
    }

    @DisplayName("A type with a String constructor or factory method converts automatically")
    @Description("""
            When no built-in conversion exists, TableTest falls back to a
            single-argument constructor or a static factory method on the
            target type — inside collections too. Both custom date types here
            wrap the LocalDate in the expectation column.
            """)
    @TableTest("""
        Scenario                 | Constructor | Factory method in type | List with fallback | Wrapped date?
        ISO date in every column | 2025-05-27  | 2025-05-27             | [2025-05-27]       | 2025-05-27
        """)
    void converts_custom_types_with_constructor_or_type_internal_factory(
        ConstructorDate withConstructor,
        TypeFactoryDate withFactoryMethodInsideType,
        List<TypeFactoryDate> listWithFallbackConversion,
        LocalDate expectedDate
    ) {
        assertEquals(expectedDate, withConstructor.date());
        assertEquals(expectedDate, withFactoryMethodInsideType.date());
        assertEquals(expectedDate, listWithFallbackConversion.get(0).date());
    }

}
