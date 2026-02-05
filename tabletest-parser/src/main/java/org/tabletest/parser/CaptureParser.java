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
package org.tabletest.parser;

import org.tabletest.parser.ParseResult.Failure;
import org.tabletest.parser.ParseResult.Success;

import java.util.function.Function;

/**
 * Utility class for creating parsers that capture matched input values.
 */
public class CaptureParser {

    private CaptureParser() {}


    /**
     * Creates a parser that captures the matched value on success as an unquoted string.
     *
     * @param parser base parser to use
     * @return parser that captures matched values
     */
    public static Parser captureUnquoted(Parser parser) {
        return createCaptureFunction(parser, Success::captureTrimmed);
    }

    /**
     * Creates a parser that captures the matched value on success as a quoted string.
     *
     * @param parser base parser to use
     * @param quoteChar the character used for quoting
     * @return parser that captures matched values
     */
    public static Parser captureQuoted(Parser parser, char quoteChar) {
        return createCaptureFunction(parser, success -> success.capture(quoteChar));
    }

    /**
     * Creates a parser that collects captured values to a list.
     *
     * @param parser parser capturing values
     * @return created parser
     */
    public static Parser collectToList(Parser parser) {
        return createCaptureFunction(parser, Success::collectCapturesToList);
    }

    /**
     * Creates a parser that collects captured values to a set.
     *
     * @param parser parser capturing values
     * @return created parser
     */
    public static Parser collectToSet(Parser parser) {
        return createCaptureFunction(parser, Success::collectCapturesToSet);
    }

    /**
     * Creates a parser that collects captured key-value pairs to a map.
     *
     * @param parser parser capturing values
     * @return created parser
     */
    public static Parser collectToMap(Parser parser) {
        return createCaptureFunction(parser, Success::collectCapturesToMap);
    }

    private static Parser createCaptureFunction(Parser parser, Function<Success, Success> function) {
        return input -> switch (parser.parse(input)) {
            case Failure ignored -> ignored;
            case Success success -> function.apply(success);
        };
    }
}
