package org.tabletest.junit.converting.builtin.datetime;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DisplayName("Instants, offsets, and zones")
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

        Each type here fixes a point on the world timeline. Some of them instead name the offset
        or the zone that fixes it. A date or a time that carries neither is in the neighbouring
        feature.
        """)
public class JavaInstantAndZoneConversionTest {

    @DisplayName("Converts a UTC timestamp written with a trailing Z to Instant")
    @TableTest("""
        Scenario     | Input value          | Parameter type?    | Instant epoch second?
        Unix epoch   | 1970-01-01T00:00:00Z | java.time.Instant  | 0
        Pi Day 2017  | 2017-03-14T12:00:00Z | java.time.Instant  | 1489492800
        """)
    void converts_instants(Instant value, Class<?> parameterType, long expectedEpochSecond) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedEpochSecond, value.getEpochSecond());
    }

    @DisplayName("Converts a date-time carrying an offset from UTC to OffsetDateTime")
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

    @DisplayName("Converts a date-time with a region id to ZonedDateTime")
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

    @DisplayName("Converts a time of day with an offset from UTC to OffsetTime")
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

    @DisplayName("Converts an offset from UTC to ZoneOffset, Z meaning none")
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

    @DisplayName("Converts a region name, UTC, or a fixed offset to ZoneId")
    @Description("A region zone carries daylight-saving rules. UTC and a plain offset carry none.")
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
