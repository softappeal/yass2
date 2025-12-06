// https://kotlinlang.org/docs/multiplatform-intro.html

@file:Suppress("SpellCheckingInspection")

import org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.abi.ExperimentalAbiValidation
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

allprojects {
    apply(plugin = "org.jetbrains.kotlin.multiplatform")
    repositories {
        mavenCentral()
    }
    kotlin {
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
        compilerOptions {
            allWarningsAsErrors.set(true)
            extraWarnings.set(true)
        }
    }
}

kotlin {
    @OptIn(ExperimentalAbiValidation::class)
    abiValidation {
        enabled.set(true)
    }
    explicitApi()
    sourceSets {
        jvmMain {
            dependencies {
                implementation(kotlin("reflect"))
                implementation(libs.ksp)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.coroutines.core)
                implementation(libs.bundles.ktor)
                implementation(kotlin("test"))
                implementation(libs.coroutines.test)
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.bundles.ktor.cio)
                implementation(libs.kct)
            }
        }
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
