package org.tabletest.junit.converting;

import org.tabletest.junit.TableTest;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.AnnotationBasedArgumentConverter;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JavaConvertWithTest {

    @TableTest("""
        Person                 | AgeCategory?
        [name: Fred, age: 22]  | ADULT
        [name: Wilma, age: 19] | TEEN
        """)
    void testConvertWith(@ConvertWith(PersonConverter.class) Person person, AgeCategory expectedAgeCategory) {
        assertEquals(expectedAgeCategory, person.ageCategory());
    }

    @TableTest("""
        Person                 | AgeCategory?
        [name: Fred, age: 22]  | ADULT
        [name: Wilma, age: 19] | TEEN
        """)
    void testComposedConvertWith(@PersonType Person person, AgeCategory expectedAgeCategory) {
        assertEquals(expectedAgeCategory, person.ageCategory());
    }

    record Person(String firstName, String lastName, int age) {
        AgeCategory ageCategory() {
            return AgeCategory.of(age);
        }
    }

    enum AgeCategory {
        CHILD, TEEN, ADULT;

        static AgeCategory of(int age) {
            if (age < 13) return AgeCategory.CHILD;
            if (age < 20) return AgeCategory.TEEN;
            return AgeCategory.ADULT;
        }

    }

    private static Person convertToPerson(Object source) {
        if (source instanceof Map attributes) {
            return new Person(
                (String) attributes.getOrDefault("name", "Fred"),
                "Flintstone",
                Integer.parseInt((String) attributes.getOrDefault("age", "16"))
            );
        }
        throw new ArgumentConversionException("Cannot convert " + source.getClass().getSimpleName() + " to Person");
    }

    private static class PersonConverter implements ArgumentConverter {
        @SuppressWarnings({"rawtypes", "NullableProblems"})
        @Override
        public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
            return convertToPerson(source);
        }

    }

    @Target({ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @ConvertWith(PersonType.PersonConverter.class)
    public @interface PersonType {

        @SuppressWarnings("NullableProblems")
        class PersonConverter extends AnnotationBasedArgumentConverter<PersonType> {
            @Override
            protected Object convert(Object source, Class<?> targetType, PersonType annotation) throws ArgumentConversionException {
                return convertToPerson(source);
            }
        }
    }
}