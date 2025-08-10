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

dependencies {
    testImplementation("io.github.nchaugen:tabletest-junit:0.5.1-SNAPSHOT")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testImplementation(kotlin("test"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
