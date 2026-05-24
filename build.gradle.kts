// https://kotlinlang.org/docs/multiplatform-intro.html

@file:Suppress("SpellCheckingInspection")

import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
import java.util.regex.Pattern

val allPlatforms = System.getProperty("os.name").lowercase().contains("linux")

val webPlatform = allPlatforms
val linuxPlatform = allPlatforms

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish)
}

// see https://amper.org/latest/user-guide/multiplatform/#module-layout
fun KotlinMultiplatformExtension.configureSourceSets() {
    sourceSets {
        commonMain {
            kotlin.srcDir("src")
        }
        commonTest {
            kotlin.srcDir("test")
        }
        jvmMain {
            kotlin.srcDir("src@jvm")
            resources.srcDir("resources@jvm")
        }
        jvmTest {
            kotlin.srcDir("test@jvm")
        }
        if (webPlatform) {
            webTest {
                kotlin.srcDir("test@web")
                resources.srcDir("testResources@web")
            }
        }
    }
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.multiplatform")
    apply(plugin = "com.google.devtools.ksp")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "com.vanniktech.maven.publish")

    repositories {
        mavenCentral()
    }

    kotlin {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        dependencies {
            testImplementation(kotlin("test"))
        }
        jvm()
        if (webPlatform) {
            js {
                outputModuleName.set(project.name)
                nodejs()
                binaries.executable()
                compilerOptions {
                    target.set("es2015")
                }
            }
            @OptIn(ExperimentalWasmDsl::class)
            wasmJs {
                outputModuleName.set(project.name)
                nodejs()
                binaries.executable()
            }
        }
        if (linuxPlatform) {
            linuxX64()
            linuxArm64()
        }
        configureSourceSets()
        compilerOptions {
            extraWarnings.set(true)
            freeCompilerArgs.add("-Xname-based-destructuring=complete")
            // freeCompilerArgs.add("-Xreturn-value-checker=full") TODO: check with allWarningsAsErrors.set(false)
            allWarningsAsErrors.set(true)
        }
        if (project.name.startsWith("yass2-")) {
            explicitApi()
            @OptIn(ExperimentalAbiValidation::class)
            if (allPlatforms) abiValidation {
                filters {
                    exclude {
                        annotatedWith.add("ch.softappeal.yass2.core.InternalYassApi")
                        annotatedWith.add("ch.softappeal.yass2.core.TestingYassApi")
                    }
                }
            }
        }
        if (!project.file("src").exists()) sourceSets {
            commonMain {
                kotlin.srcDir(rootProject.projectDir.resolve("gradle/publish.workaround"))
            }
        }
    }

    if (project.name.startsWith("yass2")) { // includes the root project (needed for doc over all modules)
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
        mavenPublishing {
            publishToMavenCentral()
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

dependencies {
    dokka(project(":yass2-core"))
    dokka(project(":yass2-generate"))
    dokka(project(":yass2-coroutines"))
    dokka(project(":yass2-ktor"))
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
