package dev.vanutp.jb_internship_gitea

import java.security.MessageDigest

@OptIn(ExperimentalStdlibApi::class)
fun String.sha1(): String {
    val bytes = this.toByteArray(Charsets.UTF_8)
    val byteHash = MessageDigest.getInstance("SHA-1").digest(bytes)
    return byteHash.toHexString()
}
