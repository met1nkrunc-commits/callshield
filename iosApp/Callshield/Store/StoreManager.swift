import Foundation
import StoreKit

@MainActor
final class StoreManager: ObservableObject {
    static let productIDs = [
        "bengel_standard_monthly",
        "bengel_standard_yearly",
        "bengel_family_monthly",
        "bengel_family_yearly"
    ]

    @Published private(set) var products: [Product] = []
    @Published private(set) var purchasedProductIDs: Set<String> = []
    @Published private(set) var isLoading = false
    @Published private(set) var didLoadProducts = false
    @Published var errorMessage: String?

    private var updatesTask: Task<Void, Never>?

    var isPremium: Bool {
        !purchasedProductIDs.isDisjoint(with: Self.productIDs)
    }

    init() {
        updatesTask = observeTransactionUpdates()
    }

    deinit {
        updatesTask?.cancel()
    }

    func configure(force: Bool = false) async {
        if isLoading { return }
        if didLoadProducts, !force { return }

        isLoading = true
        defer { isLoading = false }

        await refreshEntitlements()

        do {
            errorMessage = nil
            products = try await Product.products(for: Self.productIDs)
                .sorted { sortRank($0.id) < sortRank($1.id) }
            didLoadProducts = true
            if products.isEmpty {
                errorMessage = "Satın alma seçenekleri şu anda App Store'dan alınamıyor. Biraz sonra tekrar dene."
            }
        } catch {
            didLoadProducts = false
            products = []
            errorMessage = userFacingMessage(for: error)
        }
    }

    func purchase(_ product: Product) async {
        isLoading = true
        defer { isLoading = false }

        do {
            errorMessage = nil
            let result = try await product.purchase()
            switch result {
            case .success(let verification):
                let transaction = try checkVerified(verification)
                await transaction.finish()
                await refreshEntitlements()
            case .userCancelled:
                break
            case .pending:
                errorMessage = "Satın alma onay bekliyor. Onaylandığında Premium otomatik açılacak."
            @unknown default:
                break
            }
        } catch {
            errorMessage = userFacingMessage(for: error)
        }
    }

    func restore() async {
        isLoading = true
        defer { isLoading = false }

        do {
            errorMessage = nil
            try await AppStore.sync()
            await refreshEntitlements()
            if !isPremium {
                errorMessage = "Bu App Store hesabında geri yüklenecek aktif satın alma bulunamadı."
            }
        } catch {
            errorMessage = userFacingMessage(for: error)
        }
    }

    func refreshEntitlements() async {
        var activeIDs = Set<String>()

        for await entitlement in Transaction.currentEntitlements {
            guard let transaction = try? checkVerified(entitlement) else { continue }
            if Self.productIDs.contains(transaction.productID) {
                activeIDs.insert(transaction.productID)
            }
        }

        purchasedProductIDs = activeIDs
        AppGroupStorage.shared.isPremiumEnabled = !activeIDs.isEmpty
    }

    private func observeTransactionUpdates() -> Task<Void, Never> {
        Task {
            for await update in Transaction.updates {
                guard let transaction = try? checkVerified(update) else { continue }
                await transaction.finish()
                await refreshEntitlements()
            }
        }
    }

    private func checkVerified<T>(_ result: VerificationResult<T>) throws -> T {
        switch result {
        case .verified(let safe):
            return safe
        case .unverified:
            throw StoreError.failedVerification
        }
    }

    private func sortRank(_ id: String) -> Int {
        switch id {
        case "bengel_standard_monthly": return 0
        case "bengel_standard_yearly": return 1
        case "bengel_family_monthly": return 2
        case "bengel_family_yearly": return 3
        default: return 99
        }
    }

    private func userFacingMessage(for error: Error) -> String {
        let nsError = error as NSError
        if nsError.domain == NSURLErrorDomain {
            switch nsError.code {
            case NSURLErrorNotConnectedToInternet:
                return "İnternet bağlantısı yok. Bağlantını kontrol edip tekrar dene."
            case NSURLErrorTimedOut, NSURLErrorCannotConnectToHost, NSURLErrorNetworkConnectionLost:
                return "App Store'a ulaşılamadı. Biraz sonra tekrar dene."
            default:
                return "Satın alma işlemi şu anda tamamlanamıyor. Biraz sonra tekrar dene."
            }
        }

        if nsError.domain == SKError.errorDomain,
           nsError.code == SKError.paymentCancelled.rawValue {
            return ""
        }

        return "Satın alma işlemi şu anda tamamlanamıyor. Biraz sonra tekrar dene."
    }
}

enum StoreError: LocalizedError {
    case failedVerification

    var errorDescription: String? {
        "Satın alma doğrulanamadı."
    }
}
