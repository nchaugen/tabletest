/*
 * Copyright 2025-present Nils Christian Haugen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.nchaugen.tabletest.parser;

import io.github.nchaugen.tabletest.parser.ParseResult.Failure;
import io.github.nchaugen.tabletest.parser.ParseResult.Success;

import java.util.Arrays;

import static io.github.nchaugen.tabletest.parser.ParseResult.failure;
import static io.github.nchaugen.tabletest.parser.ParseResult.success;

/**
 * Provides combinators for creating complex parsers from simpler ones.
 */
public class CombinationParser {

    private CombinationParser() {}

    /**
     * Creates a parser that applies multiple parsers and succeeds if any one succeeds.
     * Tries parsers in order, returning on first success or failing if all fail.
     *
     * @param parsers alternative parsers to try
     * @return a parser requiring at least one component parser to succeed
     */
    public static Parser either(Parser... parsers) {
        return input -> Arrays.stream(parsers)
            .map(it -> it.parse(input))
            .filter(ParseResult::isSuccess)
            .findFirst()
            .orElse(failure());
    }

    /**
     * Creates a parser that applies parsers in sequence, succeeding only if all parsers succeed.
     *
     * @param parsers sequence of parsers to apply
     * @return a parser requiring all component parsers to succeed
     */
    public static Parser sequence(Parser... parsers) {
        return input -> Arrays.stream(parsers)
            .reduce(
                success("", input),
                (result, parser) -> result.append(() -> parser.parse(result.rest())),
                CombinationParser::unsupportedCombine
            );
    }

    /**
     * Creates a parser that makes the provided parser repeatable zero or more times.
     * Equivalent to atLeast(0, parser).
     *
     * @param parser the parser to repeat
     * @return a parser that applies the component parser zero or more times
     */
    public static Parser zeroOrMore(Parser parser) {
        return atLeast(0, parser);
    }

    /**
     * Creates a parser that applies the provided parser at least n times.
     *
     * @param n minimum number of repetitions required
     * @param parser parser to repeat
     * @return a parser that applies the component parser repeatedly
     */
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

    /**
     * Creates a parser that makes the provided parser optional.
     * Always succeeds, with or without consuming input.
     *
     * @param parser the parser to make optional
     * @return a parser that always succeeds, with or without consuming input
     */
    public static Parser optional(Parser parser) {
        return input -> switch (parser.parse(input)) {
            case Success success -> success;
            case Failure ignored -> success("", input);
        };
    }
}
