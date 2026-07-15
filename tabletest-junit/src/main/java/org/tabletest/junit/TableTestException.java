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

import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.tabletest.parser.Row;

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

    static String multipleTypeConvertersFound(Class<?> converterClass, ParameterType parameterType) {
        return String.format(
            "Multiple type converters found for type %s in class %s",
            parameterType.name(),
            converterClass.getTypeName()
        );
    }

    static String typeConverterFailed(
        Method converter,
        Object value,
        ParameterType targetType
    ) {
        return String.format(
            "Failed to convert %s \"%s\" to type %s with type converter %s.%s()",
            value.getClass().getTypeName(),
            value,
            targetType.name(),
            converter.getDeclaringClass().getTypeName(),
            converter.getName()
        );
    }

    static String typeConverterCycle(Object value, ParameterType targetType) {
        return String.format(
            "Type converter cycle detected while converting value \"%s\" to type %s: " +
                "resolving the converter's parameter requires converting to %s again. " +
                "A type converter must not take the type it returns, directly or via other converters.",
            value,
            targetType.name(),
            targetType.name()
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
        Stream<Class<?>> typeConverterSearchPath
    ) {
        return String.format(
            "Built-in conversion of value \"%s\" to type %s failed. " +
                "Are you missing a type converter for this conversion? " +
                "Locations searched for type converters: %s",
            parsedValue,
            targetType.name(),
            typeConverterSearchPath.map(Class::getTypeName).collect(Collectors.joining(", "))
        );
    }

    static String rowWidthMismatch(int rowNumber, Row row, int columnCount) {
        return String.format(
            "Data row %d has %d cells but the header row has %d columns. " +
                "All rows must have the same number of cells as the header row. " +
                "Offending row: `%s`",
            rowNumber,
            row.valueCount(),
            columnCount,
            row.values().stream().map(String::valueOf).collect(Collectors.joining(" | "))
        );
    }

    static String notEnoughTestParameters(int parameterCount, int columnCount) {
        return String.format(
            "There are fewer parameters in test method (%d) than columns in table (%d). " +
                "All columns except scenario name must have a corresponding test method parameter.",
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
