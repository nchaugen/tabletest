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

import org.junit.jupiter.params.converter.ConvertWith;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Detects whether a parameter carries an explicit JUnit converter, either as a direct
 * {@link ConvertWith} annotation or composed into a meta-annotation at any depth.
 */
public class ExplicitConverterDetector {
    private ExplicitConverterDetector() {
    }

    public static boolean hasExplicitConverter(Parameter parameter) {
        return hasExplicitConverter(parameter.getAnnotations(), new HashSet<>());
    }

    /**
     * Searches annotations and their meta-annotations for {@link ConvertWith}.
     * Each annotation type is visited at most once, so cyclic meta-annotations
     * cannot cause infinite recursion.
     */
    private static boolean hasExplicitConverter(
        Annotation[] annotations,
        Set<Class<? extends Annotation>> visited
    ) {
        return Arrays.stream(annotations)
            .filter(ExplicitConverterDetector::isNotLanguageMetaAnnotation)
            .filter(it -> visited.add(it.annotationType()))
            .anyMatch(it -> isOrHasExplicitConverter(it, visited));
    }

    private static boolean isOrHasExplicitConverter(
        Annotation it,
        Set<Class<? extends Annotation>> visited
    ) {
        return ConvertWith.class.equals(it.annotationType())
            || hasExplicitConverter(it.annotationType().getAnnotations(), visited);
    }

    /**
     * Language-provided meta-annotations (Retention, Target, etc.) cannot compose a
     * converter and are skipped. Matches on the type name rather than
     * {@code Class.getPackage()}, which can return null.
     */
    private static boolean isNotLanguageMetaAnnotation(Annotation it) {
        String typeName = it.annotationType().getName();
        return !typeName.startsWith("java.lang.annotation.")
            && !typeName.startsWith("kotlin.annotation.");
    }
}
