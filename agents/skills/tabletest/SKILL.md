---
name: tabletest
description: Write TableTest-style JUnit tests in Java/Kotlin for data-driven cases. Use when consolidating 2+ similar @Test methods, adding multiple input/output examples, or documenting business rules as a table; avoid for single-case or divergent logic.
---

# TableTest Skill

Use this skill before converting similar JUnit tests or adding a new TableTest.

## Pre-Check

Before proceeding, verify:
- [ ] 2+ test cases share the same setup and assertion logic
- [ ] Differences between cases can be expressed as data (inputs/outputs)
- [ ] JUnit 5.11+ is available

If any check fails, use standard `@Test` methods instead.

## Quick Example

```java
@TableTest("""
    Scenario         | a  | b  | Sum?
    positive numbers | 1  | 2  | 3
    with zero        | 0  | 5  | 5
    negative number  | -3 | 7  | 4
    """)
void shouldAddNumbers(int a, int b, int sum) {
    assertEquals(sum, Calculator.add(a, b));
}
```

**Notes:**
- TableTest works with any assertion library (JUnit, AssertJ, Hamcrest, etc.). Use the project's existing style.
- Table syntax and built-in conversion are identical for Java and Kotlin. Only method signatures differ.
- **Escape sequences differ**: Java text blocks process `\t`, `\n`, etc. Kotlin raw strings do NOT — they remain literal. For special characters in Kotlin, use actual characters or regular strings.

---

## Table Syntax

### Basic Structure and Mapping

Keep a scenario column as the leftmost column. Do not map it to a parameter unless you annotate the parameter with `@Scenario` (needed for referencing scenario description in test method, or when using inject parameters). Suffix expectation columns with `?` to signal intent (e.g., `Expected?`, `Valid?`).

Rules:
- Align parameter order to data column order (scenario column excluded).
- Provide one parameter per data column.
- Expect one invocation per data row.
- Keep methods non-private, non-static, and returning void.

### Single Values and Quoting

Use blank cells for `null` (reference types). Use `''` for empty strings. Use `'   '` for blank strings.

| Value contains or starts with | Action                           |
|-------------------------------|----------------------------------|
| `\|` (pipe)                   | Quote with `"..."`               |
| `"` or `'`                    | Quote with the other quote style |
| Starts with `[`               | Quote to avoid list syntax       |
| Starts with `{`               | Quote to avoid set syntax        |

```java
@TableTest("""
    Value             | Description
    simple            | No quotes needed
    "contains | pipe" | Quotes required for special chars
    ''                | Empty string
                      | Blank cell = null
    "[1,2,3]"         | Quote to avoid list syntax
    "{a,b}"           | Quote to avoid set syntax
    """)
void testValues(String value, String description) { ... }
```

### Collections

Lists use `[]`, sets use `{}`, and maps use `[]` with `key: value` entries.

```java
// List
@TableTest("""
    Numbers   | Sum?
    []        | 0
    [1]       | 1
    [1, 2, 3] | 6
    """)
void testSum(List<Integer> numbers, int sum) { ... }

// Set
@TableTest("""
    Values       | Size?
    {1, 2, 3}    | 3
    {1, 1, 2, 2} | 2
    """)
void testSetSize(Set<Integer> values, int size) { ... }

// Map (empty map uses [:])
@TableTest("""
    Scores               | Highest?
    [Alice: 95, Bob: 87] | 95
    [:]                  | 0
    """)
void testHighestScore(Map<String, Integer> scores, int highest) { ... }
```

### Built-in Value Conversion

JUnit converts many standard types automatically: primitives, `String`, `Path`, `File`, `URI`, `URL`, `UUID`, `LocalDate`, `LocalTime`, `LocalDateTime`, enums, and more. Prefer direct parameter types that JUnit can convert.

Built-in conversion also applies to collection elements: `[com/example]` → `List<Path>`, `[Bob: 1980-03-04]` → `Map<String, LocalDate>`, `{https://claude.ai}` → `Set<URL>`.                   

```java
@TableTest("""
    Scenario     | Class Name      | Resolved Path?
    With package | com.example.Foo | com/example/Foo
    Nested class | Outer$Inner     | Outer/Inner
    """)
void converts_class_names(String className, Path expectedPath) {
    assertThat(resolver.resolve(className)).isEqualTo(expectedPath);
}
```

---

## Table Design

### Design Black-Box Tables

Model observable inputs and outputs. Avoid internal flags or setup-only columns unless they are part of the public contract.

