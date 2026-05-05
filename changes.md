# TODO

## Dependencies

## Internal

- use Amper default layout
- tests moved from module `yass2-ktor` to new module `tests`

# 28.0.0 (2026-04-29)

## Breaking changes

- better ktor API
- fromByteArray and toByteArray moved
- better keep-alive
- generate.reflect.GenerateMode removed
- Serializer: Writer and Reader are now extensions BaseStringEncoder: write → writeBase, read → readBase
- readBinaryOptional and writeBinaryOptional moved
- BinaryInt.kt and BinaryVarInt.kt moved to BinaryPrimitives.kt
- readBinaryInt and writeBinaryBoolean removed
- annotation ExperimentalApi removed from most items
- KSP dependency is now compileOnly instead of implementation

## New features

- long encoders added

## Dependencies

    Java 25.0.2

    kotlin     = "2.3.21"        # https://github.com/JetBrains/kotlin
    coroutines = "1.10.2"        # https://github.com/Kotlin/kotlinx.coroutines
    ktor       = "3.4.3"         # https://github.com/ktorio/ktor
    ksp        = "2.3.7"         # https://github.com/google/ksp

# 27.0.0 (2026-02-11)

## Dependencies

    Java 25.0.2

    kotlin     = "2.3.10"        # https://github.com/JetBrains/kotlin
    coroutines = "1.10.2"        # https://github.com/Kotlin/kotlinx.coroutines
    ktor       = "3.4.0"         # https://github.com/ktorio/ktor
    ksp        = "2.3.5"         # https://github.com/google/ksp
