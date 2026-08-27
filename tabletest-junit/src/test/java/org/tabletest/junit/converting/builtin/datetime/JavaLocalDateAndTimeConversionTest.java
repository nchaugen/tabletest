package org.tabletest.junit.converting.builtin.datetime;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DisplayName("Dates and times without an offset")
@Description("""
        Cell text converts automatically to the declared parameter type. There is no type
        converter to write.

        Every table below reads the same way. An Input value column holds the text as written in
        the cell. A Parameter type column holds the type the text converts to. Expectation
        columns state observable properties of the object the test method receives. Each row
        therefore shows that the text became a valid object of that type, and not merely that
        it converted.

        The test method signature fixes a parameter type, and no row can vary it. Each type
        therefore gets its own short table.

        Each type here names a date or a time alone. None of them carries an offset from UTC or
        a time zone. None of them therefore fixes a point on the world timeline.
        """)
public class JavaLocalDateAndTimeConversionTest {

    @DisplayName("Converts a four-digit calendar year to Year")
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

    @DisplayName("Converts a year and a month to YearMonth")
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

    @DisplayName("Converts a month and day written with two leading hyphens to MonthDay")
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

    @DisplayName("Converts ISO-8601 date text to LocalDate")
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

    @DisplayName("Converts ISO-8601 time text to LocalTime")
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

    @DisplayName("Converts a date and a time joined with T to LocalDateTime")
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
}
