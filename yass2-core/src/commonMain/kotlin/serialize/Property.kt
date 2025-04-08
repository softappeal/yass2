package ch.softappeal.yass2.core.serialize

import kotlin.reflect.KClassifier
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.KType

public abstract class Property(private val property: KProperty1<out Any, *>, public val returnType: KType) {
    public val name: String get() = property.name
    public val mutable: Boolean get() = property is KMutableProperty1<out Any, *>
    public val nullable: Boolean get() = returnType.isMarkedNullable
    public val classifier: KClassifier? get() = returnType.classifier
}
