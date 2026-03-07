import org.tabletest.junit.Scenario;
import org.tabletest.junit.TableTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScenarioTest {
    @TableTest({
        "Scenario | a | b | sum?",
        "Adding   | 1 | 2 | 3",
        "Zero sum | 0 | 0 | 0"
    })
    void scenario_parameter(@Scenario String scenario, int a, int b, int sum) {
        assertEquals(sum, a + b);
    }
}
