package io.github.nchaugen.tabletest.parser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParseAssertions {
    static void assertParseSuccess(String expectedRest, List<Object> expectedCaptures, ParseResult actual) {
        assertParseResult(true, expectedRest, expectedCaptures, actual);
    }

    static void assertParseFailure(String expectedRest, List<Object> expectedCaptures, ParseResult actual) {
        assertParseResult(false, expectedRest, expectedCaptures, actual);
    }

    static void assertParseResult(
        boolean expectedSuccess,
        String expectedRest,
        List<Object> expectedCaptures,
        ParseResult actual
    ) {
        assertAll(
            () -> assertEquals(expectedSuccess, actual.isSuccess(), "success"),
            () -> assertEquals(expectedRest, actual.rest(), "rest"),
            () -> assertEquals(expectedCaptures, actual.captures(), "captures")
        );
    }
}
