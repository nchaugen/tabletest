import org.tabletest.junit.Scenario
import org.tabletest.junit.TableTest
import kotlin.test.assertEquals

class ScenarioTest {
    @TableTest("""
        Scenario | a | b | sum?
        Adding   | 1 | 2 | 3
        Zero sum | 0 | 0 | 0
        """)
    fun scenario_parameter(@Scenario scenario: String, a: Int, b: Int, sum: Int) {
        assertEquals(sum, a + b)
    }
}
