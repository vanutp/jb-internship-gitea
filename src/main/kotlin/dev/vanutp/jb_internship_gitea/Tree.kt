package dev.vanutp.jb_internship_gitea

class Tree(val objects: Map<String, String>): Object() {
    override val sha1: String by lazy {
        val sortedObjects = objects.map { Pair(it.key, it.value) }.sortedBy { it.first }
        // this string will be unique and deterministic,
        // because each object takes fixed space and objects are sorted
        val hashString = sortedObjects.joinToString("") { it.first.sha1() + it.second }
        hashString.sha1()
    }
}
