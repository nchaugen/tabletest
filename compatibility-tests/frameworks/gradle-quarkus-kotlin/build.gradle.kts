plugins {
    kotlin("jvm") version "2.3.20"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}

// Allow overriding Quarkus version via -Pquarkus.version
val quarkusVersion = (project.findProperty("quarkus.version") as String?) ?: "3.21.2"
// Allow overriding TableTest version via -Ptabletest.version
val tabletestVersion = (project.findProperty("tabletest.version") as String?) ?: "1.2.2-SNAPSHOT"

dependencies {
    implementation(enforcedPlatform("io.quarkus:quarkus-bom:$quarkusVersion"))

    testImplementation("org.tabletest:tabletest-junit:$tabletestVersion")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
