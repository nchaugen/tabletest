package org.tabletest.junit.converting;

import org.tabletest.junit.TypeConverter;
import org.tabletest.junit.javadomain.Age;
import org.tabletest.junit.javadomain.Ages;

import java.util.List;
import java.util.Map;

public class JavaTestSuperClass {

    @TypeConverter
    @SuppressWarnings("unused")
    public static Ages parseAges(Map<String, List<Age>> age) {
        return new Ages(age.get("ages"));
    }

}
