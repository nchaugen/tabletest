package org.tabletest.junit.javadomain;

import java.util.Comparator;
import java.util.List;

public record Grades(List<Integer> grades) {
     public int findHighestGrade() {
        return grades.stream()
            .max(Comparator.naturalOrder())
            .orElse(0);
    }

     public double averageGrade() {
        return grades.stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);
    }

     public boolean hasPassed() {
        return averageGrade() >= 70.0;
    }
}
