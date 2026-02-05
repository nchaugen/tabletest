package org.tabletest.junit.javadomain;

import java.time.LocalDate;

public record ConstructorDate(LocalDate date) {
    @SuppressWarnings("unused")
    ConstructorDate(String date) {
        this(LocalDate.parse(date));
    }
}
