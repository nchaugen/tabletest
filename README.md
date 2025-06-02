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
- [Usage](#usage)
- [Value Formats](#value-formats)
  - [Single Values](#single-values)
  - [List Values](#list-values)
  - [Set Values](#set-values)
  - [Map Values](#map-values)
  - [Nested Values](#nested-values)
- [Value Conversion](#value-conversion)
  - [Implicit Type Conversion of Single Values](#implicit-type-conversion-of-single-values) 
  - [Custom Type Conversion](#custom-type-conversion)
  - [Explicit Type Conversion](#explicit-type-conversion)
- [Other Features](#other-features)
  - [Scenario Names](#scenario-names)
  - [Null Values](#null-values)
  - [Set of Applicable Values](#set-of-applicable-values)
  - [Comments and Blank Lines](#comments-and-blank-lines)
  - [Table in External File](#table-in-external-file)
- [Installation](#installation)
  - [Requirements](#requirements)
  - [Using TableTest with JUnit Jupiter 5.12.2](#using-tabletest-with-junit-jupiter-5122)
  - [Using TableTest with JUnit Jupiter 5.11.0 to 5.12.1](#using-tabletest-with-junit-jupiter-5110-to-5121)
  - [Using TableTest with JUnit versions prior to 5.11.0](#using-tabletest-with-junit-versions-prior-to-5110)
- [IDE Support](#ide-support)
- [License](#license)

## Usage
TableTest-style test methods are declared using the `@TableTest` annotation. The annotation accepts a table of data as a multi-line string or as an external resource. 

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

Each data row will invoke the test method with the column values being passed as arguments. Optionally, the first column may contain a [scenario name](#scenario-names) describing the situation being exemplified by each row. There must be a test method parameter for each value column (scenario name column excluded). Columns map to parameters based strictly on order, so the first value column maps to the first parameter, the second value column to the second parameter, etc. The column header names and parameter names can be different, but keeping them aligned improves readability. 

Column values can be [single values](#single-values), [lists](#list-values), [sets](#set-values), or [maps](#map-values). Values are automatically converted to the type of the corresponding test parameter.

Technically `@TableTest` is implemented as a JUnit `@ParameterizedTest` with a custom-format argument source. Like regular JUnit test methods, `@TableTest` methods must not be `private` or `static` and must not return a value.

## Value Formats
TableTest supports three types of values: [single values](#single-values), [lists](#list-values), [sets](#set-values), or [maps](#map-values).

### Single Values
Single values can appear with or without quotes. Unquoted values must not contain `[`, `|`, `,`, or `:` characters. These special characters require single or double quotes.

Whitespace around unquoted values is trimmed. To preserve leading or trailing whitespace, use quotes. Empty values are represented by adjacent quote pairs (`""` or `''`).

```java

@TableTest("""
    Value          | Length?
    Hello world    | 11
    "World, hello" | 12
    "|"            | 1
    ''             | 0
    """)
void testString(String value, int expectedLength) {
    assertEquals(expectedLength, value.length());
}
```

### List Values
Lists are enclosed in square brackets with comma-separated elements. Lists can contain single values or compound values (nested lists/maps). Empty lists are represented by `[]`.

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
Maps use square brackets with comma-separated key-value pairs. Colons separate keys and values. Keys must be unquoted single values, while values can be single or compound. Empty maps are represented by `[:]`.

```java

@TableTest("""
    Map                        | Size?
    [one: 1, two: 2, three: 3] | 2
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
After parsing, compound values are represented as `List`, `Set`, and `Map` according the the value specification, but all single values are represented as a `String` type. TableTest will then attempt to convert the value to the type required by the method parameter.

### Implicit Type Conversion of Single Values
TableTest leverages [JUnit Jupiter's built-in implicit type converters](https://junit.org/junit5/docs/5.12.1/user-guide/index.html#writing-tests-parameterized-tests-argument-conversion) for automatic conversion of single values to various types.

For example:
- String `"42"` converts to `int`, `long`, or Integer value `42`
- String `"true"` converts to `boolean` or `Boolean` value `true`
- String `"2024-01-15"` converts to `LocalDate` for January 15, 2024

```java
@TableTest("""
    Number | Text | Date       | Class
    1      | abc  | 2025-01-20 | java.lang.Integer
    """)
void singleValues(short number, String text, LocalDate date, Class<?> type) {
    // test implementation
}
```

Compound values like list, set, and map also benefit from implicit conversion to match parameterized types. For example, `[1, 2, 3]` becomes `List<Integer>` when the parameter is declared as such. Even nested values are traversed and converted to match parameterized types. Map keys remain String type and are not converted.

### Custom Type Conversion
TableTest supports using static conversion methods defined in your test class to help convert table values to declared parameter types. This eliminates the need for manual conversion in your test method, keeping tests focused on invoking the system under test and asserting the results.

#### How It Works
Custom type converters are static methods in your test class that:
1. Accept one parameter
2. Return an object of the parameter type
3. Are accessible (public, protected, or package-private)

There is no specific naming pattern for custom type converters, any static method fulfilling the requirements above will be considered. However only one converter method per return type is possible. If TableTest finds multiple it will throw an error. 

When a converted value is not directly assignable to the parameter type, TableTest searches for an applicable custom converter method and if found will use it for the conversion. This happens before implicit conversion, so it is possible to override the JUnit type converters to convert the same format Strings to another type. If no applicable type converter is found, TableTest will fallback to try JUnit implicit conversion.

#### Example
Building on the previous "Nested Values" example, we can create a custom type converter to directly accept a `Students` parameter instead of manually converting it in the test method:

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

// This static method serves as a custom converter
static Students fromGradesMap(Map<String, List<Integer>> input) {
  // mapping implementation  
}
```

In this example:
1. The first parameter is now directly of type `Students` instead of `Map<String, List<Integer>>`
2. TableTest starts converting the parsed value of type `Map<String, List<String>>` to the parameter type
3. Seeing the required type `Students`, TableTest searches for a customer converter method returning this type
4. Finding `fromGradesMap` it sees that this requires a parameter of type `Map<String, List<Integer>>`
5. TableTest then converts the parsed value `Map<String, List<String>>` to `Map<String, List<Integer>>`
6. When converting the list elements, TableTest looks for a custom converter method returning type `Integer`
7. Finding none, it falls back to using JUnit implicit conversion
8. Having successfully converted the value to `Map<String, List<Integer>>`, TableTest invokes the custom converter `fromGradesMap` it found earlier with the converted value
9. The custom converter method turns this into a `Students` object that TableTest can pass on to the test

### Explicit Type Conversion
In addition to automatic type conversion, TableTest supports JUnit standard [explicit argument conversion](https://junit.org/junit5/docs/5.12.1/user-guide/index.html#writing-tests-parameterized-tests-argument-conversion-explicit) or [argument aggregation](https://junit.org/junit5/docs/5.12.1/user-guide/index.html#writing-tests-parameterized-tests-argument-aggregation). This can be used for conversion to custom types not supported by implicit conversion.

The custom ArgumentConverters will receive the parsed value, so compound types have been converted but all single values are of type `String`. In the example below, the value of the `source` parameter received by `PersonConvert.convert` will be of type `Map<String, String>`. However, since the `ArgumentConverter` interface specifies `source` parameter as type `Object`, the value needs to be inspected and processed using `instanceof`.  

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
TableTest supports providing a scenario name describing the situation being exemplified by each row. This makes the tests easier to understand and failures easier to diagnose. For scenario naming, add one extra column at the beginning of your table. This column's values will be used as test case names and won't be mapped to any method parameters.

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
Blank cells and empty quoted values will translate to `null` for all parameter types except String and primitives. For String the value will be the empty string, and for primitives it will cause an exception as they cannot represent a `null` value.

The built-in conversion will **not** translate empty quoted values to `null` when they appear as elements inside lists, sets, and maps, even when the parameterized value of the collection is not String. This will instead cause a `ConversionException`. Consider writing a [custom type converter](#custom-type-conversion) if you need this.

```java
@TableTest("""
    Scenario            | String | Integer | List | Map | Set
    Blank               |        |         |      |     |
    Empty single quoted | ''     | ''      | ''   | ''  | ''
    Empty double quoted | ""     | ""      | ""   | ""  | ""
    """)
void testBlankMeansNullForNonString(String string, Integer integer, List<?> list, Map<String, ?> map, Set<?> set) {
    assertEquals("", string);
    assertNull(integer);
    assertNull(list);
    assertNull(map);
    assertNull(set);
}
```

### Set of Applicable Values
TableTest automatically recognizes when a set represents alternative applicable values for a test parameter. When a cell contains a set (enclosed in curly braces) and the corresponding parameter isn't declared as a `Set` type, TableTest will expand that row into multiple test invocations — one for each value in the set, while maintaining all other parameter values. 

In this example, the test method is run 12 times, three times for each row, once for each value in the set in column `Example years`.

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
This feature works with scenario names and preserves the original scenario name for all generated test invocations. This allows grouping related test cases under a descriptive heading while still testing multiple specific values.

When multiple cells in the same row contain applicable value sets, TableTest performs a cartesian product, generating test cases for all possible combinations of values. This powerful feature enables testing multiple scenarios without redundant table entries.

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

Use expandable sets judiciously. The number of test cases grows multiplicatively with each additional set (two sets of size 10 generate 100 test cases), which can significantly increase test execution time.

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
Lines starting with `//` (ignoring leading whitespace) are treated as comments and ignored during parsing. Comments allow adding explanations or temporarily disabling data rows.

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

## Installation

### Requirements
TableTest requires **Java version 21 or higher**.

TableTest version 0.3.0 is compatible with **JUnit Jupiter versions from 5.11.0 up to and including 5.12.2**.

Please note that **TableTest version 0.3.0 is not compatible with recently released JUnit Jupiter 5.13.0**. This is due to an interface change in AnnotationBasedArgumentsProvider. This will be addressed in a coming version of TableTest.

### Using TableTest with JUnit Jupiter 5.12.2
To use TableTest with JUnit Jupiter **5.12.2**, simply add `tabletest-junit` as a test scope dependency alongside `junit-jupiter`.

#### Maven (pom.xml)
```xml
<dependencies>
    <dependency>
        <groupId>io.github.nchaugen</groupId>
        <artifactId>tabletest-junit</artifactId>
        <version>0.3.0</version>
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
    testImplementation 'io.github.nchaugen:tabletest-junit:0.3.0'    
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
    testImplementation("io.github.nchaugen:tabletest-junit:0.3.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2") 
    testRuntimeOnly("org.junit.platform:junit-platform-launcher") 
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
```

### Using TableTest with JUnit Jupiter 5.11.0 to 5.12.1
TableTest version 0.3.0 supports JUnit Jupiter versions 5.11.0 up to and including 5.12.2. For projects using JUnit Jupiter versions in this range, but prior to 5.12.2, you need to exclude the transitive dependencies to avoid conflicts.

#### Maven (pom.xml)
```xml
<dependencies>
    <dependency>
        <groupId>io.github.nchaugen</groupId>
        <artifactId>tabletest-junit</artifactId>
        <version>0.3.0</version>
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
    testImplementation('io.github.nchaugen:tabletest-junit:0.3.0') { 
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
    testImplementation("io.github.nchaugen:tabletest-junit:0.3.0") {
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
Unfortunately, TableTest is not supported for JUnit Jupiter versions prior to 5.11.0. If your project is currently using an older version of JUnit you will need to upgrade to a supported version to be able to use TableTest.


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
