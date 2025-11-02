package io.github.nchaugen.tabletest.parser;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.github.nchaugen.tabletest.parser.CaptureParser.*;
import static io.github.nchaugen.tabletest.parser.CombinationParser.atLeast;
import static io.github.nchaugen.tabletest.parser.CombinationParser.optional;
import static io.github.nchaugen.tabletest.parser.CombinationParser.sequence;
import static io.github.nchaugen.tabletest.parser.CombinationParser.zeroOrMore;
import static io.github.nchaugen.tabletest.parser.ParseResult.failure;
import static io.github.nchaugen.tabletest.parser.StringParser.anyWhitespace;
import static io.github.nchaugen.tabletest.parser.StringParser.character;
import static io.github.nchaugen.tabletest.parser.StringParser.characterExcept;
import static io.github.nchaugen.tabletest.parser.StringParser.characters;
import static io.github.nchaugen.tabletest.parser.StringParser.string;
import static org.junit.jupiter.api.Assertions.*;

class CaptureParserTest {

    @Test
    void shouldCaptureMatchedValues() {
        // Simple expression parser example
        Parser plusExpression = sequence(
            capture(atLeast(1, characters("0123456789"))),
            character('+'),
            capture(atLeast(1, characters("0123456789")))
        );

        // Verify capture works correctly
        assertEquals(List.of("123", "456"), plusExpression.parse("123+456").captures());
        assertEquals(List.of("1", "2"), plusExpression.parse("1+2").captures());

        // Verify failure cases
        assertEquals(failure("-2"), plusExpression.parse("1-2"));
        assertEquals(failure("a+b"), plusExpression.parse("a+b"));
        assertEquals(failure(""), plusExpression.parse("1+"));
        assertEquals(failure("+2"), plusExpression.parse("+2"));
    }

    @Test
    void shouldHandleEmptyCaptures() {
        // Test capturing an optional value
        Parser optionalDigit = capture(optional(characters("0123456789")));

        // Should capture the digit when present
        assertEquals(List.of("5"), optionalDigit.parse("5").captures());

        // Should capture an empty string when not present
        assertEquals(List.of(""), optionalDigit.parse("").captures());
    }

    @Test
    void shouldCaptureElementsAsGroup() {
        // Create a list parser
        Parser listParser = sequence(
            character('['),
            captureAsList(
                optional(
                    sequence(
                        capture(atLeast(1, characterExcept(',', ']'))),
                        zeroOrMore(
                            sequence(
                                character(','),
                                anyWhitespace(),
                                capture(atLeast(1, characterExcept(',', ']')))
                            )
                        )
                    )
                )
            ),
            character(']')
        );

        // Test empty list
        assertEquals(List.of(List.of()), listParser.parse("[]").captures());

        // Test single element list
        assertEquals(List.of(List.of("a")), listParser.parse("[a]").captures());

        // Test multi-element list
        assertEquals(List.of(List.of("a", "b", "c")), listParser.parse("[a,b,c]").captures());
        assertEquals(List.of(List.of("a", "b", "c")), listParser.parse("[a, b, c]").captures());

        // Test failure cases
        assertEquals(failure(",]"), listParser.parse("[a,]"));
        assertEquals(failure(",b]"), listParser.parse("[,b]"));
    }

    @Test
    void shouldCaptureAsMap() {
        // Create a simple map parser
        Parser keyValueParser = sequence(
            anyWhitespace(),
            capture(atLeast(1, characterExcept(' ', ':', ',', '}'))),
            anyWhitespace(),
            character(':'),
            anyWhitespace(),
            capture(atLeast(1, characterExcept(',', '}'))),
            anyWhitespace()
        );

        Parser mapParser = sequence(
            character('{'),
            captureAsMap(
                optional(
                    sequence(
                        keyValueParser,
                        zeroOrMore(sequence(character(','), keyValueParser))
                    )
                )
            ),
            character('}')
        );

        // Test empty map
        assertEquals(List.of(Map.of()), mapParser.parse("{}").captures());

        // Test single key-value pair
        assertEquals(List.of(Map.of("name", "value")), mapParser.parse("{name:value}").captures());

        // Test multiple key-value pairs
        assertEquals(
            List.of(Map.of("a", "1", "b", "2", "c", "3")),
            mapParser.parse("{a:1, b:2, c:3}").captures()
        );

        // Test with whitespace
        assertEquals(
            List.of(Map.of("name", "John Doe ")),
            mapParser.parse("{ name : John Doe }").captures()
        );

        // Test failure cases - odd number of elements should throw exception
        Parser invalidMap = sequence(
            character('{'),
            captureAsMap(
                sequence(
                    capture(string("key")),
                    character(':'),
                    capture(string("value")),
                    character(','),
                    capture(string("orphanKey"))
                )
            ),
            character('}')
        );

        assertThrows(IllegalStateException.class, () -> invalidMap.parse("{key:value,orphanKey}"));
    }

    @Test
    void shouldHandleNestedCaptures() {
        // Create nested capture example - a tagged value
        Parser taggedValue = sequence(
            character('<'),
            capture(atLeast(1, characterExcept('>'))),
            character('>'),
            capture(
                sequence(
                    anyWhitespace(),
                    capture(atLeast(1, characterExcept(' ', '<'))),
                    anyWhitespace()
                )
            ),
            string("</"),
            capture(atLeast(1, characterExcept('>'))),
            character('>')
        );

        // XML-like element with nested captures
        ParseResult result = taggedValue.parse("<tag> content </tag>");
        List<Object> captures = result.captures();

        assertEquals(4, captures.size());
        assertEquals("tag", captures.get(0));
        assertEquals("content", captures.get(1));
        assertEquals(" content ", captures.get(2));
        assertEquals("tag", captures.get(3));
    }
}
