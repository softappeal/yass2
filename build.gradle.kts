// https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html

val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9"

fun ktor(module: String) = "io.ktor:ktor-$module:1.4.0"

plugins {
    kotlin("multiplatform") version "1.4.0"
    id("org.jetbrains.dokka") version "1.4.0-rc"
    id("maven-publish")
    signing
}

// configurations.all { resolutionStrategy.failOnVersionConflict() }

repositories {
    jcenter()
}

group = "ch.softappeal.yass2"

tasks.register<Jar>("dokkaJar") {
    dependsOn(tasks.dokkaHtml)
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

    mingwX64("windows")

    targets.all {
        compilations.all {
            explicitApi()
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
                name = "ossrh"
                credentials(PasswordCredentials::class)
                url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            }
        }
    }

    signing {
        sign(publishing.publications)
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(coroutinesCore)
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
                implementation(kotlin("reflect"))
                api(ktor("client-core-jvm"))
                api(ktor("server-core"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation(ktor("server-netty"))
                implementation(ktor("client-apache"))
                implementation(ktor("client-cio"))
                implementation(ktor("websockets"))
            }
        }

        val jsMain by getting
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }

        val windowsMain by getting
        val windowsTest by getting
    }
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTask> {
    dokkaSourceSets {
        val commonMain by creating
        val jvmMain by creating
        val jsMain by creating
    }
}
