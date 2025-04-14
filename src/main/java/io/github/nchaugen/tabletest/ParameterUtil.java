package io.github.nchaugen.tabletest;

import org.junit.jupiter.api.extension.ParameterContext;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ParameterUtil {

    public static ParameterContext contextOf(Parameter parameter) {
        return new ParameterContext() {
            @Override
            public Parameter getParameter() {
                return parameter;
            }

            @Override
            public int getIndex() {
                return 0;
            }

            @Override
            public Optional<Object> getTarget() {
                return Optional.empty();
            }
        };
    }

    public static List<? extends Class<?>> nestedElementTypesOf(Parameter parameter) {
        return Arrays.stream(parameter.getParameterizedType().getTypeName().split("<"))
            .map(it -> it.replaceAll(">", ""))
            .map(it -> it.split(","))
            .map(it -> it.length > 1 ? it[1] : it[0])
            .map(String::trim)
            .map(ParameterUtil::findClass)
            .filter(Objects::nonNull)
            .toList();
    }

    private static Class<?> findClass(String typeName) {
        try {
            return switch (typeName) {
                case "boolean" -> Boolean.class;
                case "byte" -> Byte.class;
                case "char" -> Character.class;
                case "short" -> Short.class;
                case "int" -> Integer.class;
                case "long" -> Long.class;
                case "float" -> Float.class;
                case "double" -> Double.class;
                default -> Class.forName(typeName);
            };
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
