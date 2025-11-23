package io.github.nchaugen.tabletest.junit;

import io.github.nchaugen.tabletest.parser.Row;
import io.github.nchaugen.tabletest.parser.Table;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(JunitTableMetadataTest.ExtensionContextResolver.class)
class JunitTableMetadataTest {

    @Test
    @DisplayName("Table Title")
    void shouldExtractTableTitle(ExtensionContext context) {
        Table table = new Table(new Row(List.of("a", "b")), List.of(new Row(List.of("1", "2"))));
        assertEquals("Table Title", new JunitTableMetadata(context, table).title());
    }

    @Test
    @Description("""
        This is the test description.
        
        It can include multiple lines and **formatting**. Such as __lists__:
        - item 1
        - item 2
        """)
    void shouldExtractDescriptionWhenPresent(ExtensionContext context) {
        Table table = new Table(new Row(List.of("a", "b")), List.of(new Row(List.of("1", "2"))));
        assertEquals(
            """
                This is the test description.
                
                It can include multiple lines and **formatting**. Such as __lists__:
                - item 1
                - item 2
                """.stripIndent(),
            new JunitTableMetadata(context, table).description()
        );
    }

    @SuppressWarnings("NullableProblems")
    static class ExtensionContextResolver implements ParameterResolver {
        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
            return parameterContext.getParameter().getType() == ExtensionContext.class;
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
            return extensionContext;
        }
    }

}