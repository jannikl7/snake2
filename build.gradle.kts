plugins {
    kotlin("jvm") version "2.0.20"
    id("org.openjfx.javafxplugin") version "0.0.6"
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        // Set the JVM target version
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_22)
    }
}

tasks.withType<JavaCompile> {
    sourceCompatibility = "22"
    targetCompatibility = "22"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.7.3") // If using JavaFX
    implementation("org.openjfx:javafx-controls:22")
    implementation("org.openjfx:javafx-fxml:22")
}

javafx {
    version = "23"
    modules("javafx.controls", "javafx.fxml")
}

tasks.test {
    useJUnitPlatform()
}