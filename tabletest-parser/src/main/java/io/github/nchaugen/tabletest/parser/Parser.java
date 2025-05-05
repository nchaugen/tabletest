package io.github.nchaugen.tabletest.parser;

import java.util.function.Supplier;

/**
 * Represents a functional interface for defining parsers that process an input string
 * and return a result encapsulated in a {@link ParseResult}.
 * <p>
 * Parsers are designed to match a portion of the input against a particular rule or pattern,
 * and produce a result that indicates whether the parsing operation succeeded or failed.
 */
@FunctionalInterface
public interface Parser {

    /**
     * Parses the given input string and attempts to match it against a predefined rule
     * or pattern, producing a parsing result.
     *
     * @param input the input string to be parsed; must not be null
     * @return a {@link ParseResult} object representing the outcome of the parsing operation.
     *         This can be either a successful result with matched content and remaining input,
     *         or a failure result indicating that the parsing operation was unsuccessful.
     */
    ParseResult parse(String input);

    /**
     * Creates a {@link Parser} that defers its parsing work to another {@link Parser} provided
     * by a {@link Supplier}. This is particularly useful for creating parsers that refer to
     * themselves or other parsers in mutually recursive ways.
     *
     * @param ref a {@link Supplier} that provides the {@link Parser} to be deferred to. The
     *            supplier must return a non-null {@link Parser} when invoked.
     * @return a {@link Parser} that, when executed, retrieves a {@link Parser} from the
     *            supplier and uses it to parse the input.
     */
    static Parser forwardRef(Supplier<Parser> ref) {
        return input -> ref.get().parse(input);
    }
}
