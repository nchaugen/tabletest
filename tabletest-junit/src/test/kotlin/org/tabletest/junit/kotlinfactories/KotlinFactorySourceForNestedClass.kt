@file:Suppress("unused")

package org.tabletest.junit.kotlinfactories

import org.tabletest.junit.javadomain.Age
import org.tabletest.junit.javadomain.Ages
import java.time.LocalDate

object KotlinFactorySourceForNestedClass {

    @JvmStatic
    fun parseLocalDate(input: String): LocalDate {
        return when (input) {
            "last year" -> LocalDate.parse("2024-06-07")
            "this year" -> LocalDate.parse("2025-06-07")
            "next year" -> LocalDate.parse("2026-06-07")
            else -> LocalDate.parse(input)
        }
    }

    @JvmStatic
    fun parseAges(input: Map<String, List<Age>>): Ages {
        return Ages(input["ages"]?.map { Age(it.age + 10) })
    }
}
