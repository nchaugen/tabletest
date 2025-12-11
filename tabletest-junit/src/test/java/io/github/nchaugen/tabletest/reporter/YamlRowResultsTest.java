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
package io.github.nchaugen.tabletest.reporter;

import io.github.nchaugen.tabletest.parser.TableParser;
import io.github.nchaugen.tabletest.renderer.ColumnRoles;
import io.github.nchaugen.tabletest.renderer.TableMetadata;
import io.github.nchaugen.tabletest.renderer.YamlTableRenderer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlRowResultsTest {

    private final YamlTableRenderer renderer = new YamlTableRenderer();

    @Test
    void shouldIncludeRowResultsInYaml() {
        var table = TableParser.parse("""
            a | b
            1 | 2
            3 | 4
            """);

        var metadata = new TestMetadataWithResults(
            List.of(
                new RowResult(0, true, null, "test[1]"),
                new RowResult(1, false, new AssertionError("Expected 4"), "test[2]")
            )
        );

        String yaml = renderer.render(table, metadata);

        // Verify rowResults section exists
        assertTrue(yaml.contains("\"rowResults\":"), "YAML should contain rowResults section");

        // Verify first row result (passed)
        assertTrue(yaml.contains("\"rowIndex\": !!int \"0\""), "Should have row 0");
        assertTrue(yaml.contains("\"passed\": !!bool \"true\""), "Row 0 should be marked as passed");
        assertTrue(yaml.contains("\"displayName\": \"test[1]\""), "Should have display name for row 0");

        // Verify second row result (failed)
        assertTrue(yaml.contains("\"rowIndex\": !!int \"1\""), "Should have row 1");
        assertTrue(yaml.contains("\"passed\": !!bool \"false\""), "Row 1 should be marked as failed");
        assertTrue(yaml.contains("\"errorMessage\": \"Expected 4\""), "Should have error message for row 1");
    }

    // Test implementation of TableMetadata with row results
    private static class TestMetadataWithResults implements TableMetadata {
        private final List<RowResult> results;

        TestMetadataWithResults(List<RowResult> results) {
            this.results = results;
        }

        @Override
        public ColumnRoles columnRoles() {
            return ColumnRoles.NO_ROLES;
        }

        @Override
        public String title() {
            return "Test Table";
        }

        @Override
        public String description() {
            return null;
        }

        @Override
        public List<RowResult> rowResults() {
            return results;
        }
    }
}
