rootProject.name = "yass2"

include(
    "yass2-core",
    "yass2-coroutines",
    "yass2-reflect",
    "yass2-generate",
    "yass2-ktor",
    "test",
    "tutorial-contract",
    "tutorial",
)

pluginManagement {
    plugins {
        kotlin("multiplatform").version(extra["kotlin.version"] as String)
    }
}
