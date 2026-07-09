# tabletest

The core library — extends JUnit for data-driven testing with a concise table format.
Maven coordinate `org.tabletest:tabletest-junit`.

USERGUIDE.md is the canonical spec for table syntax, value formats, and type conversion —
the source of truth; keep it in sync when changing parsing or conversion behaviour.

## Architecture

Two Maven modules under groupId `org.tabletest`:

- **tabletest-parser** — parses table syntax into a `Table`. No JUnit dependency.
- **tabletest-junit** — the JUnit 5 extension behind `@TableTest`; depends on the parser.

## Build & test

`mvn clean test` (Gradle: `gradle clean test`).

After changing the TableTest version, a *clean* rebuild is required — tests run without it
fail with `AnnotationTypeMismatchException` (Java only; Kotlin unaffected).

## Compatibility matrix

`./test-compatibility.sh` runs the sample suites across the latest and minimum supported
JUnit, Quarkus, and Spring Boot versions (defaults in `compatibility-tests/latest-versions.env`;
override per matrix via env vars documented in the script header). Run before a release,
and whenever changing parser or extension behaviour, the public API, or supported version
ranges.

## Commits

No commit-msg hook here — by hand: conventional commits, first line under 50 chars, no
AI-attribution footer.
