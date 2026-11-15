import Foundation
#if canImport(FoundationModels)
import FoundationModels
#endif

/// Apple Intelligence（端末内の言語モデル）が使えるかどうか
struct AIStatus: Equatable {
    var isAvailable: Bool
    /// 使えないときに画面に出す説明
    var message: String?

    static func current() -> AIStatus {
        #if canImport(FoundationModels)
        if #available(iOS 26.0, *) {
            switch SystemLanguageModel.default.availability {
            case .available:
                return AIStatus(isAvailable: true, message: nil)
            case .unavailable(.deviceNotEligible):
                return AIStatus(isAvailable: false, message: "この端末はApple Intelligenceに対応していないため、AI機能は使えません。ほかの機能はすべて使えます。")
            case .unavailable(.appleIntelligenceNotEnabled):
                return AIStatus(isAvailable: false, message: "設定アプリで「Apple Intelligence」をオンにすると、AI機能が使えるようになります。")
            case .unavailable(.modelNotReady):
                return AIStatus(isAvailable: false, message: "AIモデルを準備中です。しばらくしてからもう一度お試しください。")
            case .unavailable:
                return AIStatus(isAvailable: false, message: "現在AI機能は使えません。")
            }
        }
        #endif
        return AIStatus(isAvailable: false, message: "AI機能はiOS 26以降のApple Intelligence対応端末で使えます。ほかの機能はすべて使えます。")
    }
}

/// AIが状況説明から読み取った条件
struct SituationReading: Equatable {
    var conditions: Conditions
    var advice: String
}

/// AIが参加者向けに書き直した進め方
struct AdaptedGuide: Equatable {
    var steps: [Icebreaker.Step]
    var hostTip: String
}

enum AIError: LocalizedError {
    case unavailable
    case message(String)

    var errorDescription: String? {
        switch self {
        case .unavailable: "AI機能は現在使えません。"
        case .message(let text): text
        }
    }
}

#if canImport(FoundationModels)

// MARK: - 生成用の型（Guided Generation）

@available(iOS 26.0, *)
@Generable
enum AIPeople {
    case small, medium, large
}

@available(iOS 26.0, *)
@Generable
enum AIAudience {
    case preschool, elementaryLower, elementaryUpper, juniorHigh, highSchool, universityStudent, business, family, mixed
}

@available(iOS 26.0, *)
@Generable
enum AIVenue {
    case seated, mediumSpace, wide, outdoor
}

@available(iOS 26.0, *)
@Generable
struct AISituation {
    @Guide(description: "参加する人の合計人数の目安。説明から読み取れなければ0")
    var headcount: Int
    @Guide(description: "人数の区分。small=2〜5人、medium=6〜15人、large=16人以上")
    var people: AIPeople
    @Guide(description: "参加者の属性。preschool=未就学児、elementaryLower=小学1〜3年、elementaryUpper=小学4〜6年、juniorHigh=中学生、highSchool=高校生、universityStudent=大学・専門学校生、business=社会人・職場、family=親子・ファミリー、mixed=年齢や立場がバラバラ")
    var audience: AIAudience
    @Guide(description: "会場。seated=狭い・着席のまま・オンライン、mediumSpace=少し立って動ける、wide=体育館やホールなど広い、outdoor=屋外")
    var venue: AIVenue
    @Guide(description: "この集まりでアイスブレイクを進めるときの、具体的なひとことアドバイス。日本語で60文字以内")
    var advice: String
}

@available(iOS 26.0, *)
@Generable
struct AITopics {
    @Guide(description: "アイスブレイクで使う新しいお題。日本語で、1つ30文字以内", .count(5))
    var topics: [String]
}

@available(iOS 26.0, *)
@Generable
struct AIStep {
    @Guide(description: "ステップの見出し。日本語で15文字以内")
    var title: String
    @Guide(description: "ステップの説明。日本語で70文字以内")
    var description: String
}

@available(iOS 26.0, *)
@Generable
struct AIGuide {
    @Guide(description: "進め方の3ステップ", .count(3))
    var steps: [AIStep]
    @Guide(description: "司会者向けのコツ。日本語で60文字以内")
    var hostTip: String
}

// MARK: - サービス

@available(iOS 26.0, *)
enum OnDeviceAI {
    private static let baseInstructions = """
    あなたは学校・職場・地域イベントの進行を手伝う、明るいアイスブレイク係です。
    回答はすべて自然でやさしい日本語で書いてください。
    参加者全員が笑顔で楽しめる、前向きでシンプルな内容にしてください。
    """

    static func readSituation(_ text: String) async throws -> SituationReading {
        let session = LanguageModelSession(instructions: baseInstructions + "\n利用者が書いた集まりの説明から、人数・参加者・会場の条件を読み取ってください。はっきり書かれていない条件は、最も自然なものを推測してください。")
        let result = try await respond(session, to: "集まりの説明：\(text)", generating: AISituation.self)
        // 人数は小さなモデルだと区分を取り違えやすいので、数字はコード側で判定する
        let people = HeadcountParser.people(in: text)
            ?? (result.headcount > 0 ? HeadcountParser.people(for: result.headcount) : nil)
            ?? result.people.model
        return SituationReading(
            conditions: Conditions(people: people, audience: result.audience.model, venue: result.venue.model),
            advice: result.advice
        )
    }

