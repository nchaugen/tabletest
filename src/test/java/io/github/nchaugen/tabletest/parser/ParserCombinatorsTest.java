package io.github.nchaugen.tabletest.parser;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.atLeast;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.capture;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.character;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.characterExcept;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.characterExceptNonEscaped;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.decimal;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.digit;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.either;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.integer;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.number;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.optional;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.quotedString;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.sequence;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.string;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.whitespace;
import static io.github.nchaugen.tabletest.parser.ParseResult.failure;
import static io.github.nchaugen.tabletest.parser.ParseResult.success;

public class ParserCombinatorsTest {

    @Test
    void shouldMatchSpecificCharacter() {
        assertTrue(character('a').parse("abc").isSuccess());
        assertFalse(character('b').parse("abc").isSuccess());
    }

    @Test
    void shouldMatchEitherOr() {
        Parser aOrB = either(character('a'), character('b'));

        assertTrue(aOrB.parse("a").isSuccess());
        assertTrue(aOrB.parse("b").isSuccess());
        assertFalse(aOrB.parse("c").isSuccess());
    }

    @Test
    void shouldMatchSingleDigits() {
        assertTrue(digit().parse("123").isSuccess());
        assertTrue(digit().parse("911").isSuccess());
        assertFalse(digit().parse("A1").isSuccess());
    }

    @Test
    void shouldMatchSequence() {
        Parser aAndB = sequence(character('a'), character('b'));

        assertEquals(success("ab", "cdef"), aAndB.parse("abcdef"));
        assertEquals(failure(), aAndB.parse("bcdef"));
    }

    @Test
    void shouldMatchString() {
        Parser string = string("abc");

        assertEquals(success("abc", "def"), string.parse("abcdef"));
        assertEquals(failure(), string.parse("abdef"));
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

    @Test
    void shouldMatchInteger() {
        assertEquals(success("123", ""), integer().parse("123"));
        assertEquals(success("123", "abc"), integer().parse("123abc"));
        assertEquals(failure(), integer().parse("abc"));
    }

    @Test
    void shouldMatchDecimal() {
        assertEquals(success("12.3", ""), decimal().parse("12.3"));
        assertEquals(success("1.2", "x"), decimal().parse("1.2x"));
        assertEquals(failure(), decimal().parse("x1.0"));
        assertEquals(failure(), decimal().parse(".3"));
    }

    @Test
    void shouldMatchNumber() {
        assertEquals(success("123", ""), number().parse("123"));
        assertEquals(success("123", "abc"), number().parse("123abc"));
        assertEquals(success("12.3", ""), number().parse("12.3"));
        assertEquals(success("1.2", "x"), number().parse("1.2x"));
        assertEquals(failure(), number().parse("abc"));
        assertEquals(failure(), number().parse("x1.0"));
        assertEquals(failure(), number().parse(".3"));
    }

    @Test
    void shouldCaptureMatchedValues() {
        Parser plus = sequence(
            capture(integer()),
            character('+'),
            capture(integer())
        );

        assertEquals(List.of("1", "2"), plus.parse("1+2").captures());
    }

    @Test
    void shouldMatchWhitespace() {
        assertEquals(success(" ", ""), whitespace().parse(" "));
        assertEquals(success("\t", ""), whitespace().parse("\t"));
        assertEquals(success("\n", ""), whitespace().parse("\n"));
        assertEquals(success("\r", ""), whitespace().parse("\r"));
        assertEquals(success("\f", ""), whitespace().parse("\f"));
    }

    @Test
    void shouldMatchDigitsInRange() {
        assertTrue(digit(0, 2).parse("0").isSuccess());
        assertTrue(digit(0, 2).parse("1").isSuccess());
        assertTrue(digit(0, 2).parse("2").isSuccess());
        assertFalse(digit(0, 2).parse("3").isSuccess());
        assertFalse(digit(0, 2).parse("a").isSuccess());
    }

    @Test
    void shouldMatchCharactersExcept() {
        Parser notA = characterExcept('a');

        assertTrue(notA.parse("b").isSuccess());
        assertFalse(notA.parse("a").isSuccess());
    }

    @Test
    void shouldMatchCharactersExceptUnescaped() {
        Parser notUnescapedA = characterExceptNonEscaped('a');

        assertFalse(notUnescapedA.parse("a").isSuccess());
        assertTrue(notUnescapedA.parse("b").isSuccess());
        assertTrue(notUnescapedA.parse("\\a").isSuccess());
    }

    @Test
    void shouldMatchDoubleQuotedString() {
        Parser quotedString = quotedString('"');
        assertEquals(success("\"abc\"", ""), quotedString.parse("\"abc\""));
        assertEquals(success("\"a\\\"bc\"", ""), quotedString.parse("\"a\\\"bc\""));
        assertEquals(success("\"a\\\"bc\"", "def"), quotedString.parse("\"a\\\"bc\"def"));
        assertEquals(failure(), quotedString.parse("abc"));
    }

    @Test
    void shouldMatchSingleQuotedString() {
        Parser quotedString = quotedString('\'');
        assertEquals(success("'abc'", ""), quotedString.parse("'abc'"));
        assertEquals(success("'a\\'bc'", ""), quotedString.parse("'a\\'bc'"));
        assertEquals(success("'a\\'bc'", "def"), quotedString.parse("'a\\'bc'def"));
        assertEquals(failure(), quotedString.parse("abc"));
    }

    @Test
    void shouldMatchSurroundedString() {
        Parser surroundedString = ParserCombinators.surroundedString('(', ')');
        assertEquals(success("(abc)", ""), surroundedString.parse("(abc)"));
        assertEquals(success("(a\\)bc)", ""), surroundedString.parse("(a\\)bc)"));
        assertEquals(success("(a\\)bc)", "def"), surroundedString.parse("(a\\)bc)def"));
        assertEquals(failure(), surroundedString.parse("abc"));
    }

    @Test
    void shouldMatchTableRow() {
        Parser tableRow = sequence(
            capture(atLeast(1, characterExceptNonEscaped('|'))),
            atLeast(
                0, sequence(
                    character('|'),
                    capture(atLeast(0, characterExceptNonEscaped('|')))
                )
            )
        );

        assertEquals(failure(), tableRow.parse(""));
        assertEquals(List.of("a"), tableRow.parse("a").captures());
        assertEquals(List.of("a ", ""), tableRow.parse("a |").captures());
        assertEquals(List.of("a ", " b"), tableRow.parse("a | b").captures());
        assertEquals(List.of("a ", " ", " c"), tableRow.parse("a | | c").captures());
        assertEquals(List.of("a \\| b ", " c"), tableRow.parse("a \\| b | c").captures());
    }

}
