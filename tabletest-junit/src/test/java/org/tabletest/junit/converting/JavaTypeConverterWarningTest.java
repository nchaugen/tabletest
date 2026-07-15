package org.tabletest.junit.converting;

import org.junit.jupiter.api.Test;
import org.tabletest.junit.TypeConverter;
import org.tabletest.junit.javadomain.Age;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Parameter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.tabletest.junit.ParameterTypeConverter.convertValue;

/**
 * Tests that the deprecation warning for non-annotated type converters is
 * emitted only for the method actually selected as a converter, and only once
 * per method rather than on every conversion.
 */
public class JavaTypeConverterWarningTest {

    @Test
    void does_not_warn_about_public_static_methods_never_selected_as_converter() {
        String warnings = warningsPrintedBy(() ->
            convertValue("5", parameterOf(UnrelatedHelperMethods.class, int.class))
        );
        assertEquals("", warnings);
    }

    @Test
    void warns_once_when_selected_converter_lacks_annotation() {
        String warnings = warningsPrintedBy(() -> {
            convertValue("52", parameterOf(NonAnnotatedConverter.class, Age.class));
            convertValue("53", parameterOf(NonAnnotatedConverter.class, Age.class));
        });
        assertEquals(
            1,
            countOccurrences(warnings, "NonAnnotatedConverter.parseAge() is used as a type converter"),
            "Unexpected warnings: " + warnings
        );
    }

    @Test
    void does_not_warn_when_selected_converter_is_annotated() {
        String warnings = warningsPrintedBy(() ->
            convertValue("52", parameterOf(AnnotatedConverter.class, Age.class))
        );
        assertEquals("", warnings);
    }

    public static class UnrelatedHelperMethods {

        @SuppressWarnings("unused")
        private void params(int number) {
        }

        @SuppressWarnings("unused")
        public static void main(String[] args) {
        }

        @SuppressWarnings("unused")
        public static String describe(String subject) {
            return subject;
        }
    }

    public static class NonAnnotatedConverter {

        @SuppressWarnings("unused")
        private void params(Age age) {
        }

        @SuppressWarnings("unused")
        public static Age parseAge(int age) {
            return new Age(age);
        }
    }

    public static class AnnotatedConverter {

        @SuppressWarnings("unused")
        private void params(Age age) {
        }

        @TypeConverter
        public static Age parseAge(int age) {
            return new Age(age);
        }
    }

    private static String warningsPrintedBy(Runnable conversion) {
        PrintStream originalErr = System.err;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setErr(new PrintStream(captured));
        try {
            conversion.run();
        } finally {
            System.setErr(originalErr);
        }
        return captured.toString();
    }

    private static int countOccurrences(String text, String needle) {
        Matcher matcher = Pattern.compile(Pattern.quote(needle)).matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private static Parameter parameterOf(Class<?> converterClass, Class<?> parameterType) {
        try {
            return converterClass.getDeclaredMethod("params", parameterType).getParameters()[0];
        } catch (NoSuchMethodException cause) {
            throw new IllegalStateException(cause);
        }
    }
}
