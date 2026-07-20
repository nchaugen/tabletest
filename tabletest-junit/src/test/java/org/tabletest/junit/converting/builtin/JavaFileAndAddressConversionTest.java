package org.tabletest.junit.converting.builtin;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@DisplayName("Files and addresses")
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
public class JavaFileAndAddressConversionTest {

    @DisplayName("File converts from path text")
    @TableTest("""
        Scenario      | Input value     | Parameter type? | File name? | File is absolute?
        Absolute path | /path/to/file   | java.io.File    | file       | true
        Relative path | ./relative/path | java.io.File    | path       | false
        """)
    void converts_files(File value, Class<?> parameterType, String expectedName, boolean expectedAbsolute) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedName, value.getName());
        assertEquals(expectedAbsolute, value.isAbsolute());
    }

    @DisplayName("Path converts from path text")
    @TableTest("""
        Scenario      | Input value     | Parameter type?    | Path file name? | Path is absolute?
        Absolute path | /path/to/file   | java.nio.file.Path | file            | true
        Relative path | ./relative/path | java.nio.file.Path | path            | false
        """)
    void converts_paths(Path value, Class<?> parameterType, String expectedFileName, boolean expectedAbsolute) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedFileName, value.getFileName().toString());
        assertEquals(expectedAbsolute, value.isAbsolute());
    }

    @DisplayName("URI converts from address text, including non-URL schemes")
    @TableTest("""
        Scenario   | Input value         | Parameter type? | URI scheme?
        Web        | https://junit.org/  | java.net.URI    | https
        Local file | file:///tmp/test    | java.net.URI    | file
        URN        | urn:isbn:0451450523 | java.net.URI    | urn
        """)
    void converts_uris(URI value, Class<?> parameterType, String expectedScheme) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedScheme, value.getScheme());
    }

    @DisplayName("URL converts from address text")
    @Description("A URL needs a protocol handler, so unlike URI it rejects schemes such as urn:.")
    @TableTest("""
        Scenario   | Input value        | Parameter type? | URL protocol?
        Web        | https://junit.org/ | java.net.URL    | https
        Local file | file:///tmp/test   | java.net.URL    | file
        """)
    void converts_urls(URL value, Class<?> parameterType, String expectedProtocol) {
        assertInstanceOf(parameterType, value);
        assertEquals(expectedProtocol, value.getProtocol());
    }
}
