plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.spring") version "2.2.0"
    id("io.spring.dependency-management") version "1.1.7"
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

// Allow overriding SpringBoot version via -Pspringboot.version
val springBootVersion = (project.findProperty("springboot.version") as String?) ?: "3.5.7"
// Allow overriding TableTest version via -Ptabletest.version
val tabletestVersion = (project.findProperty("tabletest.version") as String?) ?: "0.5.9-SNAPSHOT"

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))

    testImplementation("io.github.nchaugen:tabletest-junit:$tabletestVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
