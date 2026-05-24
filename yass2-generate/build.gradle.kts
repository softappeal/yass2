kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                implementation(project(":yass2-core"))
                implementation(kotlin("reflect"))
                compileOnly(libs.ksp)
            }
        }
    }
}
