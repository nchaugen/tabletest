package io.github.nchaugen.tabletest;

import java.util.List;

import static io.github.nchaugen.tabletest.ParameterFixture.parameter;
import static io.github.nchaugen.tabletest.ParameterUtil.nestedElementTypesOf;
import static org.junit.jupiter.api.Assertions.*;

class ParameterUtilTest {

    @TableTest("""
        type name | types?
        boolean   | [java.lang.Boolean]
        byte      | [java.lang.Byte]
        char      | [java.lang.Character]
        short     | [java.lang.Short]
        int       | [java.lang.Integer]
        long      | [java.lang.Long]
        float     | [java.lang.Float]
        double    | [java.lang.Double]
        """)
    void findsPrimitiveElementTypes(String typeName, List<Class<?>> expectedTypes) {
        assertEquals(expectedTypes, nestedElementTypesOf(parameter(typeName)));
    }

    @TableTest("""
        type name                                                                         | types?
        java.util.List<?>                                                                 | [java.util.List]
        java.util.List<java.lang.Object>                                                  | [java.util.List, java.lang.Object]
        java.util.List<java.lang.String>                                                  | [java.util.List, java.lang.String]
        java.util.List<java.lang.Integer>                                                 | [java.util.List, java.lang.Integer]
        java.util.List<java.lang.Long>                                                    | [java.util.List, java.lang.Long]
        java.util.List<java.lang.Double>                                                  | [java.util.List, java.lang.Double]
        java.util.List<java.util.List<java.lang.Long>>                                    | [java.util.List, java.util.List, java.lang.Long]
        java.util.List<java.util.List<java.util.List<java.lang.Byte>>>                    | [java.util.List, java.util.List, java.util.List, java.lang.Byte]
        "java.util.List<java.util.Map<java.lang.String, java.util.List<java.lang.Long>>>" | [java.util.List, java.util.Map, java.util.List, java.lang.Long]
        """)
    void findsNestedElementTypesOfListParameter(String typeName, List<Class<?>> expectedTypes) {
        assertEquals(expectedTypes, nestedElementTypesOf(parameter(typeName)));
    }

}