```java
@TableTest("""
    Scenario                    | Build Dir | JUnit Property | Configured Dir | Resolved Dir?
    Configured input wins       | build     | report/junit   | tabletest      | tabletest
    JUnit property takes effect | target    | report/junit   |                | report/junit
    Fallback when none set      | build     |                |                | build/junit-jupiter
    """)
void resolvesInputDirectory(String buildDir, String junitProperty, String configuredDir, String resolvedDir) {
    // setup derived from inputs, assert resolvedDir
}
```

### Include All Outputs of a Concern

When an operation produces multiple observable outputs, include them all as expectation columns in one table. Each row should give the complete picture of what happens for a given scenario. Don't split outputs of the same behavioural concern across separate test methods.

```java
// Good — all outputs of priority resolution in one table
@TableTest("""
    Scenario                  | Input Dir | JUnit Dir    | Resolved Path?       | Source?        | Searched Locations?
    Configured input wins     | my-config | report/junit | my-config            | CONFIGURED     | [my-config]
    JUnit property wins       |           | report/junit | report/junit         | JUNIT_PROPERTY | [report/junit, build/junit-jupiter]
    Fallback wins             |           |              | build/junit-jupiter  | FALLBACK       | [build/junit-jupiter]
    """)
void resolvesWithPriority(String inputDir, String junitDir,
                          String resolvedPath, ResolutionSource source, List<String> searchLocations) { ... }
```

```java
// Bad — same outputs split across separate tests
void resolvesPath(...)            // tests Resolved Path? only
void reportsSource(...)           // tests Source? only
void reportsSearchedLocations(...)// tests Searched Locations? only
```

Splitting forces the reader to cross-reference multiple tables to understand one behaviour. If the outputs all come from the same operation and concern, they belong together.

Separate tests are appropriate when testing a **different concern** of the same operation (e.g. path normalization vs. priority resolution) or a different method entirely.

### Match Table Structure to the Logic Being Tested

The type of logic under test determines what each row should represent:

- **Decision/priority logic**: Each row is a distinct decision point. Scenario names describe which rule takes precedence (e.g., "X wins over Y").
- **Parsing/validation logic**: Each row is a distinct input variation. Scenario names describe the input condition (e.g. "Empty input", "With special characters").
- **Transformation logic**: Each row is an input/output pair. Scenario names describe the transformation case.

If rows feel out of place — parsing variations in a decision table, or decision branches in a parsing table — this signals the code under test may be mixing responsibilities. Consider whether the method should be split before adding more test rows.

### Name Expectation Columns Clearly

End expectation columns with `?` **suffix** to signal which columns are outputs being verified versus inputs being provided.

Examples: `Valid?`, `Formatted?`, `Result?`, `Throws?`, `Expected?`

**Common mistake** — `?` as prefix instead of suffix:
```
?Source        ← WRONG
Source?        ← CORRECT
```

### Name Scenarios Descriptively

Describe the condition being tested, not the expected outcome. Good scenario names answer "under what circumstances?" rather than "what happens?".

| Good                         | Bad             |
|------------------------------|-----------------|
| `Negative input`             | `Returns error` |
| `Empty list`                 | `Sum is zero`   |
| `User without licence`       | `Cannot rent`   |
| `Divisible by 4 but not 100` | `Is leap year`  |

Scenario names appear in test failure messages, so clarity helps diagnose failures quickly.

### Use Concrete Domain Values

Column values should be concrete, meaningful data — not abstract flags or codes. Expectation column values should be traceable to input column values.

**Good** — directory names as inputs, resolved dir traceable to an input column:
```java
@TableTest("""
    Scenario             | Configured Dir | JUnit Dir    | Fallback State | Resolved Dir? | Source?
    Configured wins      | my-config      | report/junit | yaml           | my-config     | CONFIGURED
    JUnit property wins  |                | report/junit | yaml           | report/junit  | JUNIT_PROPERTY
    Fallback wins        |                |              | yaml           | target/junit  | FALLBACK
    """)
```

**Bad** — abstract flags, expectation values not traceable to inputs:
```java
@TableTest("""
    Scenario             | Has Config | Override State | Fallback State | Resolved?
    Configured wins      | true       | yaml           | yaml           | configured
    Override wins        | false      | yaml           | yaml           | override
    Fallback wins        | false      |                | yaml           | fallback
    """)
```
In the bad example, `configured`, `override`, and `fallback` in Resolved? are names hardcoded in the test body, not visible in the table. The reader cannot understand the table without reading the test code.

