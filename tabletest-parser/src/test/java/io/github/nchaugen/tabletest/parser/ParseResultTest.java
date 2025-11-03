package io.github.nchaugen.tabletest.parser;

import io.github.nchaugen.tabletest.parser.ParseResult.Success;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.nchaugen.tabletest.parser.ParseResult.failure;
import static io.github.nchaugen.tabletest.parser.ParseResult.success;
import static org.junit.jupiter.api.Assertions.*;

class ParseResultTest {

    @Test
    void successIsSuccess() {
        ParseResult result = success("value", "rest", List.of("capture"));
        assertAll(
            () -> assertTrue(result.isSuccess(), "Should be success"),
            () -> assertFalse(result.isFailure(), "Should not be failure"),
            () -> assertEquals("rest", result.rest()),
            () -> assertEquals(List.of("capture"), result.captures())
        );
    }

    @Test
    void failureIsFailure() {
        ParseResult result = failure("rest");
        assertAll(
            () -> assertFalse(result.isSuccess(), "Should not be success"),
            () -> assertTrue(result.isFailure(), "Should be failure"),
            () -> assertEquals("rest", result.rest()),
            () -> assertEquals(List.of(), result.captures())
        );
    }

    @Test
    void incompleteness() {
        assertAll(
            () -> assertFalse(success("value", "").isIncomplete(), "Successful result with empty rest is complete"),
            () -> assertTrue(success("value", "rest").isIncomplete(), "Result with rest is incomplete"),
            () -> assertTrue(failure("rest").isIncomplete(), "Failure result is always incomplete"),
            () -> assertTrue(failure("").isIncomplete(), "Failure result is always incomplete")
        );
    }

    @Nested
    class Appending {

        @Test
        void nothingAppendsToFailure() {
            ParseResult failure = failure("failure");

            assertSame(
                failure, failure.append(() -> {
                    throw new RuntimeException("should not be called");
                })
            );
        }

        @Test
        void appendingSuccessesCombinesResult() {
            ParseResult firstSuccess = success("first", "secondrest", List.of("first"));
            ParseResult thenSuccess = success("second", "rest", List.of("second"));

            ParseResult result = firstSuccess.append(() -> thenSuccess);

            assertTrue(result.isSuccess());
            assertEquals("firstsecond", ((Success) result).consumed());
            assertEquals("rest", result.rest());
            assertEquals(List.of("first", "second"), result.captures());

        }

        @Test
        void appendingFailureToSuccessIsFailure() {
            ParseResult firstSuccess = success("first", "rest");
            ParseResult thenFailure = failure("rest");

            assertSame(thenFailure, firstSuccess.append(() -> thenFailure));
        }

    }

    @Nested
    class Capturing {

        @Test
        void captureSavesConsumedString() {
            Success success = success("consumed", "");
            assertEquals(List.of(), success.captures());

            Success capturedSuccess = success.capture();
            assertEquals(List.of("consumed"), capturedSuccess.captures());
        }

        @Test
        void captureTrimmedSavesConsumedStringWithoutWhitespace() {
            Success success = success("  consumed\t\t", "");
            assertEquals(List.of(), success.captures());

            Success capturedSuccess = success.captureTrimmed();
            assertEquals(List.of("consumed"), capturedSuccess.captures());
        }

        @Test
        void captureTrimmedSavesEmptyStringAsNull() {
            Success success = success("", "");
            assertEquals(List.of(), success.captures());

            Success capturedSuccess = success.captureTrimmed();
            assertEquals(Collections.singletonList(null), capturedSuccess.captures());
        }

        @Test
        void captureTrimmedSavesBlankStringAsNull() {
            Success success = success(" \t ", "");
            assertEquals(List.of(), success.captures());

            Success capturedSuccess = success.captureTrimmed();
            assertEquals(Collections.singletonList(null), capturedSuccess.captures());
        }

        @Test
        void capturesAreImmutable() {
            ParseResult result = success("capture", "", List.of("capture"));
            assertThrows(UnsupportedOperationException.class, () -> result.captures().add("newCapture"));
        }

        @Test
        void capturesCanBeCollectedToImmutableInsertionOrderSet() {
            ParseResult result = success("capture1-capture2", "", List.of("capture1", "capture2"))
                .collectCapturesToSet();

            assertEquals(List.of(Set.of("capture1", "capture2")), result.captures());
            assertThrows(UnsupportedOperationException.class, () -> result.captures().add("more"));
            assertEquals("[[capture1, capture2]]", result.captures().toString());
        }

        @Test
        void capturesCanBeCollectedToImmutableList() {
            ParseResult result = success("capture1-capture2", "", List.of("capture1", "capture2"))
                .collectCapturesToList();

            assertEquals(List.of(List.of("capture1", "capture2")), result.captures());
            assertThrows(UnsupportedOperationException.class, () -> result.captures().add("more"));
        }

        @Test
        void capturesCanBeCollectedToImmutableInsertionOrderMap() {
            ParseResult result = success("capture1-capture2", "", List.of("capture1", "value1", "capture2", "value2"))
                .collectCapturesToMap();

            assertEquals(List.of(Map.of("capture1", "value1", "capture2", "value2")), result.captures());
            assertThrows(UnsupportedOperationException.class, () -> result.captures().add("more"));
            assertEquals("[{capture1=value1, capture2=value2}]", result.captures().toString());
        }

        @Test
        void collectingEmptyCapturesIsPossible() {
            assertEquals(
                List.of(List.of()),
                success("", "", List.of()).collectCapturesToList().captures()
            );
            assertEquals(
                List.of(Set.of()),
                success("", "", List.of()).collectCapturesToSet().captures()
            );
            assertEquals(
                List.of(Map.of()),
                success("", "", List.of()).collectCapturesToMap().captures()
            );
        }

        @Test
        void collectingUnevenNumberOfCapturesToMapIsNotPossible() {
            assertThrows(
                TableTestParseException.class,
                () -> success("", "", List.of("a", "1", "b", "2", "c"))
                    .collectCapturesToMap()
            );
        }
    }
}