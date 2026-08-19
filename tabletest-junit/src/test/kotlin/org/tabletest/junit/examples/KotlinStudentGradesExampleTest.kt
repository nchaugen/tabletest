package org.tabletest.junit.examples

import org.tabletest.junit.TableTest
import org.tabletest.junit.TypeConverter
import org.tabletest.junit.javadomain.Grades
import org.tabletest.junit.javadomain.Student
import org.tabletest.junit.javadomain.Students
import org.junit.jupiter.api.Assertions.assertEquals

class KotlinStudentGradesExampleTest {

    @TableTest(
        """    
        Scenario    | Student grades                                                  | Pass Mark | Highest Grade? | Average Grade? | Pass Count?
        All pass    | [Alice: [95, 87, 92], Bob: [78, 85, 90], Charlie: [98, 89, 91]] | 70        | 98             | 89.4           | 3
        Some pass   | [David: [45, 60, 70], Emma: [65, 70, 75], Frank: [82, 78, 60]]  | 70        | 82             | 67.2           | 2
        No students | [:]                                                             | 70        | 0              | 0.0            | 0
        """
    )
    fun `should calculate student grade statistics`(
        students: Students,
        passMark: Int,
        expectedHighestGrade: Int,
        expectedAverageGrade: Double,
        expectedPassCount: Long
    ) {
        assertEquals(expectedHighestGrade, students.highestGrade())
        assertEquals(expectedAverageGrade, students.averageGrade(), 0.1)
        assertEquals(expectedPassCount, students.passCount(passMark))
    }

    companion object {
        @JvmStatic
        @TypeConverter
        @Suppress("unused")
        fun parseStudents(input: Map<String, Grades>): Students =
            Students(input.map { Student(it.key, it.value) }.toList())

        @JvmStatic
        @TypeConverter
        @Suppress("unused")
        fun parseGrades(input: List<Int>): Grades = Grades(input)
    }

}