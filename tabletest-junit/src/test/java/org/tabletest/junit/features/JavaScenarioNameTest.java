package org.tabletest.junit.features;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.tabletest.junit.Description;
import org.tabletest.junit.Scenario;
import org.tabletest.junit.TableTest;
import org.junit.jupiter.api.TestInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("spec")
@DisplayName("Scenario names")
@Description("""
        A table can carry one more column than the method has table-bound
        parameters; the extra first column then names each row's scenario and
        becomes the invocation display name.
        """)
public class JavaScenarioNameTest {

    @DisplayName("An extra first column names the row without binding to a parameter")
    @Description("The name may be empty or blank; the row runs regardless.")
    @TableTest("""
        Scenario | a | b
        Zero     | 0 | 0
        ""       | 1 | 2
                 | 2 | 4
        """)
    void scenario_column_can_be_undeclared(int a, int b) {
        assertEquals(b, 2 * a);
    }

    @DisplayName("A @Scenario parameter receives the scenario name as an argument")
    @TableTest("""
        Scenario | Expected Scenario
                 |
        ""       | ""
        Example  | Example
        """)
    void scenario_column_can_be_declared(@Scenario String scenario, String expectedScenario) {
        assertEquals(expectedScenario, scenario);
    }

    // Not published: asserts JUnit's internal `[n]`-prefixed display names —
    // implementation detail, not spec.
    @Tag("unpublished")
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

    @Tag("unpublished")
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
