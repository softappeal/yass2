package ch.softappeal.yass2.reflect

import ch.softappeal.yass2.*
import java.util.concurrent.*
import kotlin.reflect.*
import kotlin.reflect.full.*

@Suppress("UNCHECKED_CAST")
private fun KClass<*>.properties(): List<KProperty1<Any, Any?>> = memberProperties
    .filter { !isSubclassOf(Throwable::class) || (it.name != "cause" && it.name != "message") }
    .sortedBy { it.name }
    .map { it as KProperty1<Any, Any?> }

private val Class2dumperProperties = ConcurrentHashMap<KClass<*>, List<KProperty1<Any, Any?>>>()
public val ReflectionDumperProperties: DumperProperties = { type ->
    Class2dumperProperties.computeIfAbsent(type) { type.properties() }
}
