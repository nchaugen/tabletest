package org.tabletest.junit.javatypeconverters;

import org.tabletest.junit.TypeConverter;
import org.tabletest.junit.javadomain.Age;

import java.time.LocalDate;

@SuppressWarnings("unused")
public class SecondTierTypeConverterSource {

    @TypeConverter
    public static Age parseAge(int age) {
        throw new RuntimeException("Should not be called, another converter should take precedence");
    }

    @TypeConverter
    public static LocalDate parseLocalDate(String input) {
        throw new RuntimeException("Should not be called, another converter should take precedence");
    }

}
