package ch.softappeal.yass2

@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.EXPRESSION)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
public annotation class UnspecifiedInitializationOrder(val workaround: String) // NOTE: documents workaround for https://youtrack.jetbrains.com/issue/KT-38181
