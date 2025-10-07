package io.github.nchaugen.tabletest.junit.examples;

import io.github.nchaugen.tabletest.junit.TableTest;
import io.github.nchaugen.tabletest.junit.javadomain.Grades;
import io.github.nchaugen.tabletest.junit.javadomain.Student;
import io.github.nchaugen.tabletest.junit.javadomain.Students;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StudentGradesExampleTest {

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

    @SuppressWarnings("unused")
    public static Grades parseGrades(List<Integer> input) {
        return new Grades(input);
    }

}
