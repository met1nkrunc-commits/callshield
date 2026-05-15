import SwiftUI

struct StatsView: View {
    @EnvironmentObject private var sync: FraudSyncService
    @Environment(\.scenePhase) private var scenePhase
    @State private var stats = AppGroupStorage.FilterStats.empty

    private var storage: AppGroupStorage { .shared }

    private var todayCount: Int {
        let start = Calendar.current.startOfDay(for: Date())
        return stats.count(since: start)
    }
    private var weekCount: Int {
        let start = Calendar.current.date(byAdding: .day, value: -7, to: Date())!
        return stats.count(since: start)
    }
    private var totalCount: Int { stats.total }
    private var manualBlockedCount: Int { storage.manualBlockedNumbers.count }
    private var systemBlockedCount: Int { storage.remoteBlockedNumbers.count }

    private var bySenderCount: Int { stats.byReason["sender", default: 0] }
    private var bySenderRuleCount: Int { stats.byReason["sender_rule", default: 0] }
    private var byKeywordCount: Int { stats.byReason["keyword", default: 0] }
    private var byAlphanumericCount: Int { stats.byReason["alphanumeric", default: 0] }
    private var byLinkCount: Int { stats.byReason["link", default: 0] }

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                    // Main stats
                    HStack(spacing: 12) {
                        BigStatCard(value: todayCount == 0 ? "Temiz" : "\(todayCount)", label: "Bugün", color: CS.accent)
                        BigStatCard(value: weekCount == 0 ? "Temiz" : "\(weekCount)", label: "Bu Hafta", color: CS.orange)
                        BigStatCard(value: totalCount == 0 ? "Hazır" : "\(totalCount)", label: "Toplam", color: CS.red)
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
                            title: "Engellenen SMS",
                            value: totalCount == 0 ? "Temiz" : "\(totalCount)"
                        )
                    }
                    .background(CS.surface)
                    .clipShape(RoundedRectangle(cornerRadius: 14))

                    // Detection type breakdown
                    VStack(alignment: .leading, spacing: 14) {
                        Text("TESPİT YÖNTEMİ")
                            .font(.system(size: 11, weight: .semibold))
                            .foregroundStyle(CS.secondary)

                        if totalCount == 0 {
                            Text("Henüz riskli mesaj yakalanmadı. Siper aktif kalır; riskli SMS geldiğinde dağılım burada görünür.")
                                .font(.subheadline)
                                .foregroundStyle(CS.secondary)
                        } else {
                            detectionBar(
                                label: "Engellenen Gönderici",
                                count: bySenderCount,
                                total: max(totalCount, 1),
                                color: CS.red
                            )
                            detectionBar(
                                label: "Gönderici Kuralı",
                                count: bySenderRuleCount,
                                total: max(totalCount, 1),
                                color: CS.accent
                            )
                            detectionBar(
                                label: "Spam Anahtar Kelime",
                                count: byKeywordCount,
                                total: max(totalCount, 1),
                                color: CS.orange
                            )
                            detectionBar(
                                label: "Alfanümerik Gönderici",
                                count: byAlphanumericCount,
                                total: max(totalCount, 1),
                                color: CS.yellow
                            )
                            detectionBar(
                                label: "Riskli Link",
                                count: byLinkCount,
                                total: max(totalCount, 1),
                                color: CS.red
                            )
                        }
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
        .background(CS.bg.ignoresSafeArea())
        .navigationTitle("İstatistikler")
        .navigationBarTitleDisplayMode(.large)
        .onAppear(perform: reload)
        .onChange(of: scenePhase) { phase in
            if phase == .active {
                reload()
            }
        }
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

    private func reload() {
        stats = AppGroupStorage.shared.filteredStats
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
