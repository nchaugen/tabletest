package io.github.nchaugen.tabletest.junit.converting;

import io.github.nchaugen.tabletest.junit.TableTest;
import io.github.nchaugen.tabletest.junit.javadomain.Age;
import org.junit.jupiter.api.Test;

import static io.github.nchaugen.tabletest.junit.TableTestExceptionAssertions.*;

public class JavaConversionFailureTest {

    @TableTest("""
        table value | parameter type
        0.1         | java.lang.Byte
        256         | java.lang.Byte
        abc         | java.lang.Character
        invalid     | java.time.LocalDate
        52          | io.github.nchaugen.tabletest.junit.javadomain.Ages
        """)
    void fails_builtin_conversion(String value, Class<?> type) {
        assertThrowsWhenFallbackFails(value, type);
    }

    @TableTest("""
        table value | parameter type
        52          | io.github.nchaugen.tabletest.junit.javadomain.Age
        """)
    void fails_when_multiple_factory_methods_found(String value, Class<?> type) {
        assertThrowsWhenMultipleFactoryMethodsFound(value, type);
    }

    @SuppressWarnings("unused")
    public static Age parseAge(String age) {
        throw new IllegalStateException("should not be called");
    }

    @SuppressWarnings("unused")
    public static Age anotherParseAge(Integer age) {
        throw new IllegalStateException("should not be called");
    }

    @Test
    void failing_primitive_conversion() {
        assertThrowsWhenNullSpecifiedForPrimitiveType(null, boolean.class);
    }

}
