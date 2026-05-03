import SwiftUI
import StoreKit

struct ComposeView: View {
    @AppStorage("onboardingDone") private var onboardingDone = false
    @EnvironmentObject private var store: StoreManager

    var body: some View {
        Group {
            if !onboardingDone {
                OnboardingView()
            } else {
                MainTabView()
            }
        }
        .sheet(isPresented: $store.isPaywallPresented) {
            PremiumGateView()
                .presentationDetents([.medium, .large])
                .presentationDragIndicator(.visible)
        }
    }
}

private struct PremiumGateView: View {
    @EnvironmentObject private var store: StoreManager

    var body: some View {
        ZStack {
            CS.bg.ignoresSafeArea()

            ScrollView {
                VStack(spacing: 22) {
                    Spacer(minLength: 28)

                    BrandMarkView(size: 108)

                    VStack(spacing: 10) {
                        Text("B-engel Premium")
                            .font(.title.bold())
                            .foregroundStyle(CS.primary)
                        Text("Uygulamayı kullanmak için premium abonelik gerekiyor.")
                            .font(.subheadline)
                            .foregroundStyle(CS.secondary)
                            .multilineTextAlignment(.center)
                    }

                    premiumFeatureCard
                    plansCard
                    restoreRow

                    if let error = store.lastError {
                        Text(error)
                            .font(.caption)
                            .foregroundStyle(CS.red)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal, 24)
                    }
                }
                .padding(.horizontal, 20)
                .padding(.bottom, 36)
            }
        }
    }

    private var premiumFeatureCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            premiumFeature("Bahis ve spam SMS filtreleme")
            premiumFeature("Sürekli güncellenen dolandırıcı listesi")
            premiumFeature("Engellenen mesaj geçmişi")
            premiumFeature("Gelişmiş koruma ve premium destek")
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(CS.surface)
        .clipShape(RoundedRectangle(cornerRadius: 18))
    }

    private var plansCard: some View {
        VStack(spacing: 12) {
            if store.products.isEmpty {
                Text("Abonelik ürünleri yükleniyor. App Store Connect ürünleri hazır olduğunda burada görünecek.")
                    .font(.caption)
                    .foregroundStyle(CS.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.vertical, 12)
            } else {
                ForEach(store.products, id: \.id) { product in
                    planRow(for: product)
                }
            }
        }
        .padding(16)
        .background(CS.surface)
        .clipShape(RoundedRectangle(cornerRadius: 18))
    }

    private func planRow(for product: Product) -> some View {
        let isYearly = product.id.contains("yearly")

        return VStack(alignment: .leading, spacing: 10) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 8) {
                        Text(isYearly ? "Yıllık Premium" : "Aylık Premium")
                            .font(.headline)
                            .foregroundStyle(CS.primary)
                        if isYearly {
                            Text("7 GÜN ÜCRETSİZ")
                                .font(.caption2.bold())
                                .foregroundStyle(.black)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 4)
                                .background(CS.accent)
                                .clipShape(Capsule())
                        }
                    }
                    Text(isYearly ? "En avantajlı paket. 7 günlük denemeden sonra yıllık yenilenir." : "Kısa dönemli kullanım için aylık erişim.")
                        .font(.caption)
                        .foregroundStyle(CS.secondary)
                }
                Spacer()
                if store.isLoading {
                    ProgressView()
                        .tint(CS.accent)
                } else {
                    Button(product.displayPrice) {
                        Task { await store.purchase(product) }
                    }
                    .font(.subheadline.bold())
                    .foregroundStyle(CS.accent)
                }
            }
        }
        .padding(16)
        .background(CS.surfaceVar)
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }

    private var restoreRow: some View {
        HStack {
            Text("Eski satın alımın mı var?")
                .font(.subheadline)
                .foregroundStyle(CS.secondary)
            Spacer()
            Button("Geri Yükle") {
                Task { await store.restorePurchases() }
            }
            .font(.subheadline.bold())
            .foregroundStyle(CS.accent)
        }
        .padding(16)
        .background(CS.surface)
        .clipShape(RoundedRectangle(cornerRadius: 18))
    }

    private func premiumFeature(_ text: String) -> some View {
        HStack(spacing: 10) {
            Image(systemName: "checkmark.circle.fill")
                .foregroundStyle(CS.accent)
            Text(text)
                .font(.subheadline)
                .foregroundStyle(CS.primary)
        }
    }
}
