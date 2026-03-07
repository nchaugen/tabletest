import org.tabletest.junit.TableTest;
import org.tabletest.junit.TypeConverterSources;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TypeConverterSources({YesNoConverter.class})
public class TypeConverterSourcesTest {
    @TableTest("""
        Scenario         | input | expected?
        yes maps to true | yes   | yes
        no maps to false | no    | no
        """)
    void yes_no_converter(boolean input, boolean expected) {
        assertEquals(expected, input);
    }
}
