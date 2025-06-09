package io.github.nchaugen.tabletest.junit.features;

import io.github.nchaugen.tabletest.junit.TableTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaScenarioNameTest {

    @TableTest("""
        Scenario     | a | b
        Zero is zero | 0 | 0
        One is two   | 1 | 2
        Two is four  | 2 | 4
        """)
    void scenario_column_without_parameter(int a, int b) {
        assertEquals(b, 2 * a);
    }

    @TableTest("""
        Scenario     | a | b
        Zero is zero | 0 | 0
        One is two   | 1 | 2
        Two is four  | 2 | 4
        """)
    void scenario_column_with_parameter(String scenario, int a, int b) {
        assertEquals(b, 2 * a, scenario);
    }

}
