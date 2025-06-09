package io.github.nchaugen.tabletest.junit.converting;

import io.github.nchaugen.tabletest.junit.TableTest;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.ArgumentConversionException;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;

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

    private static class PersonConverter implements ArgumentConverter {
        @SuppressWarnings("rawtypes")
        @Override
        public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
            if (source instanceof Map attributes) {
                return new Person(
                    (String) attributes.getOrDefault("name", "Fred"),
                    "Flintstone",
                    Integer.parseInt((String) attributes.getOrDefault("age", "16"))
                );
            }
            throw new ArgumentConversionException("Cannot convert " + source.getClass().getSimpleName() + " to Person");
        }
    }

}
