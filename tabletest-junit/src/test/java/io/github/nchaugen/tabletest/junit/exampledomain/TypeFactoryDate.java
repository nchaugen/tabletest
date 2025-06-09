package io.github.nchaugen.tabletest.junit.exampledomain;

import java.time.LocalDate;

public record TypeFactoryDate(LocalDate date) {
    @SuppressWarnings("unused")
    public static TypeFactoryDate parse(String date) {
        return new TypeFactoryDate(LocalDate.parse(date));
    }
}
