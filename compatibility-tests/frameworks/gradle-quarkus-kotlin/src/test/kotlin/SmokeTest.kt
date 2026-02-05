import org.tabletest.junit.TableTest
import kotlin.test.assertEquals

class SmokeTest {
    @TableTest("""
        a | b | sum
        1 | 2 | 3
        4 | 5 | 9
        """)
    fun testBasicTableTest(a: Int, b: Int, sum: Int) {
        assertEquals(sum, a + b);
    }
}
