package io.github.nchaugen.tabletest.parser;

import static io.github.nchaugen.tabletest.parser.CaptureParser.capture;
import static io.github.nchaugen.tabletest.parser.CaptureParser.captureElements;
import static io.github.nchaugen.tabletest.parser.CaptureParser.captureNamedElements;
import static io.github.nchaugen.tabletest.parser.CombinationParser.atLeast;
import static io.github.nchaugen.tabletest.parser.CombinationParser.either;
import static io.github.nchaugen.tabletest.parser.CombinationParser.optional;
import static io.github.nchaugen.tabletest.parser.CombinationParser.sequence;
import static io.github.nchaugen.tabletest.parser.Parser.forwardRef;
import static io.github.nchaugen.tabletest.parser.StringParser.anyWhitespace;
import static io.github.nchaugen.tabletest.parser.StringParser.character;
import static io.github.nchaugen.tabletest.parser.StringParser.characterExcept;
import static io.github.nchaugen.tabletest.parser.StringParser.characterExceptNonEscaped;
import static io.github.nchaugen.tabletest.parser.StringParser.string;

public class RowParser {

    public static ParseResult parse(String input) {
        return PARSER.parse(input);
    }

    private static final Parser PARSER = line();

    static Parser line() {
        return either(comment(), row());
    }

    static Parser comment() {
        return sequence(
            anyWhitespace(),
            string("//"),
            atLeast(0, characterExceptNonEscaped('\n'))
        );
    }

    static Parser row() {
        return entries(cell(), character('|'));
    }

    static Parser cell() {
        return sequence(anyWhitespace(), anyValue(), anyWhitespace());
    }

    static Parser anyValue() {
        return either(mapValue(), listValue(), singleValue());
    }

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
        return sequence(entry, atLeast(0, sequence(separator, entry)));
    }

    static Parser singleValue() {
        return either(singleQuotedValue(), doubleQuotedValue(), unquotedValue());
    }

    private static Parser singleQuotedValue() {
        return sequence(
            character('\''),
            capture(atLeast(0, characterExceptNonEscaped('\''))),
            character('\'')
        );
    }

    private static Parser doubleQuotedValue() {
        return sequence(
            character('"'),
            capture(atLeast(0, characterExceptNonEscaped('"'))),
            character('"')
        );
    }

    private static Parser unquotedValue() {
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
