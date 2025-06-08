package io.github.nchaugen.tabletest.junit.converting

import io.github.nchaugen.tabletest.junit.TableTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.MonthDay
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.Period
import java.time.Year
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class KotlinSingleValueBuiltInConversionTest {

    @TableTest(
        """    
        Format  | Byte | Short | Integer | Long | Expected
        Decimal | 15   | 15    | 15      | 15   | 15
        Hex     | 0xF  | 0xF   | 0xF     | 0xF  | 15
        Octal   | 017  | 017   | 017     | 017  | 15
        """
    )
    fun converts_numeric_formats_to_expected_value(
        byteValue: Byte,
        shortValue: Short,
        intValue: Int,
        longValue: Long,
        expected: String
    ) {
        assertEquals(expected, byteValue.toString())
        assertEquals(expected, shortValue.toString())
        assertEquals(expected, intValue.toString())
        assertEquals(expected, longValue.toString())
    }

    @TableTest(
        """    
        Float Value | Double Value | BigDecimal Value | BigInteger Value
        3.14159     | 2.718281828  | 123.456e789      | 1234567890123456789
        0.1         | 0.1          | 1                | 15
        1.23e4      | 1.23e4       | 1.23e4           | 123
        """
    )
    fun converts_decimal_types(
        floatVal: Float,
        doubleVal: Double,
        bigDecVal: BigDecimal,
        bigIntVal: BigInteger
    ) {
        assertNotNull(floatVal)
        assertNotNull(doubleVal)
        assertNotNull(bigDecVal)
        assertNotNull(bigIntVal)
    }

    @TableTest(
        """    
        byte | widenedByte? | float | widenedFloat? | double | widenedDouble?
        123  | 123          | 123   | 123.0         | 123    | 123.0
        """
    )
    fun allows_widening_primitive_conversion(
        byteVal: Byte,
        widenedByte: Byte,
        floatVal: Float,
        widenedFloat: Float,
        doubleVal: Double,
        widenedDouble: Double
    ) {
        assertEquals(byteVal, widenedByte)
        assertEquals(floatVal, widenedFloat)
        assertEquals(doubleVal, widenedDouble)
    }
    @TableTest(
        """    
        Character | String   | TimeUnit | Boolean
        a         | abc      | SECONDS  | true
        Z         | "quoted" | MINUTES  | false
        1         | 'single' | HOURS    | true
        """
    )
    fun converts_basic_types(
        charVal: Char,
        stringVal: String,
        timeUnit: TimeUnit,
        boolVal: Boolean
    ) {
        assertNotNull(charVal)
        assertNotNull(stringVal)
        assertNotNull(timeUnit)
        assertNotNull(boolVal)
    }

    @TableTest(
        """    
        File            | Path            | URI                 | URL                 | Class Name
        /path/to/file   | /path/to/file   | https://junit.org/  | https://junit.org/  | java.lang.Integer
        C:\Users\test   | C:\Users\test   | file:///tmp/test    | https://example.com | java.lang.String
        ./relative/path | ./relative/path | urn:isbn:1234567890 | file:///tmp/test    | byte
        """
    )
    fun converts_resource_types(
        fileVal: java.io.File,
        pathVal: java.nio.file.Path,
        uriVal: java.net.URI,
        urlVal: java.net.URL,
        classVal: Class<*>
    ) {
        assertNotNull(fileVal)
        assertNotNull(pathVal)
        assertNotNull(uriVal)
        assertNotNull(urlVal)
        assertNotNull(classVal)
    }

    @TableTest(
        """    
        Charset    | Currency | Locale | UUID
        UTF-8      | NOK      | en     | d043e930-7b3b-48e3-bdbe-5a3ccfb833db
        UTF-16     | USD      | en_US  | 550e8400-e29b-41d4-a716-446655440000
        ISO-8859-1 | EUR      | no_NO  | 6ba7b810-9dad-11d1-80b4-00c04fd430c8
        """
    )
    fun converts_locale_types(
        charsetVal: java.nio.charset.Charset,
        currencyVal: java.util.Currency,
        localeVal: java.util.Locale,
        uuidVal: java.util.UUID
    ) {
        assertNotNull(charsetVal)
        assertNotNull(currencyVal)
        assertNotNull(localeVal)
        assertNotNull(uuidVal)
    }

    @TableTest(
        """    
        Duration | Period  | Year | YearMonth | MonthDay
        PT3S     | P2M6D   | 2017 | 2017-03   | --03-14
        PT1H30M  | P1Y2M3D | 2025 | 2025-12   | --12-25
        PT0.123S | P0D     | 1999 | 1999-01   | --01-01
        """
    )
    fun converts_temporal_periods(
        durationVal: Duration,
        periodVal: Period,
        yearVal: Year,
        yearMonthVal: YearMonth,
        monthDayVal: MonthDay
    ) {
        assertNotNull(durationVal)
        assertNotNull(periodVal)
        assertNotNull(yearVal)
        assertNotNull(yearMonthVal)
        assertNotNull(monthDayVal)
    }

    @TableTest(
        """    
        LocalDate  | LocalTime    | LocalDateTime           | ZoneId
        2017-03-14 | 12:34:56.789 | 2017-03-14T12:34:56.789 | Europe/Berlin
        2025-12-25 | 00:00:00     | 2025-12-25T23:59:59     | America/New_York
        1999-01-01 | 23:59:59.999 | 1999-01-01T00:00:01     | Asia/Tokyo
        """
    )
    fun converts_local_temporal_types(
        localDateVal: LocalDate,
        localTimeVal: LocalTime,
        localDateTimeVal: LocalDateTime,
        zoneIdVal: ZoneId
    ) {
        assertNotNull(localDateVal)
        assertNotNull(localTimeVal)
        assertNotNull(localDateTimeVal)
        assertNotNull(zoneIdVal)
    }

    @TableTest(
        """    
        Instant                  | OffsetDateTime            | OffsetTime     | ZonedDateTime             | ZoneOffset
        1977-08-17T18:19:20Z     | 2017-03-14T12:34:56.789Z  | 12:34:56.789Z  | 2017-03-14T12:34:56.789Z  | +02:30
        2025-01-01T00:00:00Z     | 2025-12-25T00:00:00+01:00 | 23:59:59+05:30 | 2025-12-25T12:00:00+09:00 | -05:00
        2000-01-01T12:00:00.123Z | 2000-06-15T18:30:45-07:00 | 06:15:30-03:00 | 2000-06-15T18:30:45+02:00 | +00:00
        """
    )
    fun converts_zoned_temporal_types(
        instantVal: Instant,
        offsetDateTimeVal: OffsetDateTime,
        offsetTimeVal: OffsetTime,
        zonedDateTimeVal: ZonedDateTime,
        zoneOffsetVal: ZoneOffset
    ) {
        assertNotNull(instantVal)
        assertNotNull(offsetDateTimeVal)
        assertNotNull(offsetTimeVal)
        assertNotNull(zonedDateTimeVal)
        assertNotNull(zoneOffsetVal)
    }

    @TableTest(
        """    
        Scenario     | String | Float | List
        Empty string | ""     | ""    | ""
        Blank cell   |        |       | 
        """
    )
    fun converts_blank_to_null_for_non_string_parameters(stringVal: String, floatVal: Float?, listVal: List<*>?) {
        assertNotNull(stringVal)
        assertNull(floatVal)
        assertNull(listVal)
    }

}
