package ch.softappeal.yass2.serialize

/** An internal API must not be used. */
@MustBeDocumented
@Target(
    AnnotationTarget.CLASS,
)
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
public annotation class InternalSerializeApi
