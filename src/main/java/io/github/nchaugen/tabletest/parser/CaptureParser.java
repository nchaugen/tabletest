package io.github.nchaugen.tabletest.parser;

import static io.github.nchaugen.tabletest.parser.ParseResult.failure;

public class CaptureParser {

    public static Parser capture(Parser parser) {
        return input -> switch (parser.parse(input)) {
            case ParseResult.Failure ignored -> failure();
            case ParseResult.Success success -> success.capture();
        };
    }

    public static Parser captureElements(Parser parser) {
        return input -> switch (parser.parse(input)) {
            case ParseResult.Failure ignored -> failure();
            case ParseResult.Success success -> success.captureGroup();
        };
    }

    public static Parser captureNamedElements(Parser parser) {
        return input -> switch (parser.parse(input)) {
            case ParseResult.Failure ignored -> failure();
            case ParseResult.Success success -> success.captureNamedElements();
        };
    }
}
