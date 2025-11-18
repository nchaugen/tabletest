# TableTest Changelog

## [Unreleased]
### Added
- AsciiDoc custom role `scenario` added to cells in scenario column
- AsciiDoc custom role `expectation` added to cells in columns where header name ends in `?`
### Fixed
- Preventing table values from being misinterpreted as AsciiDoc markup when rendered

## [0.5.5] - 2025-11-12
### Added
- Configurable styling of lists and sets in AsciiDoc format
### Fixed
- Detects `@ConvertWith` parameter annotation when used in custom composed annotations
- Published AsciiDoc files now uses `.adoc` extension instead of `.asciidoc`
- Corrected AsciiDoc rendering of collections containing an empty collection

## [0.5.4] - 2025-11-03
### Added
- Configurable publishing of tables to TableTest, Markdown and AsciiDoc formats
### Changed
- Set and maps conserve insertion order
- Improved error message for table parse failures

## [0.5.3] - 2025-10-26
### Added
- Unsuccessful parsing of table fails the test with TableTestParseException pointing to the problematic section

## [0.5.2] - 2025-09-14
### Changed
- Empty quoted values no longer convert to `null` for non-string types
### Fixed
- Support for factory methods returning primitive type

## [0.5.1] - 2025-08-20
### Added
- Inherited factory methods are now found and used (for Java-based tests, inheritance of static methods not supported in Kotlin) 
- Compatibility tests for build systems (Maven, Gradle) and frameworks (JUnit, Quarkus, SpringBoot)
### Changed
- Easier install as dependent JUnit modules are no longer packaged with TableTest distribution
- TableTestException now of type ParameterResolutionException
- Improved user guide on value conversion topic

## [0.5.0] - 2025-06-21
### Added
- Descriptive error messages
- Allow test method parameters provided by ParameterResolvers (TestInfo, TestReporter, etc.)
- Unique test invocation display names when using value sets
- Explicit scenario name column with `@Scenario` annotated parameter can be in any position
### Changed
- Bumped JUnit dependency to 5.13.1
- More concise README.md, moved details to USERGUIDE.md


## [0.4.0] - 2025-06-10
### Added
- TableTest will search classes listed in new annotation `@FactorySources` for factory methods
- For `@Nested` test classes, TableTest will search enclosing test classes for factory methods 
- Blank cell converts to `null` also for String types
### Fixed
- Explicit leading and trailing whitespace in a quoted string is no longer removed
- More robust analysis of parameterized target types


## [0.3.1] - 2025-06-03
### Added
- Searching Kotlin test file for top-level converter functions
### Fixed
- Collection types passed to tests are no longer mutable
- An empty value set will translate to `null` to avoid row to be unintentionally ignored


## [0.3.0] - 2025-06-01
### Added
- Blank cells and empty quoted values convert to `null` for non-String types
- Support for custom type conversion methods in test class


## [0.2.1] - 2025-05-25
### Added
- Compatibility with JUnit Jupiter versions 5.11.0 and higher


## [0.2.0] - 2025-05-22
### Added
- Support for the first column being a scenario name
- Support for set values in tables
- Support for expanding set values to one test per value
- Support for loading table from external file


## [0.1.0] - 2025-05-11
### Added
- `@TableTest` annotation to declare parameterized test with multiline string input in TableTest Format
- Automatic conversion of column values to method parameter types
- Table parser for multiline strings with pipe-separated columns
- Header row for descriptive column names
- Support for simple values (quoted, unquoted)
- Support for lists with comma-separated elements
- Support for maps with key-value pairs
- Support for nested collections (lists of maps, maps of lists)
- Comment support with // prefix for documentation
- Empty line support for better formatting
