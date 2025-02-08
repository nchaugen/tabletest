package io.github.nchaugen.tabletest.parser;

@FunctionalInterface
public interface Parser {
    ParseResult parse(String input);
}
