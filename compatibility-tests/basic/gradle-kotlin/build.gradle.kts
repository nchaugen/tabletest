plugins {
    kotlin("jvm") version "2.1.21"
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
    testImplementation("io.github.nchaugen:tabletest-junit:0.5.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.1")
    testImplementation(kotlin("test"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
