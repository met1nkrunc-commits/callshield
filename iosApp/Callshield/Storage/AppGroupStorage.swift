import Foundation

/// Shared storage between the main app and the SmsFilter extension.
/// Both targets must declare the same App Group ID in their entitlements.
final class AppGroupStorage {

    static let shared = AppGroupStorage()
    private init() {}

    static let groupID = "group.com.callshield"

    // MARK: - Keyword scoring

    private struct ScoredKeyword {
        let text: String  // pre-normalized, no Turkish diacritics
        let score: Int
    }

    // Score >= 4 = block. Higher score = more exclusively betting-related.
    private static let scoredKeywords: [ScoredKeyword] = [
        // ── 5: Definitive — alone sufficient ─────────────────────────────
        .init(text: "iddaa",                score: 5),
        .init(text: "freebet",              score: 5),
        .init(text: "freespin",             score: 5),
        .init(text: "sportsbook",           score: 5),
        .init(text: "livecasino",           score: 5),
        .init(text: "deneme bonusu",        score: 5),
        .init(text: "canli bahis",          score: 5),
        .init(text: "bahis kuponu",         score: 5),
        .init(text: "casino giris",         score: 5),
        .init(text: "yatirimsiz bonus",     score: 5),
        .init(text: "cevrimsiz bonus",      score: 5),
        .init(text: "bedava freebet",       score: 5),
        .init(text: "freebonus",            score: 5),
        .init(text: "deneme freespin",      score: 5),
        .init(text: "giris bonusu",         score: 5),
        // Leetspeak
        .init(text: "b0nus",               score: 5),
        .init(text: "bonu$",               score: 5),
        .init(text: "freeb3t",             score: 5),
        .init(text: "fr33bet",             score: 5),
        .init(text: "1ddaa",               score: 5),
        .init(text: "1dda",                score: 5),
        .init(text: "bah1s",               score: 5),
        .init(text: "cas1no",              score: 5),
        .init(text: "sl0t",                score: 5),
        .init(text: "pr0mosyon",           score: 5),
        .init(text: "freesp1n",            score: 5),
        .init(text: "giris b0nusu",        score: 5),
        .init(text: "uyel1k",              score: 5),
        .init(text: "hemen uye0l",         score: 5),
        .init(text: "yatirims1z",          score: 5),
        .init(text: "canli bah1s",         score: 5),
        .init(text: "oran1ar",             score: 5),
        .init(text: "jackp0t",             score: 5),

        // ── 4: High confidence — alone sufficient ────────────────────────
        .init(text: "bahis",               score: 4),
        .init(text: "casino",              score: 4),
        .init(text: "jackpot",             score: 4),
        .init(text: "betting",             score: 4),
        .init(text: "bahis severlere",     score: 4),
        .init(text: "vip bonus",           score: 4),
        .init(text: "mac kuponu",          score: 4),
        .init(text: "kupon hazir",         score: 4),
        .init(text: "aninda kazan",        score: 4),
        .init(text: "hesabina tanimlandi", score: 4),
        .init(text: "kayip bonusu",        score: 4),
        .init(text: "yatirim bonusu",      score: 4),
        .init(text: "hediye bonus",        score: 4),
        .init(text: "oran guncel",         score: 4),
        .init(text: "super oran",          score: 4),
        .init(text: "canli oran",          score: 4),
        .init(text: "kuponunu al",         score: 4),

        // ── 3: Medium-high — need combination ────────────────────────────
        .init(text: "slot",                score: 3),
        .init(text: "yatirimsiz",          score: 3),
        .init(text: "cevrimsiz",           score: 3),
        .init(text: "sartsiz",             score: 3),
        .init(text: "cashback",            score: 3),
        .init(text: "free spin",           score: 3),
        .init(text: "hemen kazan",         score: 3),
        .init(text: "slot oyunu",          score: 3),

        // ── 2: Medium ─────────────────────────────────────────────────────
        .init(text: "bonus",               score: 2),
        .init(text: "kupon",               score: 2),
        .init(text: "bet",                 score: 2),
        .init(text: "uye ol",              score: 2),
        .init(text: "promosyon",           score: 2),
        .init(text: "kombine",             score: 2),
        .init(text: "yatirim",             score: 2),
        .init(text: "cekim",               score: 2),
        .init(text: "whatsapp grubu",      score: 2),
        .init(text: "ozel teklif",         score: 2),
        .init(text: "sana ozel",           score: 2),
        .init(text: "uyelik",              score: 2),
        .init(text: "hemen katil",         score: 2),
        .init(text: "uye girisi",          score: 2),
        .init(text: "ilk yatirim",         score: 2),

        // ── 1: Low confidence — needs many signals ────────────────────────
        .init(text: "oran",                score: 1),
        .init(text: "kampanya",            score: 1),
        .init(text: "kazanc",              score: 1),
        .init(text: "spin",                score: 1),
        .init(text: "telegram",            score: 1),
        .init(text: "firsat",              score: 1),
        .init(text: "aninda",              score: 1),
    ]

    private static let blockThreshold = 4

    // MARK: - Shared UserDefaults

    private var defaults: UserDefaults {
        UserDefaults(suiteName: Self.groupID) ?? .standard
    }

    // MARK: - Keys

    private enum Key {
        static let manualBlockedNumbers = "manualBlockedNumbers"
        static let remoteBlockedNumbers = "remoteBlockedNumbers"
        static let lastSyncDate         = "lastSyncDate"
        static let syncVersion          = "syncVersion"
        static let premiumEnabled       = "premiumEnabled"
        static let trustedNumbers       = "trustedNumbers"
    }

