package org.tabletest.junit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tabletest.junit.TableTestArgumentsProvider.provideArgumentsForInput;

/**
 * Tests validation of the table shape against the test method parameters.
 */
class TableTestArgumentsProviderTest {

    @Test
    void provides_one_argument_set_per_row_when_rows_match_header_width() {
        Stream<? extends Arguments> arguments = provideArgumentsForInput(
            twoIntParameters(),
            """
                a | b
                1 | 2
                3 | 4
                """
        );
        assertEquals(2, arguments.count());
    }

    @Test
    void allows_scenario_name_column_without_declared_parameter() {
        Stream<? extends Arguments> arguments = provideArgumentsForInput(
            twoIntParameters(),
            """
                Scenario | a | b
                First    | 1 | 2
                Second   | 3 | 4
                """
        );
        assertEquals(2, arguments.count());
    }

    @Test
    void fails_when_row_is_wider_than_header() {
        TableTestException exception = assertThrowsWhileProviding(
            twoIntParameters(),
            """
                a | b
                1 | 2
                9 | 1 | 2
                """
        );
        assertMessageDescribesOffendingRow(exception, 3, 2, "9 | 1 | 2");
    }

    @Test
    void fails_when_row_is_narrower_than_header() {
        TableTestException exception = assertThrowsWhileProviding(
            twoIntParameters(),
            """
                Scenario | a | b
                First    | 1 | 2
                3        | 4
                """
        );
        assertMessageDescribesOffendingRow(exception, 2, 3, "3 | 4");
    }

    @Test
    void fails_when_row_is_wider_than_header_and_parameter_list() {
        TableTestException exception = assertThrowsWhileProviding(
            twoIntParameters(),
            """
                a | b
                1 | 2 | 3 | 4
                """
        );
        assertMessageDescribesOffendingRow(exception, 4, 2, "1 | 2 | 3 | 4");
    }

    @Test
    void fails_when_table_has_more_columns_than_parameters_can_take() {
        TableTestException exception = assertThrowsWhileProviding(
            twoIntParameters(),
            """
                a | b | c | d
                1 | 2 | 3 | 4
                """
        );
        assertTrue(
            exception.getMessage().contains("fewer parameters")
                && exception.getMessage().contains("must have a corresponding test method parameter"),
            "Unexpected message: " + exception.getMessage()
        );
    }

    private static TableTestException assertThrowsWhileProviding(Method testMethod, String input) {
        return assertThrows(
            TableTestException.class,
            () -> provideArgumentsForInput(testMethod, input).count()
        );
    }

    private static void assertMessageDescribesOffendingRow(
        TableTestException exception,
        int cellCount,
        int columnCount,
        String rowContent
    ) {
        String message = exception.getMessage();
        assertTrue(
            message.contains(cellCount + " cell") && message.contains(columnCount + " column"),
            "Message does not describe the width mismatch: " + message
        );
        assertTrue(
            message.contains(rowContent),
            "Message does not include the offending row `" + rowContent + "`: " + message
        );
    }

    @SuppressWarnings("unused")
    private void twoInts(int a, int b) {
    }

    private static Method twoIntParameters() {
        return Arrays.stream(TableTestArgumentsProviderTest.class.getDeclaredMethods())
            .filter(method -> method.getName().equals("twoInts"))
            .findFirst()
            .orElseThrow();
    }
}
