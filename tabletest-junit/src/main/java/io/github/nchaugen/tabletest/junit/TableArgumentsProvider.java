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

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.AnnotationBasedArgumentsProvider;
import org.junit.jupiter.params.provider.Arguments;
import org.tabletest.junit.InputResolver;

import java.util.stream.Stream;

import static org.tabletest.junit.TableTestArgumentsProvider.provideArgumentsForInput;

/**
 * Please use {@link org.tabletest.junit.TableArgumentsProvider} instead
 */
@Deprecated(since = "1.0.0")
public class TableArgumentsProvider extends AnnotationBasedArgumentsProvider<TableTest> {

    @Override
    protected Stream<? extends Arguments> provideArguments(ExtensionContext context, TableTest tableTest) {
        String input = tableTest.resource().isBlank()
            ? tableTest.value()
            : InputResolver.loadResource(tableTest.resource(), tableTest.encoding(), context.getRequiredTestClass());
        return provideArgumentsForInput(context, input);
    }

}
