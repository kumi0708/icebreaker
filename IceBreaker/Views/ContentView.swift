import SwiftUI

struct ContentView: View {
    @Environment(AppModel.self) private var model

    var body: some View {
        @Bindable var model = model
        ScrollViewReader { proxy in
            ScrollView {
                VStack(spacing: 28) {
                    HeaderView()
                    ConditionCard()
                    if model.aiStatus.isAvailable {
                        AIConsultCard()
                    }
                    if model.current != nil {
                        ResultSection()
                            .id("result")
                    }
                    FooterNote(aiAvailable: model.aiStatus.isAvailable, aiMessage: model.aiStatus.message)
                }
                .padding(.horizontal, 16)
                .padding(.top, 20)
                .padding(.bottom, 40)
                .frame(maxWidth: 760)
                .frame(maxWidth: .infinity)
            }
            .scrollDismissesKeyboard(.interactively)
            .background(DottedBackground())
            .onChange(of: model.resultRevision) {
                // 結果が画面に配置されてからスクロールする
                Task { @MainActor in
                    try? await Task.sleep(for: .milliseconds(80))
                    withAnimation(.easeInOut(duration: 0.45)) {
                        proxy.scrollTo("result", anchor: .top)
                    }
                }
            }
            .alert("AIからのお知らせ", isPresented: Binding(
                get: { model.aiErrorMessage != nil },
                set: { if !$0 { model.aiErrorMessage = nil } }
            )) {
                Button("OK", role: .cancel) {}
            } message: {
                Text(model.aiErrorMessage ?? "")
            }
        }
    }
}

// MARK: - ヘッダー

private struct HeaderView: View {
    var body: some View {
        VStack(spacing: 10) {
            Text("🎉 場の空気を一瞬でほぐす！")
                .font(Theme.font(13))
                .foregroundStyle(Theme.ink)
                .padding(.horizontal, 14)
                .padding(.vertical, 6)
                .background(Theme.yellow, in: Capsule())
                .overlay(Capsule().strokeBorder(Theme.ink, lineWidth: 2.5))
                .rotationEffect(.degrees(-2))

            Text("アイスブレイク\n提案メーカー")
                .font(Theme.font(34, .black))
                .foregroundStyle(Theme.orange)
                .multilineTextAlignment(.center)
                .shadow(color: Theme.orangeDeep.opacity(0.35), radius: 0, x: 2, y: 2)
                .accessibilityAddTraits(.isHeader)

            Text("条件を選ぶだけで、イラスト付きのわかりやすい進行手順を提案するよ！")
                .font(Theme.font(13, .bold))
                .foregroundStyle(Theme.gray)
                .multilineTextAlignment(.center)
        }
        .padding(.top, 8)
    }
}

// MARK: - 条件入力

private struct ConditionCard: View {
    @Environment(AppModel.self) private var model

    var body: some View {
        @Bindable var model = model
        VStack(alignment: .leading, spacing: 16) {
            Text("🔍 どんな集まりですか？")
                .font(Theme.font(20))
                .foregroundStyle(Theme.ink)

            ConditionPanel(title: "👥 人数は？", style: Theme.peoplePanel) {
                SelectBox(selection: $model.conditions.people, options: PeopleCount.allCases, label: \.label)
            }
            ConditionPanel(title: "🎈 参加者の属性は？", style: Theme.audiencePanel) {
                SelectBox(selection: $model.conditions.audience, options: Audience.allCases, label: \.label)
            }
            ConditionPanel(title: "🏢 会場の広さは？", style: Theme.venuePanel) {
                SelectBox(selection: $model.conditions.venue, options: Venue.allCases, label: \.label)
            }

            Button {
                model.generate()
            } label: {
                Text("✨ アイディアを出す！")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(PopButtonStyle(
                fill: AnyShapeStyle(LinearGradient(colors: [Theme.orangeLight, Theme.orange], startPoint: .top, endPoint: .bottom)),
                shadow: Theme.orangeDeep,
                fontSize: 20,
                verticalPadding: 16
            ))
            .padding(.top, 6)
            .padding(.bottom, 6)
        }
        .padding(20)
        .popCard()
    }
}

private struct ConditionPanel<Content: View>: View {
    var title: String
    var style: Theme.Panel
    @ViewBuilder var content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(title)
                .font(Theme.font(15))
                .foregroundStyle(style.title)
            content
        }
        .padding(14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(style.background, in: RoundedRectangle(cornerRadius: 18))
        .overlay(RoundedRectangle(cornerRadius: 18).strokeBorder(style.border, lineWidth: 2))
    }
}

// MARK: - AIに相談（Apple Intelligence）

private struct AIConsultCard: View {
    @Environment(AppModel.self) private var model
    @FocusState private var focused: Bool

    var body: some View {
        @Bindable var model = model
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 8) {
                Text("🤖 AIに相談して条件を決める")
                    .font(Theme.font(17))
                    .foregroundStyle(Theme.indigoDeep)
                Spacer(minLength: 0)
                Chip(text: "Apple Intelligence", background: .white)
            }
            Text("集まりの様子を書くと、AIが条件を読み取って提案します。")
                .font(Theme.font(13, .bold))
                .foregroundStyle(Theme.gray)

            TextField("例：来週の新人研修。20人くらいで会議室。初対面同士", text: $model.situationText, axis: .vertical)
                .lineLimit(2...4)
                .font(Theme.font(15, .semibold))
                .focused($focused)
                .padding(12)
                .background(Color.white, in: RoundedRectangle(cornerRadius: 14))
                .overlay(RoundedRectangle(cornerRadius: 14).strokeBorder(Theme.ink, lineWidth: 2.5))

            Button {
                focused = false
                Task { await model.askAI() }
            } label: {
                HStack(spacing: 8) {
                    if model.busy == .situation { ProgressView().tint(.white) }
                    Text(model.busy == .situation ? "AIが考え中…" : "💬 AIに相談する")
                }
                .frame(maxWidth: .infinity)
            }
            .buttonStyle(PopButtonStyle(fill: AnyShapeStyle(Theme.violet), fontSize: 17))
            .disabled(model.busy != nil || model.situationText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
            .opacity(model.situationText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? 0.6 : 1)

            if let advice = model.situationAdvice {
                Label {
                    Text(advice)
                        .font(Theme.font(14, .bold))
                        .foregroundStyle(Theme.indigoDeep)
                } icon: {
                    Image(systemName: "lightbulb.fill").foregroundStyle(Theme.orange)
                }
                .padding(12)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color.white, in: RoundedRectangle(cornerRadius: 14))
            }
        }
        .padding(20)
        .popCard(background: Theme.violetSoft)
    }
}

// MARK: - フッター

private struct FooterNote: View {
    var aiAvailable: Bool
    var aiMessage: String?

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("🔒 安心設計：入力した条件などの個人情報は保存・送信しません。")
            if aiAvailable {
                Text("🤖 AI機能はApple Intelligenceを使い、すべてこの端末の中で処理します。")
            } else if let aiMessage {
                Text("ℹ️ \(aiMessage)")
            }
        }
        .font(Theme.font(12, .bold))
        .foregroundStyle(Theme.gray)
        .padding(14)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.white.opacity(0.8), in: RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).strokeBorder(Color(hex: 0xE5E7EB), lineWidth: 2))
    }
}

#Preview {
    ContentView()
        .environment(AppModel())
}
