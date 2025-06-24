import io.github.nchaugen.tabletest.junit.TableTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SmokeTest {
    @TableTest("""
        a | b | sum
        1 | 2 | 3
        4 | 5 | 9
        """)
    void testBasicTableTest(int a, int b, int sum) {
        assertEquals(sum, a + b);
    }
}
