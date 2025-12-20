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
    alias(libs.plugins.ksp)
    alias(libs.plugins.dokka)
    alias(libs.plugins.publish)
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.multiplatform")
    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "com.vanniktech.maven.publish")

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
        if (project.name.startsWith("yass2-")) {
            explicitApi()
            @OptIn(ExperimentalAbiValidation::class)
            abiValidation {
                enabled.set(true)
                filters {
                    excluded {
                        annotatedWith.add("ch.softappeal.yass2.core.InternalApi")
                    }
                }
            }
        }
        if (!project.file("src/commonMain/kotlin").exists()) sourceSets { /* TODO */
            commonMain {
                kotlin.srcDir(rootProject.projectDir.resolve("publish.workaround"))
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

val libraries = libs

val coreProject = project(":yass2-core")

val coroutinesProject = project(":yass2-coroutines") {
    kotlin {
        sourceSets {
            commonMain {
                dependencies {
                    api(coreProject)
                    api(libraries.coroutines.core)
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
                    api(libraries.bundles.ktor)
                }
            }
        }
    }
}

val generateProject = project(":yass2-generate") {
    kotlin {
        sourceSets {
            jvmMain {
                dependencies {
                    api(coreProject)
                    implementation(kotlin("reflect"))
                    implementation(libraries.ksp)
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

project(":test") { // tests are here due to https://youtrack.jetbrains.com/issue/KT-35073
    kotlin {
        sourceSets {
            commonTest {
                dependencies {
                    implementation(ktorProject)
                    implementation(kotlin("test"))
                    implementation(libraries.coroutines.test)
                }
            }
            jvmTest {
                dependencies {
                    implementation(libraries.bundles.ktor.cio)
                    implementation(generateProject)
                    implementation(libraries.kct)
                }
            }
        }
    }
}

project("test-ksp") {
    apply(plugin = "com.google.devtools.ksp")
    kotlin {
        sourceSets {
            commonMain {
                dependencies {
                    api(coreProject)
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
    ksp {
        arg("yass.GenerateMode", "WithExpectAndActual")
    }
    dependencies {
        add("kspJvm", generateProject)
        if (webPlatform) {
            add("kspJs", generateProject)
            add("kspWasmJs", generateProject)
        }
        if (linuxPlatform) {
            add("kspLinuxX64", generateProject)
            add("kspLinuxArm64", generateProject)
        }
    }
}

project(":tutorial") {
    apply(plugin = "com.google.devtools.ksp")
    kotlin {
        sourceSets {
            jvmMain {
                dependencies {
                    implementation(ktorProject)
                    implementation(libraries.bundles.ktor.cio)
                }
            }
            jvmTest {
                dependencies {
                    implementation(kotlin("test"))
                }
            }
        }
    }
    ksp {
        arg("yass.GenerateMode", "InRepository") // or remove line for default InBuildDir
    }
    dependencies {
        ksp(generateProject)
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
