# Large Tables: Comments, Grouping, and External Files

## Comments and Blank Lines

Use comment lines starting with `//` to document sections. Use blank lines to group related cases visually.

```java
@TableTest("""
    Scenario        | Input | Output?

    // Basic cases
    Zero            | 0     | 0
    Positive        | 5     | 25

    // Edge cases
    Negative        | -3    | 9
    """)
void testSquares(int input, int output) {
    assertEquals(output, input * input);
}
```

## External Table Files

Load large tables from resource files to keep test classes readable:

```java
@TableTest(resource = "/test-data/user-permissions.table")
void testUserPermissions(String role, String action, boolean allowed) {
    assertEquals(allowed, hasPermission(role, action));
}
```

The table file uses the same syntax as inline tables:

```
Scenario          | Role  | Action | Allowed?
Admin can edit    | admin | edit   | true
Guest cannot edit | guest | edit   | false
...
```

Place table files in `src/test/resources/`.
