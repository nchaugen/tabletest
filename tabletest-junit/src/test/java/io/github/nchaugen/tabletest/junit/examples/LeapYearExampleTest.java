package io.github.nchaugen.tabletest.junit.examples;

import io.github.nchaugen.tabletest.junit.TableTest;
import org.junit.jupiter.api.Test;

import java.time.Year;

import static org.junit.jupiter.api.Assertions.*;

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

    @SuppressWarnings("unused")
    public static boolean parseYesNo(String input) {
        return input.equalsIgnoreCase("yes");
    }

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
