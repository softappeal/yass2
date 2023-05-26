// https://kotlinlang.org/docs/mpp-intro.html

@file:Suppress("SpellCheckingInspection")

import java.util.regex.*

@Suppress("DSL_SCOPE_VIOLATION") // https://github.com/gradle/gradle/issues/22797
plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.ksp)
    id("maven-publish")
    signing
}

val libraries = libs

val publishYass2 = "publishYass2"
fun Boolean.disableNativeTargetIfPublish() = this && (publishYass2 !in project.gradle.startParameter.taskNames)

val jsTarget = true
val linuxTarget = true.disableNativeTargetIfPublish()
val macTarget = true.disableNativeTargetIfPublish()

// TODO: fun String.firstCharToUppercase() = this[0].toUpperCase() + this.substring(1)

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
                /*
                kotlin.sourceSets {
                    if (targetName == "metadata") {
                        /* TODO
                        getByName("commonMain") {
                            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
                        }
                        */
                    } else {
                        val name = compilationName.firstCharToUppercase()
                        getByName("$targetName$name") {
                            kotlin.srcDir("build/generated/ksp/$targetName/$targetName$name/kotlin")
                        }
                    }
                }
                 */
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

val generateProject = project("yass2-generate") {
    kotlin {
        sourceSets {
            val jvmMain by getting {
                dependencies {
                    api(coreProject)
                    api(libraries.ksp)
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
    dependencies { // TODO
        ksp(generateProject)
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
                    implementation(kotlin("reflect"))
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
    dependencies { // TODO
        ksp(generateProject)
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
                        implementation(libraries.coroutines.test)
                    }
                }
            }
        }
    }
    dependencies { // TODO
        ksp(generateProject)
        /*
        add("kspCommonMainMetadata", generateProject)
        add("kspJvm", generateProject)
        add("kspJvmTest", generateProject)
        add("kspJs", generateProject) // TODO: if (target)
        add("kspJsTest", generateProject)
        add("kspLinux", generateProject)
        add("kspLinuxTest", generateProject)
        add("kspMac", generateProject)
        add("kspMacTest", generateProject)
        */
    }
}

tasks.register(publishYass2) {
    listOf(coreProject, coroutinesProject, ktorProject, generateProject).forEach {
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
