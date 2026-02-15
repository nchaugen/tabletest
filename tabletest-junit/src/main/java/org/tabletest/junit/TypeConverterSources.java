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
 * Specifies classes to search for {@link TypeConverter} methods.
 * <p>
 * Use this annotation to share type converters across multiple test classes
 * without duplication. Classes are searched in the order listed, after
 * converters declared in the test class itself.
 * <p>
 * Example:
 * <pre>
 * &#64;TypeConverterSources({CommonConverters.class, DomainConverters.class})
 * class MyTableTests { }
 * </pre>
 *
 * @see TypeConverter
 * @see TableTest
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TypeConverterSources {
    Class<?>[] value();
}
