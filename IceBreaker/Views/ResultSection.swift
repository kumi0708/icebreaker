import SwiftUI

struct ResultSection: View {
    @Environment(AppModel.self) private var model

    var body: some View {
        if let current = model.current {
            VStack(alignment: .leading, spacing: 16) {
                HStack(alignment: .center) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("💡 おすすめのアイスブレイク")
                            .font(Theme.font(19))
                            .foregroundStyle(Theme.ink)
                            .lineLimit(1)
                            .minimumScaleFactor(0.7)
                        Text("\(model.currentIndex + 1) / \(model.recommendations.count) 件")
                            .font(Theme.font(12, .bold))
                            .foregroundStyle(Theme.gray)
                    }
                    Spacer(minLength: 8)
                    Button {
                        withAnimation(.snappy) { model.showNext() }
                    } label: {
                        Text("🔄 別のアイディア")
                    }
                    .buttonStyle(PopButtonStyle(fill: AnyShapeStyle(Theme.teal), fontSize: 14, verticalPadding: 9, horizontalPadding: 14))
                }

                IdeaCard(recommendation: current)
                    .id(current.id)
                    .transition(.opacity)
            }
        }
    }
}

private struct IdeaCard: View {
    @Environment(AppModel.self) private var model
    var recommendation: Recommendation

    private var item: Icebreaker { recommendation.icebreaker }

    var body: some View {
        VStack(alignment: .leading, spacing: 18) {
            // 見出し
            VStack(alignment: .leading, spacing: 10) {
                FlowChips(items: [
                    ("⏱️ 目安時間：\(item.minutes)", Theme.yellow),
                    ("✨ 盛り上がり度 \(stars(item.excitement))", Color(hex: 0xFBCFE8)),
                ])
                MatchBadge(recommendation: recommendation)
                Text(item.title)
                    .font(Theme.font(24, .black))
                    .foregroundStyle(Theme.ink)
                    .fixedSize(horizontal: false, vertical: true)
                Text(item.summary)
                    .font(Theme.font(14, .bold))
                    .foregroundStyle(Theme.gray)
                    .fixedSize(horizontal: false, vertical: true)
                Label(item.preparation, systemImage: "bag.fill")
                    .font(Theme.font(13, .bold))
                    .foregroundStyle(Theme.gray)
            }

            DashedDivider()

            TopicPanel()

            StepsSection()
        }
        .padding(20)
        .popCard()
    }

    private func stars(_ value: Int) -> String {
        let v = min(max(value, 1), 5)
        return String(repeating: "★", count: v) + String(repeating: "☆", count: 5 - v)
    }
}

private struct MatchBadge: View {
    var recommendation: Recommendation

    var body: some View {
        Label(recommendation.matchLabel, systemImage: recommendation.isPerfect ? "checkmark.circle.fill" : "exclamationmark.triangle.fill")
            .font(Theme.font(12, .heavy))
            .foregroundStyle(recommendation.isPerfect ? Color(hex: 0x166534) : Color(hex: 0x9A3412))
            .padding(.horizontal, 10)
            .padding(.vertical, 5)
            .background(recommendation.isPerfect ? Color(hex: 0xDCFCE7) : Color(hex: 0xFFEDD5), in: Capsule())
    }
}

private struct FlowChips: View {
    var items: [(String, Color)]

    var body: some View {
        ViewThatFits(in: .horizontal) {
            HStack(spacing: 8) { chips }
            VStack(alignment: .leading, spacing: 6) { chips }
        }
    }

    @ViewBuilder private var chips: some View {
        ForEach(items, id: \.0) { item in
            Chip(text: item.0, background: item.1)
        }
    }
}

private struct DashedDivider: View {
    var body: some View {
        Rectangle()
            .fill(.clear)
            .frame(height: 2)
            .overlay(
                Line().stroke(style: StrokeStyle(lineWidth: 2, dash: [6, 5]))
                    .foregroundStyle(Color(hex: 0xD1D5DB))
            )
    }

