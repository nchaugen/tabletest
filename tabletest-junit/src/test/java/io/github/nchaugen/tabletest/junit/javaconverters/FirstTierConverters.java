package io.github.nchaugen.tabletest.junit.javaconverters;

import io.github.nchaugen.tabletest.junit.javadomain.Age;
import io.github.nchaugen.tabletest.junit.javadomain.Ages;
import io.github.nchaugen.tabletest.junit.javadomain.ExternalFactoryDate;
import org.junit.platform.commons.support.conversion.ConversionException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class FirstTierConverters {

    public static ExternalFactoryDate parseDate(String date) {
        return new ExternalFactoryDate(LocalDate.parse(date));
    }

    public static Age parseAge(int age) {
        return new Age(age);
    }

    public Age parseAge(Integer age) {
        throw new ConversionException("Factory method not static, should not be called");
    }

    static Age parseAge(Long age) {
        throw new ConversionException("Factory method not accessible, should not be called");
    }

    public static Ages parseAges(Map<String, List<Age>> age) {
        return new Ages(age.get("ages"));
    }

    public static LocalDate parseLocalDate(String input) {
        throw new RuntimeException("Should not be called, another converter should take precedence");
    }

}
