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
    implementation(enforcedPlatform("io.quarkus:quarkus-bom:3.25.2"))

    testImplementation("io.github.nchaugen:tabletest-junit:0.5.1-SNAPSHOT")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
