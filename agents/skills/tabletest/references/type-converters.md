# Type Converters for Custom Type Conversion

When JUnit's built-in converters don't support your parameter type, add custom type converters.

**Important**: Type converters must be annotated with `@TypeConverter`. Non-annotated converter methods are deprecated and will not be supported in a future version.

## Java

Place type converters as `public static` methods in the **public** test class or a **public class** listed in `@TypeConverterSources`. Annotate them with `@TypeConverter`.

```java
@TableTest("""
    Date       | Days Until?
    today      | 0
    tomorrow   | 1
    """)
void testDaysUntil(LocalDate date, int expected) {
    assertEquals(expected, ChronoUnit.DAYS.between(LocalDate.now(), date));
}

@TypeConverter
public static LocalDate parseLocalDate(String input) {
    return switch (input) {
        case "today" -> LocalDate.now();
        case "tomorrow" -> LocalDate.now().plusDays(1);
        default -> LocalDate.parse(input);
    };
}
```

## Kotlin

For Kotlin tests, there are two ways to declare type converters:

### Option 1: Package-Level Functions

Declare type converters at package level in the same file as the test class:

```kotlin
// At package level (top of file, outside class)
@TypeConverter
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

Declare type converters in the companion object with `@JvmStatic`:

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
        @TypeConverter
        fun parseLocalDate(input: String): LocalDate = when (input) {
            "today" -> LocalDate.now()
            "tomorrow" -> LocalDate.now().plusDays(1)
            else -> LocalDate.parse(input)
        }
    }
}
```

**Note**: `@Nested` inner classes in Kotlin cannot have companion objects. Use package-level functions or outer class companion object instead.

## Using @TypeConverterSources

For shared type converters across multiple test classes:

**Java:**
```java
@TypeConverterSources(DateConverters.class)
class DateTest {
    @TableTest("""
        ...
        """)
    void testWithSharedConverters(LocalDate date, Duration duration) { ... }
}
```

**Kotlin** — use an `object` declaration with `@JvmStatic`:
```kotlin
object DateConverters {
    @JvmStatic
    @TypeConverter
    fun parseLocalDate(input: String): LocalDate = when (input) {
        "today" -> LocalDate.now()
        "tomorrow" -> LocalDate.now().plusDays(1)
        else -> LocalDate.parse(input)
    }
}

@TypeConverterSources(DateConverters::class)
class DateTest {
    @TableTest("""
        ...
        """)
    fun testWithSharedConverters(date: LocalDate, duration: Duration) { ... }
}
```

## Type Converter Requirements

A type converter will be used when it:
1. Is annotated with `@TypeConverter`
2. Is defined as a public static method in a public class
3. Accepts exactly one parameter
4. Returns an object of the target parameter type
5. Is the only method matching the above criteria for the target type

There is no specific naming pattern required, but `parse<TypeName>` (e.g., `parseLocalDate`, `parseMoney`) is conventional.

## Search Strategy

TableTest searches for type converters in this order (stops at first match):

**Java:**
1. Current test class (including inherited methods)
2. Enclosing classes (for `@Nested` tests, starting with direct outer class)
3. Classes listed in `@TypeConverterSources` (in order listed)
4. `@TypeConverterSources` of enclosing classes (for `@Nested` tests)

**Kotlin:**
1. Current file (package-level functions and outer class companion object)
2. Classes listed in `@TypeConverterSources` (in order listed)
3. `@TypeConverterSources` of enclosing classes (for `@Nested` tests)

If a type converter isn't being found, check that it meets the requirements above and is in a location that matches the search order.
