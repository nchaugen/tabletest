# TableTest-Junit Changelog

## [Unreleased]
### Added
- Blank cells and empty quoted values convert to `null` for non-String types

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
