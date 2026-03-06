# Compatibility Tests

This directory contains compatibility test projects that verify TableTest works correctly across different build tools, JUnit versions, and framework versions.

## Structure

| Group | Projects | Purpose |
|-------|----------|---------|
| `basic` | maven-java, maven-kotlin, gradle-java, gradle-kotlin | Core JUnit integration; uses text blocks (Java 17 target) |
| `frameworks` | maven-quarkus-java, maven-springboot-java, gradle-quarkus-kotlin, gradle-springboot-kotlin | Framework-managed JUnit integration |

## Version Strategy

**Minimum versions** are hardcoded in each project's `pom.xml` or `build.gradle` properties. They represent the oldest supported release and are never auto-bumped.

**Latest versions** are tracked in [`latest-versions.env`](latest-versions.env) and auto-bumped weekly by the `check-versions.yml` workflow. They represent the newest compatible release within the allowed range for each branch.

CI runs both minimum and latest to catch regressions at both ends of the supported range.

## Branch Differences

| | `main` branch | `java8` branch |
|-|--------------|----------------|
| Runtime(s) | Java 17 | Java 8 + Java 15 |
| JUnit | 5.x + 6.x | 5.x only (cap) |
| Spring Boot | 3.x / 4.x | 2.x only |
| Quarkus | 3.x | 2.x only |

JUnit 6, Spring Boot 3.x, and Quarkus 3.x all require Java 17+, so the `java8` branch caps below those.

## Running Locally

Run all groups with default versions:

```bash
./test-compatibility.sh
```

Run with specific versions:

```bash
TEST_GROUPS="basic frameworks" JUNIT_VERSIONS="5.11.0" QUARKUS_VERSIONS="3.21.2" SPRINGBOOT_VERSIONS="3.0.0" ./test-compatibility.sh
```

The `TABLETEST_VERSION` environment variable overrides the artifact version used (defaults to the version defined in each project's build file):

```bash
TABLETEST_VERSION="1.0.0" ./test-compatibility.sh
```
