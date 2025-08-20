package io.github.nchaugen.tabletest.junit.converting

import io.github.nchaugen.tabletest.junit.javadomain.Age
import io.github.nchaugen.tabletest.junit.javadomain.Ages

open class KotlinTestSuperClass {

    companion object {
        @JvmStatic
        @Suppress("unused")
        fun parseAges(ages: Map<String, List<Age>>): Ages = Ages(ages["ages"])
    }

}
