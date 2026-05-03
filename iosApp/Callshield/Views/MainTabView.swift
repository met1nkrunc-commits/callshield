import SwiftUI

struct MainTabView: View {
    @State private var selected = 0
    @EnvironmentObject private var store: StoreManager

    var body: some View {
        TabView(selection: $selected) {
            NavigationStack { HomeView() }
                .tabItem { Label("Ana Sayfa", systemImage: selected == 0 ? "shield.fill" : "shield") }
                .tag(0)

            NavigationStack { NumbersView() }
                .tabItem { Label("Numaralar", systemImage: selected == 1 ? "list.bullet.rectangle.fill" : "list.bullet.rectangle") }
                .tag(1)

            NavigationStack { StatsView() }
                .tabItem { Label("İstatistik", systemImage: selected == 2 ? "chart.bar.fill" : "chart.bar") }
                .tag(2)

            NavigationStack { SettingsView() }
                .tabItem { Label("Ayarlar", systemImage: selected == 3 ? "gearshape.fill" : "gearshape") }
                .tag(3)
        }
        .tint(CS.accent)
        .preferredColorScheme(.dark)
    }
}

// Blocklist + Trusted Numbers combined
struct NumbersView: View {
    @State private var segment = 0
    @EnvironmentObject private var store: StoreManager

    var body: some View {
        ZStack {
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
            .blur(radius: store.isPremiumUnlocked ? 0 : 4)
            .allowsHitTesting(store.isPremiumUnlocked)

            if !store.isPremiumUnlocked {
                PremiumLockOverlay(
                    title: "Numara Listeleri Premium",
                    message: "Engellenen ve güvenilir numara listelerini yönetmek için premium başlat.",
                    buttonTitle: "7 Gün Deneyin"
                ) {
                    store.presentPaywall()
                }
            }
        }
        .background(CS.bg.ignoresSafeArea())
        .navigationTitle("Numaralar")
        .navigationBarTitleDisplayMode(.large)
    }
}

struct PremiumLockOverlay: View {
    let title: String
    let message: String
    let buttonTitle: String
    let action: () -> Void

    var body: some View {
        VStack(spacing: 14) {
            Image(systemName: "crown.fill")
                .font(.system(size: 28))
                .foregroundStyle(CS.orange)
            Text(title)
                .font(.headline)
                .foregroundStyle(CS.primary)
            Text(message)
                .font(.subheadline)
                .foregroundStyle(CS.secondary)
                .multilineTextAlignment(.center)
            Button(buttonTitle, action: action)
                .font(.subheadline.bold())
                .foregroundStyle(.black)
                .padding(.horizontal, 18)
                .padding(.vertical, 12)
                .background(CS.accent)
                .clipShape(Capsule())
        }
        .padding(22)
        .background(CS.surface.opacity(0.96))
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .padding(24)
    }
}
