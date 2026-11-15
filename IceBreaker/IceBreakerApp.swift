import SwiftUI

@main
struct IceBreakerApp: App {
    @State private var model = AppModel()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environment(model)
                // 共有元アプリの明るい配色をそのまま使うため、ライト表示に固定
                .preferredColorScheme(.light)
        }
    }
}
