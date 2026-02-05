package org.tabletest.junit.javatypeconverters;

import org.tabletest.junit.TypeConverter;
import org.tabletest.junit.javadomain.Age;
import org.tabletest.junit.javadomain.Ages;
import org.tabletest.junit.javadomain.ExternalFactoryDate;
import org.junit.platform.commons.support.conversion.ConversionException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public class FirstTierTypeConverterSource {

    @TypeConverter
    public static ExternalFactoryDate parseDate(String date) {
        return new ExternalFactoryDate(LocalDate.parse(date));
    }

    @TypeConverter
    public static Age parseAge(int age) {
        return new Age(age);
    }

    public Age parseAge(Integer age) {
        throw new ConversionException("Type converter not static, should not be called");
    }

    static Age parseAge(Long age) {
        throw new ConversionException("Type converter not accessible, should not be called");
    }

    @TypeConverter
    public static Ages parseAges(Map<String, List<Age>> age) {
        return new Ages(age.get("ages"));
    }

}
