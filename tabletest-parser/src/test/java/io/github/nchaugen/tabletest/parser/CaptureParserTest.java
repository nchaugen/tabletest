package io.github.nchaugen.tabletest.parser;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.nchaugen.tabletest.parser.CaptureParser.captureQuoted;
import static io.github.nchaugen.tabletest.parser.CaptureParser.captureUnquoted;
import static io.github.nchaugen.tabletest.parser.CaptureParser.collectToList;
import static io.github.nchaugen.tabletest.parser.CaptureParser.collectToMap;
import static io.github.nchaugen.tabletest.parser.CaptureParser.collectToSet;
import static io.github.nchaugen.tabletest.parser.CombinationParser.optional;
import static io.github.nchaugen.tabletest.parser.CombinationParser.sequence;
import static io.github.nchaugen.tabletest.parser.CombinationParser.zeroOrMore;
import static io.github.nchaugen.tabletest.parser.ParseAssertions.assertParseFailure;
import static io.github.nchaugen.tabletest.parser.ParseAssertions.assertParseSuccess;
import static io.github.nchaugen.tabletest.parser.StringValue.doubleQuoted;
import static io.github.nchaugen.tabletest.parser.StringValue.singleQuoted;
import static io.github.nchaugen.tabletest.parser.StringValue.unquoted;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CaptureParserTest {

    private static final Parser DIGIT = StringParser.characters("0123456789");
    private static final Parser NON_DIGIT = StringParser.characterExcept("0123456789");
    private static final Parser WHITESPACE = StringParser.characters(" \t");

    @Test
    void successfully_parsed_quoted_values_are_captured_even_if_empty() {
        assertAll(
            () -> assertParseSuccess("23", List.of(doubleQuoted("1")), captureQuoted(optional(DIGIT), '"').parse("123")),
            () -> assertParseSuccess("", List.of(singleQuoted("")), captureQuoted(optional(DIGIT), '\'').parse("")),
            () -> assertParseSuccess("abc", List.of(doubleQuoted("")), captureQuoted(optional(DIGIT), '"').parse("abc"))
        );
    }

    @Test
    void unsuccessfully_parsed_quoted_values_are_not_captured() {
        assertAll(
            () -> assertParseSuccess("23", List.of(singleQuoted("1")), captureQuoted(DIGIT, '\'').parse("123")),
            () -> assertParseFailure("", List.of(), captureQuoted(DIGIT, '\'').parse("")),
            () -> assertParseFailure("abc", List.of(), captureQuoted(DIGIT, '\'').parse("abc"))
        );
    }

    @Test
    void capturing_optional_vs_optionally_capturing() {
        assertAll(
            () -> assertParseSuccess("", List.of(doubleQuoted("")), captureQuoted(optional(DIGIT), '"').parse("")),
            () -> assertParseSuccess("", List.of(), optional(captureQuoted(DIGIT, '"')).parse(""))
        );
    }

    @Test
    void capturing_multiple_values() {
        assertAll(
            () -> assertParseSuccess("", List.of(doubleQuoted("1"), doubleQuoted("2"), doubleQuoted("3")), zeroOrMore(captureQuoted(DIGIT, '"')).parse("123")),
            () -> assertParseSuccess("", List.of(), zeroOrMore(captureQuoted(DIGIT, '"')).parse(""))
        );
    }

    @Test
    void capturing_unquoted_values_trims_whitespace() {
        Parser spaceousDigit = sequence(zeroOrMore(WHITESPACE), optional(DIGIT), zeroOrMore(WHITESPACE));
        assertAll(
            () -> assertParseSuccess("", List.of(unquoted("1")), captureUnquoted(spaceousDigit).parse("1")),
            () -> assertParseSuccess("", List.of(unquoted("2")), captureUnquoted(spaceousDigit).parse("\t2")),
            () -> assertParseSuccess("", List.of(unquoted("3")), captureUnquoted(spaceousDigit).parse("3\t")),
            () -> assertParseSuccess("", List.of(unquoted("4")), captureUnquoted(spaceousDigit).parse(" 4 "))
        );
    }

    @Test
    void unsuccessfully_parsed_unquoted_values_are_not_captured() {
        assertAll(
            () -> assertParseSuccess("23", List.of(unquoted("1")), captureUnquoted(DIGIT).parse("123")),
            () -> assertParseFailure("", List.of(), captureUnquoted(DIGIT).parse("")),
            () -> assertParseFailure("abc", List.of(), captureUnquoted(DIGIT).parse("abc"))
        );
    }

    @Test
    void capturing_unquoted_saves_empty_and_blank_values_as_null() {
        Parser optionalNonTrimmedDigit = zeroOrMore(WHITESPACE);
        assertAll(
            () -> assertParseSuccess("", singletonList(null), captureUnquoted(optionalNonTrimmedDigit).parse("")),
            () -> assertParseSuccess("", singletonList(null), captureUnquoted(optionalNonTrimmedDigit).parse(" \t ")),
            () -> assertParseSuccess("", singletonList(null), captureUnquoted(optionalNonTrimmedDigit).parse("   "))
        );
    }

    @Test
    void collect_captures_to_list() {
        assertAll(
            () -> assertParseSuccess(
                "",
                List.of(List.of(doubleQuoted("1"), doubleQuoted("2"), doubleQuoted("3"))),
                collectToList(zeroOrMore(captureQuoted(DIGIT, '"'))).parse("123")
            ),
            () -> assertParseSuccess(
                "",
                List.of(emptyList()),
                collectToList(zeroOrMore(captureQuoted(DIGIT, '"'))).parse("")
            ),
            () -> assertParseSuccess(
                "",
                List.of(List.of(doubleQuoted(""))),
                collectToList(captureQuoted(zeroOrMore(DIGIT), '"')).parse("")
            ),
            () -> assertThrows(
                TableTestParseException.class,
                () -> collectToList(captureUnquoted(zeroOrMore(DIGIT))).parse(""),
                "Null values cannot be collected to a list"
            )
        );
    }

    @Test
    void collect_captures_to_set() {
        assertAll(
            () -> assertParseSuccess(
                "",
                List.of(Set.of(singleQuoted("1"), singleQuoted("2"), singleQuoted("3"))),
                collectToSet(zeroOrMore(captureQuoted(DIGIT, '\''))).parse("123")
            ),
            () -> assertParseSuccess(
                "",
                List.of(emptySet()),
                collectToSet(zeroOrMore(captureQuoted(DIGIT, '\''))).parse("")
            ),
            () -> assertParseSuccess(
                "",
                List.of(Set.of(singleQuoted(""))),
                collectToSet(captureQuoted(zeroOrMore(DIGIT), '\'')).parse("")
            ),
            () -> assertThrows(
                TableTestParseException.class,
                () -> collectToSet(captureUnquoted(zeroOrMore(DIGIT))).parse(""),
                "Null values cannot be collected to a set"
            )
        );
    }

    @Test
    void collect_captures_to_map() {
        assertAll(
            () -> assertParseSuccess(
                "",
                List.of(Map.of(doubleQuoted("1"), doubleQuoted("2"), doubleQuoted("3"), doubleQuoted("4"))),
                collectToMap(zeroOrMore(captureQuoted(DIGIT, '"'))).parse("1234")
            ),
            () -> assertParseSuccess(
                "",
                List.of(emptyMap()),
                collectToMap(zeroOrMore(captureQuoted(DIGIT, '"'))).parse("")
            ),
            () -> assertParseSuccess(
                "",
                List.of(Map.of(unquoted("1"), doubleQuoted(""))),
                collectToMap(sequence(captureUnquoted(DIGIT), captureQuoted(zeroOrMore(DIGIT), '"'))).parse("1")
            ),
            () -> assertParseSuccess(
                "",
                List.of(Map.of(doubleQuoted(""), doubleQuoted("1"))),
                collectToMap(sequence(captureQuoted(optional(NON_DIGIT), '"'), captureQuoted(DIGIT, '"'))).parse("1")
            ),
            () -> assertThrows(
                TableTestParseException.class,
                () -> collectToMap(zeroOrMore(captureQuoted(DIGIT, '"'))).parse("123"),
                "Uneven number of captures cannot be collected to a map"
            ),
            () -> assertThrows(
                TableTestParseException.class,
                () -> collectToMap(sequence(captureQuoted(DIGIT, '"'), captureUnquoted(zeroOrMore(DIGIT)))).parse("1"),
                "Null values cannot be collected to a map"
            )
        );
    }

}
