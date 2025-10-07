package io.github.nchaugen.tabletest.junit.javadomain;

import java.util.List;

public record Students(List<Student> students) {
    public int highestGrade() {
        return students.stream()
            .map(Student::findHighestGrade)
            .mapToInt(Integer::intValue)
            .max()
            .orElse(0);
    }

    public double averageGrade() {
        return students.stream()
            .map(Student::averageGrade)
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
    }

    public long passCount() {
        return students.stream()
            .filter(Student::hasPassed)
            .count();
    }
}
