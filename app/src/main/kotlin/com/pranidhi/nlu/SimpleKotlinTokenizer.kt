package com.pranidhi.nlu

object SimpleKotlinTokenizer {
    private val vocab = mapOf("[PAD]" to 0, "[UNK]" to 1, "open" to 2, "scan" to 3, "files" to 4)
    fun tokenizeToIds(text: String, maxLen: Int = 16, padId: Int = 0): IntArray {
        val normalized = text.lowercase().replace(Regex("[^a-z0-9\\s]"), " ").trim()
        val tokens = normalized.split(Regex("\\s+")).filter { it.isNotEmpty() }
        val ids = IntArray(maxLen) { padId }
        var i = 0
        for (t in tokens) {
            if (i >= maxLen) break
            ids[i++] = vocab[t] ?: vocab["[UNK]"] ?: 1
        }
        return ids
    }
}
