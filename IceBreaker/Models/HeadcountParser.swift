import Foundation

/// 文章中の「30人」「20名くらい」などから人数区分を判定する
enum HeadcountParser {
    static func people(for count: Int) -> PeopleCount {
        switch count {
        case ..<6: .small
        case 6...15: .medium
        default: .large
        }
    }

    /// 文章に人数が書かれていれば区分を返す（複数あれば最大値を使う）
    static func people(in text: String) -> PeopleCount? {
        let normalized = text.applyingTransform(.fullwidthToHalfwidth, reverse: false) ?? text
        let pattern = /(\d{1,4})\s*(?:人|名)/
        let counts = normalized.matches(of: pattern).compactMap { Int($0.output.1) }
        guard let max = counts.max(), max > 0 else { return nil }
        return people(for: max)
    }
}
