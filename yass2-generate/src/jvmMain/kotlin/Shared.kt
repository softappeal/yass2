package ch.softappeal.yass2.generate

internal fun Appendable.write(s: String, level: Int = 0) {
    append(s.replaceIndent("    ".repeat(level))).appendLine()
}

internal const val CSY = "ch.softappeal.yass2"
