package com.example.icebreaker.model

import kotlin.random.Random

data class Recommendation(
    val icebreaker: Icebreaker,
    /** 条件に合わなかった項目（空なら全条件にぴったり） */
    val mismatches: List<ConditionKind>,
) {
    val id: String get() = icebreaker.id
    val isPerfect: Boolean get() = mismatches.isEmpty()

    val matchLabel: String
        get() = if (isPerfect) {
            "条件にぴったり"
        } else {
            mismatches.joinToString("・") { it.label } + "は少しアレンジが必要かも"
        }
}

object Recommender {
    /**
     * 条件に合うアイスブレイクを、合う度合いの高い順に返す。
     * - 全条件一致 → 1つだけ不一致 の順。2つ以上合わないものは出さない。
     * - 同じ度合いの中では均等にシャッフルする。
     */
    fun recommend(
        conditions: Conditions,
        catalog: List<Icebreaker>,
        random: Random = Random.Default,
    ): List<Recommendation> {
        val scored = catalog.map { item ->
            Recommendation(
                icebreaker = item,
                mismatches = ConditionKind.entries.filterNot { item.matches(it, conditions) },
            )
        }
        val perfect = scored.filter { it.mismatches.isEmpty() }.shuffled(random)
        val near = scored.filter { it.mismatches.size == 1 }.shuffled(random)
        return perfect + near
    }

    /** 今のお題とは違うお題の番号をランダムに選ぶ */
    fun randomTopicIndex(count: Int, excluding: Int, random: Random = Random.Default): Int {
        if (count <= 1) return 0
        return (0 until count).filter { it != excluding }.random(random)
    }
}
