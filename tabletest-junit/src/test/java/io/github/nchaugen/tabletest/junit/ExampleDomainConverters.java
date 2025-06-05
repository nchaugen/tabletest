package io.github.nchaugen.tabletest.junit;

import io.github.nchaugen.tabletest.junit.exampledomain.FactoryMethodDate;

import java.time.LocalDate;

@SuppressWarnings("unused")
public class ExampleDomainConverters {

    public static FactoryMethodDate parseDate(String date) {
        return new FactoryMethodDate(LocalDate.parse(date));
    }

}
