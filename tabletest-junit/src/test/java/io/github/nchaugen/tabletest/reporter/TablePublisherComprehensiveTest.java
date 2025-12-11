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

import io.github.nchaugen.tabletest.junit.TableTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.OutputDirectoryCreator;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * Comprehensive test that verifies TablePublisher captures test execution results
 * and publishes YAML with row-level pass/fail information.
 * Uses EngineTestKit to run tests programmatically and verify output in one test.
 */
class TablePublisherComprehensiveTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldPublishYamlWithRowResultsForAllPassingRows() throws IOException {
        // Run the test class
        var results = EngineTestKit
            .engine("junit-jupiter")
            .selectors(selectClass(AllRowsPassTest.class))
            .configurationParameter("junit.platform.output.dir", tempDir.toString())
            .enableImplicitConfigurationParameters(true)
            .outputDirectoryCreator(new OutputDirectoryCreator() {
                @Override
                public Path getRootDirectory() {
                    return tempDir;
                }

                @Override
                public Path createOutputDirectory(TestDescriptor testDescriptor) throws IOException {
                    return tempDir;
                }
            })
            .execute();

        // Verify test execution
        results.testEvents()
            .assertStatistics(stats -> stats
                .started(3)
                .succeeded(3)
                .failed(0));

        // Find and verify published YAML
        Path yamlFile = findYamlFile(tempDir, "allRowsPass");
        assertTrue(Files.exists(yamlFile), "YAML file should exist");

        String content = Files.readString(yamlFile);

        // Verify basic structure
        assertTrue(content.contains("\"title\":"), "Should have title");
        assertTrue(content.contains("\"rowResults\":"), "Should have rowResults section");

        // Verify all 3 rows with correct indices (JUnit uses 1-based)
        assertTrue(content.contains("\"rowIndex\": !!int \"1\""), "Should have row 1");
        assertTrue(content.contains("\"rowIndex\": !!int \"2\""), "Should have row 2");
        assertTrue(content.contains("\"rowIndex\": !!int \"3\""), "Should have row 3");

        // Verify all rows passed
        int passedCount = countOccurrences(content, "\"passed\": !!bool \"true\"");
        assertEquals(3, passedCount, "All 3 rows should have passed");

        int failedCount = countOccurrences(content, "\"passed\": !!bool \"false\"");
        assertEquals(0, failedCount, "No rows should have failed");
    }

    @Test
    void shouldPublishYamlWithRowResultsIncludingFailures() throws IOException {
        // Run the test class (one row will fail)
        var results = EngineTestKit
            .engine("junit-jupiter")
            .selectors(selectClass(OneRowFailsTest.class))
            .configurationParameter("junit.platform.output.dir", tempDir.toString())
            .enableImplicitConfigurationParameters(true)
            .outputDirectoryCreator(new OutputDirectoryCreator() {
                @Override
                public Path getRootDirectory() {
                    return tempDir;
                }

                @Override
                public Path createOutputDirectory(TestDescriptor testDescriptor) throws IOException {
                    return tempDir;
                }
            })
            .execute();

        // Verify test execution
        results.testEvents()
            .assertStatistics(stats -> stats
                .started(3)
                .succeeded(2)
                .failed(1));

        // Find and verify published YAML
        Path yamlFile = findYamlFile(tempDir, "oneRowFails");
        assertTrue(Files.exists(yamlFile), "YAML file should exist");

        String content = Files.readString(yamlFile);

        // Verify rowResults section exists
        assertTrue(content.contains("\"rowResults\":"), "Should have rowResults section");

        // Verify 2 passed, 1 failed
        int passedCount = countOccurrences(content, "\"passed\": !!bool \"true\"");
        assertEquals(2, passedCount, "2 rows should have passed");

        int failedCount = countOccurrences(content, "\"passed\": !!bool \"false\"");
        assertEquals(1, failedCount, "1 row should have failed");

        // Verify error message is included
        assertTrue(content.contains("\"errorMessage\":"), "Failed row should have error message");
    }

    private Path findYamlFile(Path baseDir, String methodName) throws IOException {
        try (var paths = Files.walk(baseDir)) {
            return paths
                .filter(p -> p.toString().contains(methodName))
                .filter(p -> p.toString().endsWith(".yaml"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("YAML file not found for " + methodName));
        }
    }

    private int countOccurrences(String content, String substring) {
        int count = 0;
        int index = 0;
        while ((index = content.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }

    // Test classes that will be executed by EngineTestKit
    // These must be public for EngineTestKit to discover them

    @ExtendWith(TablePublisher.class)
    public static class AllRowsPassTest {
        @TableTest("""
            a | b | sum
            1 | 1 | 2
            2 | 2 | 4
            3 | 3 | 6
            """)
        public void allRowsPass(int a, int b, int sum) {
            assertEquals(sum, a + b);
        }
    }

    @ExtendWith(TablePublisher.class)
    public static class OneRowFailsTest {
        @TableTest("""
            a | b | sum
            1 | 1 | 2
            2 | 2 | 5
            3 | 3 | 6
            """)
        public void oneRowFails(int a, int b, int sum) {
            assertEquals(sum, a + b);
        }
    }
}
