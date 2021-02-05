// https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html

plugins {
    kotlin("multiplatform") version "1.4.30"
    id("maven-publish")
    signing
}
val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2"
fun ktor(module: String) = "io.ktor:ktor-$module:1.5.1"

val windowsTarget = true
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
                kotlinOptions.jvmTarget = "11"
            }
        }

        if (jsTarget) {
            js {
                moduleName = project.name
                nodejs()
            }
        }

        if (windowsTarget) mingwX64("windows")

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
                    api(coroutinesCore)
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
            val jvmMain by getting {
                dependencies {
                    api(ktor("server-core"))
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
                    implementation(kotlin("test-common"))
                    implementation(kotlin("test-annotations-common"))
                }
            }
            val jvmTest by getting {
                dependencies {
                    implementation(generateProject)
                    implementation(ktorProject)
                    implementation(ktor("server-netty"))
                    implementation(ktor("client-java"))
                    implementation(ktor("websockets"))
                    implementation(kotlin("test-junit"))
                }
            }
            if (jsTarget) {
                val jsTest by getting {
                    dependencies {
                        implementation(kotlin("test-js"))
                    }
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
