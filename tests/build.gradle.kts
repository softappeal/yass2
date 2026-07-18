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
    add("kspJvmTest", project(":yass2-generate"))
    if (project.extra["webPlatform"] as Boolean) {
        add("kspJsTest", project(":yass2-generate"))
        add("kspWasmJsTest", project(":yass2-generate"))
    }
    if (project.extra["linuxPlatform"] as Boolean) {
        add("kspLinuxX64Test", project(":yass2-generate"))
        add("kspLinuxArm64Test", project(":yass2-generate"))
    }
}

ksp {
    arg("yass.GenerateMode", "WithExpectAndActual")
}
