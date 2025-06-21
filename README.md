# TableTest

TableTest extends JUnit 5 for data-driven testing using a concise table format. Express system behaviour through multiple examples, reducing test code while improving readability and maintainability.

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

**Benefits:**
- **Readable**: Clear input/output relationships in structured tables
- **Maintainable**: Add test cases by adding table rows
- **Concise**: Eliminates repetitive test code
- **Self-documenting**: Tables serve as living documentation
- **Collaborative**: Non-technical stakeholders can understand and contribute

**Requirements**: Java 21+, JUnit 5.11.0-5.13.1 (except 5.13.0).

**IDE Support**: [TableTest plugin for IntelliJ](https://plugins.jetbrains.com/plugin/27334-tabletest) provides auto-formatting, syntax highlighting, and shortcuts for working with tables.

**Latest Updates**: See the [changelog](CHANGELOG.md) for details on recent releases and changes.

**User Guide**: See the [user guide](USERGUIDE.md) for more details on how to use TableTest.

## Table of Contents
- [Usage](#usage)
- [Value Formats](#value-formats)
- [Value Conversion](#value-conversion)
- [Key Features](#key-features)
- [Installation](#installation)
- [IDE Support](#ide-support)
- [License](#license)


## Usage
Annotate test methods with `@TableTest` and provide table data as a multi-line string or [external file](#table-in-external-file).

Tables use pipes (`|`) to separate columns. The first row contains headers, the following rows contain test data. Each data row invokes the test method with cell values as arguments.

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

**Key points:**
- One parameter per data column (scenario column excluded)
- Parameters map by position, not name
- Values automatically convert to parameter types
- Test methods must be non-private, non-static, void return

Technically `@TableTest` is a [JUnit `@ParameterizedTest`](https://junit.org/junit5/docs/5.13.1/user-guide/index.html#writing-tests-parameterized-tests) with a custom-format argument source. 


## Value Formats
The TableTest format supports four types of values: 
- **Single values** specified with or without quotes (`abc`, `"a|b"`, `' '`)
- **Lists** of elements enclosed in brackets (`[1, 2, 3]`)
- **Sets** of elements enclosed in curly braces (`{a, b, c}`)
- **Maps** of key:value pairs enclosed in brackets (`[a: 1, b: 2]`).

Lists, sets, and maps can be nested (`[a: [1, 2, 3], b: [4, 5, 6]]`).

```java

@TableTest("""
    Single value           | List        | Set               | Map
    Hello, world!          | [1, 2, 3]   | {1, 2, 3}         | [a: 1, b: 2, c: 3]
    'cat file.txt | wc -l' | [a, '|', b] | {[], [1], [1,2 ]} | [empty: {}, full: {1, 2, 3}]
    ""                     | []          | {}                | [:]
    """)
void testValues(String single, List<?> list, Set<?> set, Map<String, ?> map) {
    //...
}
```


## Value Conversion
TableTest converts table values to method parameter types using this priority:

1. **Factory method** (in test class or `@FactorySources`)
2. **JUnit built-in conversion** (primitives, dates, enums, etc.)

### Factory Methods
Factory methods are `public static` methods that accept one parameter and return the target type:

```java
public static LocalDate parseDate(String input) {
    return switch (input) {
        case "today" -> LocalDate.now();
        case "tomorrow" -> LocalDate.now().plusDays(1);
        default -> LocalDate.parse(input);
    };
}
```

TableTest will look for a factory method present in either the test class or in one of the classes listed by a `@FactorySources` annotation. The first factory method found will be used. If required, TableTest will first convert the cell value to match the factory method parameter type, before invoking the factory method to convert it to the test method parameter type.

There is no specific naming pattern for factory methods, any method fulfilling the requirements above will be considered. Only one factory method per target type is possible per class.

### JUnit Built-In Conversion
TableTest falls back to [JUnit's built-in type converters](https://junit.org/junit5/docs/5.12.2/user-guide/index.html#writing-tests-parameterized-tests-argument-conversion-implicit) if no factory method is found. 

```java
@TableTest("""
    Number | Text | Date       | Class
    1      | abc  | 2025-01-20 | java.lang.Integer
    """)
void singleValues(short number, String text, LocalDate date, Class<?> type) {
    // test implementation
}
```

### Parameterized Types
Compound values like List, Set, and Map will also benefit from conversion to match parameterized types. For example, `[1, 2, 3]` becomes `List<Integer>` when the parameter is declared as such. Even nested values are traversed and converted to match parameterized types. Map keys remain String type and are not converted.

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

### Null Values
Blank cells and empty quoted values will translate to `null` for all parameter types except primitives. For primitives, it will cause an exception as they cannot represent a `null` value.

```java
@TableTest("""
    Scenario          | String | Integer | List | Map | Set
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


## Key Features
TableTest contains a number of other useful features for expressing examples in a table format.

### Scenario Names
Add descriptive names to test rows by providing a scenario name in the first column:

```java
@TableTest("""
    Scenario     | Input | Output
    Basic case   | 1     | one
    Edge case    | 0     | zero
    """)
void test(int input, String output) {
    // test implementation
}
```

Scenario names make the tables better documentation and will be used as test display names. This makes the test failures more clear and debugging easier. 

Optionally scenario names can be accessed in test methods by declaring it as a test method parameter tagged with annotation `@Scenario`.

### Value Sets
TableTest allows using a set in a single-value column to express that any of the listed values give the same result. This is a powerful feature that can be used to contract multiple rows that have identical expectations. 

TableTest will create multiple test invocations for a row with a value set, one for each value in the set. The test method will be invoked 12 times, three times for each row, once for each value in the `Example years` set.

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

Scenario names will be augmented to include the value from the set being used for the current test invocation. This makes it easier to see which values caused problems in case of test failures. 

Value sets can be used multiple times in the same row. TableTest will then perform a cartesian product, generating test invocations for all possible combinations of values. Use this judiciously, as the number of test cases grows multiplicatively with each additional set, as does the test execution time.


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
Tables can be loaded from external files instead of being in-lined in the annotation. For this use the `resource` attribute. The file must be located as a resource relative to the test class. Typically it is stored in the test `resources` directory or one of its subdirectories.

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

**Requirements**: Java 21+, JUnit 5.11.0-5.13.1 (except 5.13.0)

### Maven (pom.xml)
```xml
<dependencies>
    <dependency>
        <groupId>io.github.nchaugen</groupId>
        <artifactId>tabletest-junit</artifactId>
        <version>0.4.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Gradle with Kotlin DSL (build.gradle.kts)
```kotlin
dependencies { 
    testImplementation("io.github.nchaugen:tabletest-junit:0.4.0")
}
```

### Using TableTest with JUnit 5.11.0 to 5.12.2
For projects using JUnit versions prior to 5.13.0, you need to exclude the transitive JUnit dependencies TableTest brings in to avoid conflicts.

### Maven (pom.xml)
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
</dependencies>
```

### Gradle with Kotlin DSL (build.gradle.kts)
```kotlin
dependencies { 
    testImplementation("io.github.nchaugen:tabletest-junit:0.4.0") {
        exclude(group = "org.junit.jupiter", module = "junit-jupiter-params") 
        exclude(group = "org.junit.platform", module = "junit-platform-commons") 
    } 
}
```

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
