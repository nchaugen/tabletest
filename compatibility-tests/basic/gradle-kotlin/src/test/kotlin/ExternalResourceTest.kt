import org.tabletest.junit.TableTest
import kotlin.test.assertEquals

class ExternalResourceTest {
    @TableTest(resource = "external.table")
    fun external_resource(a: Int, b: Int, sum: Int) {
        assertEquals(sum, a + b)
    }
}
