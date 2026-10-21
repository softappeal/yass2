kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":yass2-serialize"))
                api(project(":yass2-remote"))
                api(libs.bundles.ktor)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.bundles.ktor.cio)
                implementation(libs.coroutines.test)
            }
        }
        jvmTest {
            dependencies {
                implementation(project(":yass2-generate"))
            }
        }
    }
}
