import org.tabletest.junit.TableTest
import org.tabletest.junit.TypeConverterSources
import kotlin.test.assertEquals

@TypeConverterSources(YesNoConverter::class)
class TypeConverterSourcesTest {
    @TableTest("""
        Scenario         | input | expected?
        yes maps to true | yes   | yes
        no maps to false | no    | no
        """)
    fun yes_no_converter(input: Boolean, expected: Boolean) {
        assertEquals(expected, input)
    }
}
