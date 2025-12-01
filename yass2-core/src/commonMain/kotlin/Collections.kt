package ch.softappeal.yass2.core

public inline fun <T> Iterable<T>.forEachSeparator(separator: () -> Unit, action: (T) -> Unit) {
    val iterator = iterator()
    if (!iterator.hasNext()) return
    action(iterator.next())
    while (iterator.hasNext()) {
        separator()
        action(iterator.next())
    }
}
