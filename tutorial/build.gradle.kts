kotlin {
    sourceSets {
        jvmMain {
            dependencies {
                implementation(project(":yass2-ktor"))
                implementation(libs.bundles.ktor.cio)
            }
        }
    }
}

dependencies {
    add("kspJvm", project(":yass2-generate"))
}
