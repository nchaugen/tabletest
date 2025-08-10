# TableTest User Guide
TableTest allows you to express how the system is expected to behave through multiple examples in a concise table format. This reduces the amount of test code and makes it easier to understand, extend, and maintain your tests.

## Table of Contents
- [Usage](#usage)
- [Value Formats](#value-formats)
  - [Single Values](#single-values)
  - [List Values](#list-values)
  - [Set Values](#set-values)
  - [Map Values](#map-values)
  - [Nested Values](#nested-values)
- [Value Conversion](#value-conversion)
  - [Built-In Conversion](#built-in-conversion)
  - [Factory Method Conversion](#factory-method-conversion)
  - [Factory Sources](#factory-sources)
  - [Factory Methods in Kotlin](#factory-methods-in-kotlin)
  - [Factory Method Search Strategy in Java](#factory-method-search-strategy-in-java)
  - [Factory Method Search Strategy in Kotlin](#factory-method-search-strategy-in-kotlin)
  - [Overriding Built-In Conversion](#overriding-built-in-conversion)
- [Key Features](#key-features)
  - [Scenario Names](#scenario-names)
  - [Null Values](#null-values)
  - [Value Sets](#value-sets)
  - [Comments and Blank Lines](#comments-and-blank-lines)
  - [Table in External File](#table-in-external-file)
  - [Parameters Provided By ParameterResolvers](#parameters-provided-by-parameterresolvers)
  - [Escape Sequences](#escape-sequences)
  - [Explicit Argument Conversion](#explicit-argument-conversion)


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

Column values can be [single values](#single-values), [lists](#list-values), [sets](#set-values), or [maps](#map-values). Values are automatically converted to the type of the corresponding test parameter.

Each data row will invoke the test method with the cell values being passed as arguments. Optionally, the first column may contain a [scenario name](#scenario-names) describing the situation being exemplified by each row. There must be a test method parameter for each value column (scenario name column excluded). Columns map to parameters based strictly on order, so the first value column maps to the first parameter, the second value column to the second parameter, etc. The column header names and parameter names can be different, but keeping them aligned improves readability.

Technically `@TableTest` is a JUnit `@ParameterizedTest` with a custom-format argument source. Like regular JUnit test methods, `@TableTest` methods must not be `private` or `static` and must not return a value.


## Value Formats
The TableTest format supports four types of values: [single values](#single-values), [lists](#list-values), [sets](#set-values), and [maps](#map-values).

### Single Values
Single values can appear with or without quotes. Surrounding single (`'`) or double (`"`) quotes are required when the value contains a `|` character, or starts with `[` or `{`. For single values appearing as elements in a list, set, or map (see below), the characters `,`, `:`, `]`, and `}` also make the value require quotes to be successfully parsed.

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
TableTest will attempt to convert values to the type required by the test method parameter. This eliminates the need for manual conversion in your test method, keeping tests focused on invoking the system under test and asserting the results.

### Built-In Conversion
Out of the box, TableTest is able to convert single values to many of the standard types. For this it uses JUnit's built-in type converters. Please see the [JUnit documentation](https://junit.org/junit5/docs/5.13.4/user-guide/index.html#writing-tests-parameterized-tests-argument-conversion-implicit) for a list of supported types and the required format for each.

```java
@TableTest("""
    Number | Text | Date       | Class
    1      | abc  | 2025-01-20 | java.lang.Integer
    """)
void singleValues(short number, String text, LocalDate date, Class<?> type) {
    // test implementation
}
```

TableTest will also use this automatic conversion for single values appearing as elements in lists and sets, and for values in a map when the test method parameter is a parameterized type. Map keys remain String type and are not converted.

Automatic conversion for parameterized types works even for nested values:

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
Before falling back to built-in conversion, TableTest will look for a factory method present in either the test class, in a class listed by a `@FactorySources` annotation. If found, TableTest will use this factory method to convert the parsed value to the required test parameter type.

A factory method will be used when it:
1. Is defined as a public static method in a public class
2. Accepts exactly one parameter
3. Returns an object of the target parameter type
4. Is the only method matching the above criteria in the class

There is no specific naming pattern for factory methods, any method fulfilling the requirements above will be considered.

Having selected a factory method with a return type matching the test parameter type, TableTest will consider if the parsed value matches the parameter type of the factory method. If not, it will attempt to convert the value to match the parameter type.

In the example below, the map with student grades is being passed to the test as a `StudentGrades` domain value using the `toStudentGrades` factory methods:

```java
@TableTest("""
    Grades                                       | Highest Grade?
    [Alice: [95, 87, 92], Bob: [78, 85, 90]]     | 95
    [Charlie: [98, 89, 91], David: [45, 60, 70]] | 98
    """)
void testParameterizedTypes(StudentGrades grades, int expectedHighestGrade) {
    // test implementation
}

public static StudentGrades toStudentGrades(Map<String, List<Integer>> grades) {
    // conversion logic
}
```


### Factory Sources
To enable reuse of factory methods across test classes, TableTest provides a class annotation `@FactorySources` to list alternative classes to search for factory methods:

```java
@FactorySources({ClassWithFactoryMethods.class, AnotherClassWithFactoryMethods.class})
public class ExampleTest {
    //TableTest methods
}
```


### Factory Methods in Kotlin
For tests written in Kotlin, there are two ways to declare static factory methods:

1. In the companion object of a test class using [`@JvmStatic` annotation](https://kotlinlang.org/docs/java-to-kotlin-interop.html#static-methods)
2. At [package-level](https://kotlinlang.org/docs/java-to-kotlin-interop.html#package-level-functions) in the file containing the test class.

TableTest supports both factory methods declared in the test class. 

Dedicated factory sources in Kotlin should be declared `object` with factory methods annotated with `@JvmStatic`:

```kotlin
object KotlinFactorySource { 
    @JvmStatic
    fun toStudentGrades(input: Map<String, List<Int>>): StudentGrades {
        // implementation
    }
}
```

Usage:

```kotlin
@FactorySources(KotlinFactorySource::class)
class ExampleTest {
    // TableTest methods
}
```

Alternatively, regular Kotlin classes with factory methods defined as `@JvmStatic` in a companion object can also be referenced as a factory source.


### Factory Method Search Strategy in Java
TableTest uses the following strategy to search for factory methods in Java test classes:

1. Search current test class
2. In case of a `@Nested` test class, search enclosing classes, starting with the direct outer class
3. Search classes listed in a `@FactorySources` annotation on current test class in the order they are listed
4. In case of a `@Nested` test class, search classes listed by `@FactorySources` of enclosing classes, starting with the direct outer class

TableTest will stop searching as soon as it finds a matching factory method and use this for the conversion.


### Factory Method Search Strategy in Kotlin
In Kotlin, a `@Nested` test class must be declared `inner class` and these are not allowed to have companion objects. Hence, test class factory methods must be either declared in the companion object of the outer class (with `@JvmStatic`) or at package level in the same file as the test class.

So for Kotlin, the search strategy becomes as follows:

1. Search the current file (methods declared at package-level or in outer class companion object)
2. Search classes listed by a `@FactorySources` annotation on current test class in the order they are listed
3. In case of a `@Nested` test class, search classes listed by `@FactorySources` of enclosing classes, starting with direct outer

As for Java, TableTest will stop searching as soon as it finds a matching factory method and use this for the conversion.


### Overriding Built-In Conversion
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


## Key Features
TableTest contains a number of other useful features for expressing examples in a table format.

### Scenario Names
TableTest supports providing a descriptive name for each row that will be used as the test display name. This makes the tests easier to understand and failures easier to diagnose.

```java
@TableTest("""
    Scenario                              | Year | Is leap year?
    Years not divisible by 4              | 2001 | false
    Years divisible by 4                  | 2004 | true
    Years divisible by 100 but not by 400 | 2100 | false
    Years divisible by 400                | 2000 | true
    """)
public void testLeapYear(Year year, boolean expectedResult) {
    assertEquals(expectedResult, year.isLeap());
}
```

Optionally, the scenario column can also be included as a test parameter. It will then need a `@Scenario` annotation to be picked up as display name. If additional arguments are provided by [JUnit parameter resolvers](#parameters-provided-by-parameterresolvers) (TestInfo, TestReporter, etc.), declaring a parameter for the scenario name column is required.

```java
@TableTest("""
    Scenario                              | Year | Is leap year?
    Years not divisible by 4              | 2001 | false
    Years divisible by 4                  | 2004 | true
    Years divisible by 100 but not by 400 | 2100 | false
    Years divisible by 400                | 2000 | true
    """)
public void testLeapYear(@Scenario String scenario, Year year, boolean expectedResult) {
    assertEquals(expectedResult, year.isLeap(), "Failed for " + scenario);
}
```

### Null Values
Blank cells and empty quoted values will translate to `null` for all parameter types except primitives. For primitives, it will cause an exception as they cannot represent a `null` value.

The built-in conversion will **not** translate empty quoted values to `null` when they appear as elements inside lists, sets, and maps. This will instead cause a `TableTestException`. Consider using a [factory method](#factory-method-conversion) if you need this.

```java
@TableTest("""
    Scenario            | String | Integer | List | Map | Set
    Blank               |        |         |      |     |
    Empty single quoted | ''     | ''      | ''   | ''  | ''
    Empty double quoted | ""     | ""      | ""   | ""  | ""
    """)
void testBlankMeansNull(@Scenario String scenario, String string, Integer integer, List<?> list, Map<String, ?> map, Set<?> set) {
    if ("Blank".equals(scenario)) assertNull(string);
    else assertEquals("", string);
    assertNull(integer);
    assertNull(list);
    assertNull(map);
    assertNull(set);
}
```

### Value Sets
TableTest supports using the set format to specify multiple examples of values that are applicable for the current scenario. This is a powerful feature that can be used to contract multiple rows that have identical expectations. 

When a value is a set (enclosed in curly braces) and the corresponding parameter isn't declared as a `Set` type, TableTest will create multiple test invocations for this row, one for each value in the set. 

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

Using scenario names in combination with value sets, the display name will be the scenario name plus a description of the actual value used from the set for this invocation. This makes it easier to pinpoint which values caused problems in case of test failures. 

Multiple values in the same row can be specified as value sets. TableTest will then perform a cartesian product, generating test invocations for all possible combinations of values. The example below will invoke the test method 6 times for each row, with each possible combination of the provided x and y values.

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

Use value sets judiciously. The number of test cases grows multiplicatively with each additional set (two sets of size 10 generate 100 test invocations), which can significantly increase test execution time.

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


### Parameters Provided By ParameterResolvers
TableTest method parameters correspond to columns following a one-to-one correlation between table column index and method parameter index (scenario name column can be excluded). For TableTest methods to receive additional arguments provided by a ParameterResolver (TestInfo, TestReporter, etc.), these must be declared last. Also, if the table includes a scenario name column, this now needs an explicit parameter with `@Scenario` annotation:

```java
@TableTest("""
    Scenario | value | double?
    Zero     | 0     | 0
    Two      | 2     | 4
    """)
void testDoubleValue(@Scenario String scenario, int value, int expectedResult, TestInfo info) {
    assertEquals(expectedResult, 2 * value);
    assertNotNull(info);
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
Table files are read as raw text independent of the programming language, meaning escape sequences are **not** processed and remain literal.

#### Workarounds
If you need special characters in Kotlin or external Table files, you have three options:

1. Use actual characters instead of escape sequences
2. Use Kotlin regular strings for simple cases
3. Consider switching to Java for tests requiring complex escape sequences.


### Explicit Argument Conversion
In addition to implicitly called factory methods and built-in conversion, TableTest supports JUnit [explicit argument conversion](https://junit.org/junit5/docs/5.12.1/user-guide/index.html#writing-tests-parameterized-tests-argument-conversion-explicit). This can be used for explicit conversion to custom types.

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