    static func generateTopics(for icebreaker: Icebreaker, conditions: Conditions, existing: [String]) async throws -> [String] {
        let session = LanguageModelSession(instructions: baseInstructions)
        let prompt = """
        アイスブレイク「\(icebreaker.title)」で使う、新しいお題を5つ考えてください。
        ゲームの説明：\(icebreaker.summary)
        参加者：\(conditions.audience.label)
        人数：\(conditions.people.label)
        会場：\(conditions.venue.label)
        今あるお題の例（これらと同じものは出さないでください）：
        \(existing.prefix(8).map { "・\($0)" }.joined(separator: "\n"))
        今あるお題と同じ形式・同じくらいの長さで書いてください。
        """
        let result = try await respond(session, to: prompt, generating: AITopics.self)
        let existingSet = Set(existing)
        return result.topics
            .map { $0.trimmingCharacters(in: .whitespacesAndNewlines) }
            .filter { !$0.isEmpty && !existingSet.contains($0) }
    }

    static func adaptGuide(for icebreaker: Icebreaker, conditions: Conditions) async throws -> AdaptedGuide {
        let session = LanguageModelSession(instructions: baseInstructions)
        let original = icebreaker.steps.enumerated()
            .map { "STEP\($0.offset + 1)「\($0.element.title)」：\($0.element.description)" }
            .joined(separator: "\n")
        let prompt = """
        アイスブレイク「\(icebreaker.title)」の進め方を、次の集まり向けに書き直してください。
        参加者：\(conditions.audience.label)
        人数：\(conditions.people.label)
        会場：\(conditions.venue.label)
        参加者にそのまま読み上げられる言葉づかいにし、人数や会場に合わせた工夫も入れてください。
        元の進め方：
        \(original)
        """
        let result = try await respond(session, to: prompt, generating: AIGuide.self)
        let steps = zip(result.steps, icebreaker.steps).map { generated, base in
            Icebreaker.Step(title: generated.title, description: generated.description, symbol: base.symbol)
        }
        return AdaptedGuide(steps: steps, hostTip: result.hostTip)
    }

    private static func respond<T: Generable>(_ session: LanguageModelSession, to prompt: String, generating type: T.Type) async throws -> T {
        do {
            return try await session.respond(to: prompt, generating: type).content
        } catch {
            #if DEBUG
            print("[OnDeviceAI] \(Swift.type(of: error)): \(error)")
            #endif
            throw AIError.message(friendlyMessage(for: error))
        }
    }

    /// エラーを利用者向けの日本語に置き換える
    static func friendlyMessage(for error: Error) -> String {
        if let error = error as? LanguageModelSession.GenerationError {
            switch error {
            case .guardrailViolation:
                return guardrailMessage
            case .unsupportedLanguageOrLocale:
                return "この言語はAIが対応していません。"
            case .exceededContextWindowSize:
                return "入力が長すぎます。短くしてお試しください。"
            default:
                break
            }
        }
        let text = String(describing: error).lowercased()
        if text.contains("guardrail") || text.contains("safety") { return guardrailMessage }
        return "AIの回答を作れませんでした。もう一度お試しください。"
    }

    private static let guardrailMessage = "AIの安全チェックにより、今回は回答を作れませんでした。お題を変えるか、もう一度お試しください。"
}

@available(iOS 26.0, *)
private extension AIPeople {
    var model: PeopleCount {
        switch self {
        case .small: .small
        case .medium: .medium
        case .large: .large
        }
    }
}

@available(iOS 26.0, *)
private extension AIAudience {
    var model: Audience {
        switch self {
        case .preschool: .preschool
        case .elementaryLower: .elemLow
        case .elementaryUpper: .elemHigh
        case .juniorHigh: .juniorHigh
        case .highSchool: .highSchool
        case .universityStudent: .student
        case .business: .business
        case .family: .family
        case .mixed: .mixed
        }
    }
}

@available(iOS 26.0, *)
private extension AIVenue {
    var model: Venue {
        switch self {
        case .seated: .seated
        case .mediumSpace: .mediumSpace
        case .wide: .wide
        case .outdoor: .outdoor
        }
    }
}

#endif

/// 画面から呼ぶ入口。iOSのバージョンやApple Intelligenceの有無をここで吸収する。
enum AIAssistant {
    static func readSituation(_ text: String) async throws -> SituationReading {
        #if canImport(FoundationModels)
        if #available(iOS 26.0, *) { return try await OnDeviceAI.readSituation(text) }
        #endif
        throw AIError.unavailable
    }

    static func generateTopics(for icebreaker: Icebreaker, conditions: Conditions, existing: [String]) async throws -> [String] {
        #if canImport(FoundationModels)
        if #available(iOS 26.0, *) { return try await OnDeviceAI.generateTopics(for: icebreaker, conditions: conditions, existing: existing) }
        #endif
        throw AIError.unavailable
    }

    static func adaptGuide(for icebreaker: Icebreaker, conditions: Conditions) async throws -> AdaptedGuide {
        #if canImport(FoundationModels)
        if #available(iOS 26.0, *) { return try await OnDeviceAI.adaptGuide(for: icebreaker, conditions: conditions) }
        #endif
        throw AIError.unavailable
    }
}
