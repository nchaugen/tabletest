import org.tabletest.junit.TableTest
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SmokeTest {
    @TableTest("""
        a | b | sum
        1 | 2 | 3
        4 | 5 | 9
        """)
    fun testBasicTableTest(a: Int, b: Int, sum: Int) {
        assertEquals(sum, a + b)
    }

    @TableTest("""
        Scenario | a | b | sum?
        Adding   | 1 | 2 | 3
        Zero sum | 0 | 0 | 0
        """)
    fun scenario_column(a: Int, b: Int, sum: Int) {
        assertEquals(sum, a + b)
    }

    @TableTest("""
        name | value?
             |
        """)
    fun null_values(name: String?, value: String?) {
        assertNull(name)
        assertNull(value)
    }

    @TableTest("""
        a | b | sum?
        // this row is a comment
        1 | 2 | 3
        
        4 | 5 | 9
        """)
    fun comments_and_blank_lines(a: Int, b: Int, sum: Int) {
        assertEquals(sum, a + b)
    }

    @TableTest("""
        flag  | expected?
        true  | true
        false | false
        """)
    fun boolean_conversion(flag: Boolean, expected: Boolean) {
        assertEquals(expected, flag)
    }
}
