package ch.softappeal.yass2

import java.nio.file.*
import kotlin.io.path.*

private sealed class Node(val name: String)

private class FileNode(name: String, target: String) : Node(name) {
    val targets = mutableSetOf(target)
}

private class DirectoryNode(name: String) : Node(name) {
    var module: String? = null
    val name2node = mutableMapOf<String, Node>()
}

private fun Node.print(indent: Int = 0) {
    print("${"    ".repeat(indent)}- $name ")
    when (this) {
        is FileNode -> println("`${targets.sorted()}`")
        is DirectoryNode -> {
            println("`${module ?: "<no-module>"}`")
            name2node.values.sortedBy(Node::name).forEach { it.print(indent + 1) }
        }
    }
}

private const val MAIN_SUFFIX = "Main"
private const val TEST_SUFFIX = "Test"

private fun Path.forEachPath(action: (Path) -> Unit) = Files.newDirectoryStream(this).filter { ".DS_Store" != it.name }.sorted().forEach(action)

private fun Path.createNodes(modules: Set<String>?): DirectoryNode {
    val node = DirectoryNode(".")
    forEachPath { moduleDir ->
        val moduleName = moduleDir.name
        if (modules != null && moduleName !in modules) return@forEachPath
        moduleDir.resolve("src").forEachPath srcFor@{ targetDir ->
            if (targetDir.name.endsWith(TEST_SUFFIX)) return@srcFor
            check(targetDir.name.endsWith(MAIN_SUFFIX)) { "target '${targetDir.name}' must end with '$MAIN_SUFFIX'" }
            val target = targetDir.name.removeSuffix(MAIN_SUFFIX)

            fun DirectoryNode.add(directory: Path) {
                directory.forEachPath { file ->
                    fun checkSplitPackage() {
                        check(module == moduleName) {
                            "modules '$module' and '$moduleName' have split package '${targetDir.relativize(file).toString().replace('\\', '/')}'"
                        }
                    }

                    val name = file.name
                    when (val found = name2node[name]) {
                        null -> {
                            if (file.isRegularFile()) {
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

            node.add(targetDir.resolve("kotlin"))
        }
    }
    return node
}

fun Path.printModules(modules: Set<String>? = null) = createNodes(modules).print()
