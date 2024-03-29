// https://kotlinlang.org/docs/multiplatform-get-started.html

@file:Suppress("SpellCheckingInspection")

import java.util.regex.Pattern

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.ksp)
    id("maven-publish")
    signing
}

val libraries = libs

val jsTarget = true
val linuxX64Target = true
val linuxArm64Target = true
val macosArm64Target = true

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

        if (jsTarget) {
            js {
                moduleName = project.name
                nodejs()
                binaries.executable()
            }
        }

        if (linuxX64Target) linuxX64()
        if (linuxArm64Target) linuxArm64()
        if (macosArm64Target) macosArm64()

        targets.all {
            compilations.all {
                explicitApi()
                kotlinOptions {
                    allWarningsAsErrors = true
                }
            }
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

val coreProject = project("yass2-core")

val coroutinesProject = project("yass2-coroutines") {
    kotlin {
        sourceSets {
            val commonMain by getting {
                dependencies {
                    api(coreProject)
                    api(libraries.kotlinx.coroutines.core)
                }
            }
        }
    }
}

val ktorProject = project("yass2-ktor") {
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
            val jvmMain by getting {
                dependsOn(jvmAndNixMain)
            }
            if (linuxX64Target) {
                val linuxX64Main by getting {
                    dependsOn(jvmAndNixMain)
                }
            }
            if (linuxArm64Target) {
                val linuxArm64Main by getting {
                    dependsOn(jvmAndNixMain)
                }
            }
            if (macosArm64Target) {
                val macosArm64Main by getting {
                    dependsOn(jvmAndNixMain)
                }
            }
        }
    }
}

val kspProject = project("yass2-ksp") {
    kotlin {
        sourceSets {
            val jvmMain by getting {
                dependencies {
                    api(coreProject)
                    api(libraries.symbol.processing.api)
                }
            }
        }
    }
}

project("test") { // this project is needed due to https://youtrack.jetbrains.com/issue/KT-35073
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
            val jvmTest by getting {
                dependsOn(jvmAndNixTest)
                dependencies {
                    implementation(kspProject)
                    implementation(kotlin("reflect"))
                    implementation(libraries.kotlin.compile.testing.ksp)
                }
            }
            if (linuxX64Target) {
                val linuxX64Test by getting {
                    dependsOn(jvmAndNixTest)
                }
            }
            if (linuxArm64Target) {
                val linuxArm64Test by getting {
                    dependsOn(jvmAndNixTest)
                }
            }
            if (macosArm64Target) {
                val macosArm64Test by getting {
                    dependsOn(jvmAndNixTest)
                }
            }
        }
    }
    ksp {
        arg("yass2.enableLogging", "false")
    }
    dependencies {
        ksp(kspProject)
    }
}

project("tutorial") {
    kotlin {
        sourceSets {
            val commonMain by getting {
                dependencies {
                    implementation(coroutinesProject)
                }
            }
            val commonTest by getting {
                dependencies {
                    implementation(kotlin("test"))
                }
            }
            val jvmMain by getting {
                dependencies {
                    implementation(ktorProject)
                    implementation(libraries.bundles.ktor.cio)
                }
            }
            if (jsTarget) {
                val jsTest by getting {
                    dependencies {
                        implementation(libraries.kotlinx.coroutines.test)
                    }
                }
            }
        }
    }
    dependencies {
        ksp(kspProject) // NOTE: references to generated artifacts are yet wrongly red in IntelliJ; it compiles and runs correctly
    }
}

tasks.register("publishYass2") {
    listOf(coreProject, coroutinesProject, ktorProject).forEach { dependsOn("${it.name}:publishAllPublicationsToOssrhRepository") }
    dependsOn("${kspProject.name}:publishKotlinMultiplatformPublicationToOssrhRepository")
    dependsOn("${kspProject.name}:publishJvmPublicationToOssrhRepository")
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
