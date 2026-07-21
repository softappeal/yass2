kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                implementation(project(":yass2-ktor"))
                implementation(libs.bundles.ktor.cio)
            }
        }
        jvmTest {
            dependencies {
                implementation(project(":yass2-generate"))
            }
        }
    }
}
