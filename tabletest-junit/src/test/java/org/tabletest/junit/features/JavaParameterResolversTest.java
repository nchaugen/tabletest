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
        A JUnit parameter resolver supplies parameters such as TestInfo, TestReporter, and
        @TempDir. Those parameters can follow the table-bound parameters.

        Where a resolver parameter is present, a scenario column needs @Scenario. Without it
        TableTest does not recognise the column.
        """)
public class JavaParameterResolversTest {

    @DisplayName("Resolver-supplied parameters follow the table-bound parameters")
    @Description("""
            The rule is the parameter order. The byte count is how a row proves it.

            Each invocation writes its File content to a temporary directory that JUnit supplies,
            and then measures the file. A count that matches the content is possible only where
            the table's value reached the parameter the table names. It cannot come from one of
            the three parameters the resolvers fill after it.

            JUnit resolves TestInfo, TestReporter and the temporary directory alongside, and the
            body asserts that each one arrived.

            Two rows, and not one. An empty file writes no bytes, and that is also the count of a
            parameter that never received the cell.
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
