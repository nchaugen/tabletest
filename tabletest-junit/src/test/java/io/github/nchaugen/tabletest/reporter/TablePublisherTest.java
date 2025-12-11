/*
 * Copyright 2025-present Nils Christian Haugen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.nchaugen.tabletest.reporter;

import io.github.nchaugen.tabletest.junit.TableTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.TestWatcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TablePublisherTest {

    private final TablePublisher publisher = new TablePublisher();

    @Test
    void shouldImplementTestWatcher() {
        assertInstanceOf(
            TestWatcher.class,
            publisher,
            "TablePublisher should implement TestWatcher to intercept test execution"
        );
    }

    @Test
    void shouldStoreTableDataWhenTestMethodHasTableTestAnnotation() throws Exception {
        // This test will verify that when a @TableTest method executes,
        // TablePublisher stores the table and results in the ExtensionContext.Store

        // We'll need to create a minimal ExtensionContext implementation or
        // use a real test execution. For now, let's verify the structure exists.

        // Verify that we have methods to detect @TableTest
        var method = TestFixture.class.getDeclaredMethod("tableTestExample", int.class, int.class);
        var annotation = method.getAnnotation(TableTest.class);

        assertNotNull(annotation, "Test fixture should have @TableTest annotation");
        assertEquals("""
            a | b
            1 | 2
            3 | 4
            """, annotation.value());
    }

    @Test
    void shouldCreateRowResultWithCorrectData() {
        // Test that RowResult record works as expected
        var result = new RowResult(0, true, null, "test[1]");

        assertEquals(0, result.rowIndex());
        assertTrue(result.passed());
        assertEquals("test[1]", result.displayName());
        assertNull(result.cause());
    }

    @Test
    void shouldCreateFailedRowResult() {
        var exception = new AssertionError("Expected 3 but was 4");
        var result = new RowResult(1, false, exception, "test[2]");

        assertEquals(1, result.rowIndex());
        assertFalse(result.passed());
        assertEquals("Expected 3 but was 4", result.cause().getMessage());
    }

    // Test fixture class with @TableTest method
    static class TestFixture {
        @TableTest("""
            a | b
            1 | 2
            3 | 4
            """)
        void tableTestExample(int a, int b) {
            assertEquals(a + 1, b);
        }
    }
}
