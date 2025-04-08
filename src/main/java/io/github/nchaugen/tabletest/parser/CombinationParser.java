package io.github.nchaugen.tabletest.parser;

import java.util.Arrays;

import static io.github.nchaugen.tabletest.parser.ParseResult.failure;
import static io.github.nchaugen.tabletest.parser.ParseResult.success;

public class CombinationParser {

    public static Parser either(Parser... parsers) {
        return input -> Arrays.stream(parsers)
            .map(it -> it.parse(input))
            .filter(ParseResult::isSuccess)
            .findFirst()
            .orElse(failure());
    }

    public static Parser sequence(Parser... parsers) {
        return input -> Arrays.stream(parsers)
            .reduce(
                success("", input),
                (result, parser) -> result.append(() -> parser.parse(result.rest())),
                CombinationParser::unsupportedCombine
            );
    }

    public static Parser atLeast(int n, Parser parser) {
        return input -> atLeast(n, parser, success("", input));
    }

    private static ParseResult atLeast(int n, Parser parser, ParseResult result) {
        return switch (parser.parse(result.rest())) {
            case ParseResult.Failure failure -> n < 1 ? result : failure;
            case ParseResult.Success success -> atLeast(n - 1, parser, result.append(() -> success));
        };
    }

    private static ParseResult unsupportedCombine(ParseResult a, ParseResult b) {
        throw new UnsupportedOperationException();
    }

    public static Parser optional(Parser parser) {
        return input -> switch (parser.parse(input)) {
            case ParseResult.Success success -> success;
            case ParseResult.Failure ignored -> success("", input);
        };
    }
}
