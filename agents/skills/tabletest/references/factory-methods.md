# Factory Methods for Custom Type Conversion

When JUnit's built-in converters don't support your parameter type, add factory methods.

## Java

Place factory methods as `public static` methods in the test class or a class listed in `@FactorySources`.

```java
@TableTest("""
    Date       | Days Until?
    today      | 0
    tomorrow   | 1
    """)
void testDaysUntil(LocalDate date, int expected) {
    assertEquals(expected, ChronoUnit.DAYS.between(LocalDate.now(), date));
}

public static LocalDate parseLocalDate(String input) {
    return switch (input) {
        case "today" -> LocalDate.now();
        case "tomorrow" -> LocalDate.now().plusDays(1);
        default -> LocalDate.parse(input);
    };
}
```

## Kotlin

For Kotlin tests, there are two ways to declare factory methods:

### Option 1: Package-Level Functions

Declare factory methods at package level in the same file as the test class:

```kotlin
// At package level (top of file, outside class)
fun parseLocalDate(input: String): LocalDate = when (input) {
    "today" -> LocalDate.now()
    "tomorrow" -> LocalDate.now().plusDays(1)
    else -> LocalDate.parse(input)
}

class DateTest {
    @TableTest("""
        Date       | Days Until?
        today      | 0
        tomorrow   | 1
        """)
    fun testDaysUntil(date: LocalDate, expected: Int) {
        assertEquals(expected, ChronoUnit.DAYS.between(LocalDate.now(), date))
    }
}
```

### Option 2: Companion Object with @JvmStatic

Declare factory methods in the companion object with `@JvmStatic`:

```kotlin
class DateTest {
    @TableTest("""
        Date       | Days Until?
        today      | 0
        tomorrow   | 1
        """)
    fun testDaysUntil(date: LocalDate, expected: Int) {
        assertEquals(expected, ChronoUnit.DAYS.between(LocalDate.now(), date))
    }

    companion object {
        @JvmStatic
        fun parseLocalDate(input: String): LocalDate = when (input) {
            "today" -> LocalDate.now()
            "tomorrow" -> LocalDate.now().plusDays(1)
            else -> LocalDate.parse(input)
        }
    }
}
```

**Note**: `@Nested` inner classes in Kotlin cannot have companion objects. Use package-level functions or outer class companion object instead.

## Using @FactorySources

For shared factory methods across multiple test classes:

**Java:**
```java
@FactorySources(DateFactories.class)
class DateTest {
    @TableTest("""
        ...
        """)
    void testWithSharedFactories(LocalDate date, Duration duration) { ... }
}
```

**Kotlin** — use an `object` declaration with `@JvmStatic`:
```kotlin
object DateFactories {
    @JvmStatic
    fun parseLocalDate(input: String): LocalDate = when (input) {
        "today" -> LocalDate.now()
        "tomorrow" -> LocalDate.now().plusDays(1)
        else -> LocalDate.parse(input)
    }
}

@FactorySources(DateFactories::class)
class DateTest {
    @TableTest("""
        ...
        """)
    fun testWithSharedFactories(date: LocalDate, duration: Duration) { ... }
}
```

## Factory Method Requirements

A factory method will be used when it:
1. Is defined as a public static method in a public class
2. Accepts exactly one parameter
3. Returns an object of the target parameter type
4. Is the only method matching the above criteria in the class

There is no specific naming pattern required, but `parse<TypeName>` (e.g., `parseLocalDate`, `parseMoney`) is conventional.

## Search Strategy

TableTest searches for factory methods in this order (stops at first match):

**Java:**
1. Current test class (including inherited methods)
2. Enclosing classes (for `@Nested` tests, starting with direct outer class)
3. Classes listed in `@FactorySources` (in order listed)
4. `@FactorySources` of enclosing classes (for `@Nested` tests)

**Kotlin:**
1. Current file (package-level functions and outer class companion object)
2. Classes listed in `@FactorySources` (in order listed)
3. `@FactorySources` of enclosing classes (for `@Nested` tests)

If a factory method isn't being found, check that it meets the requirements above and is in a location that matches the search order.
