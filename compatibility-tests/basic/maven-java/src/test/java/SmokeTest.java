import org.tabletest.junit.TableTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SmokeTest {
    @TableTest("""
        Scenario | a | b | sum
        One      | 1 | 2 | 3
        Two      | 4 | 5 | 9
        """)
    void testBasicTableTest(int a, int b, int sum) {
        assertEquals(sum, a + b);
    }
}
