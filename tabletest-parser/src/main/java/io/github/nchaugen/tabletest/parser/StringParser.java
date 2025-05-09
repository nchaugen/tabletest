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

import static io.github.nchaugen.tabletest.parser.CombinationParser.atLeast;
import static io.github.nchaugen.tabletest.parser.CombinationParser.either;
import static io.github.nchaugen.tabletest.parser.CombinationParser.sequence;
import static io.github.nchaugen.tabletest.parser.CombinationParser.zeroOrMore;
import static io.github.nchaugen.tabletest.parser.ParseResult.failure;
import static io.github.nchaugen.tabletest.parser.ParseResult.success;

/**
 * Provides utility methods for string parsing with pattern matching.
 * Supports character matching, string matching, whitespace handling, and escape sequences.
 */
public class StringParser {

    private StringParser() {}

    /**
     * Creates a parser matching a specific character.
     *
     * @param c character to match
     * @return parser that succeeds if input starts with specified character
     */
    public static Parser character(char c) {
        return character(String.valueOf(c));
    }

    private static Parser character(String c) {
        return input -> input.startsWith(c)
                        ? success(c, input.substring(1))
                        : failure();
    }

    /**
     * Creates a parser matching any of the given characters.
     *
     * @param anyOf possibly characters to match
     * @return parser that succeeds if input starts with any of the given characters
     */
    public static Parser characters(char... anyOf) {
        return characters(new String(anyOf));
    }

    /**
     * Creates a parser matching any of the given characters.
     *
     * @param anyOf possible characters to match
     * @return parser that succeeds if input starts with any of the given characters
     */
    public static Parser characters(String anyOf) {
        return either(eachOf(anyOf));
    }

    private static Parser[] eachOf(String cs) {
        return Arrays.stream(cs.split(""))
            .filter(it -> !it.isEmpty())
            .map(StringParser::character)
            .toArray(Parser[]::new);
    }

    /**
     * Creates a parser matching any character except the given ones.
     *
     * @param noneOf characters not to match
     * @return parser that succeeds if input starts with a character not among the given characters
     */
    public static Parser characterExcept(char... noneOf) {
        return input -> switch (characters(noneOf).parse(input)) {
            case Success ignored -> failure();
            case Failure ignored -> input.isEmpty()
                                    ? failure()
                                    : success(String.valueOf(input.charAt(0)), input.substring(1));
        };
    }

    /**
     * Creates a parser matching an entire given string.
     *
     * @param str the string to match
     * @return parser that succeeds if input starts with the specified string
     */
    public static Parser string(String str) {
        return sequence(eachOf(str));
    }

    /**
     * Creates a parser matching one or more whitespace character, including space, tab,
     * newline, carriage return, or form feed.
     *
     * @return parser that succeeds if input starts with at least one whitespace character
     */
    public static Parser whitespace() {
        return atLeast(1, characters(" \t\n\r\f"));
    }

    /**
     * Creates a parser matching zero or more whitespace characters, including space, tab,
     * newline, carriage return, or form feed.
     *
     * @return parser that succeeds if input starts with any number of whitespace characters
     */
    public static Parser anyWhitespace() {
        return zeroOrMore(whitespace());
    }

}
