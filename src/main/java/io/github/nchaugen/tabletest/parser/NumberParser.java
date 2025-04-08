package io.github.nchaugen.tabletest.parser;

import java.util.stream.IntStream;

import static io.github.nchaugen.tabletest.parser.CombinationParser.atLeast;
import static io.github.nchaugen.tabletest.parser.CombinationParser.either;
import static io.github.nchaugen.tabletest.parser.CombinationParser.sequence;

public class NumberParser {

    public static Parser digit() {
        return StringParser.characters("0123456789");
    }

    public static Parser integer() {
        return atLeast(1, digit());
    }

    public static Parser decimal() {
        return sequence(integer(), StringParser.character('.'), integer());
    }

    public static Parser number() {
        return either(decimal(), integer());
    }

    public static Parser digit(int fromInclusive, int toInclusive) {
        return either(
            IntStream.rangeClosed(fromInclusive, toInclusive)
                .mapToObj(digit -> StringParser.character(Character.forDigit(digit, 10)))
                .toArray(Parser[]::new)
        );
    }
}
