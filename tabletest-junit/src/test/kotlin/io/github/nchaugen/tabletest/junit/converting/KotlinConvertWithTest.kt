package io.github.nchaugen.tabletest.junit.converting

import io.github.nchaugen.tabletest.junit.TableTest
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.params.converter.AnnotationBasedArgumentConverter
import org.junit.jupiter.params.converter.ArgumentConversionException
import org.junit.jupiter.params.converter.ArgumentConverter
import org.junit.jupiter.params.converter.ConvertWith
import kotlin.test.assertEquals

class KotlinConvertWithTest {

    @TableTest(
        """
        Person                 | AgeCategory?
        [name: Fred, age: 22]  | ADULT
        [name: Wilma, age: 19] | TEEN
        """
    )
    fun testConvertWith(@ConvertWith(PersonConverter::class) person: Person, expectedAgeCategory: AgeCategory) {
        assertEquals(expectedAgeCategory, person.ageCategory())
    }

    @TableTest(
        """
        Person                 | AgeCategory?
        [name: Fred, age: 22]  | ADULT
        [name: Wilma, age: 19] | TEEN
        """
    )
    fun testComposedConvertWith(@PersonType person: Person, expectedAgeCategory: AgeCategory) {
        assertEquals(expectedAgeCategory, person.ageCategory())
    }

    data class Person(val firstName: String, val lastName: String, val age: Int) {
        fun ageCategory(): AgeCategory {
            return AgeCategory.of(age)
        }
    }

    enum class AgeCategory {
        CHILD, TEEN, ADULT;

        companion object {
            fun of(age: Int): AgeCategory {
                if (age < 13) return CHILD
                if (age < 20) return TEEN
                return ADULT
            }
        }
    }

    class PersonConverter : ArgumentConverter {
        override fun convert(
            source: Any?,
            context: ParameterContext
        ): Any = convertToPerson(source)
    }

    @Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    @ConvertWith(PersonType.PersonConverter::class)
    annotation class PersonType {
        class PersonConverter : AnnotationBasedArgumentConverter<PersonType>() {
            @Throws(ArgumentConversionException::class)
            override fun convert(source: Any?, targetType: Class<*>, annotation: PersonType): Any =
                convertToPerson(source)
        }
    }

}

private fun convertToPerson(source: Any?): KotlinConvertWithTest.Person {
    if (source is Map<*, *>) {
        return KotlinConvertWithTest.Person(
            source.getOrDefault("name", "Fred") as String,
            "Flintstone",
            (source.getOrDefault("age", 16) as String).toInt()
        )
    }
    throw ArgumentConversionException("Cannot convert " + source?.javaClass?.simpleName + " to Person")
}
