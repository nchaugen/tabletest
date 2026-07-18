package org.tabletest.junit.examples;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.tabletest.junit.TypeConverter;
import org.tabletest.junit.javadomain.Grades;
import org.tabletest.junit.javadomain.Student;
import org.tabletest.junit.javadomain.Students;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("spec")
@DisplayName("Student grades")
@Description("""
        A richer domain example: each cell holds a whole class register — a map of
        student names to grade lists — converted to domain types (Students, Grades)
        by custom type converters before the test method runs.
        """)
public class StudentGradesExampleTest {

    @DisplayName("Nested collections convert to domain types")
    @TableTest("""
        Student grades                                                  | Highest Grade? | Average Grade? | Pass Count?
        [Alice: [95, 87, 92], Bob: [78, 85, 90], Charlie: [98, 89, 91]] | 98             | 89.4           | 3
        [David: [45, 60, 70], Emma: [65, 70, 75], Frank: [82, 78, 60]]  | 82             | 67.2           | 2
        [:]                                                             | 0              | 0.0            | 0
        """)
    void testNestedParameterizedTypes(
        Students students,
        int expectedHighestGrade,
        double expectedAverageGrade,
        int expectedPassCount
    ) {
        assertEquals(expectedHighestGrade, students.highestGrade());
        assertEquals(expectedAverageGrade, students.averageGrade(), 0.1);
        assertEquals(expectedPassCount, students.passCount());
    }

    @TypeConverter
    @SuppressWarnings("unused")
    public static Students parseStudents(Map<String, Grades> input) {
        return new Students(
            input.entrySet().stream()
                .map(entry -> new Student(
                    entry.getKey(),
                    entry.getValue()
                ))
                .toList()
        );
    }

    @TypeConverter
    @SuppressWarnings("unused")
    public static Grades parseGrades(List<Integer> input) {
        return new Grades(input);
    }

}
