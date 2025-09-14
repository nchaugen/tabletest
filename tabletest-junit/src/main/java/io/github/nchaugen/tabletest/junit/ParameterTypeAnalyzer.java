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

import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Extracts the type stack of a method parameter.
 */
public class ParameterTypeAnalyzer {

    private ParameterTypeAnalyzer() {
    }

    /**
     * Extracts the type stack of a method parameter.
     * <p>
     * For non-generic parameters it returns a list of one element: the parameter's class.
     * For generic parameters it returns the list of nested class objects with the outermost first.
     * For generic Map types, only the value type is included in the list (key types are ignored).
     * <p>
     * Example:
     * A parameter of type {@code List<Map<String, Integer>>}, would return [List.class, Map.class, Integer.class].
     *
     * @param parameter The parameter whose types should be extracted
     * @return A list of Class objects representing the types in the parameter
     */
    public static List<? extends Class<?>> typeStackOf(Parameter parameter) {
        return collectTypes(parameter.getParameterizedType()).toList();
    }

    /**
     * Recursively collects all Class types from a Type, excluding Map key types.
     */
    private static Stream<Class<?>> collectTypes(Type type) {
        return switch (type) {
            case Class<?> clazz -> Stream.of(clazz);

            case ParameterizedType paramType when paramType.getRawType() instanceof Class<?> rawClass -> Stream.concat(
                Stream.<Class<?>>of(rawClass),
                Map.class.isAssignableFrom(rawClass)
                    ? collectMapValueTypes(paramType)
                    : collectAllTypeArguments(paramType)
            );

            case WildcardType wildcardType -> Stream.concat(
                collectBounds(wildcardType.getUpperBounds()),
                collectBounds(wildcardType.getLowerBounds())
            );

            case TypeVariable<?> typeVar -> collectBounds(typeVar.getBounds());

            default -> Stream.empty();
        };
    }

    /**
     * Collects types from Map value type only (skips key type).
     */
    private static Stream<Class<?>> collectMapValueTypes(ParameterizedType mapType) {
        Type[] typeArgs = mapType.getActualTypeArguments();
        return typeArgs.length >= 2
            ? collectTypes(typeArgs[1])  // Skip key (index 0), process value (index 1)
            : Stream.empty();
    }

    /**
     * Collects types from all type arguments.
     */
    private static Stream<Class<?>> collectAllTypeArguments(ParameterizedType paramType) {
        return Arrays.stream(paramType.getActualTypeArguments())
            .flatMap(ParameterTypeAnalyzer::collectTypes);
    }

    /**
     * Collects types from bounds, filtering out Object.class.
     */
    private static Stream<Class<?>> collectBounds(Type[] bounds) {
        return Arrays.stream(bounds)
            .filter(bound -> !bound.equals(Object.class))
            .flatMap(ParameterTypeAnalyzer::collectTypes);
    }

}
