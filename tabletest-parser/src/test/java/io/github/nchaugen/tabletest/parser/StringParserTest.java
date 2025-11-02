package io.github.nchaugen.tabletest.parser;

import org.junit.jupiter.api.Test;

import static io.github.nchaugen.tabletest.parser.ParseResult.failure;
import static io.github.nchaugen.tabletest.parser.ParseResult.success;
import static io.github.nchaugen.tabletest.parser.StringParser.anyWhitespace;
import static io.github.nchaugen.tabletest.parser.StringParser.character;
import static io.github.nchaugen.tabletest.parser.StringParser.characterExcept;
import static io.github.nchaugen.tabletest.parser.StringParser.characters;
import static io.github.nchaugen.tabletest.parser.StringParser.string;
import static io.github.nchaugen.tabletest.parser.StringParser.whitespace;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringParserTest {

    @Test
    void shouldMatchSpecificCharacter() {
        assertTrue(character('a').parse("abc").isSuccess());
        assertFalse(character('b').parse("abc").isSuccess());
        assertEquals(success("a", "bc"), character('a').parse("abc"));
        assertEquals(failure(""), character('a').parse(""));
    }

    @Test
    void shouldMatchCharactersFromSet() {
        Parser anyVowel = characters('a', 'e', 'i', 'o', 'u');

        assertTrue(anyVowel.parse("apple").isSuccess());
        assertTrue(anyVowel.parse("egg").isSuccess());
        assertEquals(success("e", "gg"), anyVowel.parse("egg"));
        assertFalse(anyVowel.parse("xyz").isSuccess());
        assertEquals(failure(""), anyVowel.parse(""));

        // Test with string overload
        Parser digits = characters("0123456789");
        assertTrue(digits.parse("123").isSuccess());
        assertFalse(digits.parse("abc").isSuccess());
    }

    @Test
    void shouldMatchString() {
        Parser string = string("abc");

        assertEquals(success("abc", "def"), string.parse("abcdef"));
        assertEquals(failure("def"), string.parse("abdef"));
        assertEquals(failure(""), string.parse("ab"));
        assertEquals(failure(""), string.parse(""));

        // Test empty string
        assertEquals(success("", "xyz"), string("").parse("xyz"));
    }

    @Test
    void shouldMatchWhitespace() {
        // Test single whitespace characters
        assertEquals(success(" ", ""), whitespace().parse(" "));
        assertEquals(success("\t", ""), whitespace().parse("\t"));
        assertEquals(success("\n", ""), whitespace().parse("\n"));
        assertEquals(success("\r", ""), whitespace().parse("\r"));
        assertEquals(success("\f", ""), whitespace().parse("\f"));

        // Test failure cases
        assertEquals(failure(""), whitespace().parse(""));
        assertEquals(failure("a"), whitespace().parse("a"));

        // Test multiple whitespace characters
        assertEquals(success("  ", "a"), whitespace().parse("  a"));
        assertEquals(success(" \t\n", "x"), whitespace().parse(" \t\nx"));
    }

    @Test
    void shouldMatchAnyWhitespace() {
        // Zero whitespace succeeds
        assertEquals(success("", "abc"), anyWhitespace().parse("abc"));

        // Multiple whitespace succeeds
        assertEquals(success("   ", "abc"), anyWhitespace().parse("   abc"));
        assertEquals(success(" \t\n", "abc"), anyWhitespace().parse(" \t\nabc"));

        // Empty input succeeds with empty match
        assertEquals(success("", ""), anyWhitespace().parse(""));
    }

    @Test
    void shouldMatchCharactersExcept() {
        Parser notA = characterExcept('a');

        assertTrue(notA.parse("b").isSuccess());
        assertEquals(success("b", ""), notA.parse("b"));
        assertFalse(notA.parse("a").isSuccess());
        assertEquals(failure(""), notA.parse(""));

        // Test with multiple excluded characters
        Parser notDigit = characterExcept('0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
        assertTrue(notDigit.parse("a").isSuccess());
        assertFalse(notDigit.parse("5").isSuccess());
    }

}
