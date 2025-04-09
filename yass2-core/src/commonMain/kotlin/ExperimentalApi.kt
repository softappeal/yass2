package ch.softappeal.yass2.core

/**
 * This annotation marks an experimental API.
 *
 * > Beware using the annotated API especially if you're developing a library, since your library might become binary incompatible with  future versions.
 */
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
public annotation class ExperimentalApi
