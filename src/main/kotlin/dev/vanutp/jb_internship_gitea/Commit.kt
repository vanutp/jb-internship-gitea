package dev.vanutp.jb_internship_gitea

import dev.vanutp.jb_internship_gitea.sha1
import kotlinx.datetime.Instant

class Commit(val treeHash: String, val author: String, val message: String, val time: Instant) {
    val sha1: String by lazy {
        (treeHash + author.sha1() + message.sha1() + time.toString().sha1()).sha1()
    }
}
