package dev.vanutp.jb_internship_gitea

import dev.vanutp.jb_internship_gitea.exceptions.AbsolutePathException
import dev.vanutp.jb_internship_gitea.exceptions.EmptyPathException
import java.nio.file.Path


/**
 * An object representing a mutable file tree
 */
class Index {
    internal val subtrees = mutableMapOf<String, Index>()
    internal val files = mutableMapOf<String, String>()

    fun addFile(path: List<String>, contents: String) {
        if (path.isEmpty()) {
            throw EmptyPathException()
        }
        if (path.size == 1) {
            files[path[0]] = contents
        } else {
            (subtrees[path[0]] ?: let {
                val index = Index()
                subtrees[path[0]] = index
                index
            })
                .addFile(path.drop(1), contents)
        }
    }

    fun addFile(path: Path, contents: String) {
        if (path.isAbsolute) {
            throw AbsolutePathException()
        }
        return addFile(path.map { it.toString() }, contents)
    }

    fun removeFile(path: List<String>) {
        if (path.isEmpty()) {
            throw EmptyPathException()
        }
        if (path.size == 1) {
            files.remove(path[0])
        } else {
            subtrees[path[0]]?.removeFile(path.drop(1))
        }
    }

    fun removeFile(path: Path) {
        if (path.isAbsolute) {
            throw AbsolutePathException()
        }
        return removeFile(path.map { it.toString() })
    }

    fun clear() {
        subtrees.clear()
        files.clear()
    }

    fun print(prefix: String = "") {
        for (subtree in subtrees) {
            subtree.value.print(subtree.key + "/")
        }
        for (file in files) {
            println("File $prefix${file.key}:")
            println(file.value)
        }
    }
}
