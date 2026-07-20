package org.tabletest.junit.converting.builtin;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DisplayName("Dates and times")
@Description("""
        Cell text converts automatically to the declared parameter type, with no
        type converter to write. Every table below reads the same way: an Input
        value column holding the text as written in the cell, the parameter type
        it is converted to, and expectation columns stating observable properties
        of the object the test method received — so each row shows that the text
        became a valid object of that type, not merely that it converted.

        A parameter type is fixed by the test method signature and cannot vary by
        row, so each type gets its own short table.
        """)
public class JavaDateAndTimeConversionTest {

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
}
