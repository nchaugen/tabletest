package io.github.nchaugen.tabletest.parser;

import org.junit.jupiter.api.Test;

import java.util.List;

import static io.github.nchaugen.tabletest.parser.CaptureParser.capture;
import static io.github.nchaugen.tabletest.parser.CombinationParser.sequence;
import static io.github.nchaugen.tabletest.parser.NumberParser.integer;
import static io.github.nchaugen.tabletest.parser.StringParser.character;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CaptureParserTest {

    @Test
    void shouldCaptureMatchedValues() {
        Parser plus = sequence(
            capture(integer()),
            character('+'),
            capture(integer())
        );

        assertEquals(List.of("1", "2"), plus.parse("1+2").captures());
    }

}
