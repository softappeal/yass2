kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                api(project(":yass2-core"))
                implementation(kotlin("reflect"))
                compileOnly(libs.ksp)
            }
        }
    }
}
