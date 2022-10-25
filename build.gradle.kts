// https://kotlinlang.org/docs/mpp-intro.html

@file:Suppress("SpellCheckingInspection")

import java.util.regex.*

plugins {
    kotlin("multiplatform") version "1.7.20"
    id("maven-publish")
    signing
}
fun coroutines(module: String) = "org.jetbrains.kotlinx:kotlinx-coroutines-$module:1.6.4"
fun ktor(module: String) = "io.ktor:ktor-$module:2.1.2"

val macTarget = false

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

        js {
            moduleName = project.name
            nodejs()
        }

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

val coreProject = project("yass2-core")

val coroutinesProject = project("yass2-coroutines") {
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

val reflectProject = project("yass2-reflect") {
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

val generateProject = project("yass2-generate") {
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

val ktorProject = project("yass2-ktor") {
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

project("test") {
    kotlin {
        sourceSets {
            val commonTest by getting {
                all {
                    languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi") // for runTest
                }
                dependencies {
                    implementation(coroutinesProject)
                    implementation(kotlin("test"))
                    implementation(coroutines("test"))
                }
            }
            val jvmAndNixTest by creating {
                dependsOn(commonTest)
                dependencies {
                    implementation(ktorProject)
                    implementation(ktor("client-cio"))
                    implementation(ktor("server-cio"))
                    implementation(ktor("server-websockets"))
                }
            }
            val jvmTest by getting {
                dependsOn(jvmAndNixTest)
                dependencies {
                    implementation(generateProject)
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
                    implementation(ktor("client-cio"))
                    implementation(ktor("server-cio"))
                    implementation(ktor("server-websockets"))
                }
            }
            val jvmTest by getting {
                dependencies {
                    implementation(generateProject)
                }
            }
            val jsTest by getting {
                dependencies {
                    implementation(coroutines("test"))
                }
            }
        }
    }
}

tasks.register("publishYass2") {
    listOf(coreProject, coroutinesProject, reflectProject, generateProject, ktorProject).forEach {
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
