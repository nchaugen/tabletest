package org.tabletest.junit.examples;

import org.junit.jupiter.api.DisplayName;
import org.tabletest.junit.Description;
import org.tabletest.junit.TableTest;
import org.tabletest.junit.TypeConverter;
import org.tabletest.junit.javadomain.Grades;
import org.tabletest.junit.javadomain.Student;
import org.tabletest.junit.javadomain.Students;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Student grades")
@Description("""
        A richer domain example. Each cell holds a whole class register, which is a map of student
        names to grade lists. Custom type converters turn it into the domain types Students and
        Grades before the test method runs.
        """)
public class StudentGradesExampleTest {

    @DisplayName("Nested collections convert to domain types")
    @Description("""
            A student passes on their own average, so Pass Count is a count of students rather
            than of grades. Emma's average in the second row is exactly the pass mark, which is
            the boundary the rule is decided at.
            """)
    @TableTest("""
        Scenario                     | Student grades                                                  | Pass Mark | Highest Grade? | Average Grade? | Pass Count?
        Every student above the mark | [Alice: [95, 87, 92], Bob: [78, 85, 90], Charlie: [98, 89, 91]] | 70        | 98             | 89.4           | 3
        One below, one at the mark   | [David: [45, 60, 70], Emma: [65, 70, 75], Frank: [82, 78, 60]]  | 70        | 82             | 67.2           | 2
        No students at all           | [:]                                                             | 70        | 0              | 0.0            | 0
        """)
    void testNestedParameterizedTypes(
        Students students,
        int passMark,
        int expectedHighestGrade,
        double expectedAverageGrade,
        int expectedPassCount
    ) {
        assertEquals(expectedHighestGrade, students.highestGrade());
        assertEquals(expectedAverageGrade, students.averageGrade(), 0.1);
        assertEquals(expectedPassCount, students.passCount(passMark));
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
