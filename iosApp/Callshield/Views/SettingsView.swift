import SwiftUI
import StoreKit

struct SettingsView: View {
    @EnvironmentObject private var sync: FraudSyncService
    @EnvironmentObject private var store: StoreManager

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                smsFilterSection
                syncSection
                appSection
                Spacer(minLength: 40)
            }
            .padding(.horizontal, 16)
            .padding(.top, 24)
        }
        .background(CS.bg.ignoresSafeArea())
        .navigationTitle("Ayarlar")
        .navigationBarTitleDisplayMode(.large)
    }

    // MARK: - SMS Filter setup
    private var smsFilterSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader("SMS FİLTRESİ")

            if !store.isPremiumUnlocked {
                HStack(spacing: 12) {
                    Image(systemName: "lock.trianglebadge.exclamationmark.fill")
                        .font(.system(size: 22))
                        .foregroundStyle(CS.orange)
                    VStack(alignment: .leading, spacing: 4) {
                        Text("Filtre Pasif")
                            .font(.subheadline).fontWeight(.semibold)
                            .foregroundStyle(CS.primary)
                        Text("B-engel seçili olsa bile premium açılmadıkça SMS filtreleme çalışmaz.")
                            .font(.caption)
                            .foregroundStyle(CS.secondary)
                    }
                    Spacer()
                }
                .padding(16)
                .background(CS.surface)
                .clipShape(RoundedRectangle(cornerRadius: 14))
            }

            if store.isPremiumUnlocked {
                NavigationLink(destination: FilterHistoryView()) {
                    premiumHistoryRow
                }
            } else {
                Button {
                    store.presentPaywall()
                } label: {
                    premiumHistoryRow
                }
            }

            NavigationLink(destination: SetupGuideView()) {
                HStack(spacing: 14) {
                    Image(systemName: "questionmark.circle.fill")
                        .font(.system(size: 22))
                        .foregroundStyle(CS.accent)
                        .frame(width: 28)

                    VStack(alignment: .leading, spacing: 2) {
                        Text("SMS Filtresi Nasıl Aktif Edilir?")
                            .font(.subheadline).fontWeight(.medium)
                            .foregroundStyle(CS.primary)
                        Text("Kurulum rehberini görüntüle")
                            .font(.caption)
                            .foregroundStyle(CS.secondary)
                    }

                    Spacer()
                    Image(systemName: "chevron.right")
                        .font(.caption)
                        .foregroundStyle(CS.disabled)
                }
                .padding(16)
                .background(CS.surface)
                .clipShape(RoundedRectangle(cornerRadius: 14))
            }
        }
    }

    private var premiumHistoryRow: some View {
        HStack(spacing: 14) {
            Image(systemName: "nosign")
                .font(.system(size: 22))
                .foregroundStyle(CS.red)
                .frame(width: 28)
            VStack(alignment: .leading, spacing: 2) {
                Text("Engellenen SMS Geçmişi")
                    .font(.subheadline).fontWeight(.medium)
                    .foregroundStyle(CS.primary)
                Text(store.isPremiumUnlocked ? "Filtrelenen spam mesajları görüntüle" : "Premium ile açılır")
                    .font(.caption)
                    .foregroundStyle(CS.secondary)
            }
            Spacer()
            Image(systemName: store.isPremiumUnlocked ? "chevron.right" : "lock.fill")
                .font(.caption)
                .foregroundStyle(store.isPremiumUnlocked ? CS.disabled : CS.orange)
        }
        .padding(16)
        .background(CS.surface)
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }

    // MARK: - Sync section
    private var syncSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader("DOLANDIRICI LİSTESİ")

            VStack(spacing: 0) {
                infoRow(label: "Engellenen Numara", value: "\(sync.blockedCount)")
                Divider().background(CS.surfaceVar).padding(.leading, 16)
                infoRow(label: "Son Güncelleme", value: lastSyncText)
                Divider().background(CS.surfaceVar).padding(.leading, 16)

                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Listeyi Güncelle")
                            .font(.subheadline).fontWeight(.medium)
                            .foregroundStyle(CS.primary)
                        Text("24 saatte bir otomatik güncellenir")
                            .font(.caption)
                            .foregroundStyle(CS.secondary)
                    }
                    Spacer()
                    if !store.isPremiumUnlocked {
                        Button("Premium") {
                            store.presentPaywall()
                        }
                        .font(.caption).fontWeight(.semibold)
                        .foregroundStyle(CS.orange)
                    } else if sync.isSyncing {
                        ProgressView()
                            .tint(CS.accent)
                    } else {
                        Button("Güncelle") {
                            Task { await sync.sync() }
                        }
                        .font(.subheadline).fontWeight(.medium)
                        .foregroundStyle(CS.accent)
                    }
                }
                .padding(16)

                if let err = sync.lastError {
                    Divider().background(CS.surfaceVar).padding(.leading, 16)
                    HStack {
                        Image(systemName: "exclamationmark.circle.fill")
                            .foregroundStyle(CS.red)
                        Text(err)
                            .font(.caption)
                            .foregroundStyle(CS.red)
                    }
                    .padding(16)
                }
            }
            .background(CS.surface)
            .clipShape(RoundedRectangle(cornerRadius: 14))
        }
    }

    private var premiumSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader("PREMIUM")

            VStack(alignment: .leading, spacing: 0) {
                HStack(spacing: 14) {
                    Image(systemName: store.isPremiumUnlocked ? "checkmark.seal.fill" : "crown.fill")
                        .font(.system(size: 22))
                        .foregroundStyle(store.isPremiumUnlocked ? CS.accent : CS.orange)
                        .frame(width: 28)

                    VStack(alignment: .leading, spacing: 2) {
                        Text(store.isPremiumUnlocked ? "Premium Aktif" : "Premium Koruma")
                            .font(.subheadline).fontWeight(.medium)
                            .foregroundStyle(CS.primary)
                        Text(store.isPremiumUnlocked ? "Tüm koruma özellikleri açık" : "Tüm özellikler sadece premium abonelikte açık")
                            .font(.caption)
                            .foregroundStyle(CS.secondary)
                    }
                    Spacer()
                }
                .padding(16)

                if store.products.isEmpty {
                    Divider().background(CS.surfaceVar).padding(.leading, 16)
                    Text("Ürün henüz yüklenmedi. Aylık ve yıllık abonelik App Store Connect'te tanımlandığında burada görünecek.")
                        .font(.caption)
                        .foregroundStyle(CS.secondary)
                        .padding(16)
                } else {
                    ForEach(store.products, id: \.id) { product in
                        Divider().background(CS.surfaceVar).padding(.leading, 16)
                        HStack {
                            VStack(alignment: .leading, spacing: 2) {
                                Text(product.id.contains("yearly") ? "Yıllık Premium" : "Aylık Premium")
                                    .font(.subheadline).fontWeight(.medium)
                                    .foregroundStyle(CS.primary)
                                Text(product.id.contains("yearly") ? "7 gün ücretsiz deneme ile başlar" : "Aylık yenilenen premium erişim")
                                    .font(.caption)
                                    .foregroundStyle(CS.secondary)
                            }
                            Spacer()
                            if store.purchasedProductIDs.contains(product.id) {
                                Text("Aktif")
                                    .font(.caption).fontWeight(.semibold)
                                    .foregroundStyle(CS.accent)
                            } else if store.isLoading {
                                ProgressView()
                                    .tint(CS.accent)
                            } else {
                                Button(product.displayPrice) {
                                    Task { await store.purchase(product) }
                                }
                                .font(.subheadline).fontWeight(.medium)
                                .foregroundStyle(CS.accent)
                            }
                        }
                        .padding(16)
                    }
                }

                Divider().background(CS.surfaceVar).padding(.leading, 16)
                HStack {
                    Text("Satın Alımları Geri Yükle")
                        .font(.subheadline).fontWeight(.medium)
                        .foregroundStyle(CS.primary)
                    Spacer()
                    if store.isLoading {
                        ProgressView().tint(CS.accent)
                    } else {
                        Button("Geri Yükle") {
                            Task { await store.restorePurchases() }
                        }
                        .font(.subheadline).fontWeight(.medium)
                        .foregroundStyle(CS.accent)
                    }
                }
                .padding(16)

                if !store.isPremiumUnlocked {
                    Divider().background(CS.surfaceVar).padding(.leading, 16)
                    HStack {
                        Text("Debug Premium Aç")
                            .font(.subheadline).fontWeight(.medium)
                            .foregroundStyle(CS.primary)
                        Spacer()
                        Button("Aç") {
                            store.unlockDebugPremium()
                        }
                        .font(.subheadline).fontWeight(.medium)
                        .foregroundStyle(CS.orange)
                    }
                    .padding(16)
                }

                if let error = store.lastError {
                    Divider().background(CS.surfaceVar).padding(.leading, 16)
                    Text(error)
                        .font(.caption)
                        .foregroundStyle(CS.red)
                        .padding(16)
                }
            }
            .background(CS.surface)
            .clipShape(RoundedRectangle(cornerRadius: 14))
        }
    }

    // MARK: - App info
    private var appSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader("UYGULAMA")

            VStack(spacing: 0) {
                infoRow(label: "Sürüm", value: "1.0.0")
                Divider().background(CS.surfaceVar).padding(.leading, 16)
                infoRow(label: "Platform", value: "iOS 16+")
            }
            .background(CS.surface)
            .clipShape(RoundedRectangle(cornerRadius: 14))

            Text("B-engel · Türkiye Dolandırıcılık Koruma")
                .font(.caption2)
                .foregroundStyle(CS.disabled)
                .frame(maxWidth: .infinity, alignment: .center)
                .padding(.top, 4)
        }
    }

    // MARK: - Helpers
    private func sectionHeader(_ text: String) -> some View {
        Text(text)
            .font(.system(size: 11, weight: .semibold))
            .foregroundStyle(CS.secondary)
            .kerning(1.5)
    }

    private func infoRow(label: String, value: String) -> some View {
        HStack {
            Text(label)
                .font(.subheadline).fontWeight(.medium)
                .foregroundStyle(CS.primary)
            Spacer()
            Text(value)
                .font(.subheadline)
                .foregroundStyle(CS.secondary)
        }
        .padding(16)
    }

    private var lastSyncText: String {
        guard let date = sync.lastSyncDate else { return "Hiç güncellenmedi" }
        let diff = Date().timeIntervalSince(date)
        if diff < 60    { return "Az önce" }
        if diff < 3600  { return "\(Int(diff / 60)) dakika önce" }
        if diff < 86400 { return "\(Int(diff / 3600)) saat önce" }
        return "\(Int(diff / 86400)) gün önce"
    }
}
