package dev.vanutp.jb_internship_gitea

import kotlin.io.path.Path

fun main() {
    val repo = Repo()
    repo.workingTree.addFile(Path("a/b/c.txt"), "meow")
    repo.workingTree.addFile(Path("a/a.txt"), "meow")
    repo.workingTree.addFile(Path("meow.txt"), "meow meow")
    val commit = repo.commit("vanutp", "test commit")
    repo.workingTree.addFile(Path("meow.txt"), "changed meow")
    val commit2 = repo.commit("vanutp", "commit 2")
    val commitIndex = repo.getCommitContents(commit)
    val commit2Index = repo.getCommitContents(commit2)
    commitIndex.print()
    commit2Index.print()
}
