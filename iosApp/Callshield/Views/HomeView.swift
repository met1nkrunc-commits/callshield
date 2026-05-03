import SwiftUI

struct HomeView: View {
    @EnvironmentObject private var sync: FraudSyncService
    @EnvironmentObject private var store: StoreManager
    @State private var pulseScale: CGFloat = 1.0

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                shieldCard
                statsRow
                recentCard
                Spacer(minLength: 32)
            }
            .padding(.horizontal, 16)
            .padding(.top, 24)
        }
        .background(CS.bg.ignoresSafeArea())
        .navigationTitle("B-engel")
        .navigationBarTitleDisplayMode(.large)
        .onAppear {
            withAnimation(.easeInOut(duration: 1.6).repeatForever(autoreverses: true)) {
                pulseScale = 1.12
            }
        }
    }

    // MARK: - Shield card
    private var shieldCard: some View {
        VStack(spacing: 16) {
            ZStack {
                Circle()
                    .fill(CS.accent.opacity(0.12))
                    .frame(width: 120, height: 120)
                    .scaleEffect(pulseScale)

                BrandMarkView(size: 88)
            }

            BrandWordmarkView()

            Text(store.isPremiumUnlocked ? "SMS spam filtreleme ve engelleme listesi aktif." : "Premium kapalı. SMS filtresi şu anda pasif.")
                .font(.subheadline)
                .foregroundStyle(store.isPremiumUnlocked ? CS.secondary : CS.orange)
                .multilineTextAlignment(.center)
        }
        .padding(28)
        .frame(maxWidth: .infinity)
        .background(CS.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    // MARK: - Stats row
    private var statsRow: some View {
        HStack(spacing: 12) {
            StatCard(
                icon: "nosign",
                iconColor: CS.red,
                title: "Engellenen",
                value: "\(sync.blockedCount)"
            )
            StatCard(
                icon: "arrow.clockwise",
                iconColor: CS.accent,
                title: "Son Güncelleme",
                value: lastSyncText
            )
        }
    }

    private var lastSyncText: String {
        guard let date = sync.lastSyncDate else { return "Hiç" }
        let diff = Date().timeIntervalSince(date)
        if diff < 3600 { return "\(Int(diff / 60)) dk önce" }
        if diff < 86400 { return "\(Int(diff / 3600)) sa önce" }
        return "\(Int(diff / 86400)) gün önce"
    }

    // MARK: - Recent info card
    private var recentCard: some View {
        VStack(alignment: .leading, spacing: 14) {
            Label("Nasıl Çalışır?", systemImage: "info.circle.fill")
                .font(.headline)
                .foregroundStyle(CS.primary)

            infoRow(icon: "message.fill",    color: CS.accent,  text: "Gelen SMS'ler otomatik filtrelenir")
            infoRow(icon: "phone.fill",       color: CS.orange,  text: "Engellenen numaralar ortak listeden kontrol edilir")
            infoRow(icon: "arrow.clockwise",  color: CS.accent,  text: "Liste her 24 saatte bir güncellenir")
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(CS.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    private func infoRow(icon: String, color: Color, text: String) -> some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 15))
                .foregroundStyle(color)
                .frame(width: 22)
            Text(text)
                .font(.subheadline)
                .foregroundStyle(CS.secondary)
        }
    }
}

// MARK: - StatCard
private struct StatCard: View {
    let icon: String
    let iconColor: Color
    let title: String
    let value: String

    var body: some View {
        VStack(spacing: 10) {
            Image(systemName: icon)
                .font(.system(size: 22))
                .foregroundStyle(iconColor)
            Text(value)
                .font(.title2).fontWeight(.bold)
                .foregroundStyle(CS.primary)
            Text(title)
                .font(.caption)
                .foregroundStyle(CS.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 20)
        .background(CS.surface)
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }
}
