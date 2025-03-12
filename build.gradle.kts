// https://kotlinlang.org/docs/multiplatform-get-started.html

@file:Suppress("SpellCheckingInspection")

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
    id("maven-publish")
    signing
    alias(libs.plugins.binary.compatibility.validator)
}

apiValidation {
    ignoredProjects.addAll(listOf("yass2", "tutorial", "test"))
    nonPublicMarkers.add("ch.softappeal.yass2.InternalApi")
}

val libraries = libs

allprojects {
    apply(plugin = "org.jetbrains.kotlin.multiplatform")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    group = "ch.softappeal.yass2"

    repositories {
        mavenCentral()
    }

    tasks.register<Jar>("javadocJar") {
        archiveClassifier.set("javadoc")
    }

    kotlin {
        jvm {
            mavenPublication {
                artifact(tasks["javadocJar"])
            }
        }
        if (webPlatform) {
            js {
                outputModuleName.set(project.name)
                nodejs()
                binaries.executable()
            }
            @OptIn(ExperimentalWasmDsl::class)
            wasmJs {
                outputModuleName.set(project.name + "-wasm")
                nodejs()
                binaries.executable()
            }
        }
        if (linuxPlatform) {
            linuxX64()
            linuxArm64()
        }
        explicitApi()
        compilerOptions {
            allWarningsAsErrors.set(true)
            extraWarnings.set(true)
        }
        project.publishing {
            publications.withType<MavenPublication>().onEach { publication ->
                publication.pom {
                    name.set(project.name)
                    description.set("Yet Another Service Solution")
                    url.set("https://github.com/softappeal/yass2")
                    licenses { license { name.set("BSD-3-Clause") } }
                    scm { url.set("https://github.com/softappeal/yass2") }
                    organization { name.set("softappeal GmbH Switzerland") }
                    developers { developer { name.set("Angelo Salvade") } }
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
            sign(project.publishing.publications)
        }
    }
}

val coreProject = project(":yass2-core")

val coroutinesProject = project(":yass2-coroutines") {
    kotlin {
        sourceSets {
            commonMain {
                dependencies {
                    api(coreProject)
                    api(libraries.kotlinx.coroutines.core)
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
                    implementation(kotlin("test"))
                }
            }
        }
    }
}

project(":test") { // this project is needed due to https://youtrack.jetbrains.com/issue/KT-35073
    kotlin {
        sourceSets {
            val commonTest by getting {
                dependencies {
                    implementation(ktorProject)
                    implementation(kotlin("test"))
                    implementation(libraries.kotlinx.coroutines.test)
                }
            }
            jvmTest {
                dependencies {
                    implementation(libraries.bundles.ktor.cio)
                    implementation(generateProject)
                }
            }
        }
    }
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
