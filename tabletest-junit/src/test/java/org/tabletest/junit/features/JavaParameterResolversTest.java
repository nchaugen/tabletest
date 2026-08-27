package org.tabletest.junit.features;

import org.junit.jupiter.api.DisplayName;
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
            The rule is the parameter order, and the byte count is how a row proves it. Each
            invocation writes its File content to a JUnit-supplied temporary directory and measures
            the file, so a count matching the content is only possible if the table's value reached
            the parameter the table names and not one of the three resolved after it. TestInfo,
            TestReporter and the temporary directory are all resolved alongside, and the body
            asserts that each arrived.

            Two rows rather than one, because an empty file writes no bytes and would also be the
            count of a parameter that never received the cell.
            """)
    @TableTest("""
        Scenario                | File content | File size in bytes?
        Content written to file | hello        | 5
        Nothing written to file | ''           | 0
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
