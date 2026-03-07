import org.tabletest.junit.TableTest;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

public class CollectionTest {
    @TableTest("""
        numbers   | size?
        [1, 2, 3] | 3
        []        | 0
        [5]       | 1
        """)
    void list_parameter(List<String> numbers, int size) {
        assertEquals(size, numbers.size());
    }
}
