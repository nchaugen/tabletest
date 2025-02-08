package io.github.nchaugen.tabletest.parser;

import static io.github.nchaugen.tabletest.parser.ParserCombinators.anyWhitespace;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.atLeast;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.capture;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.captureElements;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.captureNamedElements;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.character;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.characterExcept;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.characterExceptNonEscaped;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.either;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.forwardRef;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.optional;
import static io.github.nchaugen.tabletest.parser.ParserCombinators.sequence;

public class RowParser {

    public static ParseResult parse(String input) {
        return PARSER.parse(input);
    }

    private static final Parser PARSER = row();

    static Parser row() {
        return entries(cell(), character('|'));
    }

    static Parser cell() {
        return sequence(anyWhitespace(), cellValue(), anyWhitespace());
    }

    static Parser cellValue() {
        return either(map(), list(), string());
    }

    static Parser map() {
        return sequence(
            character('['),
            captureNamedElements(either(emptyMap(), mapEntries())),
            character(']')
        );
    }

    private static Parser emptyMap() {
        return sequence(anyWhitespace(), character(':'), anyWhitespace());
    }

    private static Parser mapEntries() {
        return entries(mapEntry(), character(','));
    }

    private static Parser mapEntry() {
        return sequence(mapKey(), character(':'), elementValue());
    }

    private static Parser mapKey() {
        return sequence(anyWhitespace(), capture(keyName()), anyWhitespace());
    }

    private static Parser keyName() {
        return atLeast(1, characterExcept(',', '[', ']', ':'));
    }

    static Parser list() {
        return sequence(
            character('['),
            captureElements(optional(listEntries())),
            character(']')
        );
    }

    private static Parser listEntries() {
        return entries(elementValue(), character(','));
    }

    private static Parser elementValue() {
        return sequence(
            anyWhitespace(),
            either(
                forwardRef(RowParser::map),
                forwardRef(RowParser::list),
                either(
                    quotedString(),
                    capture(atLeast(1, characterExcept(',', ']')))
                )
            ),
            anyWhitespace()
        );
    }

    private static Parser entries(Parser entry, Parser separator) {
        return sequence(entry, atLeast(0, sequence(separator, entry)));
    }

    static Parser string() {
        return either(quotedString(), unquotedString());
    }

    private static Parser quotedString() {
        return sequence(
            character('"'),
            capture(atLeast(0, characterExceptNonEscaped('"'))),
            character('"')
        );
    }

    private static Parser unquotedString() {
        return capture(
            optional(
                sequence(
                    characterExceptNonEscaped('[', '|', ','),
                    atLeast(0, characterExceptNonEscaped('|'))
                )
            )
        );
    }

}
