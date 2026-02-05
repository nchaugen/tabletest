package org.tabletest.junit.examples;

import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.tabletest.junit.TypeConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Leap Year Rules")
@Description("The following describes the rules for leap years.")
public class LeapYearExampleTest {

    @Test
    void yearNotDivisibleBy4_isNotLeap() {
        assertFalse(Year.isLeap(2001));
    }

    @Test
    void yearDivisibleBy4_isLeap() {
        assertTrue(Year.isLeap(2004));
    }

    @Test
    void yearDivisibleBy100Not400_isNotLeap() {
        assertFalse(Year.isLeap(2100));
    }

    @Test
    void yearDivisibleBy400_isLeap() {
        assertTrue(Year.isLeap(2000));
    }

    @TableTest("""
        Scenario                        | Year | Is Leap Year?
        Not divisible by 4              | 2001 | No
        Divisible by 4                  | 2004 | Yes
        Divisible by 100 but not by 400 | 2100 | No
        Divisible by 400                | 2000 | Yes
        """)
    void testLeapYear(int year, boolean isLeapYear) {
        assertEquals(isLeapYear, Year.isLeap(year));
    }

    @TypeConverter
    @SuppressWarnings("unused")
    public static boolean parseYesNo(String input) {
        return input.equalsIgnoreCase("yes");
    }

    @DisplayName("Leap Year Calculation")
    @Description("""
        A leap year is a year with an extra day added to the calendar — February 29 — to keep the calendar
        year aligned with Earth’s orbit around the Sun.
        
        * A normal year has 365 days, but Earth takes about 365.2422 days to orbit the Sun.
        * To account for this extra fraction, we add one day every 4 years.
        * Exception: Years divisible by 100 are not leap years unless they are also divisible by 400.
        """)
    @TableTest("""
        Scenario                        | Year               | Is Leap Year?
        Not divisible by 4              | {1, 2001, 30001}   | No
        Divisible by 4                  | {4, 2004, 30008}   | Yes
        Divisible by 100 but not by 400 | {100, 2100, 30300} | No
        Divisible by 400                | {400, 2000, 30000} | Yes
        Year 0                          | 0                  | Yes
        Negative input                  | -1                 | No
        """)
    void testLeapYears(Year year, boolean isLeapYear) {
        assertEquals(isLeapYear, year.isLeap());
    }

}
