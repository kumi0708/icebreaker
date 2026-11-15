import Foundation
import Testing
@testable import IceBreaker

struct SeededRNG: RandomNumberGenerator {
    var state: UInt64
    mutating func next() -> UInt64 {
        state = state &* 6364136223846793005 &+ 1442695040888963407
        return state
    }
}

@MainActor
struct CatalogTests {
    let catalog = Catalog.load(bundle: Bundle(for: AppModel.self))

    @Test func データが読み込める() {
        #expect(catalog.count >= 20)
        #expect(Set(catalog.map(\.id)).count == catalog.count, "IDが重複している")
    }

    @Test func 各アイスブレイクの中身がそろっている() {
        for item in catalog {
            #expect(item.steps.count == 3, "\(item.id) のステップが3つではない")
            #expect((1...5).contains(item.excitement), "\(item.id) の盛り上がり度が範囲外")
            #expect(!item.people.isEmpty && !item.audiences.isEmpty && !item.venues.isEmpty)
        }
    }

    @Test func すべての条件の組み合わせでぴったりの提案が2つ以上ある() {
        for p in PeopleCount.allCases {
            for a in Audience.allCases {
                for v in Venue.allCases {
                    let c = Conditions(people: p, audience: a, venue: v)
                    var rng = SeededRNG(state: 1)
                    let result = Recommender.recommend(for: c, from: catalog, using: &rng)
                    let perfect = result.filter(\.isPerfect).count
                    #expect(perfect >= 2, "\(p) / \(a) / \(v) のぴったり提案が \(perfect) 件")
                }
            }
        }
    }

    @Test func 参加者ごとにお題が3つ以上ある() {
        for item in catalog {
            for a in item.audiences {
                #expect(item.topics(for: a).count >= 3, "\(item.id) の \(a) 向けお題が少ない")
            }
        }
    }

    @Test func ビジネス向けに子ども専用のお題が出ない() {
        for item in catalog {
            for topic in item.topics(for: .business) {
                #expect(topic.fits(.business))
            }
        }
    }
}

struct RecommenderTests {
    static let sample: [Icebreaker] = [
        make("perfect", people: [.small], audiences: [.business], venues: [.seated]),
        make("venueMiss", people: [.small], audiences: [.business], venues: [.wide]),
        make("twoMiss", people: [.large], audiences: [.business], venues: [.wide]),
        make("allMiss", people: [.large], audiences: [.preschool], venues: [.wide]),
    ]

    static func make(_ id: String, people: [PeopleCount], audiences: [Audience], venues: [Venue]) -> Icebreaker {
        Icebreaker(id: id, title: id, summary: "", minutes: "", excitement: 3, preparation: "",
                   people: people, audiences: audiences, venues: venues,
                   steps: [], hostTip: "", topics: [])
    }

    @Test func ぴったりが先で2つ以上合わないものは出ない() {
        var rng = SeededRNG(state: 42)
        let result = Recommender.recommend(
            for: Conditions(people: .small, audience: .business, venue: .seated),
            from: Self.sample, using: &rng
        )
        #expect(result.map(\.id) == ["perfect", "venueMiss"])
        #expect(result[1].mismatches == [.venue])
        #expect(result[1].matchLabel.contains("会場"))
    }

    @Test func ランダムなお題は今のお題と違うものを選ぶ() {
        var rng = SeededRNG(state: 7)
        for current in 0..<5 {
            for _ in 0..<50 {
                let next = Recommender.randomTopicIndex(count: 5, excluding: current, using: &rng)
                #expect(next != current)
                #expect((0..<5).contains(next))
            }
        }
        #expect(Recommender.randomTopicIndex(count: 1, excluding: 0, using: &rng) == 0)
    }

    @Test func 同じ度合いの中の並びはかたよらない() {
        // 全てぴったりの候補4つ → 先頭に来る回数がほぼ均等になるはず
        let items = (0..<4).map { Self.make("i\($0)", people: [.small], audiences: [.business], venues: [.seated]) }
        var counts: [String: Int] = [:]
        var rng = SeededRNG(state: 99)
        for _ in 0..<4000 {
            let first = Recommender.recommend(
                for: Conditions(people: .small, audience: .business, venue: .seated),
                from: items, using: &rng
            ).first!.id
            counts[first, default: 0] += 1
        }
        for (_, n) in counts { #expect((800...1200).contains(n)) }
    }
}

struct HeadcountParserTests {
    @Test func 文章から人数区分を読み取る() {
        #expect(HeadcountParser.people(in: "親子あわせて30人くらいで公園") == .large)
        #expect(HeadcountParser.people(in: "新人研修、１２名、会議室") == .medium)
        #expect(HeadcountParser.people(in: "4人のチームで") == .small)
        #expect(HeadcountParser.people(in: "ゲスト5人とスタッフ20人") == .large)
        #expect(HeadcountParser.people(in: "少人数のミーティング") == nil)
    }

    @Test func 人数の境目() {
        #expect(HeadcountParser.people(for: 5) == .small)
        #expect(HeadcountParser.people(for: 6) == .medium)
        #expect(HeadcountParser.people(for: 15) == .medium)
        #expect(HeadcountParser.people(for: 16) == .large)
    }
}