    // MARK: - Blocked numbers

    var blockedNumbers: Set<String> {
        manualBlockedNumbers.union(remoteBlockedNumbers)
    }

    var manualBlockedNumbers: Set<String> {
        get { Set(defaults.stringArray(forKey: Key.manualBlockedNumbers) ?? []) }
        set { defaults.set(Array(newValue), forKey: Key.manualBlockedNumbers) }
    }

    var remoteBlockedNumbers: Set<String> {
        get { Set(defaults.stringArray(forKey: Key.remoteBlockedNumbers) ?? []) }
        set { defaults.set(Array(newValue), forKey: Key.remoteBlockedNumbers) }
    }

    func isBlockedNumber(_ raw: String) -> Bool {
        let normalized = normalize(raw)
        return blockedNumbers.contains { normalize($0) == normalized }
    }

    func addBlocked(_ number: String) {
        var set = manualBlockedNumbers
        set.insert(normalize(number))
        manualBlockedNumbers = set
    }

    func removeBlocked(_ number: String) {
        var set = manualBlockedNumbers
        set.remove(normalize(number))
        manualBlockedNumbers = set
    }

    // MARK: - Trusted numbers

    var trustedNumbers: Set<String> {
        get { Set(defaults.stringArray(forKey: Key.trustedNumbers) ?? []) }
        set { defaults.set(Array(newValue), forKey: Key.trustedNumbers) }
    }

    func isTrusted(_ raw: String) -> Bool {
        let n = normalize(raw)
        return trustedNumbers.contains { normalize($0) == n }
    }

    func addTrusted(_ number: String) {
        var set = trustedNumbers
        set.insert(normalize(number))
        trustedNumbers = set
    }

    func removeTrusted(_ number: String) {
        var set = trustedNumbers
        set.remove(normalize(number))
        trustedNumbers = set
    }

    // MARK: - Fraud keyword scoring

    func containsFraudKeyword(in body: String) -> Bool {
        return fraudScore(in: body) >= Self.blockThreshold
    }

    func fraudScore(in body: String) -> Int {
        let normalized = Self.normalizeText(body)
        return Self.scoredKeywords.reduce(0) { total, kw in
            total + (Self.matchesAtWordStart(normalized, keyword: kw.text) ? kw.score : 0)
        }
    }

    // Left-boundary match: keyword must not be preceded by a letter.
    // Allows Turkish suffixes (bahisle, slotlar) while blocking mid-word matches (inspiration).
    private static func matchesAtWordStart(_ text: String, keyword: String) -> Bool {
        var searchRange = text.startIndex..<text.endIndex
        while let found = text.range(of: keyword, options: .literal, range: searchRange) {
            if found.lowerBound == text.startIndex {
                return true
            }
            let prev = text[text.index(before: found.lowerBound)]
            if !prev.isLetter {
                return true
            }
            guard found.upperBound < text.endIndex else { break }
            searchRange = found.upperBound..<text.endIndex
        }
        return false
    }

    // Kept for UI compatibility (keyword list display in settings).
    var fraudKeywords: [String] {
        Self.scoredKeywords.map(\.text)
    }

    // MARK: - Sync metadata

    var lastSyncDate: Date? {
        get {
            let t = defaults.double(forKey: Key.lastSyncDate)
            return t > 0 ? Date(timeIntervalSince1970: t) : nil
        }
        set { defaults.set(newValue?.timeIntervalSince1970 ?? 0, forKey: Key.lastSyncDate) }
    }

    var syncVersion: Int {
        get { defaults.integer(forKey: Key.syncVersion) }
        set { defaults.set(newValue, forKey: Key.syncVersion) }
    }

    var isPremiumEnabled: Bool {
        get { defaults.bool(forKey: Key.premiumEnabled) }
        set { defaults.set(newValue, forKey: Key.premiumEnabled) }
    }

    // MARK: - Filtered SMS history

    struct FilteredMessage: Codable {
        let sender: String
        let reason: String  // "sender" or "keyword"
        let date: Date
    }

    private enum HistoryKey {
        static let filteredMessages = "filteredMessages"
    }

    var filteredMessages: [FilteredMessage] {
        get {
            guard let data = defaults.data(forKey: HistoryKey.filteredMessages),
                  let decoded = try? JSONDecoder().decode([FilteredMessage].self, from: data)
            else { return [] }
            return decoded
        }
        set {
            let trimmed = Array(newValue.suffix(200))
            if let data = try? JSONEncoder().encode(trimmed) {
                defaults.set(data, forKey: HistoryKey.filteredMessages)
            }
        }
    }

    func recordFiltered(sender: String, reason: String) {
        var list = filteredMessages
        list.append(FilteredMessage(sender: sender, reason: reason, date: Date()))
        filteredMessages = list
    }

    // MARK: - Helpers

    private func normalize(_ number: String) -> String {
        var s = number.filter { $0.isNumber || $0 == "+" }
        if s.hasPrefix("0") { s = "+90" + s.dropFirst() }
        return s
    }

    private static func normalizeText(_ text: String) -> String {
        text
            .folding(options: [.caseInsensitive, .diacriticInsensitive], locale: Locale(identifier: "tr_TR"))
            .trimmingCharacters(in: .whitespacesAndNewlines)
    }
}
