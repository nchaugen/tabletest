package io.github.nchaugen.tabletest.junit.javadomain;

import java.time.LocalDate;

public record TypeFactoryDate(LocalDate date) {
    @SuppressWarnings("unused")
    public static TypeFactoryDate parse(String date) {
        return new TypeFactoryDate(LocalDate.parse(date));
    }
}
