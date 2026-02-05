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
 * Please use {@link org.tabletest.junit.TableTest} instead
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ParameterizedTest
@ArgumentsSource(io.github.nchaugen.tabletest.junit.TableArgumentsProvider.class)
@Deprecated(since = "1.0.0")
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
