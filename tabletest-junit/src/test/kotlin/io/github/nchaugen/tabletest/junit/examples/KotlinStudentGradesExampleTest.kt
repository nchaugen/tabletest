package io.github.nchaugen.tabletest.junit.examples

import io.github.nchaugen.tabletest.junit.TableTest
import io.github.nchaugen.tabletest.junit.javadomain.Grades
import io.github.nchaugen.tabletest.junit.javadomain.Student
import io.github.nchaugen.tabletest.junit.javadomain.Students
import org.junit.jupiter.api.Assertions.assertEquals

class KotlinStudentGradesExampleTest {

    @TableTest(
        """    
        Scenario    | Student grades                                                  | Highest Grade? | Average Grade? | Pass Count?
        All pass    | [Alice: [95, 87, 92], Bob: [78, 85, 90], Charlie: [98, 89, 91]] | 98             | 89.4           | 3
        Some pass   | [David: [45, 60, 70], Emma: [65, 70, 75], Frank: [82, 78, 60]]  | 82             | 67.2           | 2
        No students | [:]                                                             | 0              | 0.0            | 0
        """
    )
    fun `should calculate student grade statistics`(
        students: Students,
        expectedHighestGrade: Int,
        expectedAverageGrade: Double,
        expectedPassCount: Long
    ) {
        assertEquals(expectedHighestGrade, students.highestGrade())
        assertEquals(expectedAverageGrade, students.averageGrade(), 0.1)
        assertEquals(expectedPassCount, students.passCount())
    }

    companion object {
        @JvmStatic
        @Suppress("unused")
        fun parseStudents(input: Map<String, Grades>): Students =
            Students(input.map { Student(it.key, it.value) }.toList())

        @JvmStatic
        @Suppress("unused")
        fun parseGrades(input: List<Int>): Grades = Grades(input)
    }

}