When a value is derived from an input column (e.g., fallback path = Build Dir + "/junit-jupiter"), include the source column so readers can trace the derivation:
```java
@TableTest("""
    Scenario        | Build Dir | Build State | Resolved Dir?
    Maven fallback  | target    | yaml        | target/junit-jupiter
    Gradle fallback | build     | yaml        | build/junit-jupiter
    """)
```
Here `target/junit-jupiter` is visibly derived from `Build Dir = target`.

### Use Domain Terminology

Column names should use domain or feature terminology that readers understand without knowing the implementation. Avoid parameter names, variable names, or internal API terms.

| Good (Domain)          | Bad (Implementation)      |
|------------------------|---------------------------|
| `JUnit Dir`            | `Override`                |
| `Build Output`         | `junitOutputDirOverride`  |
| `Search Locations?`    | `Candidates?`             |

### Use Value Sets for "Regardless Of" Relationships

When one input takes precedence regardless of other inputs, use value sets to express this declaratively instead of listing every combination. Each `{...}` column generates a test per value.

```java
@TableTest("""
    Scenario                   | Priority | Fallback State         | Resolved?
    Priority wins regardless   | main     | {yaml, empty, missing} | main
    """)
```

This single row generates 3 tests, all asserting `main` wins regardless of fallback state. See `references/value-sets.md` for full syntax.

### Null, Empty, and Blank Values

Use blank cells for null, `''` for empty strings, and `'   '` for blank strings.

```java
@TableTest("""
    Scenario        | Input | Resolved?
    Normal input    | hello | HELLO
    Null input      |       |
    Empty input     | ''    |
    Blank input     | '   ' |
    """)
void resolves_values(String input, String resolved) {
    assertThat(transform(input)).isEqualTo(resolved);
}
```

**Note**: These are syntax examples, not test design patterns. Null/empty/blank variants of an input should typically be additional rows in the test that covers the feature, not in a separate test method. For example, if a resolver ignores blank JUnit dir values, add those as rows in the main resolution test rather than creating a separate "handles blank values" test.

---

## Workflow

### Converting Existing Tests

1. Identify tests with identical structure but different data.
2. Extract the varying parts as columns (inputs and expected values).
3. Create table with scenario column first, inputs next, expectations last (suffix with `?`).
4. Align method parameters to column order; do not bind the scenario column unless annotated with `@Scenario`.
5. Verify all rows use the same assertion logic.

### Writing New TableTest

1. List the scenarios to cover (edge cases, boundaries, error conditions).
2. Identify inputs and expected outputs for each scenario.
3. Create table with scenario column first, inputs next, expectations last (suffix with `?`).
4. Align method parameters to column order; do not bind the scenario column unless annotated with `@Scenario`.
5. Add type converters for custom type conversion when needed.

---

## Quality Checks

After writing, verify:
- [ ] **Multiple rows**: table has 2+ rows; use `@Test` for single cases
- [ ] **Black-box design**: columns represent observable inputs and outputs, not internal flags or implementation details
- [ ] **Clear communication**: scenario names describe conditions (not outcomes), column names are self-explanatory
- [ ] **Uniform assertions**: all rows use the same assertion logic; split into separate TableTests if logic differs per row
- [ ] **Straightforward method**: no `if`/`switch` statements, no parsing or conversion logic; the method only arranges, acts, and asserts
- [ ] **Parameter alignment**: parameters match data columns left-to-right (excluding scenario column)
- [ ] **Parameter conversion**: type converters or JUnit built-in converters handle type conversion, keeping the test method free of parsing code
- [ ] **Valid syntax**: values requiring quotes are quoted, collections use correct bracket syntax
- [ ] **Expectation columns present**: at least one column uses `?` suffix (not prefix)
- [ ] **Concrete values**: expectation values are traceable to input column values where applicable
- [ ] **Domain terminology**: column names use domain language, not implementation jargon or parameter names
- [ ] **Complete outputs**: all observable outputs of the same behavioural concern are in one table, not split across separate tests
- [ ] **Row coherence**: rows match the type of logic being tested (decision points for priority logic, input variations for parsing logic); out-of-place rows may signal mixed responsibilities in the code under test

---

## Advanced References

Consult these references when needed:

| Reference                           | When to use                                                                |
|-------------------------------------|----------------------------------------------------------------------------|
| `references/dependency-setup.md`    | Project lacks TableTest dependency                                         |
| `references/value-sets.md`          | Multiple input values map to same expectation                              |
| `references/type-converters.md`     | Custom types need parsing logic                                            |
| `references/large-tables.md`        | Need comments, grouping, or external table files                           |
| `references/example-patterns.md`    | Need inspiration for table design (business rules, boundaries, exceptions) |
| `references/provided-parameters.md` | Using `@TempDir` or other injected parameters                              |
