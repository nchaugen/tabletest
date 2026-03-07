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

import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.tabletest.junit.ParameterTypeAnalyzer.typeStackOf;

/**
 * Provides information about the parameter type, including generic type arguments.
 */
public class ParameterType {
    private final List<? extends Class<?>> typeStack;

    public ParameterType(List<? extends Class<?>> typeStack) {
        this.typeStack = typeStack;
    }

    public List<? extends Class<?>> typeStack() {
        return typeStack;
    }

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
            || typeStack.get(0).isAssignableFrom(valueClass)
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
        return typeStack.isEmpty() ? "<MISSING>" : typeStack.get(0).getTypeName();
    }

    /**
     * @return The Class of this parameter type, or null if none exists
     */
    public Class<?> toClass() {
        return typeStack.isEmpty() ? null : typeStack.get(0);
    }

    public boolean isPrimitive() {
        return typeStack.size() == 1 && typeStack.get(0).isPrimitive();
    }

    public boolean isSet() {
        return !typeStack.isEmpty() && Set.class.isAssignableFrom(typeStack.get(0));
    }

    public boolean isAssignableFrom(Class<?> type) {
        if (typeStack.isEmpty() || type == null) return false;

        Class<?> targetType = typeStack.get(0);
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

    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER;

    static {
        Map<Class<?>, Class<?>> map = new HashMap<>();
        map.put(boolean.class, Boolean.class);
        map.put(byte.class, Byte.class);
        map.put(char.class, Character.class);
        map.put(short.class, Short.class);
        map.put(int.class, Integer.class);
        map.put(long.class, Long.class);
        map.put(float.class, Float.class);
        map.put(double.class, Double.class);
        map.put(void.class, Void.class);
        PRIMITIVE_TO_WRAPPER = Collections.unmodifiableMap(map);
    }

    public boolean isEmpty() {
        return typeStack.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ParameterType)) return false;
        ParameterType other = (ParameterType) obj;
        return typeStack.equals(other.typeStack);
    }

    @Override
    public int hashCode() {
        return typeStack.hashCode();
    }

    @Override
    public String toString() {
        return "ParameterType[typeStack=" + typeStack + "]";
    }
}
