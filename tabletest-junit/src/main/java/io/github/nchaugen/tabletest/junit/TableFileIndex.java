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

import io.github.nchaugen.tabletest.renderer.TableFileEntry;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class TableFileIndex {

    public static final String TABLE_FILE_INDEX = "TableFileIndex";

    static void save(String title, Path path, ExtensionContext context) {
        getStore(context).put(
            TABLE_FILE_INDEX, Stream.concat(
                allForTestClass(context).stream(),
                Stream.of(new TableFileEntry(title, path))
            ).toList()
        );
    }

    static List<TableFileEntry> allForTestClass(ExtensionContext context) {
        return getStore(context).getOrDefault(TABLE_FILE_INDEX, List.class, Collections.emptyList());
    }

    private static ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getRoot().getStore(namespace(context));
    }

    private static ExtensionContext.Namespace namespace(ExtensionContext context) {
        return ExtensionContext.Namespace.create(context.getRequiredTestClass());
    }
}