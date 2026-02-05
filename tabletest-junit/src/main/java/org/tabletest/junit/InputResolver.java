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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.tabletest.junit.TableTestException.failedToReadExternalTable;

public class InputResolver {

    /**
     * Loads table data from an external resource file.
     * <p>
     * Reads the content of the specified resource using the provided encoding
     * and returns it as a string with normalized line breaks.
     *
     * @param resource  Path to the resource containing table data
     * @param encoding  Character encoding to use when reading the file
     * @param testClass Class to use for resource resolution
     * @return Contents of the resource as a string
     * @throws RuntimeException if an IO error occurs during loading
     */
    public static String loadResource(String resource, String encoding, Class<?> testClass) {
        try (InputStream resourceAsStream = resolveResourceStream(resource, testClass)) {
            return new BufferedReader(new InputStreamReader(resourceAsStream, encoding))
                .lines().collect(Collectors.joining("\n"));
        } catch (IOException cause) {
            throw new TableTestException(failedToReadExternalTable(resource, encoding), cause);
        }
    }

    /**
     * Resolves a resource path to an input stream.
     * <p>
     * Attempts to load the resource both with and without a leading slash
     * to accommodate different resource path styles.
     *
     * @param resource  Path to the resource file
     * @param testClass Class to use for resource resolution
     * @return Input stream for the resource
     * @throws NullPointerException if the resource cannot be found
     */
    private static InputStream resolveResourceStream(String resource, Class<?> testClass) {
        InputStream resourceAsStream = testClass.getResourceAsStream(resource);
        if (resourceAsStream == null) {
            resourceAsStream = testClass.getResourceAsStream("/" + resource);
        }

        return Objects.requireNonNull(resourceAsStream, "Could not load resource " + resource);
    }
}
