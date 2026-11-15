import Foundation

// MARK: - 条件

enum PeopleCount: String, Codable, CaseIterable, Identifiable, Sendable {
    case small, medium, large
    var id: String { rawValue }
    var label: String {
        switch self {
        case .small: "少人数（2〜5人）"
        case .medium: "中人数（6〜15人）"
        case .large: "大人数（16人以上）"
        }
    }
}

enum Audience: String, Codable, CaseIterable, Identifiable, Sendable {
    case preschool
    case elemLow = "elem_low"
    case elemHigh = "elem_high"
    case juniorHigh = "junior_high"
    case highSchool = "high_school"
    case student
    case business
    case family
    case mixed
    var id: String { rawValue }
    var label: String {
        switch self {
        case .preschool: "幼児（未就学児）"
        case .elemLow: "小学校低学年（1〜3年生）"
        case .elemHigh: "小学校高学年（4〜6年生）"
        case .juniorHigh: "中学生"
        case .highSchool: "高校生"
        case .student: "大学生・専門学校生"
        case .business: "ビジネス・職場（社会人）"
        case .family: "親子・ファミリーイベント"
        case .mixed: "年齢・立場バラバラの交流会"
        }
    }
}

enum Venue: String, Codable, CaseIterable, Identifiable, Sendable {
    case seated
    case mediumSpace = "medium_space"
    case wide
    case outdoor
    var id: String { rawValue }
    var label: String {
        switch self {
        case .seated: "狭い・着席のまま（会議室・オンラインも可）"
        case .mediumSpace: "普通（少し立ち上がって動ける）"
        case .wide: "広い（体育館・ホール・自由に歩き回れる）"
        case .outdoor: "屋外（公園・グラウンド・キャンプ場など）"
        }
    }
}

struct Conditions: Equatable, Sendable {
    var people: PeopleCount = .medium
    var audience: Audience = .business
    var venue: Venue = .mediumSpace
}

enum ConditionKind: CaseIterable, Sendable {
    case people, audience, venue
    var label: String {
        switch self {
        case .people: "人数"
        case .audience: "参加者"
        case .venue: "会場"
        }
    }
}

// MARK: - アイスブレイク

struct Icebreaker: Codable, Identifiable, Hashable, Sendable {
    struct Step: Codable, Hashable, Sendable {
        var title: String
        var description: String
        var symbol: String
    }

    struct Topic: Codable, Hashable, Sendable {
        var text: String
        /// nil のときは全ての参加者向け
        var audiences: [Audience]?

        func fits(_ audience: Audience) -> Bool {
            audiences?.contains(audience) ?? true
        }
    }

    var id: String
    var title: String
    var summary: String
    var minutes: String
    /// 盛り上がり度（1〜5）
    var excitement: Int
    var preparation: String
    var people: [PeopleCount]
    var audiences: [Audience]
    var venues: [Venue]
    var steps: [Step]
    var hostTip: String
    var topics: [Topic]

    func matches(_ kind: ConditionKind, in c: Conditions) -> Bool {
        switch kind {
        case .people: people.contains(c.people)
        case .audience: audiences.contains(c.audience)
        case .venue: venues.contains(c.venue)
        }
    }

    /// 参加者に合うお題（合うものが無ければ全て）
    func topics(for audience: Audience) -> [Topic] {
        let filtered = topics.filter { $0.fits(audience) }
        return filtered.isEmpty ? topics : filtered
    }
}

enum Catalog {
    static func load(bundle: Bundle = .main) -> [Icebreaker] {
        guard let url = bundle.url(forResource: "icebreakers", withExtension: "json"),
              let data = try? Data(contentsOf: url),
              let items = try? JSONDecoder().decode([Icebreaker].self, from: data)
        else {
            assertionFailure("icebreakers.json を読み込めませんでした")
            return []
        }
        return items
    }
}
