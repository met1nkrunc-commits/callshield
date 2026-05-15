import StoreKit
import SwiftUI

struct PaywallView: View {
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject private var store: StoreManager
    @State private var selectedProductID = "bengel_standard_yearly"

    private var selectedProduct: Product? {
        store.products.first { $0.id == selectedProductID } ?? store.products.first
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 22) {
                header
                featureList
                products
                storeMessage
                purchaseButton
                restoreButton
                footer
            }
            .padding(.horizontal, 18)
            .padding(.top, 24)
            .padding(.bottom, 32)
        }
        .background(CS.bg.ignoresSafeArea())
        .navigationTitle("Premium")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button("Kapat") { dismiss() }
                    .foregroundStyle(CS.secondary)
            }
        }
        .task {
            if store.products.isEmpty {
                await store.configure(force: true)
            }
            if selectedProduct == nil, let first = store.products.first {
                selectedProductID = first.id
            }
        }
        .onChange(of: store.isPremium) { premium in
            if premium { dismiss() }
        }
    }

    private var header: some View {
        VStack(spacing: 8) {
            BrandMarkView(size: 74)
            Text("Siper Premium")
                .font(.title2.weight(.bold))
                .foregroundStyle(CS.primary)
            Text("7 gün ücretsiz dene, sonra seçtiğin abonelikle reklamsız ve sınırsız korumaya devam et.")
                .font(.subheadline)
                .foregroundStyle(CS.secondary)
                .multilineTextAlignment(.center)
        }
    }

    private var featureList: some View {
        VStack(alignment: .leading, spacing: 12) {
            premiumRow("Reklamsız kullanım", icon: "rectangle.slash")
            premiumRow("Sınırsız manuel engelleme", icon: "nosign")
            premiumRow("Öncelikli dolandırıcı liste güncellemeleri", icon: "arrow.clockwise.shield")
            premiumRow("Aile planında 5 cihaza kadar kullanım", icon: "person.3.fill")
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(CS.surface)
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }

    private var products: some View {
        VStack(spacing: 10) {
            if store.isLoading && store.products.isEmpty {
                ProgressView()
                    .tint(CS.accent)
                    .frame(maxWidth: .infinity)
                    .padding(20)
            } else if store.products.isEmpty {
                ForEach(fallbackPlans, id: \.id) { plan in
                    fallbackProductRow(plan)
                }
            } else {
                ForEach(store.products, id: \.id) { product in
                    productRow(product)
                }
            }
        }
    }

    private var storeMessage: some View {
        Group {
            if store.products.isEmpty, let error = store.errorMessage {
                VStack(alignment: .leading, spacing: 10) {
                    Label("App Store bağlantısı bekleniyor", systemImage: "exclamationmark.triangle.fill")
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(CS.orange)
                    Text(error)
                        .font(.caption)
                        .foregroundStyle(CS.secondary)
                        .fixedSize(horizontal: false, vertical: true)
                    Button {
                        Task { await store.configure(force: true) }
                    } label: {
                        Label("Tekrar dene", systemImage: "arrow.clockwise")
                            .font(.caption.weight(.semibold))
                    }
                    .foregroundStyle(CS.accent)
                }
                .padding(14)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(CS.surface)
                .clipShape(RoundedRectangle(cornerRadius: 12))
            }
        }
    }

    private var purchaseButton: some View {
        Button {
            if let product = selectedProduct {
                Task { await store.purchase(product) }
            } else {
                Task { await store.configure(force: true) }
            }
        } label: {
            HStack {
                if store.isLoading {
                    ProgressView().tint(.white)
                } else {
                    Text(buttonTitle)
                        .font(.headline)
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: 52)
        }
        .disabled(store.isLoading)
        .foregroundStyle(.white)
        .background(CS.accent)
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }

    private var restoreButton: some View {
        Button("Satın alımları geri yükle") {
            Task { await store.restore() }
        }
        .font(.footnote.weight(.semibold))
        .foregroundStyle(CS.secondary)
    }

    private var footer: some View {
        VStack(spacing: 6) {
            if let error = store.errorMessage {
                Text(error)
                    .font(.caption)
                    .foregroundStyle(CS.red)
                    .multilineTextAlignment(.center)
            }
            Text("Deneme süresi ve fiyatlar App Store satın alma ekranında Apple tarafından doğrulanır. Aboneliği App Store ayarlarından iptal edebilirsin.")
                .font(.caption2)
                .foregroundStyle(CS.disabled)
                .multilineTextAlignment(.center)
        }
    }

    private var buttonTitle: String {
        guard let product = selectedProduct else { return "Tekrar Dene" }
        if hasSevenDayTrial(product) {
            return "7 Gün Ücretsiz Dene"
        }
        return "\(product.displayPrice) ile Başla"
    }

    private func premiumRow(_ text: String, icon: String) -> some View {
        HStack(spacing: 10) {
            Image(systemName: icon)
                .font(.system(size: 16, weight: .semibold))
                .foregroundStyle(CS.accent)
                .frame(width: 24)
            Text(text)
                .font(.subheadline)
                .foregroundStyle(CS.primary)
        }
    }

    private func productRow(_ product: Product) -> some View {
        Button {
            selectedProductID = product.id
        } label: {
            HStack(spacing: 12) {
                Image(systemName: selectedProductID == product.id ? "checkmark.circle.fill" : "circle")
                    .foregroundStyle(selectedProductID == product.id ? CS.accent : CS.disabled)
                VStack(alignment: .leading, spacing: 3) {
                    Text(title(for: product.id))
                        .font(.subheadline.weight(.semibold))
                        .foregroundStyle(CS.primary)
                    Text(subtitle(for: product))
                        .font(.caption)
                        .foregroundStyle(CS.secondary)
                }
                Spacer()
                Text(product.displayPrice)
                    .font(.subheadline.weight(.bold))
                    .foregroundStyle(CS.primary)
            }
            .padding(14)
            .background(selectedProductID == product.id ? CS.accent.opacity(0.12) : CS.surface)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(selectedProductID == product.id ? CS.accent : CS.surfaceVar, lineWidth: 1)
            )
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        .buttonStyle(.plain)
    }

    private func fallbackProductRow(_ plan: FallbackPlan) -> some View {
        Button {
            selectedProductID = plan.id
        } label: {
            HStack(spacing: 12) {
                Image(systemName: selectedProductID == plan.id ? "checkmark.circle.fill" : "circle")
                    .foregroundStyle(selectedProductID == plan.id ? CS.accent : CS.disabled)
                VStack(alignment: .leading, spacing: 3) {
                    Text(plan.title)
                        .font(.subheadline.weight(.semibold))
                        .foregroundStyle(CS.primary)
                    Text("7 gün ücretsiz deneme")
                        .font(.caption)
                        .foregroundStyle(CS.secondary)
                }
                Spacer()
                Text(plan.price)
                    .font(.subheadline.weight(.bold))
                    .foregroundStyle(CS.primary)
            }
            .padding(14)
            .background(selectedProductID == plan.id ? CS.accent.opacity(0.12) : CS.surface)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(selectedProductID == plan.id ? CS.accent : CS.surfaceVar, lineWidth: 1)
            )
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
        .buttonStyle(.plain)
    }

    private func title(for productID: String) -> String {
        switch productID {
        case "bengel_standard_monthly": return "Standart Aylık"
        case "bengel_standard_yearly": return "Standart Yıllık"
        case "bengel_family_monthly": return "Aile Aylık"
        case "bengel_family_yearly": return "Aile Yıllık"
        default: return "Premium"
        }
    }

    private func subtitle(for product: Product) -> String {
        hasSevenDayTrial(product) ? "7 gün ücretsiz deneme dahil" : "App Store fiyatı"
    }

    private func hasSevenDayTrial(_ product: Product) -> Bool {
        guard let offer = product.subscription?.introductoryOffer else { return false }
        return offer.paymentMode == .freeTrial &&
            offer.period.unit == .day &&
            offer.period.value == 7
    }

    private var fallbackPlans: [FallbackPlan] {
        [
            .init(id: "bengel_standard_monthly", title: "Standart Aylık", price: "₺80/ay"),
            .init(id: "bengel_standard_yearly", title: "Standart Yıllık", price: "₺600/yıl"),
            .init(id: "bengel_family_monthly", title: "Aile Aylık", price: "₺150/ay"),
            .init(id: "bengel_family_yearly", title: "Aile Yıllık", price: "₺1.100/yıl")
        ]
    }
}

private struct FallbackPlan {
    let id: String
    let title: String
    let price: String
}
