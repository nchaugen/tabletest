package org.tabletest.junit.examples;

import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.tabletest.junit.TypeConverter;
import org.junit.jupiter.api.DisplayName;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Leap year rules")
@Description("""
        The classic introductory example: the Gregorian leap year rules, first as one
        example per rule, then with value sets showing each rule holds across eras.
        """)
public class LeapYearExampleTest {

    @DisplayName("The four rules, one example year each")
    @Description("""
            Yes/No in the expectation column is read by a custom type converter.

            Six rows for four rules: the last two are outside them. Year zero and a year before
            the common era are where a reader wonders whether the arithmetic still holds, and the
            rows say that it does.
            """)
    @TableTest("""
        Scenario                        | Year | Is Leap Year?
        Not divisible by 4              | 2001 | No
        Divisible by 4                  | 2004 | Yes
        Divisible by 100 but not by 400 | 2100 | No
        Divisible by 400                | 2000 | Yes
        Year zero                       | 0    | Yes
        A year before the common era    | -1   | No
        """)
    void testLeapYear(int year, boolean isLeapYear) {
        assertEquals(isLeapYear, Year.isLeap(year));
    }

    @TypeConverter
    @SuppressWarnings("unused")
    public static boolean parseYesNo(String input) {
        return input.equalsIgnoreCase("yes");
    }

    @DisplayName("Each rule holds across eras")
    @Description("""
        A leap year is a year with an extra day, February 29. The extra day keeps the calendar
        year aligned with Earth's orbit around the Sun.

        A normal year has 365 days. Earth takes about 365.2422 days to orbit the Sun. To account
        for that extra fraction, we add one day every four years. Years divisible by 100 are an
        exception: they are not leap years, unless they are also divisible by 400.
        """)
    @TableTest("""
        Scenario                        | Year               | Is Leap Year?
        Not divisible by 4              | {1, 2001, 30001}   | No
        Divisible by 4                  | {4, 2004, 30008}   | Yes
        Divisible by 100 but not by 400 | {100, 2100, 30300} | No
        Divisible by 400                | {400, 2000, 30000} | Yes
        """)
    void testLeapYears(Year year, boolean isLeapYear) {
        assertEquals(isLeapYear, year.isLeap());
    }

}
