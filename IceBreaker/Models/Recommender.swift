import Foundation

struct Recommendation: Identifiable, Hashable, Sendable {
    var icebreaker: Icebreaker
    /// 条件に合わなかった項目（空なら全条件にぴったり）
    var mismatches: [ConditionKind]

    var id: String { icebreaker.id }
    var isPerfect: Bool { mismatches.isEmpty }

    var matchLabel: String {
        if isPerfect { return "条件にぴったり" }
        let names = mismatches.map(\.label).joined(separator: "・")
        return "\(names)は少しアレンジが必要かも"
    }
}

enum Recommender {
    /// 条件に合うアイスブレイクを、合う度合いの高い順に返す。
    /// - 全条件一致 → 1つだけ不一致 の順。2つ以上合わないものは出さない。
    /// - 同じ度合いの中では均等にシャッフルする。
    static func recommend<R: RandomNumberGenerator>(
        for conditions: Conditions,
        from catalog: [Icebreaker],
        using rng: inout R
    ) -> [Recommendation] {
        let scored = catalog.map { item in
            Recommendation(
                icebreaker: item,
                mismatches: ConditionKind.allCases.filter { !item.matches($0, in: conditions) }
            )
        }
        let perfect = scored.filter { $0.mismatches.isEmpty }.shuffled(using: &rng)
        let near = scored.filter { $0.mismatches.count == 1 }.shuffled(using: &rng)
        return perfect + near
    }

    static func recommend(for conditions: Conditions, from catalog: [Icebreaker]) -> [Recommendation] {
        var rng = SystemRandomNumberGenerator()
        return recommend(for: conditions, from: catalog, using: &rng)
    }

    /// 今のお題とは違うお題の番号をランダムに選ぶ
    static func randomTopicIndex<R: RandomNumberGenerator>(count: Int, excluding current: Int, using rng: inout R) -> Int {
        guard count > 1 else { return 0 }
        let candidates = (0..<count).filter { $0 != current }
        return candidates.randomElement(using: &rng) ?? 0
    }
}
