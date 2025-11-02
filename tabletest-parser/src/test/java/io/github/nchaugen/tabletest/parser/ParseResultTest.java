package io.github.nchaugen.tabletest.parser;

import io.github.nchaugen.tabletest.parser.ParseResult.Success;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ParseResultTest {

    @Test
    void successIsSuccess() {
        ParseResult result = ParseResult.success("value", "rest", List.of("capture"));
        assertTrue(result.isSuccess(), "Parse result should be success");
        assertFalse(result.isFailure(), "Parse result should not be failure");
//        assertEquals("value", result.consumed());
        assertTrue(result.hasRest(), "Unconsumed input should be rest");
        assertEquals("rest", result.rest());
        assertEquals(List.of("capture"), result.captures());
    }

    @Test
    void failureIsFailure() {
        ParseResult result = ParseResult.failure("failure");
        assertFalse(result.isSuccess(), "Parse result should not be success");
        assertTrue(result.isFailure(), "Parse result should be failure");
        assertTrue(result.hasRest(), "Unconsumed input should be rest");
        assertEquals("failure", result.rest());
        assertTrue(result.captures().isEmpty(), "Captures should be empty on failure");
    }

    @Nested
    class Appending {

        @Test
        void nothingAppendsToFailure() {
            ParseResult failure = ParseResult.failure("failure");

            assertSame(
                failure, failure.append(() -> {
                    throw new RuntimeException("should not be called");
                })
            );
        }

        @Test
        void appendingSuccessesCombinesResult() {
            ParseResult firstSuccess = ParseResult.success("first", "secondrest", List.of("first"));
            ParseResult thenSuccess = ParseResult.success("second", "rest", List.of("second"));

            ParseResult result = firstSuccess.append(() -> thenSuccess);

            assertTrue(result.isSuccess());
            assertEquals("firstsecond", ((Success) result).consumed());
            assertEquals("rest", result.rest());
            assertEquals(List.of("first", "second"), result.captures());

        }

        @Test
        void appendingFailureToSuccessIsFailure() {
            ParseResult firstSuccess = ParseResult.success("first", "rest");
            ParseResult thenFailure = ParseResult.failure("rest");

            assertSame(thenFailure, firstSuccess.append(() -> thenFailure));
        }

    }

    @Nested
    class Capturing {

        @Test
        void captureSavesConsumedString() {
            Success success = ParseResult.success("consumed", "");
            assertEquals(List.of(), success.captures());

            Success capturedSuccess = success.capture();
            assertEquals(List.of("consumed"), capturedSuccess.captures());
        }

        @Test
        void capturesAreImmutable() {
            ParseResult result = ParseResult.success("capture", "", List.of("capture"));
            assertThrows(UnsupportedOperationException.class, () -> result.captures().add("newCapture"));
        }

        @Test
        void capturesCanBeCollectedToImmutableInsertionOrderSet() {
            ParseResult result = ParseResult
                .success("capture1-capture2", "", List.of("capture1", "capture2"))
                .collectCapturesToSet();

            assertEquals(List.of(Set.of("capture1", "capture2")), result.captures());
            assertThrows(UnsupportedOperationException.class, () -> result.captures().add("more"));
            assertEquals("[[capture1, capture2]]", result.captures().toString());
        }

        @Test
        void capturesCanBeCollectedToImmutableList() {
            ParseResult result = ParseResult
                .success("capture1-capture2", "", List.of("capture1", "capture2"))
                .collectCapturesToList();

            assertEquals(List.of(List.of("capture1", "capture2")), result.captures());
            assertThrows(UnsupportedOperationException.class, () -> result.captures().add("more"));
        }

        @Test
        void capturesCanBeCollectedToImmutableInsertionOrderMap() {
            ParseResult result = ParseResult
                .success("capture1-capture2", "", List.of("capture1", "value1", "capture2", "value2"))
                .collectCapturesToMap();

            assertEquals(List.of(Map.of("capture1", "value1", "capture2", "value2")), result.captures());
            assertThrows(UnsupportedOperationException.class, () -> result.captures().add("more"));
            assertEquals("[{capture1=value1, capture2=value2}]", result.captures().toString());
        }

        @Test
        void collectingEmptyCapturesIsPossible() {
            assertEquals(
                List.of(List.of()),
                ParseResult.success("", "", List.of()).collectCapturesToList().captures()
            );
            assertEquals(
                List.of(Set.of()),
                ParseResult.success("", "", List.of()).collectCapturesToSet().captures()
            );
            assertEquals(
                List.of(Map.of()),
                ParseResult.success("", "", List.of()).collectCapturesToMap().captures()
            );
        }

        @Test
        void collectingUnevenNumberOfCapturesToMapIsNotPossible() {
            assertThrows(
                IllegalStateException.class,
                () -> ParseResult.success("", "", List.of("a", "1", "b", "2", "c"))
                    .collectCapturesToMap()
            );
        }
    }
}