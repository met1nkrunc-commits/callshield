import SwiftUI

struct StatsView: View {
    @EnvironmentObject private var sync: FraudSyncService
    @EnvironmentObject private var store: StoreManager
    @State private var messages: [AppGroupStorage.FilteredMessage] = []

    private var storage: AppGroupStorage { .shared }

    private var todayCount: Int {
        let start = Calendar.current.startOfDay(for: Date())
        return messages.filter { $0.date >= start }.count
    }
    private var weekCount: Int {
        let start = Calendar.current.date(byAdding: .day, value: -7, to: Date())!
        return messages.filter { $0.date >= start }.count
    }
    private var totalCount: Int { messages.count }
    private var manualBlockedCount: Int { storage.manualBlockedNumbers.count }
    private var systemBlockedCount: Int { storage.remoteBlockedNumbers.count }

    private var bySenderCount: Int { messages.filter { $0.reason == "sender" }.count }
    private var byKeywordCount: Int { messages.filter { $0.reason == "keyword" }.count }

    var body: some View {
        ZStack {
            ScrollView {
                VStack(spacing: 20) {
                    // Main stats
                    HStack(spacing: 12) {
                        BigStatCard(value: "\(todayCount)",  label: "Bugün",    color: CS.accent)
                        BigStatCard(value: "\(weekCount)",   label: "Bu Hafta", color: CS.orange)
                        BigStatCard(value: "\(totalCount)",  label: "Toplam",   color: CS.red)
                    }

                    // Blocked numbers DB
                    VStack(alignment: .leading, spacing: 0) {
                        statRow(
                            icon: "person.crop.circle.badge.minus",
                            color: CS.orange,
                            title: "Manuel Engellenenler",
                            value: "\(manualBlockedCount)"
                        )
                        Divider().background(CS.surfaceVar).padding(.leading, 16)
                        statRow(
                            icon: "externaldrive.badge.checkmark",
                            color: CS.accent,
                            title: "Sistem Veritabanı",
                            value: "\(systemBlockedCount)"
                        )
                        Divider().background(CS.surfaceVar).padding(.leading, 16)
                        statRow(
                            icon: "nosign",
                            color: CS.red,
                            title: "Filtrelenen SMS",
                            value: "\(totalCount)"
                        )
                    }
                    .background(CS.surface)
                    .clipShape(RoundedRectangle(cornerRadius: 14))

                    // Detection type breakdown
                    VStack(alignment: .leading, spacing: 14) {
                        Text("TESPİT YÖNTEMİ")
                            .font(.system(size: 11, weight: .semibold))
                            .foregroundStyle(CS.secondary)
                            .kerning(1.5)

                        detectionBar(
                            label: "Engellenen Gönderici",
                            count: bySenderCount,
                            total: max(totalCount, 1),
                            color: CS.red
                        )
                        detectionBar(
                            label: "Spam Anahtar Kelime",
                            count: byKeywordCount,
                            total: max(totalCount, 1),
                            color: CS.orange
                        )
                    }
                    .padding(16)
                    .background(CS.surface)
                    .clipShape(RoundedRectangle(cornerRadius: 16))

                    // Last sync
                    statRow(
                        icon: "arrow.clockwise.circle.fill",
                        color: CS.accent,
                        title: "Son Liste Güncellemesi",
                        value: lastSyncText
                    )

                    Spacer(minLength: 32)
                }
                .padding(.horizontal, 16)
                .padding(.top, 24)
            }
            .blur(radius: store.isPremiumUnlocked ? 0 : 4)

            if !store.isPremiumUnlocked {
                PremiumLockOverlay(
                    title: "İstatistikler Premium",
                    message: "Spam geçmişi ve filtre performansını görmek için premium başlat.",
                    buttonTitle: "7 Gün Deneyin"
                ) {
                    store.presentPaywall()
                }
            }
        }
        .background(CS.bg.ignoresSafeArea())
        .navigationTitle("İstatistikler")
        .navigationBarTitleDisplayMode(.large)
        .onAppear { messages = AppGroupStorage.shared.filteredMessages }
    }

    private func statRow(icon: String, color: Color, title: String, value: String) -> some View {
        HStack(spacing: 14) {
            Image(systemName: icon).font(.system(size: 22)).foregroundStyle(color).frame(width: 28)
            Text(title).font(.subheadline).fontWeight(.medium).foregroundStyle(CS.primary)
            Spacer()
            Text(value).font(.subheadline).foregroundStyle(CS.secondary)
        }
        .padding(16)
    }

    private func detectionBar(label: String, count: Int, total: Int, color: Color) -> some View {
        let ratio = Double(count) / Double(total)
        return VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text(label).font(.caption).foregroundStyle(CS.primary)
                Spacer()
                Text("\(count)").font(.caption).fontWeight(.semibold).foregroundStyle(color)
            }
            GeometryReader { geo in
                ZStack(alignment: .leading) {
                    RoundedRectangle(cornerRadius: 4).fill(CS.surfaceVar).frame(height: 6)
                    RoundedRectangle(cornerRadius: 4).fill(color)
                        .frame(width: geo.size.width * ratio, height: 6)
                }
            }
            .frame(height: 6)
        }
    }

    private var lastSyncText: String {
        guard let date = sync.lastSyncDate else { return "Hiç" }
        let diff = Date().timeIntervalSince(date)
        if diff < 3600  { return "\(Int(diff/60)) dk önce" }
        if diff < 86400 { return "\(Int(diff/3600)) sa önce" }
        return "\(Int(diff/86400)) gün önce"
    }
}

private struct BigStatCard: View {
    let value: String; let label: String; let color: Color
    var body: some View {
        VStack(spacing: 8) {
            Text(value).font(.title).fontWeight(.bold).foregroundStyle(color)
            Text(label).font(.caption).foregroundStyle(CS.secondary)
        }
        .frame(maxWidth: .infinity).padding(.vertical, 20)
        .background(CS.surface).clipShape(RoundedRectangle(cornerRadius: 14))
    }
}
