import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.multiplatform)
}

repositories {
    mavenCentral()
}

kotlin {
    jvm()
    js {
        nodejs()
        binaries.executable()
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
        binaries.executable()
    }
}

kotlin {
    sourceSets {
        commonTest {
            dependencies {
                api(libs.ktor.client.core)
                api(libs.ktor.server.core)
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        jvmTest {
            dependencies {
                api(libs.ktor.client.cio)
                api(libs.ktor.server.cio)
            }
        }
    }
}
