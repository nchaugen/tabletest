import org.tabletest.junit.TableTest
import kotlin.test.assertEquals

class CollectionTest {
    @TableTest("""
        numbers   | size?
        [1, 2, 3] | 3
        []        | 0
        [5]       | 1
        """)
    fun list_parameter(numbers: List<String>, size: Int) {
        assertEquals(size, numbers.size)
    }
}
