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

// Allow overriding JUnit version via -Pjunit.version, with default if not set
val junitVersion = (project.findProperty("junit.version") as String?) ?: "5.13.4"

dependencies {
    testImplementation("io.github.nchaugen:tabletest-junit:0.5.1-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation(kotlin("test"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
