package org.tabletest.junit.converting;

import org.tabletest.junit.TableTest;
import org.tabletest.junit.TypeConverter;
import org.tabletest.junit.javadomain.Age;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests that converter selection prefers the {@link TypeConverter}-annotated method
 * when non-annotated candidates with the same return type are also present.
 */
public class JavaTypeConverterSelectionTest {

    @TableTest("""
        Age | Expected age?
        16  | 16
        """)
    void annotated_converter_wins_over_non_annotated_candidates(Age age, int expectedAge) {
        assertEquals(new Age(expectedAge), age);
    }

    @TypeConverter
    @SuppressWarnings("unused")
    public static Age parseAge(int age) {
        return new Age(age);
    }

    @SuppressWarnings("unused")
    public static Age defaultAge(String ignored) {
        throw new IllegalStateException("non-annotated candidate should not be selected");
    }
}
