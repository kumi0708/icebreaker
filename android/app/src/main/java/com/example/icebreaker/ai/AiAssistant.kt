package com.example.icebreaker.ai

import com.example.icebreaker.model.Conditions
import com.example.icebreaker.model.Icebreaker

/** AI が使えるかどうか（iOS 版 AIStatus と同じ役割） */
data class AiStatus(
    val isAvailable: Boolean,
    /** 使えないときに画面に出す説明 */
    val message: String?,
)

/** AI が状況説明から読み取った条件 */
data class SituationReading(val conditions: Conditions, val advice: String)

/** AI が参加者向けに書き直した進め方 */
data class AdaptedGuide(val steps: List<Icebreaker.Step>, val hostTip: String)

class AiUnavailableException(message: String = "AI機能は現在使えません。") : Exception(message)

/**
 * 端末内 AI の入口。iOS 版の AIAssistant（Apple Intelligence）に対応する。
 * Android では Gemini Nano（ML Kit GenAI）で実装する予定。
 * 状況説明から人数を読む部分は、iOS と同じく HeadcountParser をコード側で優先すること。
 */
interface AiAssistant {
    val status: AiStatus

    suspend fun readSituation(text: String): SituationReading

    suspend fun generateTopics(icebreaker: Icebreaker, conditions: Conditions, existing: List<String>): List<String>

    suspend fun adaptGuide(icebreaker: Icebreaker, conditions: Conditions): AdaptedGuide
}

/** AI が無い環境用。AI のボタンは画面に出ず、ほかの機能はすべて使える。 */
object UnavailableAiAssistant : AiAssistant {
    override val status = AiStatus(
        isAvailable = false,
        message = "AI機能は今後のアップデートで対応予定です。ほかの機能はすべて使えます。",
    )

    override suspend fun readSituation(text: String): SituationReading = throw AiUnavailableException()

    override suspend fun generateTopics(icebreaker: Icebreaker, conditions: Conditions, existing: List<String>): List<String> =
        throw AiUnavailableException()

    override suspend fun adaptGuide(icebreaker: Icebreaker, conditions: Conditions): AdaptedGuide =
        throw AiUnavailableException()
}
