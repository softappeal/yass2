package ch.softappeal.yass2

import java.io.*

private sealed class Node(val name: String)

private class FileNode(name: String, target: String) : Node(name) {
    val targets = mutableSetOf(target)
}

private class DirectoryNode(name: String) : Node(name) {
    var module: String? = null
    val name2node = mutableMapOf<String, Node>()
}

private fun Node.print(print: (String) -> Unit, indent: Int = 0) {
    print("    ".repeat(indent))
    print("- $name ")
    when (this) {
        is FileNode -> print("`${targets.sorted()}`\n")
        is DirectoryNode -> {
            print("`${module ?: "<no-module>"}`\n")
            name2node.values.sortedBy(Node::name).forEach { it.print(print, indent + 1) }
        }
    }
}

private fun File.forEach(action: (File) -> Unit) = listFiles()!!.filter { ".DS_Store" != it.name }.sorted().forEach(action)

private val IgnoredModules = setOf("build", "test")

private const val MainSuffix = "Main"

private fun File.createNodes(): DirectoryNode {
    val node = DirectoryNode(".")
    forEach { moduleDir ->
        val moduleName = moduleDir.name
        if (moduleName in IgnoredModules) return@forEach
        File(moduleDir, "src").forEach { targetDir ->
            check(targetDir.name.endsWith(MainSuffix)) { "target '${targetDir.name}' must end with '$MainSuffix'" }
            val target = targetDir.name.removeSuffix(MainSuffix)

            fun DirectoryNode.add(directory: File) {
                directory.forEach { file ->
                    fun checkSplitPackage() {
                        check(module == moduleName) {
                            "modules '$module' and '$moduleName' have split package '${targetDir.toPath().relativize(file.toPath()).toString().replace('\\', '/')}'"
                        }
                    }

                    val name = file.name
                    when (val found = name2node[name]) {
                        null -> {
                            if (file.isFile) {
                                if (module == null) module = moduleName
                                checkSplitPackage()
                                name2node[name] = FileNode(name, target)
                            } else {
                                val d = DirectoryNode(name)
                                name2node[name] = d
                                d.add(file)
                            }
                        }
                        is FileNode -> {
                            checkSplitPackage()
                            found.targets.add(target)
                        }
                        is DirectoryNode -> found.add(file)
                    }
                }
            }

            node.add(File(targetDir, "kotlin"))
        }
    }
    return node
}

fun printModules(directory: String, print: (String) -> Unit) {
    File(directory).createNodes().print(print)
}
