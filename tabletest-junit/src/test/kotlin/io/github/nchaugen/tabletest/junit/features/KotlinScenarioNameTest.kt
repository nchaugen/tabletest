package io.github.nchaugen.tabletest.junit.features

import io.github.nchaugen.tabletest.junit.TableTest
import kotlin.test.assertEquals

class KotlinScenarioNameTest {

    @TableTest(
        """
        Scenario     | a | b
        Zero is zero | 0 | 0
        One is two   | 1 | 2
        Two is four  | 2 | 4
        """
    )
    fun scenario_name_without_parameter(a: Int, b: Int) {
        assertEquals(b, 2 * a)
    }

    @TableTest(
        """
        Scenario     | a | b
        Zero is zero | 0 | 0
        One is two   | 1 | 2
        Two is four  | 2 | 4
        """
    )
    fun scenario_name_with_parameter(scenario: String, a: Int, b: Int) {
        assertEquals(b, 2 * a, scenario)
    }

}
