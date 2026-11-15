import Foundation
import Observation

@MainActor
@Observable
final class AppModel {
    let catalog: [Icebreaker]
    var conditions = Conditions()

    // 結果
    private(set) var recommendations: [Recommendation] = []
    /// 結果を出したときの条件（あとで条件を変えても結果と混ざらないように保持）
    private(set) var resultConditions = Conditions()
    private(set) var currentIndex = 0
    private(set) var topicIndex = 0
    /// 結果を出した回数（スクロールのきっかけに使う）
    private(set) var resultRevision = 0

    // AI
    let aiStatus: AIStatus
    var situationText = ""
    private(set) var situationAdvice: String?
    private(set) var aiTopicsByKey: [String: [String]] = [:]
    private(set) var adaptedGuides: [String: AdaptedGuide] = [:]
    var showAdaptedGuide = false
    private(set) var busy: AIBusy?
    var aiErrorMessage: String?

    enum AIBusy: Equatable { case situation, topics, guide }

    init(catalog: [Icebreaker] = Catalog.load(), aiStatus: AIStatus = .current()) {
        self.catalog = catalog
        self.aiStatus = aiStatus
        #if DEBUG
        // 動作確認用：起動引数 -prefillSituation "…" で相談文を入れておける
        if let text = UserDefaults.standard.string(forKey: "prefillSituation") {
            situationText = text
        }
        #endif
    }

    // MARK: - 提案

    var current: Recommendation? {
        recommendations.indices.contains(currentIndex) ? recommendations[currentIndex] : nil
    }

    func generate() {
        resultConditions = conditions
        recommendations = Recommender.recommend(for: conditions, from: catalog)
        currentIndex = 0
        resetForCurrent()
        resultRevision += 1
    }

    func showNext() {
        guard !recommendations.isEmpty else { return }
        currentIndex = (currentIndex + 1) % recommendations.count
        resetForCurrent()
    }

    private func resetForCurrent() {
        showAdaptedGuide = false
        var rng = SystemRandomNumberGenerator()
        // 最初のお題も毎回ランダムにする
        topicIndex = Int.random(in: 0..<max(topics.count, 1), using: &rng)
    }

    // MARK: - お題

    struct TopicItem: Hashable {
        var text: String
        var isAI: Bool
    }

    private var topicKey: String? {
        guard let current else { return nil }
        return "\(current.icebreaker.id)|\(resultConditions.audience.rawValue)"
    }

    /// 参加者に合うお題 + AIで追加したお題
    var topics: [TopicItem] {
        guard let current else { return [] }
        let base = current.icebreaker.topics(for: resultConditions.audience).map { TopicItem(text: $0.text, isAI: false) }
        let ai = (topicKey.flatMap { aiTopicsByKey[$0] } ?? []).map { TopicItem(text: $0, isAI: true) }
        return base + ai
    }

    var currentTopic: TopicItem? {
        topics.indices.contains(topicIndex) ? topics[topicIndex] : topics.first
    }

    func selectTopic(_ index: Int) {
        guard topics.indices.contains(index) else { return }
        topicIndex = index
    }

    func randomTopic() {
        var rng = SystemRandomNumberGenerator()
        topicIndex = Recommender.randomTopicIndex(count: topics.count, excluding: topicIndex, using: &rng)
    }

    // MARK: - 進め方

    var adaptedGuide: AdaptedGuide? {
        guard let key = topicKey else { return nil }
        return adaptedGuides[key + "|\(resultConditions.people.rawValue)|\(resultConditions.venue.rawValue)"]
    }

    var displayedSteps: [Icebreaker.Step] {
        if showAdaptedGuide, let adaptedGuide { return adaptedGuide.steps }
        return current?.icebreaker.steps ?? []
    }

    var displayedHostTip: String {
        if showAdaptedGuide, let adaptedGuide { return adaptedGuide.hostTip }
        return current?.icebreaker.hostTip ?? ""
    }

    // MARK: - AI

    func askAI() async {
        let text = situationText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !text.isEmpty, busy == nil else { return }
        busy = .situation
        defer { busy = nil }
        do {
            let reading = try await AIAssistant.readSituation(text)
            conditions = reading.conditions
            situationAdvice = reading.advice
            generate()
        } catch {
            aiErrorMessage = error.localizedDescription
        }
    }

    func addAITopics() async {
        guard let current, let key = topicKey, busy == nil else { return }
        busy = .topics
        defer { busy = nil }
        do {
            let new = try await AIAssistant.generateTopics(
                for: current.icebreaker, conditions: resultConditions, existing: topics.map(\.text)
            )
            guard !new.isEmpty else {
                aiErrorMessage = "新しいお題が見つかりませんでした。もう一度お試しください。"
                return
            }
            let firstNewIndex = topics.count
            aiTopicsByKey[key, default: []].append(contentsOf: new)
            topicIndex = firstNewIndex
        } catch {
            aiErrorMessage = error.localizedDescription
        }
    }

    func toggleAdaptedGuide() async {
        if showAdaptedGuide { showAdaptedGuide = false; return }
        if adaptedGuide != nil { showAdaptedGuide = true; return }
        guard let current, let key = topicKey, busy == nil else { return }
        busy = .guide
        defer { busy = nil }
        do {
            let guide = try await AIAssistant.adaptGuide(for: current.icebreaker, conditions: resultConditions)
            adaptedGuides[key + "|\(resultConditions.people.rawValue)|\(resultConditions.venue.rawValue)"] = guide
            showAdaptedGuide = true
        } catch {
            aiErrorMessage = error.localizedDescription
        }
    }
}
