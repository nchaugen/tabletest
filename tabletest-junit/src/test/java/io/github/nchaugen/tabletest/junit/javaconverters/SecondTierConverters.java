package io.github.nchaugen.tabletest.junit.javaconverters;

import io.github.nchaugen.tabletest.junit.javadomain.Age;

@SuppressWarnings("unused")
public class SecondTierConverters {

    public static Age parseAge(int age) {
        throw new RuntimeException("Should not be called, another converter should take precedence");
    }

}
