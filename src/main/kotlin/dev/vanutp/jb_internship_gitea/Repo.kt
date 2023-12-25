package dev.vanutp.jb_internship_gitea

import dev.vanutp.jb_internship_gitea.exceptions.ObjectIsNotTreeException
import dev.vanutp.jb_internship_gitea.exceptions.TreeNotFoundException
import kotlinx.datetime.Clock

class Repo {
    private val objectsRw = mutableMapOf<String, Object>()
    val objects: Map<String, Object> = objectsRw
    private val commitsRw = mutableMapOf<String, Commit>()
    val commits: Map<String, Commit> = commitsRw
    private val commitsListRw = mutableListOf<String>()
    val commitList: List<Commit> get() = commitsListRw.map { commits[it]!! }

    val workingTree = Index()

    /**
     * Recursively creates a tree from index and adds it to repository objects
     */
    private fun createTree(index: Index): Tree {
        val treeObjects = mutableMapOf<String, String>()
        for (file in index.files) {
            val obj = Blob(file.value)
            treeObjects[file.key] = obj.sha1
            objectsRw[obj.sha1] = obj
        }
        for (subtree in index.subtrees) {
            val obj = createTree(subtree.value)
            treeObjects[subtree.key] = obj.sha1
        }
        val tree = Tree(treeObjects)
        objectsRw[tree.sha1] = tree
        return tree
    }

    /**
     * Creates a commit from repository working tree and adds it to repository commits
     */
    fun commit(author: String, message: String): Commit {
        val time = Clock.System.now()
        val tree = createTree(workingTree)
        val commit = Commit(tree.sha1, author, message, time)
        commitsRw[commit.sha1] = commit
        commitsListRw.add(commit.sha1)
        return commit
    }

    fun searchCommitsByAuthor(author: String): List<Commit> {
        return commitList.filter { it.author == author }
    }

    fun searchCommitsByMessage(message: String): List<Commit> {
        return commitList.filter { it.message.lowercase().contains(message.lowercase()) }
    }

    private fun getTreeContents(tree: Tree): Index {
        val res = Index()
        for (item in tree.objects) {
            when (val obj = objects[item.value]) {
                is Tree -> {
                    res.subtrees[item.key] = getTreeContents(obj)
                }
                is Blob -> {
                    res.files[item.key] = obj.data
                }
                else -> {
                    throw Exception("Unknown object type")
                }
            }
        }
        return res
    }

    fun getCommitContents(commit: Commit): Index {
        val tree = objects[commit.treeHash] ?: throw TreeNotFoundException()
        if (tree !is Tree) {
            throw ObjectIsNotTreeException()
        }
        return getTreeContents(tree)
    }
}
