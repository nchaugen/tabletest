import org.tabletest.junit.TableTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SmokeTest {
    @TableTest("""
        a | b | sum
        1 | 2 | 3
        4 | 5 | 9
        """)
    void testBasicTableTest(int a, int b, int sum) {
        assertEquals(sum, a + b);
    }

    @TableTest("""
        Scenario | a | b | sum?
        Adding   | 1 | 2 | 3
        Zero sum | 0 | 0 | 0
        """)
    void scenario_column(int a, int b, int sum) {
        assertEquals(sum, a + b);
    }

    @TableTest("""
        name | value?
             |
        """)
    void null_values(String name, String value) {
        assertNull(name);
        assertNull(value);
    }
}
