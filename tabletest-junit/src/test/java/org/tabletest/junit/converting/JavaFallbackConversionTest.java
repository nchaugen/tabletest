package org.tabletest.junit.converting;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.tabletest.junit.javadomain.ConstructorDate;
import org.tabletest.junit.javadomain.TypeFactoryDate;

import java.math.BigDecimal;
import java.math.BigInteger;
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

@Tag("spec")
@DisplayName("Built-in conversion")
@Description("""
        Cell text converts automatically to the declared parameter type. Each
        column named after a type shows accepted input formats for that type;
        the expectation columns state an observable property of the converted
        value, so every row shows what the text became — not just that it
        converted.
        """)
public class JavaFallbackConversionTest {

    @DisplayName("Integer types accept decimal, hex, and octal formats")
    @Description("Applies to byte, short, int, and long — primitive or boxed.")
    @TableTest("""
        Format  | byte | short | int | long | Value?
        Decimal | 15   | 15    | 15  | 15   | 15
        Hex     | 0xF  | 0xF   | 0xF | 0xF  | 15
        Octal   | 017  | 017   | 017 | 017  | 15
        """)
    void converts_integer_formats(
        byte byteValue,
        short shortValue,
        int intValue,
        long longValue,
        long expectedValue
    ) {
        assertEquals(expectedValue, byteValue);
        assertEquals(expectedValue, shortValue);
        assertEquals(expectedValue, intValue);
        assertEquals(expectedValue, longValue);
    }

    // Not published: boxed twin of the table above — renders identically.
    @Tag("unpublished")
    @TableTest("""
        Format  | Byte | Short | Integer | Long | Value?
        Decimal | 15   | 15    | 15      | 15   | 15
        Hex     | 0xF  | 0xF   | 0xF     | 0xF  | 15
        Octal   | 017  | 017   | 017     | 017  | 15
        """)
    void converts_integer_formats_to_boxed_types(
        Byte byteValue,
        Short shortValue,
        Integer intValue,
        Long longValue,
        long expectedValue
    ) {
        assertEquals(expectedValue, byteValue.longValue());
        assertEquals(expectedValue, shortValue.longValue());
        assertEquals(expectedValue, intValue.longValue());
        assertEquals(expectedValue, longValue.longValue());
    }

    @DisplayName("Decimal types accept plain and scientific notation")
    @Description("Applies to float and double — primitive or boxed — and BigDecimal.")
    @TableTest("""
        Format       | float   | double  | BigDecimal | Value?
        Plain        | 3.14159 | 3.14159 | 3.14159    | 3.14159
        Fraction     | 0.1     | 0.1     | 0.1        | 0.1
        Scientific   | 1.23e4  | 1.23e4  | 1.23e4     | 12300
        Whole number | 123     | 123     | 123        | 123
        """)
    void converts_decimal_formats(
        float floatValue,
        double doubleValue,
        BigDecimal bigDecimalValue,
        double expectedValue
    ) {
        assertEquals((float) expectedValue, floatValue);
        assertEquals(expectedValue, doubleValue);
        assertEquals(0, bigDecimalValue.compareTo(BigDecimal.valueOf(expectedValue)));
    }

    @DisplayName("BigInteger holds whole numbers beyond the long range")
    @TableTest("""
        Scenario        | BigInteger                     | Larger than any long?
        Nineteen digits | 1234567890123456789            | false
        Thirty digits   | 123456789012345678901234567890 | true
        """)
    void converts_big_integers(BigInteger bigIntegerValue, boolean expectedBeyondLong) {
        assertEquals(expectedBeyondLong, bigIntegerValue.bitLength() > 63);
    }

    @DisplayName("Char cells hold a single character")
    @Description("Primitive and boxed alike; a digit cell becomes the digit character, not a number.")
    @TableTest("""
        char | Character | Code point?
        a    | a         | 97
        Z    | Z         | 90
        1    | 1         | 49
        """)
    void converts_chars(char charPrimitive, Character charBoxed, int expectedCodePoint) {
        assertEquals(expectedCodePoint, charPrimitive);
        assertEquals(charPrimitive, charBoxed);
    }

    @DisplayName("Booleans convert from true and false")
    @TableTest("""
        boolean | Boolean | Negated?
        true    | true    | false
        false   | false   | true
        """)
    void converts_booleans(boolean boolPrimitive, Boolean boolBoxed, boolean expectedNegated) {
        assertEquals(expectedNegated, !boolPrimitive);
        assertEquals(boolPrimitive, boolBoxed);
    }

    @DisplayName("Enum values match by constant name")
    @Description("The parameter type decides which enum to search — TimeUnit in this table.")
    @TableTest("""
        TimeUnit | One unit in seconds?
        SECONDS  | 1
        MINUTES  | 60
        HOURS    | 3600
        """)
    void converts_enums(TimeUnit timeUnit, long expectedSeconds) {
        assertEquals(expectedSeconds, timeUnit.toSeconds(1));
    }

