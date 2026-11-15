# アイスブレイク提案メーカー（Android）

iOS 版（リポジトリ直下の `IceBreaker/`）と同じ機能・同じ見た目の Android 版。Kotlin + Jetpack Compose。

> ⚠️ **まだ一度もビルドしていません。** 作成した Mac に Android Studio / JDK / Android SDK が無かったため、
> コンパイルとテストは未確認です。最初に開いたときに小さな修正が必要になる可能性があります。

## 動かし方

1. [Android Studio](https://developer.android.com/studio) をインストールする（JDK と Android SDK も一緒に入る）
2. Android Studio で **この `android/` フォルダ** を開く（リポジトリ直下ではなく）
3. 「Gradle wrapper がない」と出たら、表示される修正（Use Gradle wrapper / Create wrapper）を選ぶ
   - `gradle/wrapper/gradle-wrapper.properties` は用意済み（Gradle 8.14.3）。`gradle-wrapper.jar` と `gradlew` は Android Studio が作る
4. 同期（Sync）が終わったら、エミュレータか実機を選んで ▶︎ 実行
5. テストは `app/src/test` を右クリック →「Run Tests」、またはターミナルで `./gradlew test`
6. ライブラリの新しい版を勧められたら、そのまま更新してよい（`gradle/libs.versions.toml` にまとめてある）

- 対応：Android 8.0（API 26）以上、compileSdk 36
- パッケージ名：`com.example.icebreaker`（公開前に自分用に変える）

## iOS 版との対応

| Android | iOS | 内容 |
|---|---|---|
| `../shared/icebreakers.json` | 同じファイル（シンボリックリンク） | アイスブレイクのデータ。`app/build.gradle.kts` の `sourceSets` で assets として読み込む |
| `model/Models.kt` | `Models/Models.swift` | データの型 |
| `model/Recommender.kt` | `Models/Recommender.swift` | 提案の並べ方・お題のランダム選択 |
| `model/HeadcountParser.kt` | `Models/HeadcountParser.swift` | 文章から人数を読む |
| `ui/AppViewModel.kt` | `AppModel.swift` | 画面の状態 |
| `ui/Theme.kt` | `Views/Theme.swift` | 配色・ポップなカード/ボタン・水玉背景 |
| `ui/IceBreakerScreen.kt` | `Views/ContentView.swift`・`ResultSection.swift` | 画面 |
| `ui/StepIcons.kt` | （SF Symbols） | JSON のアイコン名を Material Icons に変換 |
| `ai/AiAssistant.kt` | `AI/OnDeviceAI.swift` | AI の入口（今は「使えない」実装のみ） |
| `app/src/test/.../RecommenderTest.kt` | `IceBreakerTests/RecommenderTests.swift` | 同じ内容のテスト＋アイコン対応表のテスト |

## データを追加するとき

`shared/icebreakers.json` を1か所直せば iOS / Android の両方に反映される。
新しいアイコン名（`steps[].symbol`）を使ったら、`ui/StepIcons.kt` にも Android のアイコンを追加する
（追加し忘れるとテスト「JSONのアイコン名がすべてAndroidのアイコンに対応している」が失敗する）。

## AI 機能について

今は `UnavailableAiAssistant`（AI なし）を使っているので、AI のボタンは表示されず、ほかの機能はすべて使える。
iOS の Apple Intelligence に当たるものとして、Android では Gemini Nano（ML Kit GenAI）で
`AiAssistant` を実装する予定。対応端末は一部の上位機種に限られるため、使えない端末では今と同じく AI 部分を隠す。
人数の読み取りは iOS と同じく、AI より先に `HeadcountParser` で判定すること。
