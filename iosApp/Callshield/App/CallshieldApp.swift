import SwiftUI
import StoreKit

@main
struct CallshieldApp: App {
    @StateObject private var syncService = FraudSyncService()
    @StateObject private var store = StoreManager()
    @AppStorage("onboardingDone") private var onboardingDone = false

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(syncService)
                .environmentObject(store)
                .task {
                    await syncService.syncIfNeeded()
                }
        }
    }
}

@MainActor
final class StoreManager: ObservableObject {
    @Published private(set) var products: [Product] = []
    @Published private(set) var purchasedProductIDs: Set<String> = []
    @Published private(set) var isLoading = false
    @Published var lastError: String?
    @Published var isPaywallPresented = false

    let premiumProductIDs = [
        "com.callshield.ios.premium.monthly",
        "com.callshield.ios.premium.yearly"
    ]

    var isPremiumUnlocked: Bool {
        true
    }

    func presentPaywall() {
        isPaywallPresented = false
    }

    func unlockDebugPremium() {
        purchasedProductIDs.insert("debug.premium")
        AppGroupStorage.shared.isPremiumEnabled = true
        lastError = nil
    }

    init() {
        AppGroupStorage.shared.isPremiumEnabled = true
        Task {
            await refreshProducts()
            await refreshEntitlements()
            await observeTransactions()
        }
    }

    func refreshProducts() async {
        isLoading = true
        defer { isLoading = false }

        do {
            let fetched = try await Product.products(for: premiumProductIDs)
            products = fetched.sorted { $0.price < $1.price }
            lastError = nil
        } catch {
            lastError = error.localizedDescription
        }
    }

    func refreshEntitlements() async {
        var purchased: Set<String> = []

        for await result in Transaction.currentEntitlements {
            guard case .verified(let transaction) = result else { continue }
            purchased.insert(transaction.productID)
        }

        purchasedProductIDs = purchased
        AppGroupStorage.shared.isPremiumEnabled = true
    }

    func purchase(_ product: Product) async {
        isLoading = true
        defer { isLoading = false }

        do {
            let result = try await product.purchase()
            switch result {
            case .success(let verification):
                guard case .verified(let transaction) = verification else {
                    lastError = "Satın alma doğrulanamadı"
                    return
                }
                purchasedProductIDs.insert(transaction.productID)
                AppGroupStorage.shared.isPremiumEnabled = true
                await transaction.finish()
                lastError = nil
            case .userCancelled, .pending:
                break
            @unknown default:
                break
            }
        } catch {
            lastError = error.localizedDescription
        }
    }

    func restorePurchases() async {
        isLoading = true
        defer { isLoading = false }

        do {
            try await AppStore.sync()
            await refreshEntitlements()
            lastError = nil
        } catch {
            lastError = error.localizedDescription
        }
    }

    private func observeTransactions() async {
        for await result in Transaction.updates {
            guard case .verified(let transaction) = result else { continue }
            purchasedProductIDs.insert(transaction.productID)
            AppGroupStorage.shared.isPremiumEnabled = true
            await transaction.finish()
        }
    }
}
