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
package org.tabletest.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds the scenario column to a test method parameter.
 * <p>
 * In most cases this annotation is not needed—the scenario column automatically
 * provides the test display name without a corresponding parameter. Use this
 * annotation when:
 * <ul>
 *   <li>You need to reference the scenario name in your test code</li>
 *   <li>You use JUnit-provided parameters ({@code @TempDir}, {@code TestInfo}, etc.)
 *       after table parameters—binding the scenario column ensures correct parameter alignment</li>
 * </ul>
 * <p>
 * Example with provided parameter:
 * <pre>
 * &#64;TableTest("""
 *     Scenario    | files     | expected?
 *     Single file | [a.txt]   | [a]
 *     """)
 * void test(@Scenario String scenario, List&lt;String&gt; files, List&lt;String&gt; expected,
 *           &#64;TempDir Path tempDir) { }
 * </pre>
 *
 * @see TableTest
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Scenario {}
