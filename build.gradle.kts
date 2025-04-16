// https://kotlinlang.org/docs/multiplatform-intro.html

@file:Suppress("SpellCheckingInspection")

import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import java.util.regex.Pattern

val os = System.getProperty("os.name").lowercase()

val linuxPlatform = os.contains("linux")
val webPlatform = true

println("os: '$os'")
println("linuxPlatform: $linuxPlatform")
println("webPlatform: $webPlatform")

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish)
}

val libraries = libs

allprojects {
    apply(plugin = "org.jetbrains.kotlin.multiplatform")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "com.vanniktech.maven.publish")

    repositories {
        mavenCentral()
    }

    kotlin {
        jvm()
        if (project.name !in setOf("yass2", "yass2-generate")) {
            if (webPlatform) {
                js {
                    nodejs()
                    binaries.executable()
                    compilerOptions {
                        target.set("es2015")
                    }
                }
                @OptIn(ExperimentalWasmDsl::class)
                wasmJs {
                    nodejs()
                    binaries.executable()
                }
            }
            if (linuxPlatform) {
                linuxX64()
                linuxArm64()
            }
        }
        explicitApi()
        compilerOptions {
            allWarningsAsErrors.set(true)
            extraWarnings.set(true)
        }
    }

    dokka {
        dokkaPublications.html {
            failOnWarning.set(true)
        }
        dokkaSourceSets {
            configureEach {
                documentedVisibilities(VisibilityModifier.Public, VisibilityModifier.Protected)
            }
        }
    }

    if (project.name != "tutorial") { // includes the root project (needed for doc over all modules)
        mavenPublishing {
            publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
            signAllPublications()
            group = "ch.softappeal.yass2"
            pom {
                name.set(project.name)
                description.set("Yet Another Service Solution")
                url.set("https://github.com/softappeal/yass2")
                licenses { license { name.set("BSD-3-Clause") } }
                scm { url.set("https://github.com/softappeal/yass2") }
                organization { name.set("softappeal GmbH Switzerland") }
                developers { developer { name.set("Angelo Salvade") } }
            }
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
            commonMain {
                dependencies {
                    api(coroutinesProject)
                    api(libraries.ktor.client.core)
                    api(libraries.ktor.server.core)
                    api(libraries.ktor.network)
                }
            }
            commonTest {
                dependencies {
                    implementation(kotlin("test"))
                    implementation(libraries.kotlinx.coroutines.test)
                }
            }
            jvmTest {
                dependencies {
                    implementation(generateProject)
                    implementation(libraries.bundles.ktor.cio)
                }
            }
        }
    }
}

dependencies {
    dokka(coreProject)
    dokka(coroutinesProject)
    dokka(ktorProject)
    dokka(generateProject)
}

project(":tutorial") {
    kotlin {
        sourceSets {
            commonMain {
                dependencies {
                    implementation(ktorProject)
                    implementation(libraries.bundles.ktor.cio)
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
