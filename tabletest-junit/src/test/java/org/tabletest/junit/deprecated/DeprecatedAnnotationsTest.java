package org.tabletest.junit.deprecated;

import io.github.nchaugen.tabletest.junit.Description;
import io.github.nchaugen.tabletest.junit.FactorySources;
import io.github.nchaugen.tabletest.junit.Scenario;
import io.github.nchaugen.tabletest.junit.TableTest;
import org.junit.jupiter.api.DisplayName;
import org.tabletest.junit.javadomain.Age;
import org.tabletest.junit.javadomain.Ages;
import org.tabletest.junit.javafactories.FirstTierFactorySource;
import org.tabletest.junit.javafactories.SecondTierFactorySource;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Leap Year Rules")
@Description("The following describes the rules for leap years.")
@FactorySources({FirstTierFactorySource.class, SecondTierFactorySource.class})
public class DeprecatedAnnotationsTest {

    @Description("Testing that deprecated annotations still work")
    @TableTest("""
        Scenario | Int | List | Set  | AVS  | Map       | Nested           | Ages
        Example  | 16  | [16] | {16} | {16} | [age: 16] | [ages: [16, 16]] | [ages: [16, 16]]
        """)
    void using_factory_methods_in_factory_source(
        @Scenario String scenario,
        Age fromInt,
        List<Age> inList,
        Set<Age> inSet,
        Age fromValueSet,
        Map<String, Age> inMap,
        Map<String, List<Age>> inNested,
        Ages inOtherFactoryMethod
    ) {
        Age expected = new Age(16);
        assertEquals(expected, fromInt);
        assertEquals(List.of(expected), inList);
        assertEquals(Set.of(expected), inSet);
        assertEquals(expected, fromValueSet);
        assertEquals(Map.of("age", expected), inMap);
        assertEquals(Map.of("ages", List.of(expected, expected)), inNested);
        assertEquals(new Ages(List.of(expected, expected)), inOtherFactoryMethod);
    }

}
