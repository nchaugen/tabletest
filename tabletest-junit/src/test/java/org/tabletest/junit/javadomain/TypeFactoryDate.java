package org.tabletest.junit.javadomain;

import org.tabletest.junit.TypeConverter;

import java.time.LocalDate;

public record TypeFactoryDate(LocalDate date) {
    @TypeConverter
    @SuppressWarnings("unused")
    public static TypeFactoryDate parse(String date) {
        return new TypeFactoryDate(LocalDate.parse(date));
    }
}
