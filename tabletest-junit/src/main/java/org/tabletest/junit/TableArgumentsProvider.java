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

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.AnnotationBasedArgumentsProvider;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static org.tabletest.junit.TableTestArgumentsProvider.provideArgumentsForInput;

/**
 * Provides arguments for parameterized tests from tabular data defined in {@link TableTest} annotations.
 * <p>
 * Parses table format strings, validates column/parameter count matching, and converts
 * each row into appropriately typed method arguments. Supports both inline tables and
 * external resources.
 */
public class TableArgumentsProvider extends AnnotationBasedArgumentsProvider<TableTest> {

    /**
     * Provides a stream of arguments for parameterized tests from tabular data.
     * <p>
     * The method parses the table string provided in the annotation, validates
     * that the number of columns matches the number of method parameters, and
     * converts each row into one or more method arguments.
     * <p>
     * A row becomes multiple instances of method arguments when cells contain sets of
     * values and the corresponding test method parameter is not of type Set. In this
     * situation, method arguments are generated for all set values. If multiple columns
     * contain sets, method arguments are generated for all combinations.
     * <p>
     * NOTE! Be careful with excessive use of expanding sets in the same table, as the
     * number of value combinations can quickly explode and cause long run times.
     *
     * @param context   The current extension context
     * @param tableTest The TableTest annotation containing the table data
     * @return A stream of Arguments objects, one for each data row in the table
     * @throws TableTestException if unable to provide an argument
     */
    @Override
    protected Stream<? extends Arguments> provideArguments(ExtensionContext context, TableTest tableTest) {
        String input = tableTest.resource().isBlank()
            ? tableTest.value()
            : InputResolver.loadResource(tableTest.resource(), tableTest.encoding(), context.getRequiredTestClass());
        return provideArgumentsForInput(context, input);
    }

}
