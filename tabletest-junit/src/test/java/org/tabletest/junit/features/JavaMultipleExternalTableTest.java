package org.tabletest.junit.features;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("spec")
@DisplayName("Multiple external table files")
@Description("""
        Multiple external tables can be used on a single test method.
        This enables datasets to be combined from multiple external files,
        separate test cases into clean logical files,
        or mix external file resources with inline data overrides.        
        """)
@TestInstance(Lifecycle.PER_CLASS)
public class JavaMultipleExternalTableTest {
    private final AtomicInteger multiFileRowCount = new AtomicInteger(0);
    private final AtomicInteger mixedSourceRowCount = new AtomicInteger(0);

    @DisplayName("Table loaded from multiple resources file")
    @Description("Multiple external table files can be combined on a single test")
    @TableTest(resource = "external.table")
    @TableTest(resource = "/external.table")
    void table_with_multiple_external_files(int a, int b, int sum) {
        assertEquals(sum, a + b);
        multiFileRowCount.incrementAndGet();
    }

    @DisplayName("Table loaded from multiple sources")
    @Description("Mixed source inputs (inline and file resource) can be combined on a single test")
    @TableTest(resource = "external.table")
    @TableTest(value = {
            "Scenario   | a | b | sum?",
            "Inline One | 5 | 5 | 10  ",
            "Inline Two | 1 | 2 | 3   "
    })
    void table_with_mixed_sources(int a, int b, int sum) {
        assertEquals(sum, a + b);
        mixedSourceRowCount.incrementAndGet();
    }

    @AfterAll
    void verifyAllRepeatableAggregations() {
        // Since external.table contains 3 rows, repeating it twice must yield exactly 6 executions
        assertEquals(6, multiFileRowCount.get());

        // Assertion: 2 inline rows + 3 external file rows = 5 total executions
        assertEquals(5, mixedSourceRowCount.get());
    }
}