    @DisplayName("Files and paths convert from path text")
    @TableTest("""
        Scenario      | File            | Path            | File name?
        Absolute path | /path/to/file   | /path/to/file   | file
        Relative path | ./relative/path | ./relative/path | path
        """)
    void converts_files_and_paths(
        java.io.File fileValue,
        java.nio.file.Path pathValue,
        String expectedFileName
    ) {
        assertEquals(expectedFileName, fileValue.getName());
        assertEquals(expectedFileName, pathValue.getFileName().toString());
    }

    @DisplayName("URIs and URLs convert from address text")
    @Description("URI additionally accepts non-URL schemes such as urn:.")
    @TableTest("""
        Scenario   | URI                | URL                | Scheme?
        Web        | https://junit.org/ | https://junit.org/ | https
        Local file | file:///tmp/test   | file:///tmp/test   | file
        """)
    void converts_uris_and_urls(
        java.net.URI uriValue,
        java.net.URL urlValue,
        String expectedScheme
    ) {
        assertEquals(expectedScheme, uriValue.getScheme());
        assertEquals(expectedScheme, urlValue.getProtocol());
    }

    @DisplayName("Class parameters accept fully qualified, nested, and primitive names")
    @TableTest("""
        Class name             | Simple name?
        java.lang.Integer      | Integer
        java.lang.Thread$State | State
        byte                   | byte
        """)
    void converts_class_names(Class<?> classValue, String expectedSimpleName) {
        assertEquals(expectedSimpleName, classValue.getSimpleName());
    }

    @DisplayName("Charsets convert from canonical names and aliases")
    @TableTest("""
        Charset | Canonical name?
        UTF-8   | UTF-8
        utf8    | UTF-8
        latin1  | ISO-8859-1
        """)
    void converts_charsets(java.nio.charset.Charset charsetValue, String expectedCanonicalName) {
        assertEquals(expectedCanonicalName, charsetValue.name());
    }

    @DisplayName("Currencies convert from ISO 4217 codes")
    @TableTest("""
        Currency | Decimal places?
        NOK      | 2
        JPY      | 0
        BHD      | 3
        """)
    void converts_currencies(java.util.Currency currencyValue, int expectedDecimalPlaces) {
        assertEquals(expectedDecimalPlaces, currencyValue.getDefaultFractionDigits());
    }

    @DisplayName("Locales convert from IETF BCP 47 language tags")
    @Description("Language and country are separated by a hyphen, not an underscore.")
    @TableTest("""
        Locale | Language? | Country?
        en     | en        | ''
        en-US  | en        | US
        nb-NO  | nb        | NO
        """)
    void converts_locales(
        java.util.Locale localeValue,
        String expectedLanguage,
        String expectedCountry
    ) {
        assertEquals(expectedLanguage, localeValue.getLanguage());
        assertEquals(expectedCountry, localeValue.getCountry());
    }

    @DisplayName("UUIDs convert from their standard text form")
    @Description("The first digit of the third group is the UUID version.")
    @TableTest("""
        UUID                                 | Version?
        d043e930-7b3b-48e3-bdbe-5a3ccfb833db | 4
        6ba7b810-9dad-11d1-80b4-00c04fd430c8 | 1
        """)
    void converts_uuids(java.util.UUID uuidValue, int expectedVersion) {
        assertEquals(expectedVersion, uuidValue.version());
    }

    @DisplayName("Durations use the ISO-8601 duration format")
    @TableTest("""
        Duration | In milliseconds?
        PT3S     | 3000
        PT1H30M  | 5400000
        PT0.123S | 123
        """)
    void converts_durations(Duration durationValue, long expectedMilliseconds) {
        assertEquals(expectedMilliseconds, durationValue.toMillis());
    }

    @DisplayName("Periods use the ISO-8601 period format")
    @TableTest("""
        Period  | Years? | Months? | Days?
        P2M6D   | 0      | 2       | 6
        P1Y2M3D | 1      | 2       | 3
        """)
    void converts_periods(
        Period periodValue,
        int expectedYears,
        int expectedMonths,
        int expectedDays
    ) {
        assertEquals(expectedYears, periodValue.getYears());
        assertEquals(expectedMonths, periodValue.getMonths());
        assertEquals(expectedDays, periodValue.getDays());
    }

