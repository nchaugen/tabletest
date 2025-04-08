package io.github.nchaugen.tabletest.parser;

import org.junit.jupiter.api.Test;

import static io.github.nchaugen.tabletest.parser.ParseResult.failure;
import static io.github.nchaugen.tabletest.parser.ParseResult.success;
import static io.github.nchaugen.tabletest.parser.StringParser.character;
import static io.github.nchaugen.tabletest.parser.StringParser.characterExcept;
import static io.github.nchaugen.tabletest.parser.StringParser.characterExceptNonEscaped;
import static io.github.nchaugen.tabletest.parser.StringParser.quotedValue;
import static io.github.nchaugen.tabletest.parser.StringParser.string;
import static io.github.nchaugen.tabletest.parser.StringParser.surroundedValue;
import static io.github.nchaugen.tabletest.parser.StringParser.whitespace;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringParserTest {

    @Test
    void shouldMatchSpecificCharacter() {
        assertTrue(character('a').parse("abc").isSuccess());
        assertFalse(character('b').parse("abc").isSuccess());
    }

    @Test
    void shouldMatchString() {
        Parser string = string("abc");

        assertEquals(success("abc", "def"), string.parse("abcdef"));
        assertEquals(failure(), string.parse("abdef"));
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
    void shouldMatchDoubleQuotedValue() {
        Parser quotedString = quotedValue('"');
        assertEquals(success("\"abc\"", ""), quotedString.parse("\"abc\""));
        assertEquals(success("\"a\\\"bc\"", ""), quotedString.parse("\"a\\\"bc\""));
        assertEquals(success("\"a\\\"bc\"", "def"), quotedString.parse("\"a\\\"bc\"def"));
        assertEquals(failure(), quotedString.parse("abc"));
    }

    @Test
    void shouldMatchSingleQuotedValue() {
        Parser quotedString = quotedValue('\'');
        assertEquals(success("'abc'", ""), quotedString.parse("'abc'"));
        assertEquals(success("'a\\'bc'", ""), quotedString.parse("'a\\'bc'"));
        assertEquals(success("'a\\'bc'", "def"), quotedString.parse("'a\\'bc'def"));
        assertEquals(failure(), quotedString.parse("abc"));
    }

    @Test
    void shouldMatchSurroundedValue() {
        Parser surroundedString = surroundedValue('(', ')');
        assertEquals(success("(abc)", ""), surroundedString.parse("(abc)"));
        assertEquals(success("(a\\)bc)", ""), surroundedString.parse("(a\\)bc)"));
        assertEquals(success("(a\\)bc)", "def"), surroundedString.parse("(a\\)bc)def"));
        assertEquals(failure(), surroundedString.parse("abc"));
    }

}
