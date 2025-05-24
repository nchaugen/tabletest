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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Utility class providing helper methods for parameter conversion.
 * <ul>
 *   <li>Extract type information from complex generic parameter types</li>
 *   <li>Handle primitive-to-wrapper type conversions</li>
 * </ul>
 * <p>
 * These utilities are used by the {@link ParameterTypeConverter} to process
 * parameters from test methods annotated with {@link TableTest}.
 */
public class ParameterUtil {

    private ParameterUtil() {
    }

    /**
     * Extracts the nested class types from a parameterized type.
     * <p>
     * This method analyses a parameter's generic type information and returns a list
     * of all the Class objects in the type hierarchy. For example, for a parameter of type
     * {@code List<Map<String, Integer>>}, it would return [List.class, Map.class, Integer.class].
     * <p>
     * The method handles primitive types by returning their corresponding wrapper classes.
     *
     * @param parameter The parameter whose nested types should be extracted
     * @return A list of Class objects representing the nested types in the parameter
     */
    public static List<? extends Class<?>> nestedElementTypesOf(Parameter parameter) {
        return Arrays.stream(parameter.getParameterizedType().getTypeName().split("<"))
            .map(it -> it.replaceAll(">", ""))
            .map(it -> it.split(","))
            .map(it -> it.length > 1 ? it[1] : it[0])
            .map(String::trim)
            .map(ParameterUtil::findClass)
            .filter(Objects::nonNull)
            .toList();
    }

    /**
     * Resolves a type name string to its corresponding Class object.
     * <p>
     * This method handles both primitive types (converting them to their wrapper types)
     * and regular class names. For primitive types, it returns the corresponding wrapper class.
     * For class names, it attempts to load the class using Class.forName().
     * <p>
     * If the class cannot be found, this method returns null.
     *
     * @param typeName The name of the type to resolve
     * @return The Class object corresponding to the given type name, or null if not found
     */
    private static Class<?> findClass(String typeName) {
        try {
            return switch (typeName) {
                case "boolean" -> Boolean.class;
                case "byte" -> Byte.class;
                case "char" -> Character.class;
                case "short" -> Short.class;
                case "int" -> Integer.class;
                case "long" -> Long.class;
                case "float" -> Float.class;
                case "double" -> Double.class;
                default -> Class.forName(typeName);
            };
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
