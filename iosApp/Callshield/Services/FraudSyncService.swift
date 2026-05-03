import Foundation

// jsDelivr, GitHub repo içeriğini CDN üzerinden servis eder.
// "GITHUB_USER/REPO_NAME" kısmını kendi repo bilginizle değiştirin.
// Örnek: "metinkorunc/callshield-data"
private let fraudDataURL =
    "https://cdn.jsdelivr.net/gh/GITHUB_USER/REPO_NAME@main/fraud_numbers.json"

// MARK: - Codable DTOs

struct FraudNumbersResponse: Codable {
    let version: Int
    let updatedAt: String
    let numbers: [FraudNumberEntry]
    let keywordsVersion: Int
    let extraKeywords: ExtraKeywords?

    enum CodingKeys: String, CodingKey {
        case version, numbers
        case updatedAt       = "updated_at"
        case keywordsVersion = "keywords_version"
        case extraKeywords   = "extra_keywords"
    }
}

struct FraudNumberEntry: Codable {
    let number: String
    let riskLevel: String
    let category: String
    let reportCount: Int
    let note: String?

    enum CodingKeys: String, CodingKey {
        case number, category, note
        case riskLevel   = "risk_level"
        case reportCount = "report_count"
    }
}

struct ExtraKeywords: Codable {
    let betting: [String]
    let phishing: [String]
    let legal: [String]
    let social: [String]
}

// MARK: - Service

@MainActor
final class FraudSyncService: ObservableObject {

    @Published var isSyncing    = false
    @Published var lastError:   String? = nil
    @Published var blockedCount = AppGroupStorage.shared.blockedNumbers.count
    @Published var lastSyncDate = AppGroupStorage.shared.lastSyncDate

    private let storage     = AppGroupStorage.shared
    private let staleAfter: TimeInterval = 24 * 60 * 60  // 24 saat

    // MARK: - Public

    func syncIfNeeded() async {
        guard fraudDataURL.contains("GITHUB_USER") == false else {
            // URL henüz yapılandırılmamış, atla.
            return
        }
        if let last = storage.lastSyncDate, Date().timeIntervalSince(last) < staleAfter {
            return
        }
        await sync()
    }

    func sync() async {
        guard fraudDataURL.contains("GITHUB_USER") == false else {
            lastError = "fraudDataURL henüz yapılandırılmamış."
            return
        }

        isSyncing = true
        lastError = nil
        defer { isSyncing = false }

        do {
            let response = try await fetchFraudNumbers()
            apply(response)
        } catch {
            lastError = error.localizedDescription
        }
    }

    // MARK: - Private

    private func fetchFraudNumbers() async throws -> FraudNumbersResponse {
        guard let url = URL(string: fraudDataURL) else {
            throw URLError(.badURL)
        }
        let (data, response) = try await URLSession.shared.data(from: url)
        if let http = response as? HTTPURLResponse, http.statusCode != 200 {
            throw URLError(.badServerResponse)
        }
        return try JSONDecoder().decode(FraudNumbersResponse.self, from: data)
    }

    private func apply(_ response: FraudNumbersResponse) {
        // Sadece BLOCKED ve HIGH riskli numaraları engel listesine ekle
        var numbers = Set<String>()
        for entry in response.numbers
        where entry.riskLevel == "BLOCKED" || entry.riskLevel == "HIGH" {
            numbers.insert(entry.number)
        }
        storage.remoteBlockedNumbers = numbers

        storage.lastSyncDate = Date()
        storage.syncVersion  = response.version

        blockedCount = storage.blockedNumbers.count
        lastSyncDate = storage.lastSyncDate
    }
}