    private struct Line: Shape {
        func path(in rect: CGRect) -> Path {
            Path { p in
                p.move(to: CGPoint(x: 0, y: rect.midY))
                p.addLine(to: CGPoint(x: rect.maxX, y: rect.midY))
            }
        }
    }
}

// MARK: - お題

private struct TopicPanel: View {
    @Environment(AppModel.self) private var model

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("🎯 使う「お題・ルール」を選ぼう！")
                .font(Theme.font(16))
                .foregroundStyle(Theme.indigoDeep)

            Button {
                withAnimation(.snappy) { model.randomTopic() }
            } label: {
                Text("🎲 ランダムにお題を変える！")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(PopButtonStyle(fill: AnyShapeStyle(Theme.indigo), fontSize: 16))

            VStack(spacing: 6) {
                HStack(spacing: 6) {
                    Text("現在の選択お題")
                        .font(Theme.font(12, .heavy))
                        .foregroundStyle(Theme.indigo)
                    if model.currentTopic?.isAI == true {
                        Chip(text: "✨ AI作成", background: Theme.violetSoft)
                    }
                }
                Text(model.currentTopic?.text ?? "")
                    .font(Theme.font(22, .black))
                    .foregroundStyle(Theme.ink)
                    .multilineTextAlignment(.center)
                    .fixedSize(horizontal: false, vertical: true)
                    .contentTransition(.opacity)
            }
            .padding(.vertical, 16)
            .padding(.horizontal, 12)
            .frame(maxWidth: .infinity)
            .background(Color.white, in: RoundedRectangle(cornerRadius: 16))

            VStack(alignment: .leading, spacing: 8) {
                Text("📋 リストから選ぶ")
                    .font(Theme.font(13, .heavy))
                    .foregroundStyle(Theme.indigoDeep)
                TopicMenu()
            }
            .padding(12)
            .background(Color.white.opacity(0.6), in: RoundedRectangle(cornerRadius: 16))
            .overlay(RoundedRectangle(cornerRadius: 16).strokeBorder(Color(hex: 0xC7D2FE), lineWidth: 2))

            if model.aiStatus.isAvailable {
                Button {
                    Task { await model.addAITopics() }
                } label: {
                    HStack(spacing: 8) {
                        if model.busy == .topics { ProgressView().tint(Theme.violet) }
                        Text(model.busy == .topics ? "AIがお題を考え中…" : "✨ AIでこの集まり向けのお題を追加")
                    }
                    .frame(maxWidth: .infinity)
                }
                .buttonStyle(PopButtonStyle(fill: AnyShapeStyle(Color.white), foreground: Theme.violet, fontSize: 15, verticalPadding: 10))
                .disabled(model.busy != nil)
            }
        }
        .padding(16)
        .background(Theme.indigoSoft, in: RoundedRectangle(cornerRadius: 20))
    }
}

private struct TopicMenu: View {
    @Environment(AppModel.self) private var model

    var body: some View {
        let topics = model.topics
        Menu {
            ForEach(Array(topics.enumerated()), id: \.offset) { index, topic in
                Button {
                    model.selectTopic(index)
                } label: {
                    if index == model.topicIndex {
                        Label("\(index + 1). \(topic.text)", systemImage: "checkmark")
                    } else {
                        Text("\(index + 1). \(topic.text)\(topic.isAI ? "（AI）" : "")")
                    }
                }
            }
        } label: {
            HStack(spacing: 8) {
                Text("\(model.topicIndex + 1). \(model.currentTopic?.text ?? "")")
                    .font(Theme.font(14, .heavy))
                    .foregroundStyle(Theme.ink)
                    .multilineTextAlignment(.leading)
                    .frame(maxWidth: .infinity, alignment: .leading)
                Image(systemName: "chevron.down")
                    .font(.system(size: 12, weight: .bold))
                    .foregroundStyle(Theme.ink)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 11)
            .background(Color.white, in: RoundedRectangle(cornerRadius: 12))
            .overlay(RoundedRectangle(cornerRadius: 12).strokeBorder(Theme.ink, lineWidth: 2.5))
        }
    }
}

