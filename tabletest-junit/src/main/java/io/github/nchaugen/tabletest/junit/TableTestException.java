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

import org.junit.jupiter.api.extension.ParameterResolutionException;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TableTestException extends ParameterResolutionException {

    public TableTestException(String message, Throwable cause) {
        super(message, cause);
    }

    public TableTestException(String message) {
        super(message);
    }

    static String multipleFactoryMethodsFound(Class<?> factoryMethodClass, ParameterType parameterType) {
        return String.format(
            "Multiple factory methods found for type %s in class %s",
            parameterType.name(),
            factoryMethodClass.getTypeName()
        );
    }

    static String factoryMethodFailed(
        Method factoryMethod,
        Object value,
        ParameterType targetType
    ) {
        return String.format(
            "Failed to convert %s \"%s\" to type %s with factory method %s.%s()",
            value.getClass().getTypeName(),
            value,
            targetType.name(),
            factoryMethod.getDeclaringClass().getTypeName(),
            factoryMethod.getName()
        );
    }

    static String primitiveTypeDoesNotAllowNull(ParameterType parameterType) {
        return String.format(
            "Blank cell translates to null, but null cannot be assigned to primitive type %s",
            parameterType.name()
        );
    }

    static String fallbackJUnitConversionFailed(
        Object parsedValue,
        ParameterType targetType,
        Stream<Class<?>> factoryMethodSearchPath
    ) {
        return String.format(
            "Built-in conversion of value \"%s\" to type %s failed. " +
                "Are you missing a factory method for this conversion? " +
                "Locations searched for public static factory methods: %s",
            parsedValue,
            targetType.name(),
            factoryMethodSearchPath.map(Class::getTypeName).collect(Collectors.joining())
        );
    }

    static String notEnoughTestParameters(int parameterCount, int columnCount) {
        return String.format(
            "There are fewer parameters in test method (%d) than columns in table (%d). " +
                "All columns except scenario name must hava a corresponding test method parameter.",
            parameterCount,
            columnCount
        );
    }

    static String failedToReadExternalTable(String resource, String encoding) {
        return String.format("Failed to read table from external file %s using encoding %s", resource, encoding);
    }

    static String multipleScenarioAnnotations(Executable declaringExecutable) {
        return String.format(
            "Multiple @Scenario annotations found in test method %s.%s",
            declaringExecutable.getDeclaringClass().getTypeName(),
            declaringExecutable.getName()
        );
    }
}
