package io.github.nchaugen.tabletest.parser;

import java.util.function.Supplier;

@FunctionalInterface
public interface Parser {
    ParseResult parse(String input);

    static Parser forwardRef(Supplier<Parser> ref) {
        return input -> ref.get().parse(input);
    }
}
