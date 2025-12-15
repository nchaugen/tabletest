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

// Allow overriding Quarkus version via -Pquarkus.version
val quarkusVersion = (project.findProperty("quarkus.version") as String?) ?: "3.29.2"
// Allow overriding TableTest version via -Ptabletest.version
val tabletestVersion = (project.findProperty("tabletest.version") as String?) ?: "0.5.8-SNAPSHOT"

dependencies {
    implementation(enforcedPlatform("io.quarkus:quarkus-bom:$quarkusVersion"))

    testImplementation("io.github.nchaugen:tabletest-junit:$tabletestVersion")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
