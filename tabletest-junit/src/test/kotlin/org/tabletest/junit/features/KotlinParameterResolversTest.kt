package org.tabletest.junit.features

import org.tabletest.junit.Scenario
import org.tabletest.junit.TableTest
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.TestReporter
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class KotlinParameterResolversTest {

    @TableTest(
        """    
        Scenario | a
                 | 0
        One      | 1
        """
    )
    fun parameter_resolvers_with_declared_scenario(
        @Scenario scenario: String?,
        a: String,
        info: TestInfo,
        reporter: TestReporter,
        @TempDir tempDir: Path
    ) {
        assertNotNull(info, "TestInfo is null")
        assertNotNull(reporter, "TestReporter is null")
        assertNotNull(tempDir, "TempDir is null")
        assertTrue(a.matches(Regex("\\d")), "Data column is not digit: $a")
        assertTrue(
            info.displayName.contains(scenario ?: "null"),
            "Scenario name is not in display name: ${info.displayName}"
        )
    }

}
