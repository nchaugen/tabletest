package io.github.nchaugen.tabletest.junit.javafactories;

import io.github.nchaugen.tabletest.junit.javadomain.Age;

import java.time.LocalDate;

@SuppressWarnings("unused")
public class SecondTierFactorySource {

    public static Age parseAge(int age) {
        throw new RuntimeException("Should not be called, another converter should take precedence");
    }

    public static LocalDate parseLocalDate(String input) {
        throw new RuntimeException("Should not be called, another converter should take precedence");
    }

}
