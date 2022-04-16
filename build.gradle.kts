// https://kotlinlang.org/docs/mpp-intro.html

@file:Suppress("SpellCheckingInspection")

import java.util.regex.*

plugins {
    kotlin("multiplatform") version "1.6.20"
    id("maven-publish")
    signing
}
fun coroutines(module: String) = "org.jetbrains.kotlinx:kotlinx-coroutines-$module:1.6.1"
fun ktor(module: String) = "io.ktor:ktor-$module:2.0.0"

val windowsTarget = false
val macTarget = true
val jsTarget = true

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
            compilations.all {
                kotlinOptions.jvmTarget = "17"
            }
        }

        if (jsTarget) {
            js {
                moduleName = project.name
                nodejs()
            }
        }

        if (windowsTarget) mingwX64("windows")

        if (macTarget) macosArm64("mac")

        targets.all {
            compilations.all {
                explicitApi()
                kotlinOptions {
                    allWarningsAsErrors = true
                    @Suppress("SuspiciousCollectionReassignment") freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
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

val coreProject = project("module:yass2-core")

val coroutinesProject = project("module:yass2-coroutines") {
    kotlin {
        sourceSets {
            val commonMain by getting {
                dependencies {
                    api(coreProject)
                    api(coroutines("core"))
                }
            }
        }
    }
}

val reflectProject = project("module:yass2-reflect") {
    kotlin {
        sourceSets {
            val commonMain by getting {
                dependencies {
                    api(coreProject)
                }
            }
            val jvmMain by getting {
                dependencies {
                    api(kotlin("reflect"))
                }
            }
        }
    }
}

val generateProject = project("module:yass2-generate") {
    kotlin {
        sourceSets {
            val jvmMain by getting {
                dependencies {
                    api(reflectProject)
                }
            }
        }
    }
}

val ktorProject = project("module:yass2-ktor") {
    kotlin {
        sourceSets {
            val commonMain by getting {
                dependencies {
                    api(coroutinesProject)
                    api(ktor("client-core"))
                }
            }
            val jvmAndNixMain by creating {
                dependsOn(commonMain)
                dependencies {
                    api(ktor("server-core"))
                    api(ktor("network"))
                }
            }
            val jvmMain by getting {
                dependsOn(jvmAndNixMain)
            }
            if (macTarget) {
                val macMain by getting {
                    dependsOn(jvmAndNixMain)
                }
            }
        }
    }
}

project("module:test") {
    kotlin {
        sourceSets {
            val commonTest by getting {
                dependencies {
                    implementation(coroutinesProject)
                    implementation(kotlin("test"))
                    implementation(coroutines("test"))
                }
            }
            val jvmAndNixMain by creating {
                dependsOn(commonTest)
                dependencies {
                    implementation(ktorProject)
                    implementation(ktor("client-cio"))
                    implementation(ktor("server-cio"))
                    implementation(ktor("server-websockets"))
                }
            }
            val jvmTest by getting {
                dependsOn(jvmAndNixMain)
                dependencies {
                    implementation(generateProject)
                }
            }
            if (macTarget) {
                val macTest by getting {
                    dependsOn(jvmAndNixMain)
                }
            }
        }
    }
}

project("module:tutorial") {
    kotlin {
        sourceSets {
            @Suppress("ConstantConditionIf") fun yass2(module: String) = if (false) "$group:yass2-$module:0.0.0" else when (module) {
                "coroutines" -> coroutinesProject; "generate" -> generateProject; "ktor" -> ktorProject; else -> error("unexpected module")
            }

            val commonMain by getting {
                dependencies {
                    implementation(yass2("coroutines"))
                }
            }
            val commonTest by getting {
                dependencies {
                    implementation(kotlin("test"))
                }
            }
            val jvmMain by getting {
                dependencies {
                    implementation(yass2("ktor"))
                    implementation(ktor("client-cio"))
                    implementation(ktor("server-cio"))
                    implementation(ktor("server-websockets"))
                }
            }
            val jvmTest by getting {
                dependencies {
                    implementation(yass2("generate"))
                }
            }
        }
    }
}

tasks.register("publishYass2") {
    listOf(coreProject, coroutinesProject, reflectProject, generateProject, ktorProject).forEach {
        dependsOn("module:${it.name}:publishAllPublicationsToOssrhRepository")
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
