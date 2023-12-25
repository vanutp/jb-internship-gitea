package dev.vanutp.jb_internship_gitea

import dev.vanutp.jb_internship_gitea.exceptions.DirectoryExistsException
import dev.vanutp.jb_internship_gitea.exceptions.FileExistsException
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RepoTest {
    fun assertFileEquals(index: Index, path: String, contents: String) {
        val splitPath = path.split("/")
        var currIndex = index
        splitPath.dropLast(1).forEach {
            assertNotNull(currIndex.subtrees[it])
            currIndex = currIndex.subtrees[it]!!
        }
        assertEquals(contents, currIndex.files[splitPath.last()])
    }

    @Test
    fun simpleUsage() {
        val repo = Repo()

        repo.workingTree.addFile("file1", "file1 contents")
        repo.workingTree.addFile("file2", "file2 contents")
        repo.workingTree.addFile("directory1/file1", "file1 in directory1")
        repo.workingTree.addFile("directory2/file1", "file1 in directory2")

        repo.commit("vanutp", "test commit")

        val lastCommit = repo.commitList.last()
        val lastCommitContents = repo.getCommitContents(lastCommit)

        assertEquals(2, lastCommitContents.subtrees.size)
        assertEquals(2, lastCommitContents.files.size)
        assertFileEquals(lastCommitContents, "file1", "file1 contents")
        assertFileEquals(lastCommitContents, "file2", "file2 contents")
        assertFileEquals(lastCommitContents, "directory1/file1", "file1 in directory1")
        assertFileEquals(lastCommitContents, "directory2/file1", "file1 in directory2")
        assertEquals(1, lastCommitContents.subtrees["directory1"]!!.files.size)
        assertEquals(1, lastCommitContents.subtrees["directory2"]!!.files.size)
    }

    @Test
    fun changeFileContents() {
        val repo = Repo()

        repo.workingTree.addFile("changing_contents", "changing_contents before change")
        repo.commit("vanutp", "commit 1")

        repo.workingTree.addFile("changing_contents", "changing_contents after change")
        repo.commit("vanutp", "commit 2")

        assertEquals(2, repo.commitList.size)

        val firstCommit = repo.commitList[0]
        val firstCommitContents = repo.getCommitContents(firstCommit)
        assertEquals("commit 1", firstCommit.message)
        assertEquals(0, firstCommitContents.subtrees.size)
        assertEquals(1, firstCommitContents.files.size)
        assertFileEquals(firstCommitContents, "changing_contents", "changing_contents before change")

        val secondCommit = repo.commitList[1]
        val secondCommitContents = repo.getCommitContents(secondCommit)
        assertEquals("commit 2", secondCommit.message)
        assertEquals(0, secondCommitContents.subtrees.size)
        assertEquals(1, secondCommitContents.files.size)
        assertFileEquals(secondCommitContents, "changing_contents", "changing_contents after change")
    }

    @Test
    fun deleteFile() {
        val repo = Repo()

        repo.workingTree.addFile("static_file", "static file")
        repo.workingTree.addFile("will_be_deleted_file", "will_be_deleted_file")
        repo.workingTree.addFile("static_dir/file", "static file")
        repo.workingTree.addFile("will_be_deleted_dir/file", "will_be_deleted_dir")
        repo.commit("vanutp", "commit 1")

        repo.workingTree.removeFile("will_be_deleted_file")
        repo.workingTree.removeFile("will_be_deleted_dir/file")
        repo.commit("vanutp", "commit 2")

        assertEquals(2, repo.commitList.size)

        val firstCommit = repo.commitList[0]
        val firstCommitContents = repo.getCommitContents(firstCommit)
        assertEquals("commit 1", firstCommit.message)
        assertEquals(2, firstCommitContents.subtrees.size)
        assertEquals(2, firstCommitContents.files.size)
        assertFileEquals(firstCommitContents, "will_be_deleted_file", "will_be_deleted_file")
        assertFileEquals(firstCommitContents, "will_be_deleted_dir/file", "will_be_deleted_dir")

        val secondCommit = repo.commitList[1]
        val secondCommitContents = repo.getCommitContents(secondCommit)
        assertEquals("commit 2", secondCommit.message)
        assertEquals(1, secondCommitContents.subtrees.size)
        assertEquals(1, secondCommitContents.files.size)
        assertNull(secondCommitContents.files["will_be_deleted_file"])
        assertNull(secondCommitContents.subtrees["will_be_deleted_dir"])
    }

    @Test
    fun clearTree() {
        val repo = Repo()

        repo.workingTree.addFile("static_file", "static file")
        repo.workingTree.addFile("static_dir/file", "static file")
        repo.commit("vanutp", "commit 1")

        repo.workingTree.clear()
        repo.commit("vanutp", "commit 2")

        assertEquals(2, repo.commitList.size)
        assertEquals(2, repo.commits.size)

        val firstCommit = repo.commitList[0]
        val firstCommitContents = repo.getCommitContents(firstCommit)
        assertEquals("commit 1", firstCommit.message)
        assertEquals(1, firstCommitContents.subtrees.size)
        assertEquals(1, firstCommitContents.files.size)
        assertFileEquals(firstCommitContents, "static_file", "static file")
        assertFileEquals(firstCommitContents, "static_dir/file", "static file")

        val secondCommit = repo.commitList[1]
        val secondCommitContents = repo.getCommitContents(secondCommit)
        assertEquals("commit 2", secondCommit.message)
        assertEquals(0, secondCommitContents.subtrees.size)
        assertEquals(0, secondCommitContents.files.size)
    }

    @Test
    fun multipleActions() {
        val repo = Repo()

        repo.workingTree.addFile("static_file", "static file")
        repo.workingTree.addFile("will_be_deleted_file", "will_be_deleted_file")
        repo.workingTree.addFile("static_dir/file", "static file")
        repo.workingTree.addFile("changing_contents/file", "changing_contents before change")
        repo.workingTree.addFile("will_be_deleted_dir/file", "will_be_deleted_dir")
        repo.commit("vanutp", "commit 1")

        repo.workingTree.removeFile("will_be_deleted_file")
        repo.workingTree.removeFile("will_be_deleted_dir/file")
        repo.workingTree.addFile("changing_contents/file", "changing_contents after change")
        repo.commit("vanutp", "commit 2")

        repo.workingTree.clear()
        repo.commit("vanutp", "commit 3")

        assertEquals(3, repo.commitList.size)
        assertEquals(3, repo.commits.size)

        val firstCommit = repo.commitList[0]
        val firstCommitContents = repo.getCommitContents(firstCommit)
        assertEquals("commit 1", firstCommit.message)
        assertEquals(3, firstCommitContents.subtrees.size)
        assertEquals(2, firstCommitContents.files.size)
        assertFileEquals(firstCommitContents, "static_file", "static file")
        assertFileEquals(firstCommitContents, "will_be_deleted_file", "will_be_deleted_file")
        assertFileEquals(firstCommitContents, "static_dir/file", "static file")
        assertFileEquals(firstCommitContents, "changing_contents/file", "changing_contents before change")
        assertFileEquals(firstCommitContents, "will_be_deleted_dir/file", "will_be_deleted_dir")

        val secondCommit = repo.commitList[1]
        val secondCommitContents = repo.getCommitContents(secondCommit)
        assertEquals("commit 2", secondCommit.message)
        assertEquals(2, secondCommitContents.subtrees.size)
        assertEquals(1, secondCommitContents.files.size)
        assertFileEquals(secondCommitContents, "static_file", "static file")
        assertFileEquals(secondCommitContents, "static_dir/file", "static file")
        assertFileEquals(secondCommitContents, "changing_contents/file", "changing_contents after change")

        val thirdCommit = repo.commitList[2]
        val thirdCommitContents = repo.getCommitContents(thirdCommit)
        assertEquals("commit 3", thirdCommit.message)
        assertEquals(0, thirdCommitContents.subtrees.size)
        assertEquals(0, thirdCommitContents.files.size)
    }

    @Test
    fun basicDeduplication() {
        val repo = Repo()

        repo.workingTree.addFile("file1", "static contents")
        repo.workingTree.addFile("file2", "static contents")
        repo.workingTree.addFile("dir1/file", "static contents")
        repo.workingTree.addFile("dir2/file", "static contents")

        repo.commit("vanutp", "commit")

        assertEquals(3, repo.objects.size)
        assertEquals(1, repo.objects.filter { it.value is Blob }.size)
        // both dir1 and dir2 are equal and should be deduplicated
        // the second tree is the root tree
        assertEquals(2, repo.objects.filter { it.value is Tree }.size)
    }

    @Test
    fun crossCommitDeduplication() {
        val repo = Repo()

        repo.workingTree.addFile("file", "static contents")
        repo.commit("vanutp", "commit 1")
        repo.workingTree.removeFile("file")
        repo.workingTree.addFile("dir/renamed_file", "static contents")
        repo.commit("vanutp", "commit 2")

        assertEquals(4, repo.objects.size)
        assertEquals(1, repo.objects.filter { it.value is Blob }.size)
        // commit 1 root tree, commit 2 root tree, "dir" subtree
        assertEquals(3, repo.objects.filter { it.value is Tree }.size)
    }

    @Test
    fun sameNameFileAndDir() {
        val repo = Repo()
        repo.workingTree.addFile("name", "contents")
        assertFailsWith<FileExistsException> {
            repo.workingTree.addFile("name/file", "contents")
        }
    }

    @Test
    fun sameNameDirAndFile() {
        val repo = Repo()
        repo.workingTree.addFile("name/file", "contents")
        assertFailsWith<DirectoryExistsException> {
            repo.workingTree.addFile("name", "contents")
        }
    }

    @Test
    fun getCommitByHash() {
        val repo = Repo()

        repo.workingTree.addFile("file1", "file1")
        val firstCommit = repo.commit("vanutp", "commit 1")
        repo.workingTree.addFile("file2", "file2")
        val secondCommit = repo.commit("vanutp", "commit 2")

        val foundFirstCommit = repo.commits[firstCommit.sha1]
        assertNotNull(foundFirstCommit)
        assertEquals(foundFirstCommit.message, "commit 1")
        val foundSecondCommit = repo.commits[secondCommit.sha1]
        assertNotNull(foundSecondCommit)
        assertEquals(foundSecondCommit.message, "commit 2")
    }

    @Test
    fun searchCommitByName() {
        val repo = Repo()

        repo.workingTree.addFile("file1", "file1")
        repo.commit("vanutp", "the Commit #1")
        repo.workingTree.addFile("file2", "file2")
        repo.commit("vanutp", "the commIt #2")
        repo.workingTree.addFile("file3", "file3")
        repo.commit("vanutp", "something else")

        val foundCommits1 = repo.searchCommitsByMessage("coMMit")
        assertEquals(foundCommits1.size, 2)

        val foundCommits2 = repo.searchCommitsByMessage("else")
        assertEquals(foundCommits2.size, 1)
    }

    @Test
    fun searchCommitByAuthor() {
        val repo = Repo()

        repo.workingTree.addFile("file1", "file1")
        repo.commit("author 1", "commit 1")
        repo.workingTree.addFile("file2", "file2")
        repo.commit("author 2", "commit 2")

        val foundCommits1 = repo.searchCommitsByAuthor("author 2")
        assertEquals(foundCommits1.size, 1)

        val foundCommits2 = repo.searchCommitsByMessage("author")
        assertEquals(foundCommits2.size, 0)
    }
}
