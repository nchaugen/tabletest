package org.tabletest.parser;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.tabletest.parser.CaptureParser.captureUnquoted;
import static org.tabletest.parser.CombinationParser.atLeast;
import static org.tabletest.parser.CombinationParser.either;
import static org.tabletest.parser.CombinationParser.optional;
import static org.tabletest.parser.CombinationParser.sequence;
import static org.tabletest.parser.CombinationParser.zeroOrMore;
import static org.tabletest.parser.ParseResult.failure;
import static org.tabletest.parser.ParseResult.success;
import static org.tabletest.parser.StringParser.character;
import static org.tabletest.parser.StringParser.characterExcept;
import static org.tabletest.parser.StringParser.characters;
import static org.tabletest.parser.StringParser.string;
import static org.tabletest.parser.StringParser.whitespace;
import static org.tabletest.parser.StringValue.unquoted;

class CombinationParserTest {

    @Test
    void shouldMatchEitherOr() {
        // Simple either test with characters
        Parser aOrB = either(character('a'), character('b'));
        assertTrue(aOrB.parse("a").isSuccess());
        assertTrue(aOrB.parse("b").isSuccess());
        assertFalse(aOrB.parse("c").isSuccess());
        assertEquals(success("a", "bc"), aOrB.parse("abc"));
        assertEquals(success("b", "cd"), aOrB.parse("bcd"));
        assertEquals(failure(""), aOrB.parse(""));

        // Test order of evaluation - first match wins
        Parser orderTest = either(
            string("abc"),
            string("ab"),
            string("a")
        );
        assertEquals(success("abc", "def"), orderTest.parse("abcdef"));

        // Test with many alternatives
        Parser digits = either(
            character('0'),
            character('1'),
            character('2'),
            character('3'),
            character('4'),
            character('5'),
            character('6'),
            character('7'),
            character('8'),
            character('9')
        );
        assertTrue(digits.parse("5xyz").isSuccess());
        assertFalse(digits.parse("xyz").isSuccess());
    }

    @Test
    void shouldMatchSequence() {
        // Basic sequence test
        Parser aAndB = sequence(character('a'), character('b'));
        assertEquals(success("ab", "cdef"), aAndB.parse("abcdef"));
        assertEquals(failure("bcdef"), aAndB.parse("bcdef"));
        assertEquals(failure("cdef"), aAndB.parse("acdef"));
        assertEquals(failure("def"), aAndB.parse("adef"));
        assertEquals(failure(""), aAndB.parse(""));

        // Sequence with mixed parser types
        Parser helloWorld = sequence(
            string("hello"),
            whitespace(),
            string("world")
        );
        assertEquals(success("hello world", "!"), helloWorld.parse("hello world!"));
        assertEquals(failure("world"), helloWorld.parse("helloworld"));

        // Empty sequence should succeed
        Parser emptySeq = sequence();
        assertEquals(success("", "abc"), emptySeq.parse("abc"));

        // Test with captured elements
        Parser expr = sequence(
            captureUnquoted(atLeast(1, characters("0123456789"))),
            character('+'),
            captureUnquoted(atLeast(1, characters("0123456789")))
        );
        ParseResult result = expr.parse("123+456");
        assertTrue(result.isSuccess());
        assertEquals(List.of(unquoted("123"), unquoted("456")), result.captures());
    }

    @Test
    void shouldMatchAtLeast() {
        // Basic minimum count test
        Parser atLeastTwoDigits = atLeast(2, characters("0123456789"));
        assertEquals(success("12", ""), atLeastTwoDigits.parse("12"));
        assertEquals(success("12345", ""), atLeastTwoDigits.parse("12345"));
        assertEquals(success("123", "abc"), atLeastTwoDigits.parse("123abc"));
        assertEquals(failure(""), atLeastTwoDigits.parse(""));
        assertEquals(failure(""), atLeastTwoDigits.parse("1"));
        assertEquals(failure("abc"), atLeastTwoDigits.parse("1abc"));

        // atLeast(0, ...) should always succeed
        Parser atLeastZeroDigits = atLeast(0, characters("0123456789"));
        assertEquals(success("", "abc"), atLeastZeroDigits.parse("abc"));
        assertEquals(success("123", "abc"), atLeastZeroDigits.parse("123abc"));
        assertEquals(success("", ""), atLeastZeroDigits.parse(""));

        // Test with complex pattern
        Parser words = atLeast(2,
                               sequence(
                                   atLeast(1, characterExcept(' ')),
                                   optional(character(' '))
                               )
        );
        assertEquals(success("hello world", ""), words.parse("hello world"));
        assertEquals(success("a b", ""), words.parse("a b"));
        assertEquals(failure(""), words.parse("word"));
    }

