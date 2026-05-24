kotlin {
    sourceSets {
        commonMain {
            dependencies {
                api(project(":yass2-core"))
                api(libs.coroutines.core)
            }
        }
    }
}

dependencies {
    add("kspJvm", project(":yass2-generate"))
}

ksp {
    arg("yass.GenerateMode", "InRepository")
}
