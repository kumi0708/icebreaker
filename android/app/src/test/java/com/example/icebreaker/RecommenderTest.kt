package com.example.icebreaker

import com.example.icebreaker.model.Audience
import com.example.icebreaker.model.Catalog
import com.example.icebreaker.model.ConditionKind
import com.example.icebreaker.model.Conditions
import com.example.icebreaker.model.HeadcountParser
import com.example.icebreaker.model.Icebreaker
import com.example.icebreaker.model.PeopleCount
import com.example.icebreaker.model.Recommender
import com.example.icebreaker.model.Venue
import com.example.icebreaker.ui.StepIcons
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.random.Random

// iOS 版 IceBreakerTests/RecommenderTests.swift の移植

/** iOS と共有している shared/icebreakers.json を読む（テストの作業フォルダは android/app） */
private val catalog: List<Icebreaker> by lazy {
    val file = listOf("../../shared/icebreakers.json", "../shared/icebreakers.json", "shared/icebreakers.json")
        .map(::File)
        .first { it.exists() }
    Catalog.parse(file.readText())
}

class CatalogTest {
    @Test
    fun データが読み込める() {
        assertTrue(catalog.size >= 20)
        assertEquals("IDが重複している", catalog.size, catalog.map { it.id }.toSet().size)
    }

    @Test
    fun 各アイスブレイクの中身がそろっている() {
        for (item in catalog) {
            assertEquals("${item.id} のステップが3つではない", 3, item.steps.size)
            assertTrue("${item.id} の盛り上がり度が範囲外", item.excitement in 1..5)
            assertTrue(item.people.isNotEmpty() && item.audiences.isNotEmpty() && item.venues.isNotEmpty())
        }
    }

    @Test
    fun すべての条件の組み合わせでぴったりの提案が2つ以上ある() {
        for (p in PeopleCount.entries) for (a in Audience.entries) for (v in Venue.entries) {
            val result = Recommender.recommend(Conditions(p, a, v), catalog, Random(1))
            val perfect = result.count { it.isPerfect }
            assertTrue("$p / $a / $v のぴったり提案が $perfect 件", perfect >= 2)
        }
    }

    @Test
    fun 参加者ごとにお題が3つ以上ある() {
        for (item in catalog) for (a in item.audiences) {
            assertTrue("${item.id} の $a 向けお題が少ない", item.topicsFor(a).size >= 3)
        }
    }

    @Test
    fun ビジネス向けに子ども専用のお題が出ない() {
        for (item in catalog) for (topic in item.topicsFor(Audience.BUSINESS)) {
            assertTrue(topic.fits(Audience.BUSINESS))
        }
    }

    @Test
    fun JSONのアイコン名がすべてAndroidのアイコンに対応している() {
        val missing = catalog.flatMap { it.steps }.map { it.symbol }.toSet() - StepIcons.map.keys
        assertTrue("StepIcons に無い symbol: $missing", missing.isEmpty())
    }
}

class RecommenderTest {
    private fun make(id: String, people: List<PeopleCount>, audiences: List<Audience>, venues: List<Venue>) =
        Icebreaker(
            id = id, title = id, summary = "", minutes = "", excitement = 3, preparation = "",
            people = people, audiences = audiences, venues = venues,
            steps = emptyList(), hostTip = "", topics = emptyList(),
        )

    private val sample = listOf(
        make("perfect", listOf(PeopleCount.SMALL), listOf(Audience.BUSINESS), listOf(Venue.SEATED)),
        make("venueMiss", listOf(PeopleCount.SMALL), listOf(Audience.BUSINESS), listOf(Venue.WIDE)),
        make("twoMiss", listOf(PeopleCount.LARGE), listOf(Audience.BUSINESS), listOf(Venue.WIDE)),
        make("allMiss", listOf(PeopleCount.LARGE), listOf(Audience.PRESCHOOL), listOf(Venue.WIDE)),
    )

    @Test
    fun ぴったりが先で2つ以上合わないものは出ない() {
        val result = Recommender.recommend(
            Conditions(PeopleCount.SMALL, Audience.BUSINESS, Venue.SEATED), sample, Random(42),
        )
        assertEquals(listOf("perfect", "venueMiss"), result.map { it.id })
        assertEquals(listOf(ConditionKind.VENUE), result[1].mismatches)
        assertTrue(result[1].matchLabel.contains("会場"))
    }

    @Test
    fun ランダムなお題は今のお題と違うものを選ぶ() {
        val random = Random(7)
        for (current in 0 until 5) repeat(50) {
            val next = Recommender.randomTopicIndex(5, current, random)
            assertTrue(next != current)
            assertTrue(next in 0 until 5)
        }
        assertEquals(0, Recommender.randomTopicIndex(1, 0, random))
    }

    @Test
    fun 同じ度合いの中の並びはかたよらない() {
        // 全てぴったりの候補4つ → 先頭に来る回数がほぼ均等になるはず
        val items = (0 until 4).map { make("i$it", listOf(PeopleCount.SMALL), listOf(Audience.BUSINESS), listOf(Venue.SEATED)) }
        val random = Random(99)
        val counts = mutableMapOf<String, Int>()
        repeat(4000) {
            val first = Recommender.recommend(
                Conditions(PeopleCount.SMALL, Audience.BUSINESS, Venue.SEATED), items, random,
            ).first().id
            counts[first] = (counts[first] ?: 0) + 1
        }
        counts.values.forEach { assertTrue("偏りすぎ: $counts", it in 800..1200) }
    }
}

class HeadcountParserTest {
    @Test
    fun 文章から人数区分を読み取る() {
        assertEquals(PeopleCount.LARGE, HeadcountParser.peopleIn("親子あわせて30人くらいで公園"))
        assertEquals(PeopleCount.MEDIUM, HeadcountParser.peopleIn("新人研修、１２名、会議室"))
        assertEquals(PeopleCount.SMALL, HeadcountParser.peopleIn("4人のチームで"))
        assertEquals(PeopleCount.LARGE, HeadcountParser.peopleIn("ゲスト5人とスタッフ20人"))
        assertNull(HeadcountParser.peopleIn("少人数のミーティング"))
    }

    @Test
    fun 人数の境目() {
        assertEquals(PeopleCount.SMALL, HeadcountParser.peopleFor(5))
        assertEquals(PeopleCount.MEDIUM, HeadcountParser.peopleFor(6))
        assertEquals(PeopleCount.MEDIUM, HeadcountParser.peopleFor(15))
        assertEquals(PeopleCount.LARGE, HeadcountParser.peopleFor(16))
    }
}
