import SwiftUI

/// 共有元アプリ（Gemini Canvas版）の配色・質感をなぞったテーマ
enum Theme {
    static let ink = Color(hex: 0x1F2937)          // 枠線・影の濃いグレー
    static let cream = Color(hex: 0xFFFBEB)        // 背景
    static let dot = Color(hex: 0xFCD34D).opacity(0.55)
    static let orange = Color(hex: 0xF97316)
    static let orangeLight = Color(hex: 0xFB923C)
    static let orangeDeep = Color(hex: 0xC2410C)
    static let yellow = Color(hex: 0xFDE047)
    static let teal = Color(hex: 0x14B8A6)
    static let indigo = Color(hex: 0x6366F1)
    static let indigoDeep = Color(hex: 0x1E1B4B)
    static let indigoSoft = Color(hex: 0xEEF2FF)
    static let violet = Color(hex: 0x8B5CF6)
    static let violetSoft = Color(hex: 0xF5F3FF)
    static let gray = Color(hex: 0x4B5563)
    static let amberSoft = Color(hex: 0xFFFBEB)

    struct Panel {
        var background: Color
        var border: Color
        var title: Color
    }

    static let peoplePanel = Panel(background: Color(hex: 0xEFF6FF), border: Color(hex: 0xBFDBFE), title: Color(hex: 0x1E40AF))
    static let audiencePanel = Panel(background: Color(hex: 0xFDF2F8), border: Color(hex: 0xFBCFE8), title: Color(hex: 0x9D174D))
    static let venuePanel = Panel(background: Color(hex: 0xF0FDF4), border: Color(hex: 0xBBF7D0), title: Color(hex: 0x166534))

    /// ステップのイラスト背景（ピンク・青・黄色の順）
    static let stepBackgrounds = [Color(hex: 0xFCE7F3), Color(hex: 0xE0E7FF), Color(hex: 0xFEF9C3)]
    static let stepSymbolColors = [Color(hex: 0xE11D48), Color(hex: 0x4F46E5), Color(hex: 0xD97706)]

    static func font(_ size: CGFloat, _ weight: Font.Weight = .heavy) -> Font {
        .system(size: size, weight: weight, design: .rounded)
    }
}

extension Color {
    init(hex: UInt32) {
        self.init(
            red: Double((hex >> 16) & 0xFF) / 255,
            green: Double((hex >> 8) & 0xFF) / 255,
            blue: Double(hex & 0xFF) / 255
        )
    }
}

// MARK: - 共通の見た目

/// 太い枠線＋ずらした影の「ポップ」なカード
struct PopCard: ViewModifier {
    var background: Color = .white
    var radius: CGFloat = 24
    var shadowOffset: CGFloat = 6

    func body(content: Content) -> some View {
        content
            .background(background, in: RoundedRectangle(cornerRadius: radius))
            .overlay(RoundedRectangle(cornerRadius: radius).strokeBorder(Theme.ink, lineWidth: 3))
            .background(
                RoundedRectangle(cornerRadius: radius)
                    .fill(Theme.ink)
                    .offset(x: shadowOffset, y: shadowOffset)
            )
    }
}

extension View {
    func popCard(background: Color = .white, radius: CGFloat = 24, shadowOffset: CGFloat = 6) -> some View {
        modifier(PopCard(background: background, radius: radius, shadowOffset: shadowOffset))
    }
}

/// 押すと沈むポップなボタン
struct PopButtonStyle: ButtonStyle {
    var fill: AnyShapeStyle
    var shadow: Color = Theme.ink
    var foreground: Color = .white
    var fontSize: CGFloat = 17
    var verticalPadding: CGFloat = 12
    var horizontalPadding: CGFloat = 20

    func makeBody(configuration: Configuration) -> some View {
        let pressed = configuration.isPressed
        configuration.label
            .font(Theme.font(fontSize))
            .foregroundStyle(foreground)
            .padding(.vertical, verticalPadding)
            .padding(.horizontal, horizontalPadding)
            .background(fill, in: Capsule())
            .overlay(Capsule().strokeBorder(Theme.ink, lineWidth: 3))
            .background(Capsule().fill(shadow).offset(y: pressed ? 1 : 5))
            .offset(y: pressed ? 4 : 0)
            .animation(.easeOut(duration: 0.08), value: pressed)
    }
}

/// 小さなラベル（目安時間・盛り上がり度など）
struct Chip: View {
    var text: String
    var background: Color

    var body: some View {
        Text(text)
            .font(Theme.font(12, .heavy))
            .foregroundStyle(Theme.ink)
            .padding(.horizontal, 10)
            .padding(.vertical, 5)
            .background(background, in: Capsule())
            .overlay(Capsule().strokeBorder(Theme.ink, lineWidth: 2))
    }
}

/// 水玉模様の背景
struct DottedBackground: View {
    var body: some View {
        Theme.cream
            .overlay {
                Canvas { context, size in
                    let spacing: CGFloat = 22
                    var y: CGFloat = 0
                    while y < size.height {
                        var x: CGFloat = 0
                        while x < size.width {
                            context.fill(Path(ellipseIn: CGRect(x: x, y: y, width: 3, height: 3)), with: .color(Theme.dot))
                            x += spacing
                        }
                        y += spacing
                    }
                }
            }
            .ignoresSafeArea()
    }
}

/// プルダウン風の選択ボタン（元アプリの select をなぞる）
struct SelectBox<Value: Hashable & Identifiable>: View {
    @Binding var selection: Value
    var options: [Value]
    var label: (Value) -> String

    var body: some View {
        Menu {
            Picker("", selection: $selection) {
                ForEach(options) { option in
                    Text(label(option)).tag(option)
                }
            }
        } label: {
            HStack(spacing: 8) {
                Text(label(selection))
                    .font(Theme.font(15, .heavy))
                    .foregroundStyle(Theme.ink)
                    .multilineTextAlignment(.leading)
                    .frame(maxWidth: .infinity, alignment: .leading)
                Image(systemName: "chevron.down")
                    .font(.system(size: 13, weight: .bold))
                    .foregroundStyle(Theme.ink)
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 12)
            .background(Color.white, in: RoundedRectangle(cornerRadius: 14))
            .overlay(RoundedRectangle(cornerRadius: 14).strokeBorder(Theme.ink, lineWidth: 2.5))
        }
    }
}
