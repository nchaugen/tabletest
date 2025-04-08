package io.github.nchaugen.tabletest.parser;

import io.github.nchaugen.tabletest.parser.ParseResult.Failure;
import io.github.nchaugen.tabletest.parser.ParseResult.Success;

import java.util.Arrays;

import static io.github.nchaugen.tabletest.parser.CombinationParser.atLeast;
import static io.github.nchaugen.tabletest.parser.CombinationParser.either;
import static io.github.nchaugen.tabletest.parser.CombinationParser.sequence;
import static io.github.nchaugen.tabletest.parser.ParseResult.failure;
import static io.github.nchaugen.tabletest.parser.ParseResult.success;

public class StringParser {

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
            .map(StringParser::character)
            .toArray(Parser[]::new);
    }

    public static Parser string(String str) {
        return sequence(eachOf(str));
    }

    public static Parser whitespace() {
        return atLeast(1, characters(" \t\n\r\f"));
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

    public static Parser quotedValue(char quote) {
        return sequence(
            character(quote),
            atLeast(0, characterExceptNonEscaped(quote)),
            character(quote)
        );
    }

    public static Parser escapedCharacter(char... cs) {
        return sequence(character(ESCAPE_CHARACTER), characters(cs));
    }

    public static Parser surroundedValue(char opening, char closing) {
        return sequence(
            character(opening),
            atLeast(0, characterExceptNonEscaped(opening, closing)),
            character(closing)
        );
    }

    public static Parser anyWhitespace() {
        return atLeast(0, whitespace());
    }
}
