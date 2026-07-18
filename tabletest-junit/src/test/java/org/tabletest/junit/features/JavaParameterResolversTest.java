package org.tabletest.junit.features;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.tabletest.junit.Description;
import org.tabletest.junit.Scenario;
import org.tabletest.junit.TableTest;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
            Each invocation takes its file content from the table, writes it to
            a JUnit-supplied temporary directory, and verifies the size of the
            written file. TestInfo and TestReporter are resolved alongside.
            """)
    @TableTest("""
        Scenario     | File content | File size in bytes?
        Single word  | hello        | 5
        Two words    | hello world  | 11
        Empty file   | ''           | 0
        """)
    void parameter_resolvers_with_declared_scenario(
        @Scenario String scenario,
        String content,
        long expectedFileSize,
        TestInfo info,
        TestReporter reporter,
        @TempDir Path tempDir
    ) throws IOException {
        Path file = Files.writeString(tempDir.resolve("content.txt"), content);
        assertEquals(expectedFileSize, Files.size(file));
        assertNotNull(reporter, "TestReporter is null");
        assertTrue(
            info.getDisplayName().contains(scenario),
            "Scenario name is not in display name: " + info.getDisplayName()
        );
    }

}
