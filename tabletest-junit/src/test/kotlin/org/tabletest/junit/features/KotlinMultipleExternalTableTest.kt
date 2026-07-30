package org.tabletest.junit.features

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.TestInstance
import org.tabletest.junit.TableTest
import java.util.concurrent.atomic.AtomicInteger

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KotlinMultipleExternalTableTest {
    private val kotlinRowCount = AtomicInteger(0)
    private val kotlinMixedRowCount = AtomicInteger(0)

    @TableTest(resource = "external.table")
    @TableTest(resource = "/external.table")
    fun table_with_multiple_external_files_in_kotlin(
        a: Int,
        b: Int,
        sum: Int,
    ) {
        assertEquals(sum, a + b)
        kotlinRowCount.incrementAndGet()
    }

    @TableTest(resource = "external.table")
    @TableTest(
        """
            Scenario   | a | b | sum?
            Inline One | 5 | 5 | 10
            Inline Two | 1 | 2 | 3
        """,
    )
    fun table_with_mixed_sources_in_kotlin(
        a: Int,
        b: Int,
        sum: Int,
    ) {
        assertEquals(sum, a + b)
        kotlinMixedRowCount.incrementAndGet()
    }

    @AfterAll
    fun verifyKotlinMultiFileAggregation() {
        // Since external.table contains 3 rows, repeating it twice must yield exactly 6 executions
        assertEquals(
            6,
            kotlinRowCount.get(),
        )
        // Assertion: 2 inline rows + 3 external file rows = 5 total executions
        assertEquals(
            5,
            kotlinMixedRowCount.get(),
        )
    }
}
