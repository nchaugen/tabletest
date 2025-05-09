package io.github.nchaugen.tabletest.parser;

import io.github.nchaugen.tabletest.parser.ParseResult.Failure;
import io.github.nchaugen.tabletest.parser.ParseResult.Success;

import static io.github.nchaugen.tabletest.parser.ParseResult.failure;

/**
 * Utility class for creating parsers that capture matched input values.
 */
public class CaptureParser {

    private CaptureParser() {}

    /**
     * Creates a parser that captures the matched value on success.
     *
     * @param parser base parser to use
     * @return parser that captures matched values
     */
    public static Parser capture(Parser parser) {
        return input -> switch (parser.parse(input)) {
            case Failure ignored -> failure();
            case Success success -> success.capture();
        };
    }

    /**
     * Creates a parser that captures a matched group of values as a list.
     *
     * @param parser base parser to use
     * @return parser that captures values as a group
     */
    public static Parser captureElements(Parser parser) {
        return input -> switch (parser.parse(input)) {
            case Failure ignored -> failure();
            case Success success -> success.captureGroup();
        };
    }

    /**
     * Creates a parser that captures key-value pairs as a map.
     *
     * @param parser base parser to use
     * @return parser that captures named elements as a map
     */
    public static Parser captureNamedElements(Parser parser) {
        return input -> switch (parser.parse(input)) {
            case Failure ignored -> failure();
            case Success success -> success.captureNamedElements();
        };
    }
}
