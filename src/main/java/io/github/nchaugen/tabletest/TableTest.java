package io.github.nchaugen.tabletest;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code @TableTest} annotation enables data-driven testing with tabular data in JUnit 5.
 * <p>
 * Test methods using this annotation must follow standard JUnit rules: they cannot be {@code private}
 * or {@code static} and must return {@code void}.
 * <p>
 * The annotation takes a multi-line string containing a table with pipe-separated columns (|).
 * The first row defines column headers, and the following rows contain test data. Each test data row
 * will run as a separate test case, with values passed to matching method parameters by position.
 * <p>
 * TableTest supports:
 * <ul>
 *   <li><b>Simple values</b> - either unquoted or quoted with single or double quotes</li>
 *   <li><b>Lists</b> - enclosed in [] with comma-separated elements</li>
 *   <li><b>Maps</b> - enclosed in [] with comma-separated key:value pairs</li>
 *   <li><b>Comments</b> - lines starting with // are ignored</li>
 *   <li><b>Empty lines</b> - lines with only whitespace are ignored</li>
 * </ul>
 * <p>
 * Values are automatically converted to method parameter types, including nested parameterized types.
 * <p>
 * Example:
 * <pre>
 * &#64;TableTest("""
 *     Student grades                                                  | Highest grade? | Average grade?
 *     [Alice: [95, 87, 92], Bob: [78, 85, 90], Charlie: [98, 89, 91]] | 98             | 89.44
 *     [:]                                                             | 0              | 0.0
 *     """)
 * void testGradeStatistics(
 *     Map&lt;String, List&lt;Integer&gt;&gt; studentGrades,
 *     int expectedHighest,
 *     double expectedAverage) {
 *     // Test implementation
 * }
 * </pre>
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ParameterizedTest
@ArgumentsSource(TableArgumentsProvider.class)
public @interface TableTest {
    String value() default "";
}
