kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(libs.coroutines.core)
            }
        }
        commonTest {
            dependencies {
                implementation(libs.coroutines.test)
            }
        }
        jvmTest {
            dependencies {
                implementation(kotlin("reflect"))
                implementation(project(":yass2-generate"))
            }
        }
    }
}
