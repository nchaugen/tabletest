package io.github.nchaugen.tabletest;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ParameterFixture {

    public static Parameter parameter(Class<?> type) {
        if (!paramsByType.containsKey(type)) {
            throw new IllegalArgumentException("Type " + type.getSimpleName() + " not defined in ParameterFixture");
        }
        return paramsByType.get(type);
    }

    public static Parameter parameter(String typeName) {
        if (!paramsByTypeName.containsKey(typeName)) {
            throw new IllegalArgumentException("TypeName " + typeName + " not defined in ParameterFixture");
        }
        return paramsByTypeName.get(typeName);
    }

    private static final Map<String, Parameter> paramsByTypeName =
        Arrays.stream(ParameterFixture.class.getDeclaredMethods())
            .filter(it -> it.getName().endsWith("ValueParams"))
            .flatMap((Method method) -> Arrays.stream(method.getParameters()))
            .collect(Collectors.toMap(parameter -> parameter.getParameterizedType().getTypeName(), it -> it));

    private static final Map<Class<?>, Parameter> paramsByType =
        Arrays.stream(ParameterFixture.class.getDeclaredMethods())
            .filter(it -> "supportedValueParams".equals(it.getName()))
            .flatMap((Method method) -> Arrays.stream(method.getParameters()))
            .collect(Collectors.toMap(Parameter::getType, it -> it));

    @SuppressWarnings("unused")
    private void supportedValueParams(
        boolean primitiveBoolean,
        byte primitiveByte,
        char primitiveChar,
        short primitiveShort,
        int primitiveInt,
        long primitiveLong,
        float primitiveFloat,
        double primitiveDouble,
        Byte byteParam,
        Short shortParam,
        Integer intParam,
        Long longParam,
        Float floatParam,
        Double doubleParam,
        BigInteger bigIntParam,
        BigDecimal bigDecParam,
        Character charParam,
        String stringParam,
        TimeUnit enumParam,
        File fileParam,
        Path pathParam,
        URI uriParam,
        URL urlParam,
        Class<?> classParam,
        Charset charsetParam,
        Currency currencyParam,
        Locale localeParam,
        UUID uuidParam,
        Duration durationParam,
        Instant instantParam,
        LocalDateTime localDateTimeParam,
        LocalDate localDateParam,
        LocalTime localTimeParam,
        MonthDay monthDayParam,
        OffsetDateTime offsetDateTimeParam,
        OffsetTime offsetTimeParam,
        Period periodParam,
        YearMonth yearMonthParam,
        Year yearParam,
        ZonedDateTime zonedDateTimeParam,
        ZoneId zoneIdParam,
        ZoneOffset zoneOffsetParam,
        List<?> list,
        Map<?, ?> map
    ) {
        // This method is only used to get the parameter types
    }

    @SuppressWarnings("unused")
    private void parameterizedListValueParams(
        List<Object> objList,
        List<String> strList,
        List<Byte> byteList,
        List<Short> shortList,
        List<Integer> intList,
        List<Long> longList,
        List<Double> doublesList,
        List<List<Short>> listOfShortList,
        List<List<Long>> listOfLongsList,
        List<List<List<Byte>>> listOfListOfBytesList,
        List<Map<String, Long>> mapOfLongsList,
        List<Map<String, List<Long>>> mapOfListOfLongsList
    ) {
        // This method is only used to get the parameter type
    }

    @SuppressWarnings("unused")
    private void parameterizedMapValueParams(
        Map<?, Object> objectMap,
        Map<?, String> stringMap,
        Map<String, Byte> byteMap,
        Map<?, Short> shortMap,
        Map<?, Integer> integerMap,
        Map<?, Long> longMap,
        Map<?, Double> doubleMap,
        Map<?, List<Short>> mapOfShortList,
        Map<?, List<Long>> mapOfLongList,
        Map<?, List<List<Byte>>> mapOfListOfByteList,
        Map<?, Map<?, Long>> mapOfLongMap,
        Map<?, Map<String, List<Long>>> mapOfMapOfLongsList
    ) {
        // This method is only used to get the parameter type
    }
}
