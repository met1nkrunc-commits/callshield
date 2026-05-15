import SwiftUI

struct HomeView: View {
    @EnvironmentObject private var sync: FraudSyncService
    @Environment(\.scenePhase) private var scenePhase
    @AppStorage("filterSetupConfirmed") private var filterSetupConfirmed = false
    @State private var pulseScale: CGFloat = 1.0
    @State private var filteredMessageCount = 0
    @State private var filterInvocationCount = 0

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                shieldCard
                setupStatusCard
                statsRow
                protectionSummaryCard
                recentCard
                Spacer(minLength: 32)
            }
            .padding(.horizontal, 16)
            .padding(.top, 24)
        }
        .background(CS.bg.ignoresSafeArea())
        .navigationTitle("Siper")
        .navigationBarTitleDisplayMode(.large)
        .onAppear {
            reloadStats()
            withAnimation(.easeInOut(duration: 1.6).repeatForever(autoreverses: true)) {
                pulseScale = 1.12
            }
        }
        .onChange(of: scenePhase) { phase in
            if phase == .active {
                reloadStats()
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

            Text(filterSetupConfirmed ? "SMS spam filtreleme kurulumu tamamlandı." : "SMS filtresini ayarlardan etkinleştirmen gerekiyor.")
                .font(.subheadline)
                .foregroundStyle(filterSetupConfirmed ? CS.secondary : CS.orange)
                .multilineTextAlignment(.center)
        }
        .padding(28)
        .frame(maxWidth: .infinity)
        .background(CS.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    private var setupStatusCard: some View {
        VStack(alignment: .leading, spacing: 14) {
            HStack {
                Label("Kurulum Durumu", systemImage: filterSetupConfirmed ? "checkmark.circle.fill" : "exclamationmark.triangle.fill")
                    .font(.headline)
                    .foregroundStyle(filterSetupConfirmed ? CS.accent : CS.orange)
                Spacer()
                Text(filterSetupConfirmed ? "Hazır" : "Eksik")
                    .font(.caption.weight(.semibold))
                    .foregroundStyle(filterSetupConfirmed ? CS.accent : CS.orange)
            }

            Text(filterSetupConfirmed
                 ? "Siper filtresini etkinleştirdiğini onayladın. Test SMS'i ile son kontrolü yapabilirsin."
                 : "Ayarlar > Mesajlar > Bilinmeyen ve Spam > SMS Filtresi yolundan Siper'i seçip kurulum rehberindeki onayı ver.")
                .font(.subheadline)
                .foregroundStyle(CS.secondary)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(CS.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    // MARK: - Stats row
    private var statsRow: some View {
        HStack(spacing: 12) {
            StatCard(
                icon: "nosign",
                iconColor: CS.red,
                title: "Engellenen SMS",
                value: filteredMessageCount == 0 ? "Temiz" : "\(filteredMessageCount)"
            )
            StatCard(
                icon: "waveform.path.ecg",
                iconColor: CS.orange,
                title: "Kontrol Edilen",
                value: filterInvocationCount == 0 ? "Hazır" : "\(filterInvocationCount)"
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

    private func reloadStats() {
        filteredMessageCount = AppGroupStorage.shared.filteredStats.total
        filterInvocationCount = AppGroupStorage.shared.filterInvocationCount
    }

    private var protectionSummaryCard: some View {
        VStack(alignment: .leading, spacing: 14) {
            Label(protectionTitle, systemImage: protectionIcon)
                .font(.headline)
                .foregroundStyle(protectionColor)

            Text(protectionMessage)
                .font(.subheadline)
                .foregroundStyle(CS.secondary)

            HStack {
                Text("Son liste güncellemesi")
                    .font(.caption)
                    .foregroundStyle(CS.secondary)
                Spacer()
                Text(lastSyncText)
                    .font(.caption.weight(.semibold))
                    .foregroundStyle(CS.primary)
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(CS.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    private var protectionTitle: String {
        if filterInvocationCount == 0 {
            return "Koruma hazır"
        }
        if filteredMessageCount == 0 {
            return "Şu an temiz"
        }
        return "Koruma çalışıyor"
    }

    private var protectionMessage: String {
        if filterInvocationCount == 0 {
            return "Siper aktif olduğunda gelen SMS'ler otomatik kontrol edilir. Riskli mesaj bulunursa burada gösterilir."
        }
        if filteredMessageCount == 0 {
            return "\(filterInvocationCount) SMS kontrol edildi, riskli mesaj bulunmadı. Bu kötü değil; gelen mesajlar temiz geçmiş demektir."
        }
        return "\(filterInvocationCount) SMS kontrol edildi, \(filteredMessageCount) riskli mesaj engellendi."
    }

    private var protectionIcon: String {
        filteredMessageCount > 0 ? "shield.lefthalf.filled" : "checkmark.shield.fill"
    }

    private var protectionColor: Color {
        filterInvocationCount == 0 ? CS.orange : CS.accent
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
