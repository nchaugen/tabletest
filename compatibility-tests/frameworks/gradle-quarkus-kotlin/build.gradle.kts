plugins {
    kotlin("jvm") version "2.2.0"
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(enforcedPlatform("io.quarkus:quarkus-bom:3.25.2"))

    testImplementation("io.github.nchaugen:tabletest-junit:0.5.0")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation(kotlin("test"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
