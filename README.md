# TableTest

TableTest is an [extension to JUnit 5](#installation) for data-driven testing. It allows you to express how the system is expected to behave through multiple examples in a concise table format. This reduces the amount of test code and makes it easier to understand, extend, and maintain your tests.

```java
@TableTest("""
    Input | Expected
    1     | one
    2     | two
    3     | three
    """)
void testNumberToWord(int number, String word) {
    assertEquals(word, NumberConverter.toWord(number));
}
```

Acting as a [parameterized test](https://junit.org/junit5/docs/5.12.1/user-guide/index.html#writing-tests-parameterized-tests), a TableTest will run the test method multiple times with the values of each table row provided as arguments. Values are automatically converted to the type of the test method parameter.

**Requirements**: TableTest requires Java 21 or higher and JUnit Jupiter 5.11.0 to 5.12.2.

**Installation**: See the [Installation](#installation) section.

**IDE Support**: The [TableTest plugin for IntelliJ](https://plugins.jetbrains.com/plugin/27334-tabletest) provides auto-formatting, syntax highlighting, and shortcuts for working with tables.

**Latest Updates**: See the [changelog](https://github.com/nchaugen/tabletest/blob/main/CHANGELOG.md) for details on recent releases and changes.

## Why TableTest?
TableTest makes your tests more:
- **Readable**: Structured tables clearly show inputs and expected outputs
- **Maintainable**: Add or modify test cases by simply adding or changing table rows
- **Concise**: Eliminate repetitive test code while increasing test coverage
- **Self-documenting**: Tables serve as built-in documentation of expected system behaviour
- **Collaborative**: Non-technical stakeholders can understand and contribute to test cases


## Table of Contents
- [Why TableTest?](#why-tabletest)
- [Usage](#usage)
- [Value Formats](#value-formats)
  - [Single Values](#single-values)
  - [List Values](#list-values)
  - [Set Values](#set-values)
  - [Map Values](#map-values)
  - [Nested Values](#nested-values)
- [Value Conversion](#value-conversion)
  - [JUnit Built-In Conversion](#junit-built-in-conversion)
  - [Factory Method Conversion](#factory-method-conversion)
  - [Explicit Argument Conversion](#explicit-argument-conversion)
- [Other Features](#other-features)
  - [Scenario Names](#scenario-names)
  - [Null Values](#null-values)
  - [Set of Applicable Values](#set-of-applicable-values)
  - [Comments and Blank Lines](#comments-and-blank-lines)
  - [Table in External File](#table-in-external-file)
- [Installation](#installation)
  - [Requirements](#requirements)
  - [Using TableTest with JUnit 5.13.0](#using-tabletest-with-junit-5130)
  - [Using TableTest with JUnit 5.12.2](#using-tabletest-with-junit-5122)
  - [Using TableTest with JUnit 5.11.0 to 5.12.1](#using-tabletest-with-junit-5110-to-5121)
  - [Using TableTest with JUnit versions prior to 5.11.0](#using-tabletest-with-junit-versions-prior-to-5110)
- [IDE Support](#ide-support)
- [License](#license)

## Usage
TableTest-style test methods are declared using the `@TableTest` annotation. The annotation accepts a table of data as a multi-line string or as an [external resource](#table-in-external-file). 

```java
@TableTest("""
    Scenario                              | Year | Is leap year?
    Years not divisible by 4              | 2001 | false
    Years divisible by 4                  | 2004 | true
    Years divisible by 100 but not by 400 | 2100 | false
    Years divisible by 400                | 2000 | true
    """)
public void leapYearCalculation(Year year, boolean expectedResult) {
    assertEquals(expectedResult, year.isLeap(), "Year " + year);
}
```

Tables use the pipe character (`|`) to separate columns and newline to separate rows. The first row is the header containing column names. The following lines are data rows. 

Each data row will invoke the test method with the cell values being passed as arguments. Optionally, the first column may contain a [scenario name](#scenario-names) describing the situation being exemplified by each row. There must be a test method parameter for each value column (scenario name column excluded). Columns map to parameters based strictly on order, so the first value column maps to the first parameter, the second value column to the second parameter, etc. The column header names and parameter names can be different, but keeping them aligned improves readability. 

Column values can be [single values](#single-values), [lists](#list-values), [sets](#set-values), or [maps](#map-values). Values are automatically converted to the type of the corresponding test parameter.

Technically `@TableTest` is a JUnit `@ParameterizedTest` with a custom-format argument source. Like regular JUnit test methods, `@TableTest` methods must not be `private` or `static` and must not return a value.

## Value Formats
The TableTest format supports three types of values: [single values](#single-values), [lists](#list-values), [sets](#set-values), and [maps](#map-values).

### Single Values
Single values can appear with or without quotes. Surrounding single (`'`) or double (`"`) quotes are required when the value contains a `|` character, or starts with `[` or `{`. For single values appearing as elements in a list, set, or map (see below), the characters `,`, `:`, `]`, and `}` also make the value require quotes.

Whitespace around unquoted values is trimmed. To preserve leading or trailing whitespace, use quotes. Empty values are represented by adjacent quote pairs (`""` or `''`). [Null values](#null-values) are represented by a blank cell.

```java

@TableTest("""
    Value                  | Length?
    Hello, world!          | 13
    "cat file.txt | wc -l" | 20
    "[]"                   | 2
    ''                     | 0
    """)
void testString(String value, int expectedLength) {
    assertEquals(expectedLength, value.length());
}
```

### List Values
Lists are enclosed in square brackets with comma-separated elements. Lists can contain single values or compound values (nested lists/sets/maps). Empty lists are represented by `[]`.

```java
@TableTest("""
    List      | size? | sum?
    []        | 0     | 0
    [1]       | 1     | 1
    [3, 2, 1] | 3     | 6
    """)
void integerList(List<Integer> list, int expectedSize, int expectedSum) {
  assertEquals(expectedSize, list.size());
  assertEquals(expectedSum, list.stream().mapToInt(Integer::intValue).sum());
}
```

### Set Values
Sets are enclosed in curly braces with comma-separated elements. Sets can contain single values or compound values (nested lists/sets/maps). Empty sets are represented by `{}`.

```java
@TableTest("""
    Set              | Size?
    {1, 2, 3, 2, 1}  | 3
    {Hello, Hello}   | 1
    {}               | 0
    """)
void testSet(Set<String> set, int expectedSize) {
    assertEquals(expectedSize, set.size());
}
```

### Map Values
Maps use square brackets with comma-separated key-value pairs. Colons separate keys and values. Keys must be unquoted single values and cannot contain characters `,`, `:`, `|`, `[`, `]`, `{`, or `}`. Values can be single (unquoted/quoted) or compound (list/set/map). Empty maps are represented by `[:]`.

```java

@TableTest("""
    Map                        | Size?
    [one: 1, two: 2, three: 3] | 3
    [:]                        | 0
    """)
void testMap(Map<String, Integer> map, int expectedSize) {
    assertEquals(expectedSize, map.size());
}
```

### Nested Values
TableTest supports nesting compound types (lists, sets, maps). 

```java
@TableTest("""
    Student grades                                                  | Highest grade? | Average grade? | Pass count?
    [Alice: [95, 87, 92], Bob: [78, 85, 90], Charlie: [98, 89, 91]] | 98             | 89.4           | 3
    [David: [45, 60, 70], Emma: [65, 70, 75], Frank: [82, 78, 60]]  | 82             | 67.2           | 2
    [:]                                                             | 0              | 0.0            | 0
    """)
void testNestedParameterizedTypes(
    Map<String, List<Integer>> studentGrades,
    int expectedHighestGrade,
    double expectedAverageGrade,
    int expectedPassCount
) {
    Students students = fromGradesMap(studentGrades);
    assertEquals(expectedHighestGrade, students.highestGrade());
    assertEquals(expectedAverageGrade, students.averageGrade(), 0.1);
    assertEquals(expectedPassCount, students.passCount());
}
```

## Value Conversion
TableTest processes the table in two steps:
1. Parsing
2. Value Conversion

Parsing verifies that the table is formatted according to valid syntax and collects the cell values from the table. Compound cell values are captured as their corresponding `List`, `Set`, and `Map` types according to syntax. Single values are captured as type `String`. 

TableTest continues to convert the captured cell values to the type required by the test method parameter. This eliminates the need for manual conversion in your test method, keeping tests focused on invoking the system under test and asserting the results.

TableTest will try one of the following strategies to perform the required conversion (in this order):

1. Using explicit argument converter specified with JUnit `@ConvertWith` annotation 
2. Using a factory method found in the test class
3. Using a factory method found via `@FactorySources` annotation on test class
4. Using JUnit built-in type conversion

Let us look into how each of these strategies works, starting from the bottom.


### JUnit Built-In Conversion
TableTest leverages [JUnit's built-in type converters](https://junit.org/junit5/docs/5.12.2/user-guide/index.html#writing-tests-parameterized-tests-argument-conversion-implicit) for automatic conversion of parsed String values to various types.

#### How It Works
Using JUnit conversion service, TableTest will automatically convert a single value to any of the following types:
* Boolean
* Byte, Character
* Short, Integer, Long, Float, Double 
* java.math.BigDecimal, java.math.BigInteger
* Enum subclasses
* java.io.File, java.nio.file.Path
* java.net.URI, java.net.URL
* java.nio.charset.Charset
* java.lang.Class
* java.time.Duration, java.time.Instant, java.time.Period
* java.time.LocalDateTime, java.time.LocalDate, java.time.LocalTime
* java.time.MonthDay, java.time.YearMonth, java.time.Year
* java.time.OffsetDateTime, java.time.OffsetTime
* java.time.ZonedDateTime, java.time.ZoneId, java.time.ZoneOffset
* java.util.Currency, java.util.Locale
* java.util.UUID

For any other types it will try to find either a [factory method or factory constructor in the target type](https://junit.org/junit5/docs/5.12.2/user-guide/index.html#writing-tests-parameterized-tests-argument-conversion-implicit-fallback) accepting a single String parameter and use this to make the conversion. 

```java
@TableTest("""
    Number | Text | Date       | Class
    1      | abc  | 2025-01-20 | java.lang.Integer
    """)
void singleValues(short number, String text, LocalDate date, Class<?> type) {
    // test implementation
}
```

#### Parameterized Types
Parsed compound values like List, Set, and Map will also benefit from built-in conversion to match parameterized types. For example, `[1, 2, 3]` becomes `List<Integer>` when the parameter is declared as such. Even nested values are traversed and converted to match parameterized types. Map keys remain String type and are not converted.

In the example below, the list of grades inside the map is converted to `List<Integer>`:

```java
@TableTest("""
    Grades                                       | Highest Grade?
    [Alice: [95, 87, 92], Bob: [78, 85, 90]]     | 95
    [Charlie: [98, 89, 91], David: [45, 60, 70]] | 98
    """)
void testParameterizedTypes(Map<String, List<Integer>> grades, int expectedHighestGrade) {
    // test implementation
}
```


### Factory Method Conversion
Before falling back to [JUnit conversion service](#junit-built-in-conversion), TableTest will look for a factory method present in either the test class or in one of the classes listed by a `@FactorySources` annotation. If found, TableTest will use this factory method to convert the parsed value to the required test parameter type. 

#### How It Works
A factory method will be used when it:
1. Is defined as a public static method in a public class
2. Accepts exactly one parameter
3. Returns an object of the target parameter type
4. Is the only method matching the above criteria in the class

There is no specific naming pattern for factory methods, any method fulfilling the requirements above will be considered.

#### Factory Method Search Strategy in Java
TableTest uses the following strategy to search for factory methods in Java test classes: 

1. Search current test class
2. In case of a `@Nested` test class, search enclosing classes, starting with the direct outer class
3. Search classes in the order they are listed by a `@FactorySources` annotation on current test class
4. In case of a `@Nested` test class, search classes listed by `@FactorySources` of enclosing classes, starting with the direct outer class

TableTest will stop searching as soon as it finds a matching factory method and use this for the conversion.

#### Factory Method Search Strategy in Kotlin
For tests written in Kotlin, factory methods can be declared in two ways to become static methods:

1. In the companion object of a test class using [`@JvmStatic` annotation](https://kotlinlang.org/docs/java-to-kotlin-interop.html#static-methods)
2. At [package-level](https://kotlinlang.org/docs/java-to-kotlin-interop.html#package-level-functions) in the file containing the test class.

In Kotlin, a `@Nested` test class must be declared `inner class` and these are not allowed to have companion objects. Hence, test class factory methods must be either declared in the companion object of the outer class (with `@JvmStatic`) or at package level in the same file as the test class.

So for Kotlin, the search strategy becomes as follows:

1. Search the current file (methods declared at package-level or in outer class companion object)
2. Search classes in the order they are listed by a `@FactorySources` annotation on current test class
3. In case of a `@Nested` test class, search classes listed by `@FactorySources` of enclosing classes, starting with direct outer

As for Java, TableTest will stop searching as soon as it finds a matching factory method and use this for the conversion.

#### Factory Sources in Kotlin
The recommended solution for implementing factory sources in Kotlin is to define them as `object` and annotate factory methods with `@JvmStatic`:

```kotlin
object KotlinFactorySource { 
    @JvmStatic
    fun toAges(input: Map<String, List<Int>>): Ages {
        // implementation
    }
}
```

Usage:

```kotlin
@FactorySources(KotlinFactorySource::class)
```

Alternatively, regular Kotlin classes with factory methods defined as `@JvmStatic` in a companion object can also be referenced.


#### Overriding Built-In Conversion
As TableTest will prefer using a factory method over the built-in conversion, it is possible to override the built-in conversion of specific types. The example below demonstrates this, allowing conversion to LocalDate to understand certain custom constant values.

```java
@TableTest("""
    This Date  | Other Date | Is Before?
    today      | tomorrow   | true
    today      | yesterday  | false
    2024-02-29 | 2024-03-01 | true
    """)
void testIsBefore(LocalDate thisDate, LocalDate otherDate, boolean expectedIsBefore) {
  assertEquals(expectedIsBefore, thisDate.isBefore(otherDate));
}

public static LocalDate parseLocalDate(String input) {
    return switch (input) {
        case "yesterday" -> LocalDate.parse("2025-06-06");
        case "today" -> LocalDate.parse("2025-06-07");
        case "tomorrow" -> LocalDate.parse("2025-06-08");
        default -> LocalDate.parse(input);
    };
}
```

#### Conversion to Factory Method Parameter
Having selected a factory method with a return type matching the test parameter type, TableTest will consider if the parsed value matches the parameter type of the factory method. If not, it will attempt to convert the value to match the parameter type. 

Building on the [example from earlier](#nested-values), we can create a factory method to directly accept a `Students` parameter instead of manually converting it in the test method:

```java
@TableTest("""
    Student grades                                                  | Highest Grade? | Average Grade? | Pass Count?
    [Alice: [95, 87, 92], Bob: [78, 85, 90], Charlie: [98, 89, 91]] | 98             | 89.4           | 3
    [David: [45, 60, 70], Emma: [65, 70, 75], Frank: [82, 78, 60]]  | 82             | 67.2           | 2
    [:]                                                             | 0              | 0.0            | 0
    """)
void testWithCustomConverter(
    Students students,  // Now using the custom type directly
    int expectedHighestGrade,
    double expectedAverageGrade,
    int expectedPassCount
) {
    assertEquals(expectedHighestGrade, students.highestGrade());
    assertEquals(expectedAverageGrade, students.averageGrade(), 0.1);
    assertEquals(expectedPassCount, students.passCount());
}

// Factory method for conversion
public static Students fromGradesMap(Map<String, List<Integer>> input) {
  // mapping implementation  
}
```

In this example:
1. The first parameter is now directly of type `Students` instead of `Map<String, List<Integer>>`
2. TableTest starts converting the parsed value of type `Map<String, List<String>>` to the parameter type
3. Seeing the required type `Students`, TableTest searches for a factory method returning this type and selects `fromGradesMap` in the test class
4. Seeing that `fromGradesMap` requires a parameter of type `Map<String, List<Integer>>`, TableTest starts converting the parsed value of type `Map<String, List<String>>` to this type
5. When converting the list elements, TableTest looks for a factory method returning type `Integer`
6. Finding none, it falls back to the built-in conversion
7. Having successfully converted the value to `Map<String, List<Integer>>`, TableTest invokes the factory method `fromGradesMap` it found earlier with the converted value
8. The factory method turns this into a `Students` object that TableTest can pass on to the test


### Explicit Argument Conversion
In addition to implicitly called factory methods and built-in conversion, TableTest supports JUnit [explicit argument conversion](https://junit.org/junit5/docs/5.12.1/user-guide/index.html#writing-tests-parameterized-tests-argument-conversion-explicit) as well as JUnit [argument aggregation](https://junit.org/junit5/docs/5.12.1/user-guide/index.html#writing-tests-parameterized-tests-argument-aggregation). This can be used for explicit conversion to custom types.

As there is no parameter type information available in the ArgumentConverter interface, custom ArgumentConverters will receive the [parsed value](#value-conversion). In the example below, the value of the `source` parameter received by `PersonConverter.convert` will be of type `Map<String, String>`. However, since the `ArgumentConverter` interface specifies `source` parameter as type `Object`, the value needs to be inspected and processed using `instanceof`.  

```java
@TableTest("""
    Person                 | AgeCategory?
    [name: Fred, age: 22]  | ADULT
    [name: Wilma, age: 19] | TEEN
    """)
void testExplicitConversion(
    @ConvertWith(PersonConverter.class) Person person, 
    AgeCategory expectedAgeCategory
) {
    assertEquals(expectedAgeCategory, person.ageCategory());
}

record Person(String firstName, String lastName, int age) {
    AgeCategory ageCategory() {
        return AgeCategory.of(age);
    }
}

enum AgeCategory {
    CHILD, TEEN, ADULT;

    static AgeCategory of(int age) {
        if (age < 13) return AgeCategory.CHILD;
        if (age < 20) return AgeCategory.TEEN;
        return AgeCategory.ADULT;
    }
}

private static class PersonConverter implements ArgumentConverter {
    @Override
    public Object convert(Object source, ParameterContext context) throws ArgumentConversionException {
        if (source instanceof Map attributes) {
            return new Person(
                (String) attributes.getOrDefault("name", "Fred"),
                "Flintstone",
                Integer.parseInt((String) attributes.getOrDefault("age", "16"))
            );
        }
        throw new ArgumentConversionException("Cannot convert " + source.getClass().getSimpleName() + " to Person");
    }
}
```

## Other Features
TableTest contains a number of other useful features for expressing examples in a table format.

### Scenario Names
TableTest supports providing a scenario name describing the situation being exemplified by each row. This makes the tests easier to understand and failures easier to diagnose. For scenario naming, add one extra column at the beginning of your table. This column's values will be used as test case names and will not be mapped to any method parameters.

```java
@TableTest("""
    Scenario                              | Year | Is leap year?
    Years not divisible by 4              | 2001 | false
    Years divisible by 4                  | 2004 | true
    Years divisible by 100 but not by 400 | 2100 | false
    Years divisible by 400                | 2000 | true
    """)
public void testLeapYear(Year year, boolean expectedResult) {
    assertEquals(expectedResult, year.isLeap(), "Year " + year);
}
```

In test reports, each test case will be identified by its scenario name rather than the default parameter values, improving test readability. Scenario names do not affect the test execution logic.

### Null Values
Blank cells and empty quoted values will translate to `null` for all parameter types except primitives. For primitives, it will cause an exception as they cannot represent a `null` value.

The built-in conversion will **not** translate empty quoted values to `null` when they appear as elements inside lists, sets, and maps. This will instead cause a `ConversionException`. Consider using a [factory method](#factory-method-conversion) if you need this.

```java
@TableTest("""
    Scenario            | String | Integer | List | Map | Set
    Blank               |        |         |      |     |
    Empty single quoted | ''     | ''      | ''   | ''  | ''
    Empty double quoted | ""     | ""      | ""   | ""  | ""
    """)
void testBlankMeansNull(String scenario, String string, Integer integer, List<?> list, Map<String, ?> map, Set<?> set) {
    if ("Blank".equals(scenario)) assertNull(string);
    else assertEquals("", string);
    assertNull(integer);
    assertNull(list);
    assertNull(map);
    assertNull(set);
}
```

### Set of Applicable Values
TableTest supports using the set format to specify multiple examples of values that are applicable for the current scenario. This is a powerful feature that can be used to contract multiple scenarios that have identical expectations. 

When a cell contains a set (enclosed in curly braces) and the corresponding parameter isn't declared as a `Set` type, TableTest will invoke this row multiple times — one time for each value in the set — while maintaining all other parameter values. 

In the example below, the test method will be invoked 12 times, three times for each row, once for each value in the set in column `Example years`.

```java
@TableTest("""
    Scenario                              | Example years      | Is leap year?
    Years not divisible by 4              | {2001, 2002, 2003} | false
    Years divisible by 4                  | {2004, 2008, 2012} | true
    Years divisible by 100 but not by 400 | {2100, 2200, 2300} | false
    Years divisible by 400                | {2000, 2400, 2800} | true
    """)
public void testLeapYear(Year year, boolean expectedResult) {
    assertEquals(expectedResult, year.isLeap(), "Year " + year);
}
```
When multiple cells in the same row contain applicable value sets, TableTest will perform a cartesian product, generating test cases for all possible combinations of values. This example will invoke the test method 6 times for each row, with each possible combination of the provided x and y values.

```java
@TableTest("""
    Scenario       | x         | y       | even sum?
    Even plus even | {2, 4, 6} | {8, 10} | true
    Odd plus even  | {1, 3, 5} | {6, 8}  | false
    """)
void testEvenOddSums(int x, int y, boolean expectedResult) {
    boolean isEvenSum = (x + y) % 2 == 0;
    assertEquals(expectedResult, isEvenSum);
}
```

Use applicable value sets judiciously. The number of test cases grows multiplicatively with each additional set (two sets of size 10 generate 100 test invocations), which can significantly increase test execution time.

Sets are only expanded when the parameter type doesn't match `Set<?>`. When the parameter is declared as a set type, the entire set is passed as a single argument: 

```java
@TableTest("""
    Values       | Size?
    {1, 2, 3}    | 3
    {a, b, c, d} | 4
    {}           | 0
    """)
void testSetParameter(Set<String> values, int expectedSize) {
    assertEquals(expectedSize, values.size());
}
```

### Comments and Blank Lines
Lines starting with `//` (ignoring leading whitespace) are treated as comments and ignored. Comments allow adding explanations or temporarily disabling data rows.

Blank lines are also ignored and can be used to visually group related rows.

```java
@TableTest("""
    String         | Length?
    
    Hello world    | 11

    // The next row is currently disabled
    // "World, hello" | 12

    // Special characters must be quoted
    '|'            | 1
    '[:]'          | 3
    """)
void testComment(String string, int expectedLength) {
    assertEquals(expectedLength, string.length());
}
```

### Table in External File
As an alternative to specifying the table as a multi-line string in the annotation, you can load it from an external file using the `resource` attribute. The file is located as a resource relative to the test class and is typically stored in the test `resources` directory or one of its subdirectories.

By default, the file is assumed to use UTF-8 encoding. If your file uses a different encoding, specify it with the `encoding` attribute.

```java
@TableTest(resource = "/external.table")
void testExternalTable(int a, int b, int sum) {
    assertEquals(sum, a + b);
}

@TableTest(resource = "/custom-encoding.table", encoding = "ISO-8859-1")
void testExternalTableWithCustomEncoding(String string, int expectedLength) {
    assertEquals(expectedLength, string.length());
}
```


### Escape Sequences
Escape sequence handling varies depending on the programming language used for the test. 

#### Java Text Blocks
When providing the table using text blocks in Java, all Java escape sequences like `\t`, `\"`, `\\`, `\uXXXX`, `\XXX`, etc. are processed by the Java compiler before handed to TableTest:

```java
    @TableTest("""
        Scenario                                | Input      | Length?
        Tab character processed by compiler     | a\tb       | 3
        Quote marks processed by compiler       | Say \"hi\" | 8
        Backslash processed by compiler         | path\\file | 9
        Unicode character processed by compiler | \u0041B    | 2
        Octal character processed by compiler   | \101B      | 2
        """)
void testEscapeSequences(String input, int expectedLength) {
  assertEquals(expectedLength, input.length());
}
```

#### Kotlin Raw Strings
Using Kotlin raw strings, escape sequences are **not** processed. They remain as literal backslash characters:

```kotlin
@TableTest(
    """
    Scenario                                    | Input      | Length?
    Tab character NOT processed by compiler     | a\tb       | 4
    Quote marks NOT processed by compiler       | Say \"hi\" | 10
    Backslash NOT processed by compiler         | path\\file | 10
    Unicode character NOT processed by compiler | \u0041B    | 7
    Octal character NOT processed by compiler   | \101B      | 5
    """)
fun testEscapeSequences(input: String, expectedLength: Int) {
    assertEquals(expectedLength, input.length)
}
```

#### External File 
Table files are read as raw text independent of programming language, meaning escape sequences are **not** processed and remain literal.

#### Workarounds
If you need special characters in Kotlin or external Table files, you have three options:

1. Use actual characters instead of escape sequences
2. Use Kotlin regular strings for simple cases
3. Consider switching to Java for tests requiring complex escape sequences.


## Installation

### Requirements
TableTest requires **Java version 21 or higher**.

TableTest version 0.4.0 is compatible with **JUnit Jupiter versions from 5.11.0 up to and including 5.12.2**.

### Using TableTest with JUnit 5.13.0
Please note that **TableTest version 0.4.0 is not compatible with recently released JUnit Jupiter 5.13.0**. This is due to an interface change in AnnotationBasedArgumentsProvider. This will be addressed in a future version of TableTest.

### Using TableTest with JUnit 5.12.2
To use TableTest with JUnit Jupiter **5.12.2**, simply add `tabletest-junit` as a test scope dependency alongside `junit-jupiter`.

#### Maven (pom.xml)
```xml
<dependencies>
    <dependency>
        <groupId>io.github.nchaugen</groupId>
        <artifactId>tabletest-junit</artifactId>
        <version>0.4.0</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.12.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

#### Gradle with Groovy DSL (build.gradle)
```groovy
dependencies {
    testImplementation 'io.github.nchaugen:tabletest-junit:0.4.0'    
    testImplementation 'org.junit.jupiter:junit-jupiter:5.12.2'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

tasks.named('test', Test) {
    useJUnitPlatform()
}
```
#### Gradle with Kotlin DSL (build.gradle.kts)
```kotlin
dependencies { 
    testImplementation("io.github.nchaugen:tabletest-junit:0.4.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2") 
    testRuntimeOnly("org.junit.platform:junit-platform-launcher") 
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
```

### Using TableTest with JUnit 5.11.0 to 5.12.1
TableTest version 0.4.0 supports JUnit Jupiter versions 5.11.0 up to and including 5.12.2. For projects using JUnit Jupiter versions in this range, but prior to 5.12.2, you need to exclude the transitive dependencies to avoid conflicts.

#### Maven (pom.xml)
```xml
<dependencies>
    <dependency>
        <groupId>io.github.nchaugen</groupId>
        <artifactId>tabletest-junit</artifactId>
        <version>0.4.0</version>
        <scope>test</scope>
        <exclusions>
            <exclusion>
                <groupId>org.junit.jupiter</groupId>
                <artifactId>junit-jupiter-params</artifactId>
            </exclusion>
            <exclusion>
                <groupId>org.junit.platform</groupId>
                <artifactId>junit-platform-commons</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.11.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

#### Gradle with Groovy DSL (build.gradle)
```groovy
dependencies { 
    testImplementation('io.github.nchaugen:tabletest-junit:0.4.0') { 
        exclude group: 'org.junit.jupiter', module: 'junit-jupiter-params' 
        exclude group: 'org.junit.platform', module: 'junit-platform-commons' 
    }
    testImplementation 'org.junit.jupiter:junit-jupiter:5.11.0' 
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher' 
}
tasks.named('test', Test) { 
    useJUnitPlatform() 
}
```

#### Gradle with Kotlin DSL (build.gradle.kts)
```kotlin
dependencies { 
    testImplementation("io.github.nchaugen:tabletest-junit:0.4.0") {
        exclude(group = "org.junit.jupiter", module = "junit-jupiter-params") 
        exclude(group = "org.junit.platform", module = "junit-platform-commons") 
    } 
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0") 
    testRuntimeOnly("org.junit.platform:junit-platform-launcher") }

tasks.named<Test>("test") {
    useJUnitPlatform()
}
```
### Using TableTest with JUnit versions prior to 5.11.0
Unfortunately, TableTest is not supported for JUnit Jupiter versions prior to 5.11.0. If your project is currently using an older version of JUnit, you will need to upgrade to a supported version to be able to use TableTest.


## IDE Support
The [TableTest plugin for IntelliJ](https://plugins.jetbrains.com/plugin/27334-tabletest) enhances your development experience when working with TableTest format tables. The plugin provides:

- Code assistance for table formatting
- Syntax highlighting for table content
- Visual feedback for invalid table syntax

Installing the plugin streamlines the creation and maintenance of data-driven tests, making it easier to work with both inline and external table files.


## License
TableTest is licensed under the liberal and business-friendly [Apache Licence, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.html) and is freely available on [GitHub](https://github.com/nchaugen/tabletest). 

Additionally, the `tabletest-junit` distribution uses the following modules from JUnit 5 which is released under [Eclipse Public License 2.0](https://raw.githubusercontent.com/junit-team/junit5/refs/heads/main/LICENSE.md):
- `org.junit.jupiter:junit-jupiter-params`
- `org.junit.platform:junit-platform-commons`

TableTest binaries are published to the repositories of Maven Central. The artefacts signatures can be validated against [this PGP public key](https://keyserver.ubuntu.com/pks/lookup?search=nchaugen%40gmail.com).
