package io.github.nchaugen.tabletest.parser;

import io.github.nchaugen.tabletest.parser.ParseResult.Failure;
import io.github.nchaugen.tabletest.parser.ParseResult.Success;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static io.github.nchaugen.tabletest.parser.ParseResult.failure;
import static io.github.nchaugen.tabletest.parser.ParseResult.success;

public class ParserCombinators {

    public static final char ESCAPE_CHARACTER = '\\';

    public static Parser character(char c) {
        return character(String.valueOf(c));
    }

    private static Parser character(String c) {
        return input -> input.startsWith(c)
                        ? success(c, input.substring(1))
                        : failure();
    }

    public static Parser characters(char... anyOf) {
        return characters(new String(anyOf));
    }

    public static Parser characters(String anyOf) {
        return either(eachOf(anyOf));
    }

    private static Parser[] eachOf(String cs) {
        return Arrays.stream(cs.split(""))
            .map(ParserCombinators::character)
            .toArray(Parser[]::new);
    }

    public static Parser either(Parser... parsers) {
        return input -> Arrays.stream(parsers)
            .map(it -> it.parse(input))
            .filter(ParseResult::isSuccess)
            .findFirst()
            .orElse(failure());
    }

    public static Parser digit() {
        return characters("0123456789");
    }

    public static Parser string(String str) {
        return sequence(eachOf(str));
    }

    public static Parser sequence(Parser... parsers) {
        return input -> Arrays.stream(parsers)
            .reduce(
                success("", input),
                (result, parser) -> result.append(() -> parser.parse(result.rest())),
                ParserCombinators::unsupportedCombine
            );
    }

    public static Parser atLeast(int n, Parser parser) {
        return input -> atLeast(n, parser, success("", input));
    }

    private static ParseResult atLeast(int n, Parser parser, ParseResult result) {
        return switch (parser.parse(result.rest())) {
            case Failure failure -> n < 1 ? result : failure;
            case Success success -> atLeast(n - 1, parser, result.append(() -> success));
        };
    }

    private static ParseResult unsupportedCombine(ParseResult a, ParseResult b) {
        throw new UnsupportedOperationException();
    }

    public static Parser optional(Parser parser) {
        return input -> switch (parser.parse(input)) {
            case Success success -> success;
            case Failure ignored -> success("", input);
        };
    }

    public static Parser integer() {
        return atLeast(1, digit());
    }

    public static Parser decimal() {
        return sequence(integer(), character('.'), integer());
    }

    public static Parser number() {
        return either(decimal(), integer());
    }

    public static Parser capture(Parser parser) {
        return input -> switch (parser.parse(input)) {
            case Failure ignored -> failure();
            case Success success -> success.capture();
        };
    }

    public static Parser captureElements(Parser parser) {
        return input -> switch (parser.parse(input)) {
            case Failure ignored -> failure();
            case Success success -> success.captureGroup();
        };
    }

    public static Parser captureNamedElements(Parser parser) {
        return input -> switch (parser.parse(input)) {
            case Failure ignored -> failure();
            case Success success -> success.captureNamedElements();
        };
    }

    public static Parser whitespace() {
        return atLeast(1, characters(" \t\n\r\f"));
    }

    public static Parser digit(int fromInclusive, int toInclusive) {
        return either(
            IntStream.rangeClosed(fromInclusive, toInclusive)
                .mapToObj(digit -> character(Character.forDigit(digit, 10)))
                .toArray(Parser[]::new)
        );
    }

    public static Parser characterExcept(char... cs) {
        return input -> switch (characters(cs).parse(input)) {
            case Success ignored -> failure();
            case Failure ignored -> input.isEmpty()
                                    ? failure()
                                    : success(String.valueOf(input.charAt(0)), input.substring(1));
        };
    }

    public static Parser characterExceptNonEscaped(char c) {
        return either(escapedCharacter(c), characterExcept(c));
    }

    public static Parser characterExceptNonEscaped(char... cs) {
        return either(escapedCharacter(cs), characterExcept(cs));
    }

    public static Parser quotedString(char quote) {
        return sequence(
            character(quote),
            atLeast(0, characterExceptNonEscaped(quote)),
            character(quote)
        );
    }

    public static Parser escapedCharacter(char... cs) {
        return sequence(character(ESCAPE_CHARACTER), characters(cs));
    }

    public static Parser surroundedString(char opening, char closing) {
        return sequence(
            character(opening),
            atLeast(0, characterExceptNonEscaped(opening, closing)),
            character(closing)
        );
    }

    public static Parser forwardRef(Supplier<Parser> ref) {
        return input -> ref.get().parse(input);
    }

    public static Parser anyWhitespace() {
        return atLeast(0, whitespace());
    }
}
