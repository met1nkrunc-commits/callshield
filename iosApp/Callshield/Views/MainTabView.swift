import SwiftUI

struct MainTabView: View {
    @State private var selected = 0

    var body: some View {
        TabView(selection: $selected) {
            NavigationView { HomeView().withAdBanner() }
                .tabItem { Label("Koruma", systemImage: selected == 0 ? "shield.lefthalf.filled" : "shield") }
                .tag(0)

            NavigationView { NumbersView().withAdBanner() }
                .tabItem { Label("Listeler", systemImage: selected == 1 ? "person.crop.circle.badge.minus.fill" : "person.crop.circle.badge.minus") }
                .tag(1)

            NavigationView { StatsView().withAdBanner() }
                .tabItem { Label("Özet", systemImage: selected == 2 ? "chart.bar.xaxis" : "chart.bar") }
                .tag(2)

            NavigationView { SettingsView().withAdBanner() }
                .tabItem { Label("Ayarlar", systemImage: selected == 3 ? "slider.horizontal.3" : "gearshape") }
                .tag(3)
        }
        .tint(CS.accent)
        .preferredColorScheme(.dark)
    }
}

private extension View {
    func withAdBanner() -> some View {
        safeAreaInset(edge: .bottom, spacing: 0) {
            MonetizedBanner()
        }
    }
}

// Blocklist + Trusted Numbers combined
struct NumbersView: View {
    @State private var segment = 0

    var body: some View {
        VStack(spacing: 0) {
            Picker("", selection: $segment) {
                Text("Engellenenler").tag(0)
                Text("Güvenilirler").tag(1)
            }
            .pickerStyle(.segmented)
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(CS.bg)

            if segment == 0 { BlocklistView() } else { TrustedNumbersView() }
        }
        .background(CS.bg.ignoresSafeArea())
        .navigationTitle("Numaralar")
        .navigationBarTitleDisplayMode(.large)
    }
}
