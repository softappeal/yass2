kotlin {
    sourceSets {
        commonTest {
            dependencies {
                implementation(project(":yass2-ktor"))
                implementation(libs.bundles.ktor.cio)
                implementation(libs.coroutines.test)
            }
        }
        jvmTest {
            dependencies {
                implementation(project(":yass2-generate"))
                implementation(libs.kct)
            }
        }
    }
}

dependencies {
    add("kspJvmTest", project(":yass2-generate"))
}
