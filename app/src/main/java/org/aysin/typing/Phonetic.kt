package org.aysin.typing

/** Small deterministic phonetic layout. Uppercase distinguishes retroflex consonants. */
object Phonetic {
    private val consonants = mapOf(
        "k" to "ক", "kh" to "খ", "g" to "গ", "gh" to "ঘ", "ng" to "ং",
        "ch" to "চ", "chh" to "ছ", "j" to "জ", "jh" to "ঝ", "ny" to "ঞ",
        "T" to "ট", "Th" to "ঠ", "D" to "ড", "Dh" to "ঢ", "N" to "ণ",
        "t" to "ত", "th" to "থ", "d" to "দ", "dh" to "ধ", "n" to "ন",
        "p" to "প", "ph" to "ফ", "f" to "ফ", "b" to "ব", "bh" to "ভ",
        "v" to "ভ", "m" to "ম", "y" to "য়", "r" to "র", "l" to "ল",
        "sh" to "শ", "Sh" to "ষ", "s" to "স", "h" to "হ")
    private val vowels = mapOf(
        "a" to ("আ" to "া"), "o" to ("অ" to ""), "O" to ("ও" to "ো"),
        "i" to ("ই" to "ি"), "ee" to ("ঈ" to "ী"), "u" to ("উ" to "ু"),
        "oo" to ("ঊ" to "ূ"), "e" to ("এ" to "ে"), "oi" to ("ঐ" to "ৈ"),
        "ou" to ("ঔ" to "ৌ"))
    private val tokens = (consonants.keys + vowels.keys).sortedByDescending { it.length }
    fun convert(input: String): String = buildString {
        var pending = false
        var idx = 0
        while (idx < input.length) {
            val token = tokens.firstOrNull { input.startsWith(it,idx) }
            if (token == null) { append(input[idx++]); pending = false; continue }
            val vowel = vowels[token]
            if (vowel != null) {
                append(if (pending) vowel.second else vowel.first)
                pending = false
            } else {
                if (pending && token != "ng") append("্")
                append(consonants.getValue(token))
                pending = token != "ng"
            }
            idx += token.length
        }
    }
}
