package org.tabletest.junit.features;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.tabletest.junit.Description;
import org.tabletest.junit.Scenario;
import org.tabletest.junit.TableTest;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("spec")
@DisplayName("Parameter resolvers")
@Description("""
        Parameters supplied by JUnit parameter resolvers — TestInfo,
        TestReporter, @TempDir, and the like — can follow the table-bound
        parameters. When resolver parameters are present, a scenario column
        must be declared with @Scenario to be recognised.
        """)
public class JavaParameterResolversTest {

    @DisplayName("Resolver-supplied parameters follow the table-bound parameters")
    @Description("""
            Each invocation receives its scenario name and data column from the
            table, while JUnit supplies TestInfo, TestReporter, and a temporary
            directory.
            """)
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
