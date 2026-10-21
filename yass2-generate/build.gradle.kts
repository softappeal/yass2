kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                implementation(project(":yass2-serialize"))
                implementation(project(":yass2-remote"))
                implementation(kotlin("reflect"))
                compileOnly(libs.ksp)
            }
        }
        jvmTest {
            dependencies {
                implementation(libs.kct)
            }
        }
    }
}

dependencies {
    add("kspJvmTest", project(":yass2-generate"))
}

ksp {
    arg("yass.GenerateMode", "InRepository")
}
