package io.github.nchaugen.tabletest.junit.javadomain;

import java.time.LocalDate;

public record ConstructorDate(LocalDate date) {
    @SuppressWarnings("unused")
    ConstructorDate(String date) {
        this(LocalDate.parse(date));
    }
}
