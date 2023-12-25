package dev.vanutp.jb_internship_gitea

class Blob(val data: String) : Object() {
    override val sha1: String by lazy { data.sha1() }
}
