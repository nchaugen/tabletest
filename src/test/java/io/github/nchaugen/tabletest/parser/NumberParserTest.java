package io.github.nchaugen.tabletest.parser;

import org.junit.jupiter.api.Test;

import static io.github.nchaugen.tabletest.parser.NumberParser.decimal;
import static io.github.nchaugen.tabletest.parser.NumberParser.digit;
import static io.github.nchaugen.tabletest.parser.NumberParser.integer;
import static io.github.nchaugen.tabletest.parser.NumberParser.number;
import static io.github.nchaugen.tabletest.parser.ParseResult.failure;
import static io.github.nchaugen.tabletest.parser.ParseResult.success;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NumberParserTest {

    @Test
    void shouldMatchSingleDigits() {
        assertTrue(digit().parse("123").isSuccess());
        assertTrue(digit().parse("911").isSuccess());
        assertFalse(digit().parse("A1").isSuccess());
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

}
