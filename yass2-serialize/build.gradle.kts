kotlin {
    sourceSets {
        jvmTest {
            dependencies {
                implementation(project(":yass2-generate"))
            }
        }
    }
}
