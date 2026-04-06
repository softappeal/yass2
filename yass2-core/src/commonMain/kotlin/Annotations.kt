package ch.softappeal.yass2.core

/**
 * An internal API must not be used.
 * It's only necessary due to cross module dependencies.
 */
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
public annotation class InternalApi

/**
 * A testing API must not be used.
 * It's only necessary because tests can't be in the same module.
 */
@MustBeDocumented
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
public annotation class TestingApi

/**
 * An experimental API may be changed or removed in future versions without any warnings and without providing any migration aids.
 * > Beware using it, especially if you're developing a library, since your library might become binary incompatible in future versions.
 */
@MustBeDocumented
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.TYPEALIAS,
)
@Retention(AnnotationRetention.BINARY)
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
public annotation class ExperimentalApi
