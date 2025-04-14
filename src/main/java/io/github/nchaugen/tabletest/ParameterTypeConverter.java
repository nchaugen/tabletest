package io.github.nchaugen.tabletest;

import org.junit.jupiter.params.converter.DefaultArgumentConverter;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.nchaugen.tabletest.ParameterUtil.contextOf;

public class ParameterTypeConverter {

    public static Object convertValue(Object value, Parameter parameter/*, Class<?> testClass*/) {
        Object convertedValue = convertValue(value, NestedTypes.of(parameter), parameter);
//        if (convertedValue instanceof Map<?, ?> mapValue && parameter.getType() != Map.class) {
//            Optional<Object> parsedValue = Arrays.stream(testClass.getDeclaredMethods())
//                .filter(method -> method.getReturnType().equals(parameter.getType()))
//                .filter(method -> method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(Map.class))
//                .findFirst()
//                .map(method -> {
//                    try {
//                        return method.invoke(testClass, mapValue);
//                    } catch (IllegalAccessException | InvocationTargetException e) {
//                        return null;
//                    }
//                });
//            return parsedValue.orElse(convertedValue);
//        }
        return convertedValue;
    }

    private static Object convertValue(
        Object value,
        NestedTypes nestedTypes,
        Parameter parameter
    ) {
        return switch (value) {
            case List<?> list -> convertList(list, nestedTypes.skipNext(), parameter);
            case Map<?, ?> map -> convertMap(map, nestedTypes.skipNext(), parameter);
            default -> convertSingleValue(value, nestedTypes, parameter);
        };
    }

    @SuppressWarnings("DataFlowIssue")
    private static Object convertSingleValue(Object value, NestedTypes nestedTypes, Parameter parameter) {
        return nestedTypes.hasNext()
               ? DefaultArgumentConverter.INSTANCE.convert(value, nestedTypes.next(), contextOf(parameter))
               : value;
    }

    private static List<?> convertList(
        List<?> list,
        NestedTypes nestedTypes,
        Parameter parameter
    ) {
        return list.stream()
            .map(it -> convertValue(it, nestedTypes, parameter))
            .collect(Collectors.toList());
    }

    private static Map<?, ?> convertMap(
        Map<?, ?> map,
        NestedTypes nestedTypes,
        Parameter parameter
    ) {
        return map.entrySet().stream()
            .map(entry -> Map.entry(
                entry.getKey(),
                convertValue(
                    entry.getValue(),
                    nestedTypes,
                    parameter
                )
            ))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private record NestedTypes(
        List<? extends Class<?>> elementTypes
    ) {
        static NestedTypes of(Parameter parameter) {
            return parameter.isAnnotationPresent(org.junit.jupiter.params.converter.ConvertWith.class)
                   ? new NestedTypes(List.of())
                   : new NestedTypes(ParameterUtil.nestedElementTypesOf(parameter));
        }

        boolean hasNext() {
            return !elementTypes.isEmpty();
        }

        Class<?> next() {
            return elementTypes.isEmpty() ? null : elementTypes.getFirst();
        }

        NestedTypes skipNext() {
            return elementTypes.isEmpty()
                   ? this
                   : new NestedTypes(elementTypes.subList(1, elementTypes.size()));
        }
    }
}
