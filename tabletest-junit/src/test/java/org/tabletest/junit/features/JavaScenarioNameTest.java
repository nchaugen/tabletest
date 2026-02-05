package org.tabletest.junit.features;

import org.tabletest.junit.Scenario;
import org.tabletest.junit.TableTest;
import org.junit.jupiter.api.TestInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JavaScenarioNameTest {

    @TableTest("""
        Scenario | a | b
        Zero     | 0 | 0
        ""       | 1 | 2
                 | 2 | 4
        """)
    void scenario_column_can_be_undeclared(int a, int b) {
        assertEquals(b, 2 * a);
    }

    @TableTest("""
        Scenario | Expected Scenario
                 |
        ""       | ""
        Example  | Example
        """)
    void scenario_column_can_be_declared(@Scenario String scenario, String expectedScenario) {
        assertEquals(expectedScenario, scenario);
    }

    @TableTest("""
        Scenario | Display Name?
                 | '[1] null, "[1]'
        ""       | '[2] "", "[2]'
        Example  | '[3] Example'
        """)
    void declared_scenario_name_becomes_display_name(@Scenario String ignoredScenario, String expectedDisplayName, TestInfo info) {
        assertTrue(
            info.getDisplayName().startsWith(expectedDisplayName),
            String.format("Display name `%s` did not start with `%s`", info.getDisplayName(), expectedDisplayName)
        );
    }

    @TableTest("""
        Display Name?   | Scenario
        "[1] Example"   | Example
        """)
    void declared_scenario_name_do_not_have_to_be_first_parameter(
        String expectedDisplayName,
        @Scenario String ignoredScenario,
        TestInfo info
    ) {
        assertEquals(info.getDisplayName(), expectedDisplayName);
    }

}
