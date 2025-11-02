package com.pranidhi.util

import android.content.ContentResolver
import android.net.Uri
import java.security.MessageDigest

object Hasher {
    fun hashUri(cr: ContentResolver, uri: Uri): String {
        val digest = MessageDigest.getInstance("SHA-256")
        cr.openInputStream(uri)?.use { input ->
            val buf = ByteArray(64 * 1024)
            var read = input.read(buf)
            while (read > 0) {
                digest.update(buf, 0, read); read = input.read(buf)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
