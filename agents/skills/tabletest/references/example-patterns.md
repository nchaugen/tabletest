# Example Patterns

## Business Rules

```java
@TableTest("""
    Scenario              | Age | Has Licence | Can Rent Car?
    Too young             | 17  | true        | false
    Adult with licence    | 25  | true        | true
    Adult without licence | 30  | false       | false
    Senior with licence   | 70  | true        | true
    """)
void testCarRentalEligibility(int age, boolean hasLicence, boolean canRent) {
    assertEquals(canRent, isEligibleToRentCar(age, hasLicence));
}
```

## Boundary Testing

```java
@TableTest("""
    Scenario      | Input | Valid?
    Below minimum | -1    | false
    At minimum    | 0     | true
    Normal range  | 50    | true
    At maximum    | 100   | true
    Above maximum | 101   | false
    """)
void testValidRange(int input, boolean expectedValid) {
    assertEquals(expectedValid, isInRange(input, 0, 100));
}
```

## Exception Testing

```java
@TableTest("""
    Scenario       | Input | Exception?
    Negative age   | -1    | java.lang.IllegalArgumentException
    Empty name     | ''    | java.lang.IllegalArgumentException
    """)
void testExceptions(String input, Class<? extends Throwable> expectedException) {
    assertThrows(expectedException, () -> validateInput(input));
}
```

## Value Transformations

```java
@TableTest("""
    Scenario              | Input       | Formatted?
    Normalize spacing     | "[1,2,3]"   | "[1, 2, 3]"
    Remove extra spaces   | "[ [] ]"    | "[[]]"
    Nested lists          | "[[1,2]]"   | "[[1, 2]]"
    Empty collection      | "[]"        | "[]"
    """)
void shouldFormatCollection(String input, String formatted) {
    assertThat(format(input)).isEqualTo(formatted);
}
```
