package io.github.nchaugen.tabletest.junit.examples;

import io.github.nchaugen.tabletest.junit.TableTest;

import java.util.Comparator;
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

    public record Students(List<Student> students) {
        int highestGrade() {
            return students.stream()
                .map(Student::findHighestGrade)
                .mapToInt(Integer::intValue)
                .max()
                .orElse(0);
        }

        double averageGrade() {
            return students.stream()
                .map(Student::averageGrade)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        }

        double passCount() {
            return students.stream()
                .filter(Student::hasPassed)
                .count();
        }
    }

    private record Student(String name, Grades grades) {
        int findHighestGrade() {
            return grades.findHighestGrade();
        }

        double averageGrade() {
            return grades.averageGrade();
        }

        boolean hasPassed() {
            return grades.hasPassed();
        }
    }

    public record Grades(List<Integer> grades) {
        int findHighestGrade() {
            return grades.stream()
                .max(Comparator.naturalOrder())
                .orElse(0);
        }

        double averageGrade() {
            return grades.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
        }

        boolean hasPassed() {
            return averageGrade() >= 70.0;
        }
    }

}
