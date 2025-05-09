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

import static io.github.nchaugen.tabletest.parser.CaptureParser.capture;
import static io.github.nchaugen.tabletest.parser.CaptureParser.captureElements;
import static io.github.nchaugen.tabletest.parser.CaptureParser.captureNamedElements;
import static io.github.nchaugen.tabletest.parser.CombinationParser.atLeast;
import static io.github.nchaugen.tabletest.parser.CombinationParser.either;
import static io.github.nchaugen.tabletest.parser.CombinationParser.optional;
import static io.github.nchaugen.tabletest.parser.CombinationParser.sequence;
import static io.github.nchaugen.tabletest.parser.CombinationParser.zeroOrMore;
import static io.github.nchaugen.tabletest.parser.Parser.forwardRef;
import static io.github.nchaugen.tabletest.parser.StringParser.anyWhitespace;
import static io.github.nchaugen.tabletest.parser.StringParser.character;
import static io.github.nchaugen.tabletest.parser.StringParser.characterExcept;
import static io.github.nchaugen.tabletest.parser.StringParser.string;

/**
 * Parser for TableTest format rows. Handles cells containing single values, lists, and maps.
 */
public class RowParser {

    private RowParser() {}

    /**
     * Parses a string representing a TableTest row with pipe-separated cells.
     *
     * @param input string to parse
     * @return parse result containing captured cells
     */
    public static ParseResult parse(String input) {
        return PARSER.parse(input);
    }

    /**
     * Main parser for processing input lines, handling both comments and data rows.
     */
    private static final Parser PARSER = line();

    private static Parser line() {
        return either(comment(), row());
    }

    private static Parser comment() {
        return sequence(
            anyWhitespace(),
            string("//"),
            zeroOrMore(characterExcept('\n'))
        );
    }

    /**
     * Creates a parser for rows with pipe-separated cells.
     *
     * @return parser for table rows
     */
    static Parser row() {
        return entries(cell(), character('|'));
    }

    /**
     * Creates a parser for a single cell with surrounding whitespace.
     * Cell values can be in one of three forms: a map value, a list value, or a single value.
     *
     * @return parser for table cells
     */
    static Parser cell() {
        return sequence(anyWhitespace(), value(), anyWhitespace());
    }

    private static Parser value() {
        return either(mapValue(), listValue(), singleValue());
    }

    /**
     * Creates a parser for map values enclosed in square brackets.
     * Maps can be empty or contain key-value pairs.
     *
     * @return parser for map values
     */
    static Parser mapValue() {
        return sequence(
            character('['),
            captureNamedElements(either(emptyMapValue(), keyValuePairs())),
            character(']')
        );
    }

    private static Parser emptyMapValue() {
        return sequence(anyWhitespace(), character(':'), anyWhitespace());
    }

    private static Parser keyValuePairs() {
        return entries(keyValuePair(), character(','));
    }

    private static Parser keyValuePair() {
        return sequence(mapKey(), character(':'), elementValue());
    }

    private static Parser mapKey() {
        return sequence(anyWhitespace(), capture(mapKeyName()), anyWhitespace());
    }

    private static Parser mapKeyName() {
        return atLeast(1, characterExcept(',', '[', ']', ':'));
    }

    /**
     * Creates a parser for list values enclosed in square brackets.
     * Lists can be empty or contain comma-separated elements.
     *
     * @return parser for list values
     */
    static Parser listValue() {
        return sequence(
            character('['),
            captureElements(optional(elementValues())),
            character(']')
        );
    }

    private static Parser elementValues() {
        return entries(elementValue(), character(','));
    }

    private static Parser elementValue() {
        return sequence(
            anyWhitespace(),
            either(
                forwardRef(RowParser::mapValue),
                forwardRef(RowParser::listValue),
                either(
                    singleQuotedValue(),
                    doubleQuotedValue(),
                    capture(atLeast(1, characterExcept(',', ']')))
                )
            ),
            anyWhitespace()
        );
    }

    private static Parser entries(Parser entry, Parser separator) {
        return sequence(entry, zeroOrMore(sequence(separator, entry)));
    }

    /**
     * Creates a parser for single values in three formats:
     * single-quoted, double-quoted, or unquoted.
     *
     * @return parser for single values
     */
    static Parser singleValue() {
        return either(singleQuotedValue(), doubleQuotedValue(), unquotedValue());
    }

    private static Parser singleQuotedValue() {
        return sequence(
            character('\''),
            capture(zeroOrMore(characterExcept('\''))),
            character('\'')
        );
    }

    private static Parser doubleQuotedValue() {
        return sequence(
            character('"'),
            capture(zeroOrMore(characterExcept('"'))),
            character('"')
        );
    }

    private static Parser unquotedValue() {
        return capture(
            optional(
                sequence(
                    characterExcept('[', '|', ','),
                    zeroOrMore(characterExcept('|'))
                )
            )
        );
    }

}
