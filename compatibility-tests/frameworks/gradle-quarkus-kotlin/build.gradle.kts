plugins {
    kotlin("jvm") version "2.4.21"
}

// Allow overriding Quarkus version via -Pquarkus.version
val quarkusVersion = (project.findProperty("quarkus.version") as String?) ?: "3.21.2"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(if (quarkusVersion.startsWith("4.")) 21 else 17)
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}

// Allow overriding TableTest version via -Ptabletest.version
val tabletestVersion = (project.findProperty("tabletest.version") as String?) ?: "1.2.3-SNAPSHOT"

dependencies {
    implementation(enforcedPlatform("io.quarkus:quarkus-bom:$quarkusVersion"))

    testImplementation("org.tabletest:tabletest-junit:$tabletestVersion")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
