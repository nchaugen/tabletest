# TableTest Parser Changelog

## [Unreleased]

## [0.5.9] - 2025-12-31
### Added
- TableParser supports keeping string quotes to support table formatting tools
### Changed
- Independent releases of tabletest-junit and tabletest-parser

## [0.5.8] - 2025-12-15
### Fixed
- Sets retain order through conversion
- Restored compatibility for JUnit 5.11-5.12
### Removed
- Reporting functionality moved to [TableTest-Reporter](https://github.com/nchaugen/tabletest-reporter)

## [0.5.7] - 2025-12-09
### Fixed
- Reverted accidental usage of JUnit MediaType moved in JUnit 5.14 to restore compatibility with JUnit 5.12 upwards 

## [0.5.6] - 2025-12-09
### Added
- Publishing to YAML format by default
- Role `scenario` added to cells in report scenario column
- Role `expectation` added to cells in report columns where header name ends in `?`
- Using `@DisplayName` as test and table title in reports
- Added `@Description` annotation for test and table descriptions in reports
### Fixed
- Preventing table values from being misinterpreted as markup when rendered to AsciiDoc
- Including explicit whitespace in reports

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

## [0.5.1] - 2025-08-20
- No changes to tabletest-parser

## [0.5.0] - 2025-06-21
- No changes to tabletest-parser

## [0.4.0] - 2025-06-10
### Added
- Blank cell converts to `null` also for String types
### Fixed
- Explicit leading and trailing whitespace in a quoted string is no longer removed

## [0.3.1] - 2025-06-03
### Fixed
- Collection types passed to tests are no longer mutable
- An empty value set will translate to `null` to avoid row to be unintentionally ignored

## [0.3.0] - 2025-06-01
### Added
- Blank cells and empty quoted values convert to `null` for non-String types

## [0.2.1] - 2025-05-25
- No changes to tabletest-parser

## [0.2.0] - 2025-05-22
### Added
- Support for set values in tables

## [0.1.0] - 2025-05-11
### Added
- Table parser for multiline strings with pipe-separated columns
- Header row for descriptive column names
- Support for simple values (quoted, unquoted)
- Support for lists with comma-separated elements
- Support for maps with key-value pairs
- Support for nested collections (lists of maps, maps of lists)
- Comment support with // prefix for documentation
- Empty line support for better formatting
