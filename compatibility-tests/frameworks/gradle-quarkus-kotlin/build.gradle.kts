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

// Allow overriding Quarkus version via -Pquarkus.version, with default if not set
val quarkusVersion = (project.findProperty("quarkus.version") as String?) ?: "3.25.3"

dependencies {
    implementation(enforcedPlatform("io.quarkus:quarkus-bom:$quarkusVersion"))

    testImplementation("io.github.nchaugen:tabletest-junit:0.5.1-SNAPSHOT")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
