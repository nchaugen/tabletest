package org.tabletest.junit.features;

import org.tabletest.junit.Scenario;
import org.tabletest.junit.TableTest;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JavaParameterResolversTest {

    @TableTest("""
        Scenario | a
                 | 0
        One      | 1
        """)
    void parameter_resolvers_with_declared_scenario(
        @Scenario String scenario,
        String a,
        TestInfo info,
        TestReporter reporter,
        @TempDir Path tempDir
    ) {
        assertNotNull(info, "TestInfo is null");
        assertNotNull(reporter, "TestReporter is null");
        assertNotNull(tempDir, "TempDir is null");
        assertTrue(a.matches("\\d"), "Data column is not digit: " + a);
        assertTrue(
            info.getDisplayName().contains(scenario == null ? "null" : scenario),
            "Scenario name is not in display name: " + info.getDisplayName()
        );
    }

}