    @DisplayName("Years, year-months, and month-days use ISO-8601 formats")
    @Description("Each row spells out the same calendar date across the three partial-date types.")
    @TableTest("""
        Scenario       | Year | YearMonth | MonthDay | Month?   | Day of month?
        Pi Day 2017    | 2017 | 2017-03   | --03-14  | MARCH    | 14
        Christmas 2025 | 2025 | 2025-12   | --12-25  | DECEMBER | 25
        """)
    void converts_partial_dates(
        Year yearValue,
        YearMonth yearMonthValue,
        MonthDay monthDayValue,
        Month expectedMonth,
        int expectedDayOfMonth
    ) {
        assertEquals(yearValue.getValue(), yearMonthValue.getYear());
        assertEquals(expectedMonth, yearMonthValue.getMonth());
        assertEquals(expectedMonth, monthDayValue.getMonth());
        assertEquals(expectedDayOfMonth, monthDayValue.getDayOfMonth());
    }

    @DisplayName("Local dates and times use ISO-8601 formats")
    @Description("""
            Each row holds one moment: the LocalDateTime column combines the
            LocalDate and LocalTime columns, and the expectations hold for all
            three.
            """)
    @TableTest("""
        Scenario        | LocalDate  | LocalTime    | LocalDateTime           | Day of week? | Hour of day?
        Pi Day 2017     | 2017-03-14 | 12:34:56.789 | 2017-03-14T12:34:56.789 | TUESDAY      | 12
        Christmas night | 2025-12-25 | 23:59:59     | 2025-12-25T23:59:59     | THURSDAY     | 23
        """)
    void converts_local_dates_and_times(
        LocalDate localDateValue,
        LocalTime localTimeValue,
        LocalDateTime localDateTimeValue,
        DayOfWeek expectedDayOfWeek,
        int expectedHourOfDay
    ) {
        assertEquals(localDateValue, localDateTimeValue.toLocalDate());
        assertEquals(localTimeValue, localDateTimeValue.toLocalTime());
        assertEquals(expectedDayOfWeek, localDateValue.getDayOfWeek());
        assertEquals(expectedHourOfDay, localTimeValue.getHour());
    }

    @DisplayName("Offset and zoned date-times name the same moment as an instant in UTC")
    @Description("""
            The expectation column is itself converted to an Instant. A zoned
            value may carry a region id in brackets after the offset.
            """)
    @TableTest("""
        Scenario    | OffsetDateTime            | ZonedDateTime                            | As instant in UTC?
        East of UTC | 2017-03-14T13:00:00+01:00 | 2017-03-14T13:00:00+01:00[Europe/Berlin] | 2017-03-14T12:00:00Z
        At UTC      | 2025-12-25T00:00:00Z      | 2025-12-25T00:00:00Z                     | 2025-12-25T00:00:00Z
        West of UTC | 2000-06-15T18:30:45-07:00 | 2000-06-15T18:30:45-07:00                | 2000-06-16T01:30:45Z
        """)
    void converts_offset_and_zoned_date_times(
        OffsetDateTime offsetDateTimeValue,
        ZonedDateTime zonedDateTimeValue,
        Instant expectedInstant
    ) {
        assertEquals(expectedInstant, offsetDateTimeValue.toInstant());
        assertEquals(expectedInstant, zonedDateTimeValue.toInstant());
    }

    @DisplayName("Offset times and zone offsets state an offset from UTC")
    @TableTest("""
        Scenario    | OffsetTime     | ZoneOffset | Offset in minutes?
        East of UTC | 12:00:00+02:30 | +02:30     | 150
        West of UTC | 06:15:30-03:00 | -03:00     | -180
        At UTC      | 23:59:59Z      | Z          | 0
        """)
    void converts_offset_times_and_zone_offsets(
        OffsetTime offsetTimeValue,
        ZoneOffset zoneOffsetValue,
        int expectedOffsetMinutes
    ) {
        assertEquals(expectedOffsetMinutes, offsetTimeValue.getOffset().getTotalSeconds() / 60);
        assertEquals(expectedOffsetMinutes, zoneOffsetValue.getTotalSeconds() / 60);
    }

    @DisplayName("Zone ids accept region names, UTC, and fixed offsets")
    @Description("Region zones carry daylight-saving rules; UTC and plain offsets are fixed.")
    @TableTest("""
        Scenario     | ZoneId        | Fixed offset?
        Region zone  | Europe/Berlin | false
        UTC          | UTC           | true
        Plain offset | +02:00        | true
        """)
    void converts_zone_ids(ZoneId zoneIdValue, boolean expectedFixedOffset) {
        assertEquals(expectedFixedOffset, zoneIdValue.getRules().isFixedOffset());
    }

    @DisplayName("A type with a String constructor or factory method converts automatically")
    @Description("""
            When no built-in conversion exists, TableTest falls back to a
            single-argument constructor or a static factory method on the
            target type — inside collections too. Both custom date types here
            wrap the LocalDate in the expectation column.
            """)
    @TableTest("""
        Constructor | Factory method in type | List with fallback | Wrapped date?
        2025-05-27  | 2025-05-27             | [2025-05-27]       | 2025-05-27
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
