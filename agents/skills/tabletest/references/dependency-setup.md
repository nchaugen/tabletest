# Dependency and Import Setup

Prefer the version already used in the project. If adding a new dependency, check Maven Central and replace `VERSION`.

## Maven

```xml
<dependency>
    <groupId>io.github.nchaugen</groupId>
    <artifactId>tabletest-junit</artifactId>
    <version>VERSION</version>
    <scope>test</scope>
</dependency>
```

## Gradle

```groovy
testImplementation "io.github.nchaugen:tabletest-junit:VERSION"
```

## Imports

```java
import io.github.nchaugen.tabletest.junit.TableTest;
```

Import `@Scenario` only when binding the scenario column:
```java
import io.github.nchaugen.tabletest.junit.Scenario;
```
