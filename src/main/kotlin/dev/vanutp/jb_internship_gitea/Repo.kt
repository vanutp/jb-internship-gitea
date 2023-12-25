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
     * Recursively creates a tree from index and adds it to repository objects.
     * If passed index is empty, no tree is created and null is returned.
     * Subtrees are created only if they are non-empty
     *
     * @return the created tree or null if the passed index was empty
     */
    private fun createTree(index: Index): Tree? {
        val treeObjects = mutableMapOf<String, String>()
        for (file in index.files) {
            val obj = Blob(file.value)
            treeObjects[file.key] = obj.sha1
            objectsRw[obj.sha1] = obj
        }
        for (subtree in index.subtrees) {
            val obj = createTree(subtree.value)
            if (obj != null) {
                treeObjects[subtree.key] = obj.sha1
            }
        }
        if (treeObjects.isEmpty()) {
            return null
        }
        val tree = Tree(treeObjects)
        objectsRw[tree.sha1] = tree
        return tree
    }

    /**
     * Same as createTree, but returns empty tree if the passed root index is empty
     *
     * @return the created tree
     */
    private fun createNonNullTree(index: Index): Tree {
        return createTree(index) ?: let {
            val tree = Tree(mapOf())
            objectsRw[tree.sha1] = tree
            tree
        }
    }

    /**
     * Creates a commit from repository working tree and adds it to repository commits
     */
    fun commit(author: String, message: String): Commit {
        val time = Clock.System.now()
        val tree = createNonNullTree(workingTree)
        val commit = Commit(tree.sha1, author, message, time)
        commitsRw[commit.sha1] = commit
        commitsListRw.add(commit.sha1)
        return commit
    }

    /**
     * Returns commits with the author equal to the passed argument.
     * Case-sensitive
     */
    fun searchCommitsByAuthor(author: String): List<Commit> {
        return commitList.filter { it.author == author }
    }

    /**
     * Returns commits commit messages of which contain the given text.
     * Case-insensitive
     */
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
