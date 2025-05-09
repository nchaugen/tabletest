# TableTest release notes

### 2025-05-10: Version 1.0.0-SNAPSHOT
- First public release
- Support for JUnit 5
  - Provides `@TableTest` annotation to declare parameterized test with multiline string input in TableTest Format
  - Automatic conversion of column values to method parameter types
- TableTest format
  - Human-readable tabular test data with pipe-separated columns
  - Header row for descriptive column names
  - Support for multiple data types:
    - Simple values (strings, numbers, booleans)
    - Lists with comma-separated elements
    - Maps with key-value pairs
    - Nested collections (lists of maps, maps of lists)
  - Comment support with // prefix for documentation
  - Empty line handling for better formatting
  - Quoted values to include special characters
