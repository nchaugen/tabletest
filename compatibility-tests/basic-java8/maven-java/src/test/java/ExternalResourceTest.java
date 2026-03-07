import org.tabletest.junit.TableTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExternalResourceTest {
    @TableTest(resource = "external.table")
    void external_resource(int a, int b, int sum) {
        assertEquals(sum, a + b);
    }
}
