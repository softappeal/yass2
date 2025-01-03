// https://kotlinlang.org/docs/multiplatform-get-started.html

@file:Suppress("SpellCheckingInspection")

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import java.util.regex.Pattern

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.ksp)
    id("maven-publish")
    signing
}

val libraries = libs

fun hasTarget(@Suppress("UNUSED_PARAMETER") enabled: Boolean) = true // enabled
val wasmJsTarget = hasTarget(false)
val linuxX64Target = hasTarget(false)
val linuxArm64Target = hasTarget(false)
val macosArm64Target = hasTarget(false)

allprojects {
    apply(plugin = "org.jetbrains.kotlin.multiplatform")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    group = "ch.softappeal.yass2"

    repositories {
        mavenCentral()
    }

    tasks.register<Jar>("javadocJar") {
        archiveClassifier = "javadoc"
    }

    kotlin {
        jvm {
            mavenPublication {
                artifact(tasks["javadocJar"])
            }
        }
        @OptIn(ExperimentalWasmDsl::class)
        if (wasmJsTarget) wasmJs {
            nodejs()
            binaries.executable()
        }
        if (linuxX64Target) linuxX64()
        if (linuxArm64Target) linuxArm64()
        if (macosArm64Target) macosArm64()
        explicitApi()
        compilerOptions {
            allWarningsAsErrors = true
            // TODO: extraWarnings = true
        }
        publishing {
            publications.withType<MavenPublication>().onEach { publication ->
                publication.pom {
                    name = project.name
                    description = "Yet Another Service Solution"
                    url = "https://github.com/softappeal/yass2"
                    licenses { license { name = "BSD-3-Clause" } }
                    scm { url = "https://github.com/softappeal/yass2" }
                    organization { name = "softappeal GmbH Switzerland" }
                    developers { developer { name = "Angelo Salvade" } }
                }
            }
            repositories {
                maven {
                    name = "ossrh"
                    credentials(PasswordCredentials::class)
                    url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
                }
            }
        }
        signing {
            sign(publishing.publications)
        }
    }
}

val coreProject = project(":yass2-core")

val generateProject = project(":yass2-generate") {
    kotlin {
        sourceSets {
            jvmMain {
                dependencies {
                    api(coreProject)
                    implementation(kotlin("reflect"))
                    implementation(libraries.symbol.processing.api)
                    implementation(kotlin("test"))
                }
            }
        }
    }
}

val coroutinesProject = project(":yass2-coroutines") {
    kotlin {
        sourceSets {
            commonMain {
                dependencies {
                    api(coreProject)
                    api(libraries.kotlinx.coroutines.core)
                }
            }
            jvmTest {
                dependencies {
                    implementation(generateProject)
                    implementation(kotlin("test"))
                }
            }
        }
    }
}

val ktorProject = project(":yass2-ktor") {
    kotlin {
        sourceSets {
            val commonMain by getting {
                dependencies {
                    api(coroutinesProject)
                    api(libraries.ktor.client.core)
                }
            }
            val jvmAndNixMain by creating {
                dependsOn(commonMain)
                dependencies {
                    api(libraries.ktor.server.core)
                    api(libraries.ktor.network)
                }
            }
            jvmMain {
                dependsOn(jvmAndNixMain)
            }
            if (linuxX64Target) linuxX64Main {
                dependsOn(jvmAndNixMain)
            }
            if (linuxArm64Target) linuxArm64Main {
                dependsOn(jvmAndNixMain)
            }
            if (macosArm64Target) macosArm64Main {
                dependsOn(jvmAndNixMain)
            }
        }
    }
}

project(":test") { // this project is needed due to https://youtrack.jetbrains.com/issue/KT-35073
    apply(plugin = "com.google.devtools.ksp")
    kotlin {
        sourceSets {
            val commonTest by getting {
                dependencies {
                    implementation(ktorProject)
                    implementation(kotlin("test"))
                    implementation(libraries.kotlinx.coroutines.test)
                }
            }
            val jvmAndNixTest by creating {
                dependsOn(commonTest)
                dependencies {
                    implementation(libraries.bundles.ktor.cio)
                }
            }
            jvmTest {
                dependsOn(jvmAndNixTest)
                dependencies {
                    implementation(generateProject)
                    implementation(libraries.kotlin.compile.testing.ksp)
                }
            }
            if (linuxX64Target) linuxX64Test {
                dependsOn(jvmAndNixTest)
            }
            if (linuxArm64Target) linuxArm64Test {
                dependsOn(jvmAndNixTest)
            }
            if (macosArm64Target) macosArm64Test {
                dependsOn(jvmAndNixTest)
            }
        }
    }
    dependencies {
        add("kspJvmTest", generateProject)
        if (wasmJsTarget) add("kspWasmJsTest", generateProject)
        if (linuxX64Target) add("kspLinuxX64Test", generateProject)
        if (linuxArm64Target) add("kspLinuxArm64Test", generateProject)
        if (macosArm64Target) add("kspMacosArm64Test", generateProject)
    }
}

project(":tutorial") {
    kotlin {
        sourceSets {
            commonMain {
                dependencies {
                    implementation(coroutinesProject)
                }
            }
            commonTest {
                dependencies {
                    implementation(kotlin("test"))
                }
            }
            jvmMain {
                dependencies {
                    implementation(ktorProject)
                    implementation(libraries.bundles.ktor.cio)
                }
            }
            jvmTest {
                dependencies {
                    implementation(generateProject)
                }
            }
            if (wasmJsTarget) {
                wasmJsMain {
                    dependencies {
                        implementation(ktorProject)
                    }
                }
                wasmJsTest {
                    dependencies {
                        implementation(libraries.kotlinx.coroutines.test)
                    }
                }
            }
        }
    }
}

tasks.register("publishYass2") {
    listOf(coreProject, coroutinesProject, ktorProject).forEach { dependsOn("${it.name}:publishAllPublicationsToOssrhRepository") }
    dependsOn("${generateProject.name}:publishKotlinMultiplatformPublicationToOssrhRepository")
    dependsOn("${generateProject.name}:publishJvmPublicationToOssrhRepository")
}

tasks.register("markers") {
    doLast {
        fun divider(type: Char) = println(type.toString().repeat(132))
        val fileTree = fileTree(".")
        fileTree
            .exclude("/.git/")
            .exclude("/.gradle/")
            .exclude("/.idea/")
            .exclude("**/build/")
            .exclude("/.kotlin/")
            .exclude("/kotlin-js-store/")
            .exclude(".DS_Store")
        fun search(marker: String, help: String, abort: Boolean = false) {
            divider('=')
            println("= $marker - $help")
            val pattern = Pattern.compile("\\b$marker\\b", Pattern.CASE_INSENSITIVE)
            fileTree.visit {
                if (!isDirectory) {
                    var found = false
                    var number = 0
                    file.forEachLine { line ->
                        number++
                        if (pattern.matcher(line).find()) {
                            if (!found) {
                                divider('-')
                                println("+ $relativePath")
                            }
                            found = true
                            println("- $number: $line")
                            if (abort) throw Exception("abort marker $marker found")
                        }
                    }
                }
            }
        }
        search("FIXM" + "E", "not allowed for building a release", true)
        search("TOD" + "O", "under construction, yet a release can still be built")
        search("NOT" + "E", "important comment")
        divider('=')
    }
}
