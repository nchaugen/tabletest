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

import org.junit.jupiter.params.ParameterizedTest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container annotation for making the {@link TableTest} annotation repeatable.
 * <p>
 * This annotation acts as the required reflective wrapper array when multiple
 * {@code @TableTest} annotations are stacked onto a single test method. It is
 * meta-annotated with {@link ParameterizedTest} to ensure that native IDE test
 * runners (such as IntelliJ IDEA or VS Code) can successfully discover and execute 
 * the nested repeatable dataset streams sequentially.
 * </p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <pre>{@code
 * @TableTest(resource = "/data/base-cases.table")
 * @TableTest(resource = "/data/edge-cases.table")
 * void myParameterizedTest(int input, String expected) {
 *     // Executes for rows combined from both datasets
 * }
 * }</pre>
 *
 * @see TableTest 
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ParameterizedTest
public @interface TableTests {

    /**
     * Holds the array of individual {@link TableTest} annotation declarations 
     * attached to the target test method.
     *
     * @return an array of repeatable {@code TableTest} annotations
     */
    TableTest[] value();
}
