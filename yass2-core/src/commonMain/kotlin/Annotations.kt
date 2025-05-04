package ch.softappeal.yass2.core

/**
 * An internal API should not be used outside this library.
 * It may be changed or removed in future versions without any warnings and without providing any migration aid.
 */
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
public annotation class InternalApi

/**
 * An experimental API may be changed or removed in future versions without any warnings and without providing any migration aids.
 * > Beware using it, especially if you're developing a library, since your library might become binary incompatible in future versions.
 */
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
public annotation class ExperimentalApi

@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
public annotation class NotJsPlatform
