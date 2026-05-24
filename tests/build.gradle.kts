kotlin {
    sourceSets {
        commonTest {
            dependencies {
                implementation(project(":yass2-ktor"))
                implementation(libs.coroutines.test)
            }
        }
        jvmTest {
            dependencies {
                implementation(project(":yass2-generate"))
                implementation(libs.bundles.ktor.cio)
                implementation(libs.kct)
            }
        }
    }
}

dependencies {
    ksp(project(":yass2-generate"))
}

ksp {
    arg("yass.GenerateMode", "WithExpectAndActual")
}
