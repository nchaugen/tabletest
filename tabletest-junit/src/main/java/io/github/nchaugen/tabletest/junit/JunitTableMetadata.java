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

import io.github.nchaugen.tabletest.parser.Table;
import io.github.nchaugen.tabletest.renderer.ColumnRoles;
import io.github.nchaugen.tabletest.renderer.TableMetadata;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class JunitTableMetadata implements TableMetadata {
    private final ColumnRoles columnRoles;
    private final String title;

    public JunitTableMetadata(ExtensionContext context, Table table) {
        this.columnRoles = new ColumnRoles(
            findScenarioIndex(context, table),
            findExpectationIndices(table)
        );
        this.title = context.getDisplayName();
    }

    @Override
    public ColumnRoles columnRoles() {
        return columnRoles;
    }

    @Override
    public String title() {
        return title;
    }

    @Override
    public String description() {
        return null;
    }

    private static int findScenarioIndex(ExtensionContext context, Table table) {
        return getImplicitScenarioColumn(context, table)
            .orElseGet(() -> getExplicitScenarioColumn(context).orElse(-1));
    }

    private static OptionalInt getExplicitScenarioColumn(ExtensionContext context) {
        return IntStream.range(0, context.getRequiredTestMethod().getParameterCount())
            .filter(i -> context.getRequiredTestMethod().getParameters()[i].isAnnotationPresent(Scenario.class))
            .findFirst();
    }

    private static OptionalInt getImplicitScenarioColumn(ExtensionContext context, Table table) {
        return table.headers().size() > context.getRequiredTestMethod().getParameterCount()
            ? OptionalInt.of(0)
            : OptionalInt.empty();
    }

    private static Set<Integer> findExpectationIndices(Table table) {
        return IntStream.range(0, table.headers().size())
            .filter(i -> table.header(i).endsWith("?"))
            .boxed()
            .collect(Collectors.toSet());
    }

}
