package org.tabletest.junit.converting

import org.tabletest.junit.TypeConverter
import org.tabletest.junit.javadomain.Age
import org.tabletest.junit.javadomain.Ages

open class KotlinTestSuperClass {

    companion object {
        @JvmStatic
        @TypeConverter
        @Suppress("unused")
        fun parseAges(ages: Map<String, List<Age>>): Ages = Ages(ages["ages"])
    }

}
