package org.tabletest.junit.converting.builtin.datetime;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

import java.time.Duration;
import java.time.Period;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DisplayName("Amounts of time")
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

        Each type here measures a length of time. It does not name a date or a time. A Duration
        counts seconds. A Period counts calendar years, months, and days.
        """)
public class JavaTimeAmountConversionTest {

    @DisplayName("Converts ISO-8601 duration text to Duration")
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

    @DisplayName("Converts ISO-8601 period text to Period")
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
}
