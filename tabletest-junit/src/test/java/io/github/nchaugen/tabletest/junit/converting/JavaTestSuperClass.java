package io.github.nchaugen.tabletest.junit.converting;

import io.github.nchaugen.tabletest.junit.javadomain.Age;
import io.github.nchaugen.tabletest.junit.javadomain.Ages;

import java.util.List;
import java.util.Map;

public class JavaTestSuperClass {

    @SuppressWarnings("unused")
    public static Ages parseAges(Map<String, List<Age>> age) {
        return new Ages(age.get("ages"));
    }

}
