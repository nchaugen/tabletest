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
import java.util.function.Predicate;

public class ExplicitConverterDetector {
    private ExplicitConverterDetector() {
    }

    public static boolean hasExplicitConverter(Parameter parameter) {
        return hasExplicitConverter(parameter.getAnnotations());
    }

    private static boolean hasExplicitConverter(Annotation[] annotations) {
        return Arrays.stream(annotations)
            .filter(ExplicitConverterDetector::isNotLanguageMetaAnnotation)
            .anyMatch(ExplicitConverterDetector::isOrHasExplicitConverter);
    }

    private static boolean isNotLanguageMetaAnnotation(Annotation it) {
        return !it.annotationType().getPackageName().startsWith("java.lang.annotation")
            && !it.annotationType().getPackageName().startsWith("kotlin.annotation");
    }

    private static boolean isOrHasExplicitConverter(Annotation it) {
        return isExplicitConverter().or(hasExplicitConverter()).test(it);
    }

    private static Predicate<Annotation> isExplicitConverter() {
        return it -> ConvertWith.class.equals(it.annotationType());
    }

    private static Predicate<Annotation> hasExplicitConverter() {
        return it -> hasExplicitConverter(it.annotationType().getAnnotations());
    }
}
