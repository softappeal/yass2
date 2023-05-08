// https://kotlinlang.org/docs/mpp-intro.html

@file:Suppress("SpellCheckingInspection")

import java.util.regex.*

plugins {
    @Suppress("DSL_SCOPE_VIOLATION") alias(libs.plugins.multiplatform) // https://github.com/gradle/gradle/issues/22797
    id("maven-publish")
    signing
}

val libraries = libs

val publishYass2 = "publishYass2"
fun Boolean.disableNativeTargetIfPublish() = this && (publishYass2 !in project.gradle.startParameter.taskNames)

val jsTarget = true
val linuxTarget = true.disableNativeTargetIfPublish()
val macTarget = true.disableNativeTargetIfPublish()

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

        if (jsTarget) {
            js {
                moduleName = project.name
                nodejs()
                binaries.executable()
            }
        }

        if (linuxTarget) linuxX64("linux")

        if (macTarget) macosArm64("mac")

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
            sign(publishing.publications)
        }
    }
}

val coreProject = project("yass2-core") {
    kotlin {
        sourceSets {
            val commonTest by getting {
                dependencies {
                    implementation(kotlin("test"))
                    implementation(libraries.coroutines.test)
                }
            }
        }
    }
}

val coroutinesProject = project("yass2-coroutines") {
    kotlin {
        sourceSets {
            val commonMain by getting {
                dependencies {
                    api(coreProject)
                    api(libraries.coroutines.core)
                }
            }
        }
    }
}

val generateProject = project("yass2-generate") {
    kotlin {
        sourceSets {
            val jvmMain by getting {
                dependencies {
                    api(coreProject)
                    api(kotlin("reflect"))
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
            if (linuxTarget) {
                val linuxMain by getting {
                    dependsOn(jvmAndNixMain)
                }
            }
            if (macTarget) {
                val macMain by getting {
                    dependsOn(jvmAndNixMain)
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
                    implementation(libraries.coroutines.test)
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
                    implementation(generateProject)
                }
            }
            if (linuxTarget) {
                val linuxTest by getting {
                    dependsOn(jvmAndNixTest)
                }
            }
            if (macTarget) {
                val macTest by getting {
                    dependsOn(jvmAndNixTest)
                }
            }
        }
    }
}

val tutorialContractProject = project("tutorial-contract") {
    kotlin {
        sourceSets {
            val commonMain by getting {
                dependencies {
                    api(coroutinesProject)
                }
            }
            val jvmTest by getting {
                dependencies {
                    implementation(generateProject)
                    implementation(kotlin("test"))
                }
            }
        }
    }
}

project("tutorial-app") {
    kotlin {
        sourceSets {
            val commonMain by getting {
                dependencies {
                    implementation(tutorialContractProject)
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
                        implementation(libraries.coroutines.test)
                    }
                }
            }
        }
    }
}

tasks.register(publishYass2) {
    listOf(coreProject, coroutinesProject, generateProject, ktorProject).forEach {
        dependsOn("${it.name}:publishAllPublicationsToOssrhRepository")
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
