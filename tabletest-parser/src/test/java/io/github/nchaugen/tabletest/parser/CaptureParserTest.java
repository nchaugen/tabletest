package io.github.nchaugen.tabletest.parser;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.nchaugen.tabletest.parser.CaptureParser.*;
import static io.github.nchaugen.tabletest.parser.CombinationParser.*;
import static io.github.nchaugen.tabletest.parser.ParseAssertions.assertParseFailure;
import static io.github.nchaugen.tabletest.parser.ParseAssertions.assertParseSuccess;
import static java.util.Collections.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CaptureParserTest {

    private static final Parser DIGIT = StringParser.characters("0123456789");
    private static final Parser NON_DIGIT = StringParser.characterExcept("0123456789");
    private static final Parser WHITESPACE = StringParser.characters(" \t");

    @Test
    void successfully_parsed_values_are_captured_even_if_empty() {
        assertAll(
            () -> assertParseSuccess("23", List.of("1"), capture(optional(DIGIT)).parse("123")),
            () -> assertParseSuccess("", List.of(""), capture(optional(DIGIT)).parse("")),
            () -> assertParseSuccess("abc", List.of(""), capture(optional(DIGIT)).parse("abc"))
        );
    }

    @Test
    void unsuccessfully_parsed_values_are_not_captured() {
        assertAll(
            () -> assertParseSuccess("23", List.of("1"), capture(DIGIT).parse("123")),
            () -> assertParseFailure("", List.of(), capture(DIGIT).parse("")),
            () -> assertParseFailure("abc", List.of(), capture(DIGIT).parse("abc"))
        );
    }

    @Test
    void capturing_optional_vs_optionally_capturing() {
        assertAll(
            () -> assertParseSuccess("", List.of(""), capture(optional(DIGIT)).parse("")),
            () -> assertParseSuccess("", List.of(), optional(capture(DIGIT)).parse(""))
        );
    }

    @Test
    void capturing_multiple_values() {
        assertAll(
            () -> assertParseSuccess("", List.of("1", "2", "3"), zeroOrMore(capture(DIGIT)).parse("123")),
            () -> assertParseSuccess("", List.of(), zeroOrMore(capture(DIGIT)).parse(""))
        );
    }

    @Test
    void capturing_trimmed_values_removes_whitespace() {
        Parser spaceousDigit = sequence(zeroOrMore(WHITESPACE), optional(DIGIT), zeroOrMore(WHITESPACE));
        assertAll(
            () -> assertParseSuccess("", List.of("1"), captureTrimmed(spaceousDigit).parse("1")),
            () -> assertParseSuccess("", List.of("2"), captureTrimmed(spaceousDigit).parse("\t2")),
            () -> assertParseSuccess("", List.of("3"), captureTrimmed(spaceousDigit).parse("3\t")),
            () -> assertParseSuccess("", List.of("4"), captureTrimmed(spaceousDigit).parse(" 4 "))
        );
    }

    @Test
    void unsuccessfully_parsed_values_are_not_captured_trimmed() {
        assertAll(
            () -> assertParseSuccess("23", List.of("1"), captureTrimmed(DIGIT).parse("123")),
            () -> assertParseFailure("", List.of(), captureTrimmed(DIGIT).parse("")),
            () -> assertParseFailure("abc", List.of(), captureTrimmed(DIGIT).parse("abc"))
        );
    }

    @Test
    void capturing_trimmed_saves_empty_and_blank_values_as_null() {
        Parser optionalNonTrimmedDigit = zeroOrMore(WHITESPACE);
        assertAll(
            () -> assertParseSuccess("", singletonList(null), captureTrimmed(optionalNonTrimmedDigit).parse("")),
            () -> assertParseSuccess("", singletonList(null), captureTrimmed(optionalNonTrimmedDigit).parse(" \t ")),
            () -> assertParseSuccess("", singletonList(null), captureTrimmed(optionalNonTrimmedDigit).parse("   "))
        );
    }

    @Test
    void collect_captures_to_list() {
        assertAll(
            () -> assertParseSuccess(
                "",
                List.of(List.of("1", "2", "3")),
                collectToList(zeroOrMore(capture(DIGIT))).parse("123")
            ),
            () -> assertParseSuccess(
                "",
                List.of(emptyList()),
                collectToList(zeroOrMore(capture(DIGIT))).parse("")
            ),
            () -> assertParseSuccess(
                "",
                List.of(List.of("")),
                collectToList(capture(zeroOrMore(DIGIT))).parse("")
            ),
            () -> assertThrows(
                TableTestParseException.class,
                () -> collectToList(captureTrimmed(zeroOrMore(DIGIT))).parse(""),
                "Null values cannot be collected to a list"
            )
        );
    }

    @Test
    void collect_captures_to_set() {
        assertAll(
            () -> assertParseSuccess(
                "",
                List.of(Set.of("1", "2", "3")),
                collectToSet(zeroOrMore(capture(DIGIT))).parse("123")
            ),
            () -> assertParseSuccess(
                "",
                List.of(emptySet()),
                collectToSet(zeroOrMore(capture(DIGIT))).parse("")
            ),
            () -> assertParseSuccess(
                "",
                List.of(Set.of("")),
                collectToSet(capture(zeroOrMore(DIGIT))).parse("")
            ),
            () -> assertThrows(
                TableTestParseException.class,
                () -> collectToSet(captureTrimmed(zeroOrMore(DIGIT))).parse(""),
                "Null values cannot be collected to a set"
            )
        );
    }

    @Test
    void collect_captures_to_map() {
        assertAll(
            () -> assertParseSuccess(
                "",
                List.of(Map.of("1", "2", "3", "4")),
                collectToMap(zeroOrMore(capture(DIGIT))).parse("1234")
            ),
            () -> assertParseSuccess(
                "",
                List.of(emptyMap()),
                collectToMap(zeroOrMore(capture(DIGIT))).parse("")
            ),
            () -> assertParseSuccess(
                "",
                List.of(Map.of("1", "")),
                collectToMap(sequence(capture(DIGIT), capture(zeroOrMore(DIGIT)))).parse("1")
            ),
            () -> assertParseSuccess(
                "",
                List.of(Map.of("", "1")),
                collectToMap(sequence(capture(optional(NON_DIGIT)), capture(DIGIT))).parse("1")
            ),
            () -> assertThrows(
                TableTestParseException.class,
                () -> collectToMap(zeroOrMore(capture(DIGIT))).parse("123"),
                "Uneven number of captures cannot be collected to a map"
            ),
            () -> assertThrows(
                TableTestParseException.class,
                () -> collectToMap(sequence(capture(DIGIT), captureTrimmed(zeroOrMore(DIGIT)))).parse("1"),
                "Null values cannot be collected to a map"
            )
        );
    }

}
