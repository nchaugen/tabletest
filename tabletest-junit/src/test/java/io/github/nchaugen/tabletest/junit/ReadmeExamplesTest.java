package io.github.nchaugen.tabletest.junit;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReadmeExamplesTest {

    @TableTest("""
        Augend | Addend | Sum?
        2      | 3      | 5
        0      | 0      | 0
        1      | 1      | 2
        """)
    void testAddition(int augend, int addend, int sum) {
        assertEquals(sum, augend + addend);
    }

    @TableTest("""
        Value          | Length?
        Hello world    | 11
        "World, hello" | 12
        '|'            | 1
        ""             | 0
        """)
    void testString(String value, int expectedLength) {
        assertEquals(expectedLength, value.length());
    }

    @TableTest("""
        List             | Size?
        [Hello, World]   | 2
        ["World, Hello"] | 1
        ['|', ",", abc]  | 3
        [[1, 2], [3, 4]] | 2
        [[a: 4], [b: 5]] | 2
        []               | 0
        """)
    void testList(List<Object> list, int expectedSize) {
        assertEquals(expectedSize, list.size());
    }

    @TableTest("""
        Map                                      | Size?
        [1: Hello, 2: World]                     | 2
        // ["|": 1, ',': 2, abc: 3]                 | 3
        [string: abc, list: [1, 2], map: [a: 4]] | 3
        [:]                                      | 0
        """)
    void testMap(Map<String, Object> map, int expectedSize) {
        assertEquals(expectedSize, map.size());
    }

    @TableTest("""
        String         | Length?
        Hello world    | 11
        // The next row is currently disabled
        // "World, hello" | 12
        //
        // Special characters must be quoted
        '|'            | 1
        """)
    void testComment(String string, int expectedLength) {
        assertEquals(expectedLength, string.length());
    }

    @TableTest("""
        Student grades                                                  | Highest Grade? | Average Grade? | Pass Count?
        [Alice: [95, 87, 92], Bob: [78, 85, 90], Charlie: [98, 89, 91]] | 98             | 89.4           | 3
        [David: [45, 60, 70], Emma: [65, 70, 75], Frank: [82, 78, 60]]  | 82             | 67.2           | 2
        [:]                                                             | 0              | 0.0            | 0
        """)
    void testNestedParameterizedTypes(
        Map<String, List<Integer>> studentGrades,
        int expectedHighestGrade,
        double expectedAverageGrade,
        int expectedPassCount
    ) {
        Students students = parse(studentGrades);
        assertEquals(expectedHighestGrade, students.highestGrade());
        assertEquals(expectedAverageGrade, students.averageGrade(), 0.1);
        assertEquals(expectedPassCount, students.passCount());
    }

    static Students parse(Map<String, List<Integer>> input) {
        return new Students(
            input.entrySet().stream()
                .map(entry -> new Student(entry.getKey(), entry.getValue()))
                .toList()
        );
    }

    private record Students(List<Student> students) {
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

    private record Student(String name, List<Integer> grades) {
        int findHighestGrade() {
            return grades.stream()
                .mapToInt(Integer::intValue)
                .max()
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
