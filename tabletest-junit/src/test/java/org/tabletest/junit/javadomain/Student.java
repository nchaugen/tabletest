package org.tabletest.junit.javadomain;

public record Student(String name, Grades grades) {
    public int findHighestGrade() {
        return grades.findHighestGrade();
    }

    public double averageGrade() {
        return grades.averageGrade();
    }

    public boolean hasPassed() {
        return grades.hasPassed();
    }
}
