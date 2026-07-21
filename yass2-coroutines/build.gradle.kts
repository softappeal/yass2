kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":yass2-core"))
                api(libs.coroutines.core)
            }
        }
        jvmTest {
            dependencies {
                implementation(project(":yass2-generate"))
            }
        }
    }
}
