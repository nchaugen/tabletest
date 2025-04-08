package io.github.nchaugen.tabletest.parser;

import org.junit.jupiter.api.Test;

import static io.github.nchaugen.tabletest.parser.CombinationParser.atLeast;
import static io.github.nchaugen.tabletest.parser.CombinationParser.either;
import static io.github.nchaugen.tabletest.parser.CombinationParser.optional;
import static io.github.nchaugen.tabletest.parser.CombinationParser.sequence;
import static io.github.nchaugen.tabletest.parser.NumberParser.digit;
import static io.github.nchaugen.tabletest.parser.ParseResult.failure;
import static io.github.nchaugen.tabletest.parser.ParseResult.success;
import static io.github.nchaugen.tabletest.parser.StringParser.character;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CombinationParserTest {

    @Test
    void shouldMatchEitherOr() {
        Parser aOrB = either(character('a'), character('b'));

        assertTrue(aOrB.parse("a").isSuccess());
        assertTrue(aOrB.parse("b").isSuccess());
        assertFalse(aOrB.parse("c").isSuccess());
    }

    @Test
    void shouldMatchSequence() {
        Parser aAndB = sequence(character('a'), character('b'));

        assertEquals(success("ab", "cdef"), aAndB.parse("abcdef"));
        assertEquals(failure(), aAndB.parse("bcdef"));
    }

    @Test
    void shouldMatchAtLeast() {
        Parser atLeastTwoDigits = atLeast(2, digit());

        assertEquals(success("12", ""), atLeastTwoDigits.parse("12"));
        assertEquals(success("12345", ""), atLeastTwoDigits.parse("12345"));
        assertEquals(success("123", "abc"), atLeastTwoDigits.parse("123abc"));
        assertEquals(failure(), atLeastTwoDigits.parse(""));
        assertEquals(failure(), atLeastTwoDigits.parse("1"));
        assertEquals(failure(), atLeastTwoDigits.parse("1abc"));
    }

    @Test
    void shouldMatchOptionally() {
        Parser maybeA = optional(character('a'));

        assertEquals(success("a", "bc"), maybeA.parse("abc"));
        assertEquals(success("", "bc"), maybeA.parse("bc"));
    }

}
