plugins {
    kotlin("jvm") version "2.2.0"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}

// Allow overriding JUnit version via -Pjunit.version
val junitVersion = (project.findProperty("junit.version") as String?) ?: "5.13.4"
// Allow overriding TableTest version via -Ptabletest.version
val tabletestVersion = (project.findProperty("tabletest.version") as String?) ?: "0.5.3-SNAPSHOT"

dependencies {
    testImplementation("io.github.nchaugen:tabletest-junit:$tabletestVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation(kotlin("test"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
