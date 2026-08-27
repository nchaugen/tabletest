package org.tabletest.junit.converting.builtin;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DisplayName("Platform value types")
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
        """)
public class JavaPlatformValueConversionTest {

    @DisplayName("Converts a fully qualified, nested, or primitive type name to Class")
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

    @DisplayName("Converts a canonical name or an alias to Charset")
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

    @DisplayName("Converts an ISO 4217 code to Currency")
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

    @DisplayName("Converts an IETF BCP 47 language tag to Locale")
    @Description("""
            A language tag separates language from country with a hyphen. The underscore of Java's
            older locale spelling is the mistake to know about, because it does not fail: the tag
            simply matches nothing and both parts come back empty.
            """)
    @TableTest("""
        Scenario               | Input value | Parameter type?  | Locale language? | Locale country?
        Language only          | en          | java.util.Locale | en               | ''
        Language and country   | en-US       | java.util.Locale | en               | US
        Non-English tag        | nb-NO       | java.util.Locale | nb               | NO
        Underscore, not hyphen | en_US       | java.util.Locale | ''               | ''
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

    @DisplayName("Converts standard UUID text to UUID")
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
}
