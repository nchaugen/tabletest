package org.tabletest.junit.converting;

import org.tabletest.junit.TableTest;
import org.tabletest.junit.TypeConverter;
import org.tabletest.junit.javadomain.Age;
import org.junit.jupiter.api.Test;

import static org.tabletest.junit.TableTestExceptionAssertions.*;

public class JavaConversionFailureTest {

    @TableTest("""
        table value | parameter type
        0.1         | java.lang.Byte
        256         | java.lang.Byte
        abc         | java.lang.Character
        invalid     | java.time.LocalDate
        52          | org.tabletest.junit.javadomain.Ages
        """)
    void fails_builtin_conversion(String value, Class<?> type) {
        assertThrowsWhenFallbackFails(value, type);
    }

    @TableTest("""
        table value | parameter type
        52          | org.tabletest.junit.javadomain.Age
        """)
    void fails_when_multiple_type_converters_found(String value, Class<?> type) {
        assertThrowsWhenMultipleTypeConvertersFound(value, type);
    }

    @TypeConverter
    public static Age parseAge(String age) {
        throw new IllegalStateException("should not be called");
    }

    @TypeConverter
    public static Age anotherParseAge(Integer age) {
        throw new IllegalStateException("should not be called");
    }

    @Test
    void failing_primitive_conversion() {
        assertThrowsWhenNullSpecifiedForPrimitiveType(null, boolean.class);
    }

}
