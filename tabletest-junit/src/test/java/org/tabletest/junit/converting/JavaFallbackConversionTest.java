package org.tabletest.junit.converting;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.tabletest.junit.javadomain.ConstructorDate;
import org.tabletest.junit.javadomain.TypeFactoryDate;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Constructor and factory fallback")
@Description("""
        A type with no built-in conversion of its own still converts, as long as it
        can be built from the cell text alone. The built-in conversions are in the
        chapters under builtin; this is what happens when none of them applies.
        """)
public class JavaFallbackConversionTest {

    @DisplayName("Any type converts from text through its String constructor or static factory method")
    @Description("""
            When no built-in conversion exists, TableTest falls back to a
            single-argument constructor or a static factory method on the
            target type — inside collections too. Both custom date types here
            wrap the LocalDate in the expectation column.
            """)
    @TableTest("""
        Scenario                 | Constructor | Factory method in type | List with fallback | Wrapped date?
        ISO date in every column | 2025-05-27  | 2025-05-27             | [2025-05-27]       | 2025-05-27
        """)
    void converts_custom_types_with_constructor_or_type_internal_factory(
        ConstructorDate withConstructor,
        TypeFactoryDate withFactoryMethodInsideType,
        List<TypeFactoryDate> listWithFallbackConversion,
        LocalDate expectedDate
    ) {
        assertEquals(expectedDate, withConstructor.date());
        assertEquals(expectedDate, withFactoryMethodInsideType.date());
        assertEquals(expectedDate, listWithFallbackConversion.get(0).date());
    }
}
