fun coroutines(module: String) = "org.jetbrains.kotlinx:kotlinx-coroutines-$module:1.3.8"

fun ktor(module: String) = "io.ktor:ktor-$module:1.3.2"

plugins {
    kotlin("jvm") version "1.3.72"
    id("org.jetbrains.dokka") version "0.10.1"
}

repositories {
    jcenter()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
        allWarningsAsErrors = true
        freeCompilerArgs += "-Xmulti-platform"
        freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }
}

sourceSets.main {
    java.srcDirs("src/commonMain/kotlin", "src/jvmMain/kotlin")
}
sourceSets.test {
    java.srcDirs("src/commonTest/kotlin", "src/jvmTest/kotlin")
}

dependencies {
    // commonMain
    api(kotlin("stdlib-common"))
    api(coroutines("core-common"))

    // jvmMain
    api(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    api(coroutines("core"))
    api(ktor("client-core-jvm"))
    api(ktor("server-core"))

    // commonTest
    testImplementation(kotlin("test-common"))
    testImplementation(kotlin("test-annotations-common"))

    // jvmTest
    testImplementation(kotlin("test-junit"))
    testImplementation(ktor("server-netty"))
    testImplementation(ktor("client-apache"))
    testImplementation(ktor("client-cio"))
    testImplementation(ktor("websockets"))
}