    @Test
    void shouldMatchZeroOrMore() {
        // Basic repeated pattern
        Parser digits = zeroOrMore(characters("0123456789"));
        assertEquals(success("123", "abc"), digits.parse("123abc"));
        assertEquals(success("", "abc"), digits.parse("abc"));
        assertEquals(success("", ""), digits.parse(""));

        // Whitespace test
        Parser spaces = zeroOrMore(character(' '));
        assertEquals(success("   ", "abc"), spaces.parse("   abc"));
        assertEquals(success("", "abc"), spaces.parse("abc"));

        // Test with mixed pattern
        Parser commaSeparatedItems = sequence(
            optional(whitespace()),
            atLeast(1, characterExcept(',')),
            zeroOrMore(
                sequence(
                    character(','),
                    optional(whitespace()),
                    atLeast(1, characterExcept(','))
                )
            )
        );
        assertTrue(commaSeparatedItems.parse("a,b,c").isSuccess());
        assertTrue(commaSeparatedItems.parse("a, b, c").isSuccess());
        assertTrue(commaSeparatedItems.parse(" item").isSuccess());
    }

    @Test
    void shouldMatchOptionally() {
        // Basic optional character
        Parser maybeA = optional(character('a'));
        assertEquals(success("a", "bc"), maybeA.parse("abc"));
        assertEquals(success("", "bc"), maybeA.parse("bc"));
        assertEquals(success("", ""), maybeA.parse(""));

        // Optional prefix in pattern
        Parser prefixedNumber = sequence(
            optional(string("0x")),
            atLeast(1, characters("0123456789ABCDEF"))
        );
        assertEquals(success("0x123ABC", ""), prefixedNumber.parse("0x123ABC"));
        assertEquals(success("123", ""), prefixedNumber.parse("123"));

        // Optional surrounding elements
        Parser quotedText = sequence(
            optional(character('"')),
            atLeast(1, characterExcept('"')),
            optional(character('"'))
        );
        assertEquals(success("\"hello\"", ""), quotedText.parse("\"hello\""));
        assertEquals(success("hello\"", ""), quotedText.parse("hello\""));
        assertEquals(success("\"hello", ""), quotedText.parse("\"hello"));
        assertEquals(success("hello", ""), quotedText.parse("hello"));
    }

    @Test
    void shouldHandleNestedCombinations() {
        // Test complex nested combinators
        Parser jsonString = sequence(
            character('"'),
            zeroOrMore(
                either(
                    sequence(character('\\'), character('"')),
                    characterExcept('"')
                )
            ),
            character('"')
        );

        assertTrue(jsonString.parse("\"hello\"").isSuccess());
        assertTrue(jsonString.parse("\"hello\\\"world\"").isSuccess());
        assertEquals(success("\"hello\\\"world\"", ""), jsonString.parse("\"hello\\\"world\""));
        assertFalse(jsonString.parse("hello").isSuccess());
        assertFalse(jsonString.parse("\"hello").isSuccess());

    }

    @Test
    void shouldHandleEmptyPatterns() {
        // Empty either should fail
        Parser emptyEither = either();
        assertEquals(failure("abc"), emptyEither.parse("abc"));
        assertEquals(failure(""), emptyEither.parse(""));

        // Empty sequence should succeed with empty match
        Parser emptySequence = sequence();
        assertEquals(success("", "abc"), emptySequence.parse("abc"));
        assertEquals(success("", ""), emptySequence.parse(""));
    }
}
