package org.tabletest.junit.converting;

import org.junit.jupiter.api.Test;
import org.tabletest.junit.TypeConverter;
import org.tabletest.junit.javadomain.Age;
import org.tabletest.junit.javadomain.Ages;

import java.lang.reflect.Parameter;

import static org.tabletest.junit.TableTestExceptionAssertions.assertThrowsWhenTypeConverterCycleDetected;

/**
 * Tests that converter resolution fails fast with a clear error when a type
 * converter's parameter, directly or via other converters, requires converting
 * to the very type the converter produces.
 */
public class JavaTypeConverterCycleTest {

    @Test
    void fails_when_type_converter_takes_the_type_it_returns() {
        assertThrowsWhenTypeConverterCycleDetected("52", ageParameterOf(SelfReferentialConverter.class));
    }

    @Test
    void fails_when_type_converters_form_a_cycle() {
        assertThrowsWhenTypeConverterCycleDetected("52", ageParameterOf(MutuallyReferentialConverters.class));
    }

    public static class SelfReferentialConverter {

        @SuppressWarnings("unused")
        private void params(Age age) {
        }

        @TypeConverter
        public static Age olderAge(Age age) {
            throw new IllegalStateException("should not be called");
        }
    }

    public static class MutuallyReferentialConverters {

        @SuppressWarnings("unused")
        private void params(Age age) {
        }

        @TypeConverter
        public static Age fromAges(Ages ages) {
            throw new IllegalStateException("should not be called");
        }

        @TypeConverter
        public static Ages fromAge(Age age) {
            throw new IllegalStateException("should not be called");
        }
    }

    private static Parameter ageParameterOf(Class<?> converterClass) {
        try {
            return converterClass.getDeclaredMethod("params", Age.class).getParameters()[0];
        } catch (NoSuchMethodException cause) {
            throw new IllegalStateException(cause);
        }
    }
}
