package org.tabletest.junit;

import org.tabletest.junit.javadomain.Age;
import org.tabletest.junit.javadomain.Ages;

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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ParameterFixture {

    @TypeConverter
    public static Parameter parameter(Class<?> type) {
        if (!paramsByType.containsKey(type)) {
            throw new IllegalArgumentException("Type " + type.getSimpleName() + " not defined in ParameterFixture");
        }
        return paramsByType.get(type);
    }

    @TypeConverter
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
            .filter(it -> it.getName().endsWith("NonParameterizedValueParams"))
            .flatMap((Method method) -> Arrays.stream(method.getParameters()))
            .collect(Collectors.toMap(Parameter::getType, it -> it));

    @SuppressWarnings({"unused", "OptionalUsedAsFieldOrParameterType"})
    private void javaNonParameterizedValueParams(
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
        Object objectParam, // Added Object
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
        Set<?> set,
        Optional<?> optional
    ) {
        // This method is only used to get the parameter types
    }

    @SuppressWarnings("unused")
    private void domainNonParameterizedValueParams(
        Age age,
        Ages ages
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
        Map<String, Integer> mapStringInteger,
        Map<?, Long> longMap,
        Map<?, Double> doubleMap,
        Map<?, List<Short>> mapOfShortList,
        Map<?, List<Long>> mapOfLongList,
        Map<String, List<Long>> mapStringListLong,
        Map<?, List<List<Byte>>> mapOfListOfByteList,
        Map<?, Map<?, Long>> mapOfLongMap,
        Map<?, Map<String, List<Long>>> mapOfMapOfLongsList,
        Map<String, Map<String, Byte>> mapStringMapStringByte
    ) {
        // This method is only used to get the parameter type
    }

    @SuppressWarnings("unused")
    private void parameterizedSetValueParams(
        Set<Object> objectSet,
        Set<String> stringSet,
        Set<Byte> byteSet,
        Set<Short> shortSet,
        Set<Integer> integerSet,
        Set<Long> longSet,
        Set<Double> doubleSet,
        Set<List<String>> setOfStringList,
        Set<List<Integer>> setOfIntegerList,
        Set<List<Long>> setOfLongList,
        Set<List<Double>> setOfDoubleList,
        Set<Set<String>> setOfStringSet,
        Set<Set<Integer>> setOfIntegerSet,
        Set<Set<Double>> setOfDoubleSet,
        Set<Map<String, Integer>> setOfStringIntegerMap,
        Set<Map<String, List<Long>>> setOfStringListLongMap
    ) {
        // This method is only used to get the parameter types
    }

    @SuppressWarnings("unused")
    private void mixedCollectionValueParams(
        List<Set<String>> listOfStringSet,
        List<Set<Integer>> listOfIntegerSet,
        List<Set<Double>> listOfDoubleSet,
        Map<String, Set<String>> mapOfStringSet,
        Map<String, Set<Integer>> mapOfStringIntegerSet,
        Map<String, Set<List<Double>>> mapOfStringSetListDouble,
        List<Map<String, Set<Integer>>> listOfMapStringSetInteger,
        Map<String, Set<List<Integer>>> mapOfStringSetListInteger
    ) {
        // This method is only used to get the parameter types
    }

    @SuppressWarnings("unused")
    private void boundedWildcardValueParams(
        List<? extends Number> listExtendsNumber,
        List<? super Integer> listSuperInteger,
        Set<? extends Number> setExtendsNumber,
        Set<? super Integer> setSuperInteger,
        Map<? extends String, ? extends Number> mapExtendsStringExtendsNumber,
        Map<String, ? extends Number> mapStringExtendsNumber,
        Map<? super String, ? super Number> mapSuperStringSuperNumber
    ) {
        // This method is only used to get the parameter types
    }

    @SuppressWarnings({"unused", "OptionalUsedAsFieldOrParameterType"})
    private void otherGenericValueParams(
        Optional<String> optionalString,
        Optional<Integer> optionalInteger,
        Optional<List<String>> optionalListString,
        Optional<List<Integer>> optionalListInteger,
        Optional<Set<String>> optionalSetString,
        Optional<Map<String, Integer>> optionalMapStringInteger
    ) {
        // This method is only used to get the parameter types
    }

    @SuppressWarnings("unused")
    private void multipleWildcardValueParams(
        List<List<?>> listOfListWildcard,
        Set<Set<?>> setOfSetWildcard,
        Map<?, ?> mapWildcardWildcard,
        Set<Map<?, ?>> setOfMapWildcard,
        Map<?, List<?>> mapWildcardListWildcard,
        Map<String, ?> mapStringWildcard,
        List<Map<?, ?>> listOfMapWildcard
    ) {
        // This method is only used to get the parameter types
    }

    // Type converters for ParameterFixture parameters - intentionally multiple to test error handling
    @TypeConverter
    public static Age parseAge(int age) {
        throw new RuntimeException("Type converter is one of multiple applicable, should not be called");
    }

    @TypeConverter
    public static Age parseAge(String age) {
        throw new RuntimeException("Type converter is one of multiple applicable, should not be called");
    }

}
