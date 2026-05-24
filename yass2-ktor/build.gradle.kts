kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":yass2-coroutines"))
                api(libs.bundles.ktor)
            }
        }
    }
}
