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
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.nchaugen.tabletest.junit.ParameterTypeAnalyzer.typeStackOf;

/**
 * Provides information about the parameter type, including generic type arguments.
 */
public record ParameterType(List<? extends Class<?>> typeStack) {

    /**
     * Creates a ParameterType instance for a method parameter
     */
    public static ParameterType of(Parameter parameter) {
        return new ParameterType(typeStackOf(parameter));
    }

    /**
     * Checks if the given value is matching the specified target type.
     * <p>
     * It is considered a match if:
     * <ol>
     *     <li>No target type information is available</li>
     *     <li>The target type is assignable from the value type</li>
     *     <li>The value type is a Set, in which case it is a value set that will
     *     expand to multiple test invocations</li>
     * </ol>
     *
     * @param valueClass the type of the value
     * @return true if the value class is matching the target type; false otherwise.
     */
    public boolean isMatching(Class<?> valueClass) {
        return typeStack.isEmpty()
            || typeStack.getFirst().isAssignableFrom(valueClass)
            // if the value is a set and the parameter type not, this is a value set that will expand to multiple rows
            || Set.class.isAssignableFrom(valueClass);
    }

    /**
     * @return the ParameterType for the elements of a generic type.
     * If the type is not generic, this method returns the same ParameterType.
     */
    public ParameterType elementType() {
        return typeStack.isEmpty()
            ? this
            : new ParameterType(typeStack.subList(1, typeStack.size()));
    }

    public String name() {
        return typeStack.isEmpty() ? "<MISSING>" : typeStack.getFirst().getTypeName();
    }

    /**
     * @return The Class of this parameter type, or null if none exists
     */
    public Class<?> toClass() {
        return typeStack.isEmpty() ? null : typeStack.getFirst();
    }

    public boolean isPrimitive() {
        return typeStack.size() == 1 && typeStack.getFirst().isPrimitive();
    }

    public boolean isSet() {
        return !typeStack.isEmpty() && Set.class.isAssignableFrom(typeStack.getFirst());
    }

    public boolean isAssignableFrom(Class<?> type) {
        if (typeStack.isEmpty() || type == null) return false;

        Class<?> targetType = typeStack.getFirst();
        return targetType.isAssignableFrom(type)
            || isAssignableFromBoxed(targetType, type)
            || isAssignableFromUnboxed(targetType, type);
    }

    private static boolean isAssignableFromUnboxed(Class<?> targetType, Class<?> returnType) {
        return returnType.equals(PRIMITIVE_TO_WRAPPER.get(targetType));
    }

    private static boolean isAssignableFromBoxed(Class<?> targetType, Class<?> returnType) {
        return targetType.equals(PRIMITIVE_TO_WRAPPER.get(returnType));
    }

    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = Map.of(
        boolean.class, Boolean.class,
        byte.class, Byte.class,
        char.class, Character.class,
        short.class, Short.class,
        int.class, Integer.class,
        long.class, Long.class,
        float.class, Float.class,
        double.class, Double.class,
        void.class, Void.class
    );

    public boolean isEmpty() {
        return typeStack.isEmpty();
    }
}
