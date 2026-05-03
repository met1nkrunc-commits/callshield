// Thin copy — extension process cannot import the main app module,
// so AppGroupStorage is compiled directly into both targets.
// Keep this file in sync with Callshield/Storage/AppGroupStorage.swift.
import Foundation

final class AppGroupStorage {

    static let shared = AppGroupStorage()
    private init() {}

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
        UserDefaults(suiteName: "group.com.callshield") ?? .standard
    }

    // MARK: - Premium

    var isPremiumEnabled: Bool {
        defaults.bool(forKey: "premiumEnabled")
    }

    // MARK: - Blocked numbers

    private var manualBlockedNumbers: Set<String> {
        Set(defaults.stringArray(forKey: "manualBlockedNumbers") ?? [])
    }

    private var remoteBlockedNumbers: Set<String> {
        Set(defaults.stringArray(forKey: "remoteBlockedNumbers") ?? [])
    }

    var blockedNumbers: Set<String> {
        manualBlockedNumbers.union(remoteBlockedNumbers)
    }

    func isBlockedNumber(_ raw: String) -> Bool {
        let normalized = normalize(raw)
        return blockedNumbers.contains { normalize($0) == normalized }
    }

    // MARK: - Trusted numbers

    private var trustedNumbers: Set<String> {
        Set(defaults.stringArray(forKey: "trustedNumbers") ?? [])
    }

    func isTrusted(_ raw: String) -> Bool {
        let n = normalize(raw)
        return trustedNumbers.contains { normalize($0) == n }
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

    // MARK: - Filtered SMS history

    struct FilteredMessage: Codable {
        let sender: String
        let reason: String
        let date: Date
    }

    private var filteredMessages: [FilteredMessage] {
        get {
            guard let data = defaults.data(forKey: "filteredMessages"),
                  let decoded = try? JSONDecoder().decode([FilteredMessage].self, from: data)
            else { return [] }
            return decoded
        }
        set {
            let trimmed = Array(newValue.suffix(200))
            if let data = try? JSONEncoder().encode(trimmed) {
                defaults.set(data, forKey: "filteredMessages")
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
