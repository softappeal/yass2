package ch.softappeal.yass2.core

/**
 * If this annotation is present, the code is generated in the code repository.
 *
 * Without this annotation, the code is generated in the build directory.
 * That's the normal KSP way.
 * TODO: KSP generated code is platform code (see https://github.com/google/ksp/issues/2233).
 * It only works for platform code because common/intermediate code cannot reference platform code.
 */
public annotation class GenerateInRepository
