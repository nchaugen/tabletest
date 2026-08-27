package org.tabletest.junit.converting.custom;

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
        A type with no built-in conversion of its own still converts. It needs a way to build
        itself from the cell text alone. The features under builtin hold the built-in
        conversions. This feature covers what happens when none of them applies.
        """)
public class JavaFallbackConversionTest {

    @DisplayName("Converts text through a String constructor or factory method")
    @Description("""
            Where no built-in conversion exists, TableTest uses a single-argument constructor on
            the target type, or a static factory method on it. It does the same inside a
            collection. Both custom date types here wrap the LocalDate in the expectation
            column.
            """)
    @TableTest("""
        Scenario                | Constructor | Factory method in type | List with fallback | Wrapped date?
        A date in May           | 2025-05-27  | 2025-05-27             | [2025-05-27]       | 2025-05-27
        A leap day              | 2024-02-29  | 2024-02-29             | [2024-02-29]       | 2024-02-29
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
