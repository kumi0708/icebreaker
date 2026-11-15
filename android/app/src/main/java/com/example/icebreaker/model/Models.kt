package com.example.icebreaker.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// iOS 版 IceBreaker/Models/Models.swift と同じ構造。JSON のキーは shared/icebreakers.json に合わせる。

@Serializable
enum class PeopleCount(val label: String) {
    @SerialName("small") SMALL("少人数（2〜5人）"),
    @SerialName("medium") MEDIUM("中人数（6〜15人）"),
    @SerialName("large") LARGE("大人数（16人以上）"),
}

@Serializable
enum class Audience(val label: String) {
    @SerialName("preschool") PRESCHOOL("幼児（未就学児）"),
    @SerialName("elem_low") ELEM_LOW("小学校低学年（1〜3年生）"),
    @SerialName("elem_high") ELEM_HIGH("小学校高学年（4〜6年生）"),
    @SerialName("junior_high") JUNIOR_HIGH("中学生"),
    @SerialName("high_school") HIGH_SCHOOL("高校生"),
    @SerialName("student") STUDENT("大学生・専門学校生"),
    @SerialName("business") BUSINESS("ビジネス・職場（社会人）"),
    @SerialName("family") FAMILY("親子・ファミリーイベント"),
    @SerialName("mixed") MIXED("年齢・立場バラバラの交流会"),
}

@Serializable
enum class Venue(val label: String) {
    @SerialName("seated") SEATED("狭い・着席のまま（会議室・オンラインも可）"),
    @SerialName("medium_space") MEDIUM_SPACE("普通（少し立ち上がって動ける）"),
    @SerialName("wide") WIDE("広い（体育館・ホール・自由に歩き回れる）"),
    @SerialName("outdoor") OUTDOOR("屋外（公園・グラウンド・キャンプ場など）"),
}

data class Conditions(
    val people: PeopleCount = PeopleCount.MEDIUM,
    val audience: Audience = Audience.BUSINESS,
    val venue: Venue = Venue.MEDIUM_SPACE,
)

enum class ConditionKind(val label: String) {
    PEOPLE("人数"),
    AUDIENCE("参加者"),
    VENUE("会場"),
}

@Serializable
data class Icebreaker(
    val id: String,
    val title: String,
    val summary: String,
    val minutes: String,
    /** 盛り上がり度（1〜5） */
    val excitement: Int,
    val preparation: String,
    val people: List<PeopleCount>,
    val audiences: List<Audience>,
    val venues: List<Venue>,
    val steps: List<Step>,
    val hostTip: String,
    val topics: List<Topic>,
) {
    @Serializable
    data class Step(
        val title: String,
        val description: String,
        /** SF Symbols の名前（Android では StepIcons で Material Icons に変換） */
        val symbol: String,
    )

    @Serializable
    data class Topic(
        val text: String,
        /** null のときは全ての参加者向け */
        val audiences: List<Audience>? = null,
    ) {
        fun fits(audience: Audience): Boolean = audiences?.contains(audience) ?: true
    }

    fun matches(kind: ConditionKind, c: Conditions): Boolean = when (kind) {
        ConditionKind.PEOPLE -> c.people in people
        ConditionKind.AUDIENCE -> c.audience in audiences
        ConditionKind.VENUE -> c.venue in venues
    }

    /** 参加者に合うお題（合うものが無ければ全て） */
    fun topicsFor(audience: Audience): List<Topic> =
        topics.filter { it.fits(audience) }.ifEmpty { topics }
}

object Catalog {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(text: String): List<Icebreaker> = json.decodeFromString(text)
}
