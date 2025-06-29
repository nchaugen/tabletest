package io.github.nchaugen.tabletest.junit;

import java.util.List;

import static io.github.nchaugen.tabletest.junit.ParameterFixture.parameter;
import static io.github.nchaugen.tabletest.junit.ParameterUtil.nestedElementTypesOf;
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
        type name                                                                       | types?
        java.util.List<?>                                                               | [java.util.List]
        java.util.List<java.lang.Object>                                                | [java.util.List, java.lang.Object]
        java.util.List<java.lang.String>                                                | [java.util.List, java.lang.String]
        java.util.List<java.lang.Integer>                                               | [java.util.List, java.lang.Integer]
        java.util.List<java.lang.Long>                                                  | [java.util.List, java.lang.Long]
        java.util.List<java.lang.Double>                                                | [java.util.List, java.lang.Double]
        java.util.List<java.util.List<java.lang.Long>>                                  | [java.util.List, java.util.List, java.lang.Long]
        java.util.List<java.util.List<java.util.List<java.lang.Byte>>>                  | [java.util.List, java.util.List, java.util.List, java.lang.Byte]
        java.util.List<java.util.Map<java.lang.String, java.util.List<java.lang.Long>>> | [java.util.List, java.util.Map, java.util.List, java.lang.Long]
        """)
    void findsNestedElementTypesOfListParameter(String typeName, List<Class<?>> expectedTypes) {
        assertEquals(expectedTypes, nestedElementTypesOf(parameter(typeName)));
    }

    @TableTest("""
        type name                                        | types?
        java.util.Set<?>                                 | [java.util.Set]
        java.util.Set<java.lang.String>                  | [java.util.Set, java.lang.String]
        java.util.Set<java.util.List<java.lang.Integer>> | [java.util.Set, java.util.List, java.lang.Integer]
        java.util.Set<java.util.Set<java.lang.Double>>   | [java.util.Set, java.util.Set, java.lang.Double]
        """)
    void findsNestedElementTypesOfSetParameter(String typeName, List<Class<?>> expectedTypes) {
        assertEquals(expectedTypes, nestedElementTypesOf(parameter(typeName)));
    }

    @TableTest("""
        type name                                                                        | types?
        java.util.Map<?, ?>                                                              | [java.util.Map]
        java.util.Map<java.lang.String, java.lang.Integer>                               | [java.util.Map, java.lang.Integer]
        java.util.Map<java.lang.String, java.util.List<java.lang.Long>>                  | [java.util.Map, java.util.List, java.lang.Long]
        java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Byte>> | [java.util.Map, java.util.Map, java.lang.Byte]
        """)
    void findsNestedElementTypesOfMapParameter(String typeName, List<Class<?>> expectedTypes) {
        assertEquals(expectedTypes, nestedElementTypesOf(parameter(typeName)));
    }

    @TableTest("""
        type name                           | types?
        java.util.List<java.util.List<?>>   | [java.util.List, java.util.List]
        java.util.Map<java.lang.String, ?>  | [java.util.Map]
        java.util.List<java.util.Map<?, ?>> | [java.util.List, java.util.Map]
        """)
    void findsNestedElementTypesWithMultipleWildcards(String typeName, List<Class<?>> expectedTypes) {
        assertEquals(expectedTypes, nestedElementTypesOf(parameter(typeName)));
    }

    @TableTest("""
        type name                                                             | types?
        java.util.List<? extends java.lang.Number>                            | [java.util.List, java.lang.Number]
        java.util.List<? super java.lang.Integer>                             | [java.util.List, java.lang.Integer]
        java.util.Map<? extends java.lang.String, ? extends java.lang.Number> | [java.util.Map, java.lang.Number]
        """)
    void findsNestedElementTypesWithBoundedWildcards(String typeName, List<Class<?>> expectedTypes) {
        assertEquals(expectedTypes, nestedElementTypesOf(parameter(typeName)));
    }

    @TableTest("""
        type name                                             | types?
        java.util.Optional<java.lang.String>                  | [java.util.Optional, java.lang.String]
        java.util.Optional<java.util.List<java.lang.Integer>> | [java.util.Optional, java.util.List, java.lang.Integer]
        """)
    void findsNestedElementTypesOfOtherGenericTypes(String typeName, List<Class<?>> expectedTypes) {
        assertEquals(expectedTypes, nestedElementTypesOf(parameter(typeName)));
    }

    @TableTest("""
        type name         | types?
        java.lang.String  | [java.lang.String]
        java.lang.Integer | [java.lang.Integer]
        java.lang.Object  | [java.lang.Object]
        java.io.File      | [java.io.File]
        """)
    void findsElementTypesOfNonGenericTypes(String typeName, List<Class<?>> expectedTypes) {
        assertEquals(expectedTypes, nestedElementTypesOf(parameter(typeName)));
    }

    @TableTest("""
        type name                                                                        | types?
        java.util.List<java.util.Set<java.lang.String>>                                  | [java.util.List, java.util.Set, java.lang.String]
        java.util.Set<java.util.Map<java.lang.String, java.lang.Integer>>                 | [java.util.Set, java.util.Map, java.lang.Integer]
        java.util.Map<java.lang.String, java.util.Set<java.util.List<java.lang.Double>>> | [java.util.Map, java.util.Set, java.util.List, java.lang.Double]
        """)
    void findsNestedElementTypesOfMixedCollectionTypes(String typeName, List<Class<?>> expectedTypes) {
        assertEquals(expectedTypes, nestedElementTypesOf(parameter(typeName)));
    }
}
