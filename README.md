# TableTest

TableTest is an extension to JUnit 5 for data-driven testing. It allows you to define a table of test data in a readable format, making it easier to understand, extend and maintain your tests.

TableTest-style test methods are declared using the `@TableTest` annotation, supplying a table of data as a multi-line string. `@TableTest` is implemented as a JUnit `@ParameterizedTest` with a custom-format argument source. Like regular JUnit test methods, `@TableTest` methods must not be `private` or `static` and must not return a value.

There must be a test method parameter for each table column. Columns map to parameters purly based on order (first column maps to first parameter, second to second. etc.), thus the column header and the name of the parameter are allowed to differ.

## Table format

Tables are formatted using a pipe character (`|`) to separate columns. The first line is a header row describing each column, and the following lines are data rows. Each data row is a test case, with the values in the row passed as arguments to the test method.

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

Column values can be either **single values** or two types of compound values: **lists** and **maps**. As demonstrated in the example above and specified below, table values are implicitly converted to the declared parameter types when the test is run.

## Single-value format

Single-values can be specified with or without quotes. An unquoted value cannot contain any of the following characters: `[`, `|`, `,`, or `:`. If a value contains any of these characters, it must be enclosed in single or double quotes. Whitespace before and after an unquoted value is ignored, so if a value is to start or end in whitespace, this must be specified using quotes. An empty value is represented by two single or double quotes with no space between them.

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

## List value format

List values are enclosed in square brackets with elements separated by commas. Both single and compound values can be elements of the list. An empty list is represented by open and close square brackets with nothing inside.

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

## Map value format

Map values are enclosed in square brackets, and key-value pairs are separated by commas. A colon separates a key from its value. The key must be a single-value. The value can be either single or compound values. An empty map is represented by a colon enclosed in square brackets.

```java

@TableTest("""
    Map                                      | Size?
    [1: Hello, 2: World]                     | 2
    ["|": 1, ',': 2, abc: 3]                 | 3
    [string: abc, list: [1, 2], map: [a: 4]] | 3
    [:]                                      | 0
    """)
void testMap(Map<String, Object> map, int expectedSize) {
    assertEquals(expectedSize, map.size());
}
```

## Comment format

The table format supports comments. A comment is a line that starts with `//`, excluding any whitespace in front. Comments are ignored when parsing the table. This allows you to add explanations or notes to your tables without affecting the test data and also to temporarily disable a data row without deleting it.

```java

@TableTest("""
    String         | Length?
    Hello world    | 11
    // The next row is currently disabled
    // "World, hello" | 12
    //
    // Special characters must be quoted
    '|'            | 1
    """)
void testComment(String string, int expectedLength) {
    assertEquals(expectedLength, string.length());
}
```

## Argument conversion

TableTest will attempt to implicitly convert table values to the declared parameter types when the test is run. It supports the [implicit type converters built-in to JUnit Jupiter](https://junit.org/junit5/docs/5.12.1/user-guide/index.html#writing-tests-parameterized-tests-argument-conversion).

Using parameterized types for the test parameters, list elements and map values will also be implicitly converted. Map keys are not converted and will always be of type String.

If there is no parameter type information to guide the conversion, or the type is not supported by implicit conversion, values will be of type String.

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
