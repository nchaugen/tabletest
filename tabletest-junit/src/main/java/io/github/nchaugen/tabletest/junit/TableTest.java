/*
 * Copyright 2025-present Nils Christian Haugen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.nchaugen.tabletest.junit;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The {@code @TableTest} annotation enables data-driven testing with tabular data in JUnit.
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
 *   <li><b>Sets</b> - enclosed in {} with comma-separated elements</li>
 *   <li><b>Comments</b> - lines starting with // are ignored</li>
 *   <li><b>Empty lines</b> - lines with only whitespace are ignored</li>
 * </ul>
 * <p>
 * Values are automatically converted to method parameter types, including nested parameterized types.
 * The number of columns must match the number of parameters in the test method with one exception:
 * The first column can be used for display name of each row. In that case it should not have a method parameter.
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
    /**
     * The table data in string format.
     * <p>
     * Text-block syntax (""") is recommended for multi-line tables to maintain
     * readability.
     */
    String value() default "";

    /**
     * The path of the resource containing the table data.
     */
    String resource() default "";

    /**
     * The encoding to use when reading the table data file; must be a valid charset.
     * <p>
     * Defaults to {@code "UTF-8"}.
     *
     * @see java.nio.charset.StandardCharsets
     */
    String encoding() default "UTF-8";

}
