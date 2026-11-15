# アイスブレイク提案メーカー（iOS）

条件（人数・参加者・会場）を選ぶと、イラスト付きの進め方つきでアイスブレイクを提案するアプリ。
Gemini Canvas 版のUIをなぞり、SwiftUI で作り直したもの。

## 動かし方

1. `IceBreaker.xcodeproj` を Xcode で開く
2. Team（7F727685BQ）と Bundle ID（`com.shiraiatu.icebreaker`）は設定済み。Xcode に Apple アカウントでサインインしていれば実機でも動く
3. iPhone シミュレータを選んで実行（⌘R）。テストは ⌘U

- 対応：iOS 18 以上
- AI機能：iOS 26 以上かつ Apple Intelligence 対応・オンの端末だけで表示（使えない端末ではAI部分が隠れ、ほかは全部使える）

## 構成

| 場所 | 中身 |
|---|---|
| `IceBreaker/Resources/icebreakers.json` | アイスブレイクのデータ（22種類）。Android 版でもそのまま使える形式 |
| `IceBreaker/Models/` | データの型、提案ロジック（`Recommender`）、人数の読み取り |
| `IceBreaker/AI/OnDeviceAI.swift` | Apple Intelligence（Foundation Models）を使う部分 |
| `IceBreaker/AppModel.swift` | 画面の状態管理 |
| `IceBreaker/Views/` | 画面（元アプリの配色・カードの質感を `Theme.swift` に集約） |
| `IceBreakerTests/` | 全108通りの条件で提案が出るか等のテスト |

## データの追加方法

`icebreakers.json` に1件追加するだけ。`people` / `audiences` / `venues` に合う条件を、
`topics` の各お題に `audiences` を付けるとその参加者のときだけ出る（付けなければ全員向け）。
`steps[].symbol` は SF Symbols の名前。追加後は ⌘U で「全条件でぴったり提案が2つ以上あるか」のテストが走る。
