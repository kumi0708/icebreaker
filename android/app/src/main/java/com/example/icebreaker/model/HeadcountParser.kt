package com.example.icebreaker.model

import java.text.Normalizer

/** 文章中の「30人」「20名くらい」などから人数区分を判定する */
object HeadcountParser {
    private val pattern = Regex("""(\d{1,4})\s*(?:人|名)""")

    fun peopleFor(count: Int): PeopleCount = when {
        count < 6 -> PeopleCount.SMALL
        count <= 15 -> PeopleCount.MEDIUM
        else -> PeopleCount.LARGE
    }

    /** 文章に人数が書かれていれば区分を返す（複数あれば最大値を使う） */
    fun peopleIn(text: String): PeopleCount? {
        // 全角数字（１２など）を半角にそろえる
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFKC)
        val max = pattern.findAll(normalized)
            .mapNotNull { it.groupValues[1].toIntOrNull() }
            .maxOrNull()
        return if (max != null && max > 0) peopleFor(max) else null
    }
}
