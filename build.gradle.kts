// https://kotlinlang.org/docs/multiplatform-get-started.html

@file:Suppress("SpellCheckingInspection")

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import java.util.regex.Pattern

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.ksp)
    id("maven-publish")
    signing
}

val libraries = libs

allprojects {
    apply(plugin = "org.jetbrains.kotlin.multiplatform")
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "com.google.devtools.ksp")

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
        js {
            moduleName = project.name
            nodejs()
            binaries.executable()
        }
        linuxX64()
        linuxArm64()
        macosArm64()
        explicitApi()
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            allWarningsAsErrors = true
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
            val linuxX64Main by getting {
                dependsOn(jvmAndNixMain)
            }
            val linuxArm64Main by getting {
                dependsOn(jvmAndNixMain)
            }
            val macosArm64Main by getting {
                dependsOn(jvmAndNixMain)
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
                    api(libraries.symbol.processing.api)
                    api(kotlin("reflect"))
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
                    implementation(coroutinesProject)
                    implementation(kotlin("test"))
                    implementation(libraries.kotlinx.coroutines.test)
                }
            }
            val jvmAndNixTest by creating {
                dependsOn(commonTest)
                dependencies {
                    implementation(ktorProject)
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
            val linuxX64Test by getting {
                dependsOn(jvmAndNixTest)
            }
            val linuxArm64Test by getting {
                dependsOn(jvmAndNixTest)
            }
            val macosArm64Test by getting {
                dependsOn(jvmAndNixTest)
            }
        }
    }
    ksp {
        arg("yass2.enableLogging", "false")
    }
    dependencies {
        ksp(generateProject) // NOTE: references to generated artifacts are yet wrongly red in IntelliJ; it compiles and runs correctly
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
            jsTest {
                dependencies {
                    implementation(libraries.kotlinx.coroutines.test)
                }
            }
        }
    }
    dependencies {
        ksp(generateProject)
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
