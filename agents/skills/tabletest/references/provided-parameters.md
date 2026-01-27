# Provided Parameters (@TempDir, TestInfo etc.)

When using JUnit-provided parameters like `@TempDir` and `TestInfo` alongside table columns, follow these rules:

1. **Include scenario column in parameter list and bind it with `@Scenario`**
2. **Table column parameters come first** (matching left-to-right column order)
3. **Provided parameters come last** (`@TempDir`, `@Mock`, etc.)

## @TempDir Example

Add a `@Scenario` parameter when `@TempDir` appears after table parameters. This ensures correct parameter alignment.

```java
@TableTest("""
    Scenario       | Files          | Discovered Files?
    Single file    | [file.txt]     | [file]
    Multiple files | [a.txt, b.txt] | [a, b]
    Subdirectory   | [dir/file.txt] | []
    """)
void discovers_files(@Scenario String _scenario, List<String> files, List<String> expected,
                     @TempDir Path tempDir) throws IOException {
    for (String file : files) {
        Path path = tempDir.resolve(file);
        Files.createDirectories(path.getParent());
        Files.writeString(path, "content");
    }
    // assertions using expected...
}
```

## Parameter Order Summary

```
method(@Scenario scenario, tableCol1, tableCol2, ..., @TempDir tempDir, @Mock mock)
       └─ when present ─┘      └─ match columns ─┘      └─── provided last ───┘
```

The `@Scenario` parameter is needed when you have scenario column and provided parameters after table parameters (like `@TempDir`). Without provided parameters, `@Scenario` parameter is not needed unless you need to access scenario name in your test code.
