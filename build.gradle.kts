// https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html

fun coroutines(module: String) = "org.jetbrains.kotlinx:kotlinx-coroutines-$module:1.3.4"

fun ktor(module: String) = "io.ktor:ktor-$module:1.3.2"

plugins {
    kotlin("multiplatform") version "1.3.70"
    id("org.jetbrains.dokka") version "0.10.1"
    id("maven-publish")
    signing
}

val windows = true

configurations.all { resolutionStrategy.failOnVersionConflict() }

group = "ch.softappeal.yass2"

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository {
            jcenter()
        }
        filter {
            includeGroup("org.jetbrains.dokka")
            includeGroup("io.ktor")
        }
    }
}

tasks.register<Jar>("dokkaJar") {
    dependsOn(tasks.dokka)
    archiveClassifier.set("javadoc")
    from("$buildDir/dokka")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        mavenPublication {
            artifact(tasks["dokkaJar"])
        }
    }

    js {
        nodejs()
    }

    if (windows) mingwX64("windows")

    targets.all {
        compilations.all {
            kotlinOptions {
                allWarningsAsErrors = true
                freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
            }
        }
    }

    publishing {
        publications.withType<MavenPublication>().apply {
            forEach { publication ->
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
        }
        repositories {
            maven {
                url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
                credentials {
                    username = (project.findProperty("ossrhUsername") as String?) ?: "dummy"
                    password = (project.findProperty("ossrhPassword") as String?) ?: "dummy"
                }
            }
        }
    }

    signing {
        sign(publishing.publications)
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(kotlin("stdlib-common"))
                api(coroutines("core-common"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmMain by getting {
            dependencies {
                api(kotlin("stdlib"))
                implementation(kotlin("reflect"))
                api(coroutines("core"))
                api(ktor("client-core-jvm"))
                api(ktor("server-core"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation(ktor("client-cio"))
                implementation(ktor("server-cio"))
                implementation(ktor("websockets"))
            }
        }

        val jsMain by getting {
            dependencies {
                api(kotlin("stdlib-js"))
                api(coroutines("core-js"))
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }

        if (windows) {
            val windowsMain by getting {
                dependencies {
                    api(coroutines("core-native"))
                }
            }
            val windowsTest by getting {
                dependencies {
                }
            }
        }
    }
}