// MARK: - 進め方

private struct StepsSection: View {
    @Environment(AppModel.self) private var model

    var body: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("📖 わかりやすい進め方（3ステップ）")
                .font(Theme.font(17))
                .foregroundStyle(Theme.ink)

            if model.aiStatus.isAvailable {
                Button {
                    Task { await model.toggleAdaptedGuide() }
                } label: {
                    HStack(spacing: 8) {
                        if model.busy == .guide { ProgressView().tint(Theme.violet) }
                        Text(buttonTitle)
                    }
                    .frame(maxWidth: .infinity)
                }
                .buttonStyle(PopButtonStyle(fill: AnyShapeStyle(Color.white), foreground: Theme.violet, fontSize: 15, verticalPadding: 10))
                .disabled(model.busy != nil)
            }

            if model.showAdaptedGuide {
                Chip(text: "✨ AIが「\(model.resultConditions.audience.label)」向けにアレンジ", background: Theme.violetSoft)
            }

            ForEach(Array(model.displayedSteps.enumerated()), id: \.offset) { index, step in
                StepCard(index: index, step: step)
            }

            if !model.displayedHostTip.isEmpty {
                Label {
                    Text(model.displayedHostTip)
                        .font(Theme.font(14, .bold))
                        .foregroundStyle(Theme.ink)
                        .fixedSize(horizontal: false, vertical: true)
                } icon: {
                    Text("🎤")
                }
                .padding(14)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(Color(hex: 0xFEF3C7), in: RoundedRectangle(cornerRadius: 16))
                .overlay(alignment: .topLeading) {
                    Text("司会のコツ")
                        .font(Theme.font(11, .heavy))
                        .foregroundStyle(.white)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 3)
                        .background(Theme.orange, in: Capsule())
                        .offset(x: 10, y: -10)
                }
                .padding(.top, 6)
            }
        }
    }

    private var buttonTitle: String {
        if model.busy == .guide { return "AIがアレンジ中…" }
        return model.showAdaptedGuide ? "↩︎ 元の説明に戻す" : "✨ AIでこの参加者向けの説明にアレンジ"
    }
}

private struct StepCard: View {
    var index: Int
    var step: Icebreaker.Step

    var body: some View {
        let bg = Theme.stepBackgrounds[index % Theme.stepBackgrounds.count]
        let fg = Theme.stepSymbolColors[index % Theme.stepSymbolColors.count]
        VStack(alignment: .leading, spacing: 12) {
            Text("STEP \(index + 1)")
                .font(Theme.font(13, .black))
                .foregroundStyle(.white)
                .padding(.horizontal, 12)
                .padding(.vertical, 5)
                .background(Theme.orange, in: Capsule())
                .overlay(Capsule().strokeBorder(Theme.ink, lineWidth: 2.5))
                .background(Capsule().fill(Theme.ink).offset(x: 2, y: 3))

            ZStack {
                bg
                Image(systemName: step.symbol)
                    .font(.system(size: 46, weight: .bold))
                    .foregroundStyle(fg)
                    .symbolRenderingMode(.hierarchical)
            }
            .frame(height: 96)
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .overlay(RoundedRectangle(cornerRadius: 14).strokeBorder(Theme.ink, lineWidth: 2.5))
            .accessibilityHidden(true)

            Text(step.title)
                .font(Theme.font(17, .black))
                .foregroundStyle(Theme.ink)
                .fixedSize(horizontal: false, vertical: true)
            Text(step.description)
                .font(Theme.font(14, .bold))
                .foregroundStyle(Theme.gray)
                .fixedSize(horizontal: false, vertical: true)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Theme.amberSoft, in: RoundedRectangle(cornerRadius: 20))
        .overlay(RoundedRectangle(cornerRadius: 20).strokeBorder(Color(hex: 0xFDE68A), lineWidth: 2))
    }
}
