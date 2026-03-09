# Compatibility Tests

This directory contains compatibility test projects that verify TableTest works correctly across different build tools, JUnit versions, and framework versions.

## Structure

| Group | Projects | Purpose |
|-------|----------|---------|
| `basic` | maven-java, maven-kotlin, gradle-java, gradle-kotlin | Core JUnit integration; uses text blocks (Java 17 target) |
| `basic-java8` | maven-java, gradle-java | Core JUnit integration; uses string array syntax (Java 8 target) |
| `frameworks` | maven-quarkus-java, maven-springboot-java, gradle-quarkus-kotlin, gradle-springboot-kotlin | Framework-managed JUnit integration |

## Version Strategy

**Minimum and latest versions** are both tracked in [`latest-versions.env`](latest-versions.env). Minimum versions represent the oldest supported release and are updated manually. Latest versions represent the newest compatible release and are auto-bumped weekly by the `check-versions.yml` workflow.

CI runs both minimum and latest to catch regressions at both ends of the supported range.

## Runtime Testing

The `basic-java8` group runs on a Java 8 JVM to verify the compiled bytecode works on the minimum supported runtime. The `basic` and `frameworks` groups run on Java 17+ where text blocks and framework dependencies are available.

JUnit 6, Spring Boot 3.x, and Quarkus 3.x all require Java 17+, so the `basic-java8` group is capped at JUnit 5.x.

## Running Locally

Build and install the TableTest artifacts first, then run all groups with default versions:

```bash
mvn install -q && ./test-compatibility.sh
```

Run with specific versions:

```bash
TEST_GROUPS="basic frameworks" JUNIT_VERSIONS="5.11.0" QUARKUS_VERSIONS="3.21.2" SPRINGBOOT_VERSIONS="3.0.0" ./test-compatibility.sh
```

The `TABLETEST_VERSION` environment variable overrides the artifact version used (defaults to the version defined in each project's build file):

```bash
TABLETEST_VERSION="1.0.0" ./test-compatibility.sh
```
