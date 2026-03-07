import org.tabletest.junit.TableTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class SmokeTest {
    @TableTest("""
        Scenario | a | b | sum
        One      | 1 | 2 | 3
        Two      | 4 | 5 | 9
        """)
    void testBasicTableTest(int a, int b, int sum) {
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

    @TableTest("""
        a | b | sum?
        // this row is a comment
        1 | 2 | 3
        
        4 | 5 | 9
        """)
    void comments_and_blank_lines(int a, int b, int sum) {
        assertEquals(sum, a + b);
    }

    @TableTest("""
        flag  | expected?
        true  | true
        false | false
        """)
    void boolean_conversion(boolean flag, boolean expected) {
        assertEquals(expected, flag);
    }
}
