package io.github.nchaugen.tabletest.junit;

import io.github.nchaugen.tabletest.junit.exampledomain.ExternalFactoryDate;

import java.time.LocalDate;

@SuppressWarnings("unused")
public class ExampleDomainConverters {

    public static ExternalFactoryDate parseDate(String date) {
        return new ExternalFactoryDate(LocalDate.parse(date));
    }

}
