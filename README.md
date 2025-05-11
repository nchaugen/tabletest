# TableTest

TableTest offers extensions to JVM-based test frameworks for data-driven testing using a table format. It allows you to define test data for multiple test scenarios in a readable table format, making it easier to understand, extend and maintain your tests.

Currently, JUnit 5 is the only supported test framework.

## Usage

TableTest-style test methods are declared using the `@TableTest` annotation, supplying a table of data as a multi-line string. `@TableTest` is implemented as a JUnit `@ParameterizedTest` with a custom-format argument source. Like regular JUnit test methods, `@TableTest` methods must not be `private` or `static` and must not return a value.

There must be a test method parameter for each table column. Columns map to parameters purly based on order (first column maps to first parameter, second to second. etc.), thus the column header and the name of the parameter are allowed to differ.

### Table Format

Tables use pipe characters (`|`) to separate columns. The first line contains header descriptions, and subsequent lines represent individual test cases whose values are passed as arguments to the test method.

```java

@TableTest("""
    Augend | Addend | Sum?
    2      | 3      | 5
    0      | 0      | 0
    1      | 1      | 2
    """)
void testAddition(int augend, int addend, int sum) {
    assertEquals(sum, augend + addend);
}
```

Column values can be **single values**, **lists**, or **maps**.

### Single-Value Format

Single values can appear with or without quotes. Unquoted values must not contain `[`, `|`, `,`, or `:` characters. These special characters require single or double quotes.

Whitespace around unquoted values is trimmed. To preserve leading or trailing whitespace, use quotes. Empty values are represented by adjacent quote pairs (`""` or `''`).

```java

@TableTest("""
    Value          | Length?
    Hello world    | 11
    "World, hello" | 12
    '|'            | 1
    ""             | 0
    """)
void testString(String value, int expectedLength) {
    assertEquals(expectedLength, value.length());
}
```

### List Value Format

Lists are enclosed in square brackets with comma-separated elements. Lists can contain single values or compound values (nested lists/maps). Empty lists are represented by `[]`.

```java

@TableTest("""
    List             | Size?
    [Hello, World]   | 2
    ["World, Hello"] | 1
    ['|', ",", abc]  | 3
    [[1, 2], [3, 4]] | 2
    [[a: 4], [b: 5]] | 2
    []               | 0
    """)
void testList(List<Object> list, int expectedSize) {
    assertEquals(expectedSize, list.size());
}
```

### Map Value Format

Maps use square brackets with comma-separated key-value pairs. Keys and values are separated by colons. Keys must be unquoted single values, while values can be single or compound. Empty maps are represented by `[:]`.

```java

@TableTest("""
    Map                                      | Size?
    [1: Hello, 2: World]                     | 2
    [string: abc, list: [1, 2], map: [a: 4]] | 3
    [:]                                      | 0
    """)
void testMap(Map<String, Object> map, int expectedSize) {
    assertEquals(expectedSize, map.size());
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

### Argument Conversion

TableTest automatically converts table values to match declared parameter types during test execution. It leverages [JUnit Jupiter's built-in implicit type converters](https://junit.org/junit5/docs/5.12.1/user-guide/index.html#writing-tests-parameterized-tests-argument-conversion).

List elements and map values benefit from implicit conversion to match parameterized types. Map keys remain as a String type and are not converted.

Without parameter type information or for unsupported conversion types, single values default to the String type.

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
    Students students = parse(studentGrades);
    assertEquals(expectedHighestGrade, students.highestGrade());
    assertEquals(expectedAverageGrade, students.averageGrade(), 0.1);
    assertEquals(expectedPassCount, students.passCount());
}
```

JUnit standard [explicit argument conversion](https://junit.org/junit5/docs/5.12.1/user-guide/index.html#writing-tests-parameterized-tests-argument-conversion-explicit) or [argument aggregation](https://junit.org/junit5/docs/5.12.1/user-guide/index.html#writing-tests-parameterized-tests-argument-aggregation) can be used for conversions not supported by implicit conversion.


## Installation

To use TableTest with JUnit 5, add `tabletest-junit` as a test scope dependency alongside `junit-jupiter`.

`tabletest-junit` requires Java version 21 or higher. It includes `junit-jupiter-params` library version 5.12.2, so using the same version for `junit-jupiter` in your project is recommended.

### Maven
Add `tabletest-junit` and `junit-jupiter` as test scope dependencies in your `pom.xml` file:

```xml
    <dependencies>
        <dependency>
            <groupId>io.github.nchaugen.tabletest</groupId>
            <artifactId>tabletest-junit</artifactId>
            <version>0.1.0</version>
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

### Gradle

Add `tabletest-junit` and `junit-jupiter` as testImplementation dependencies in your `build.gradle` file:

```groovy
dependencies {
    testImplementation 'io.github.nchaugen.tabletest:tabletest-junit:0.1.0'    
    testImplementation 'org.junit.jupiter:junit-jupiter:5.12.2'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

tasks.named('test', Test) {
    useJUnitPlatform()

    maxHeapSize = '1G'

    testLogging {
        events "passed"
    }
}
```

Please see the [Gradle docs](https://docs.gradle.org/current/userguide/java_testing.html) for more information on how to configure testing.


## License

TableTest is licensed under the liberal and business-friendly [Apache Licence, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.html) and is freely available on [GitHub](https://github.com/nchaugen/tabletest). Additionally, the `tabletest-junit` distribution uses `junit-jupiter-params` which is released under [Eclipse Public License 2.0](https://raw.githubusercontent.com/junit-team/junit5/refs/heads/main/LICENSE.md).

TableTest binaries are published to the repositories of Maven Central. The artefacts signatures can be validated against [this PGP public key](https://keyserver.ubuntu.com/pks/lookup?search=nchaugen%40gmail.com).
