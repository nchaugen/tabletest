package org.tabletest.junit.features

import org.tabletest.junit.Scenario
import org.tabletest.junit.TableTest
import org.junit.jupiter.api.TestInfo
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KotlinScenarioNameTest {

    @TableTest(
        """    
        Scenario | a | b
        Zero     | 0 | 0
        ""       | 1 | 2
                 | 2 | 4
        """
    )
    fun scenario_column_can_be_undeclared(a: Int, b: Int) {
        assertEquals(b, 2 * a)
    }

    @TableTest(
        """    
        Scenario | Expected Scenario
                 |
        ""       | ""
        Example  | Example
        """
    )
    fun scenario_column_can_be_declared(@Scenario scenario: String?, expectedScenario: String?) {
        assertEquals(expectedScenario, scenario)
    }

    @TableTest(
        """    
        Scenario | Display Name?
                 | '[1] null, "[1]'
        ""       | '[2] "", "[2]'
        Example  | '[3] Example'
        """
    )
    fun declared_scenario_name_becomes_display_name(
        @Suppress("unused") @Scenario scenario: String?,
        expectedDisplayName: String,
        info: TestInfo
    ) {
        assertTrue(
            info.displayName.startsWith(expectedDisplayName),
            "Display name `${info.displayName}` did not start with `$expectedDisplayName`"
        )
    }

}
