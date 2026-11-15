package com.example.icebreaker.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.icebreaker.ai.AdaptedGuide
import com.example.icebreaker.ai.AiAssistant
import com.example.icebreaker.ai.UnavailableAiAssistant
import com.example.icebreaker.model.Catalog
import com.example.icebreaker.model.Conditions
import com.example.icebreaker.model.Icebreaker
import com.example.icebreaker.model.Recommendation
import com.example.icebreaker.model.Recommender
import kotlinx.coroutines.launch
import kotlin.random.Random

/** iOS 版 AppModel.swift の移植 */
class AppViewModel @JvmOverloads constructor(
    application: Application,
    val ai: AiAssistant = UnavailableAiAssistant,
) : AndroidViewModel(application) {

    val catalog: List<Icebreaker> =
        application.assets.open("icebreakers.json").bufferedReader().use { Catalog.parse(it.readText()) }

    var conditions by mutableStateOf(Conditions())

    // 結果
    var recommendations by mutableStateOf<List<Recommendation>>(emptyList())
        private set

    /** 結果を出したときの条件（あとで条件を変えても結果と混ざらないように保持） */
    var resultConditions by mutableStateOf(Conditions())
        private set
    var currentIndex by mutableStateOf(0)
        private set
    var topicIndex by mutableStateOf(0)
        private set

    /** 結果を出した回数（スクロールのきっかけに使う） */
    var resultRevision by mutableStateOf(0)
        private set

    // AI
    var situationText by mutableStateOf("")
    var situationAdvice by mutableStateOf<String?>(null)
        private set
    private val aiTopicsByKey = mutableStateMapOf<String, List<String>>()
    private val adaptedGuides = mutableStateMapOf<String, AdaptedGuide>()
    var showAdaptedGuide by mutableStateOf(false)
        private set
    var busy by mutableStateOf<AiBusy?>(null)
        private set
    var aiErrorMessage by mutableStateOf<String?>(null)

    enum class AiBusy { SITUATION, TOPICS, GUIDE }

    data class TopicItem(val text: String, val isAi: Boolean)

    // MARK: 提案

    val current: Recommendation? get() = recommendations.getOrNull(currentIndex)

    fun generate() {
        resultConditions = conditions
        recommendations = Recommender.recommend(conditions, catalog)
        currentIndex = 0
        resetForCurrent()
        resultRevision += 1
    }

    fun showNext() {
        if (recommendations.isEmpty()) return
        currentIndex = (currentIndex + 1) % recommendations.size
        resetForCurrent()
    }

    private fun resetForCurrent() {
        showAdaptedGuide = false
        // 最初のお題も毎回ランダムにする
        topicIndex = Random.nextInt(maxOf(topics.size, 1))
    }

    // MARK: お題

    private val topicKey: String?
        get() = current?.let { "${it.icebreaker.id}|${resultConditions.audience.name}" }

    private val guideKey: String?
        get() = topicKey?.let { "$it|${resultConditions.people.name}|${resultConditions.venue.name}" }

    /** 参加者に合うお題 + AI で追加したお題 */
    val topics: List<TopicItem>
        get() {
            val current = current ?: return emptyList()
            val base = current.icebreaker.topicsFor(resultConditions.audience).map { TopicItem(it.text, false) }
            val ai = topicKey?.let { aiTopicsByKey[it] }.orEmpty().map { TopicItem(it, true) }
            return base + ai
        }

    val currentTopic: TopicItem? get() = topics.getOrNull(topicIndex) ?: topics.firstOrNull()

    fun selectTopic(index: Int) {
        if (index in topics.indices) topicIndex = index
    }

    fun randomTopic() {
        topicIndex = Recommender.randomTopicIndex(topics.size, topicIndex)
    }

    // MARK: 進め方

    val adaptedGuide: AdaptedGuide? get() = guideKey?.let { adaptedGuides[it] }

    val displayedSteps: List<Icebreaker.Step>
        get() = adaptedGuide?.takeIf { showAdaptedGuide }?.steps ?: current?.icebreaker?.steps.orEmpty()

    val displayedHostTip: String
        get() = adaptedGuide?.takeIf { showAdaptedGuide }?.hostTip ?: current?.icebreaker?.hostTip.orEmpty()

    // MARK: AI（AI が使えない環境では画面にボタンが出ない）

    fun askAi() {
        val text = situationText.trim()
        if (text.isEmpty() || busy != null) return
        runAi(AiBusy.SITUATION) {
            val reading = ai.readSituation(text)
            conditions = reading.conditions
            situationAdvice = reading.advice
            generate()
        }
    }

    fun addAiTopics() {
        val current = current ?: return
        val key = topicKey ?: return
        if (busy != null) return
        runAi(AiBusy.TOPICS) {
            val new = ai.generateTopics(current.icebreaker, resultConditions, topics.map { it.text })
            if (new.isEmpty()) {
                aiErrorMessage = "新しいお題が見つかりませんでした。もう一度お試しください。"
                return@runAi
            }
            val firstNewIndex = topics.size
            aiTopicsByKey[key] = aiTopicsByKey[key].orEmpty() + new
            topicIndex = firstNewIndex
        }
    }

    fun toggleAdaptedGuide() {
        if (showAdaptedGuide) { showAdaptedGuide = false; return }
        if (adaptedGuide != null) { showAdaptedGuide = true; return }
        val current = current ?: return
        val key = guideKey ?: return
        if (busy != null) return
        runAi(AiBusy.GUIDE) {
            adaptedGuides[key] = ai.adaptGuide(current.icebreaker, resultConditions)
            showAdaptedGuide = true
        }
    }

    private fun runAi(kind: AiBusy, block: suspend () -> Unit) {
        busy = kind
        viewModelScope.launch {
            try {
                block()
            } catch (e: Exception) {
                aiErrorMessage = e.message ?: "AIの回答を作れませんでした。もう一度お試しください。"
            } finally {
                busy = null
            }
        }
    }
}
