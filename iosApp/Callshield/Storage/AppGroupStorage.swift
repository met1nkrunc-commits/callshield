import Foundation

/// Shared storage between the main app and the SmsFilter extension.
/// Both targets must declare the same App Group ID in their entitlements.
final class AppGroupStorage {

    static let shared = AppGroupStorage()
    private init() {}

    static let groupID = "group.com.metinkorunc.bengel"

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

    private static let exactTokenKeywords: Set<String> = [
        "bet"
    ]
    private static let senderKeywordHints = [
        "bet",
        "bonus",
        "casino",
        "freebet",
        "freespin",
        "iddaa",
        "jackpot",
        "slot",
        "vip"
    ]
    private static let riskyLinkTLDs: Set<String> = [
        "bet",
        "casino",
        "click",
        "top",
        "vip",
        "win",
        "xyz"
    ]
    private static let riskyLinkHints = [
        "bahis",
        "bet",
        "bonus",
        "casino",
        "freebet",
        "freespin",
        "iddaa",
        "jackpot",
        "kupon",
        "slot"
    ]
    private static let shortLinkHosts: Set<String> = [
        "bit.ly",
        "cutt.ly",
        "is.gd",
        "rebrand.ly",
        "shorturl.at",
        "t.co",
        "tinyurl.com"
    ]
    private static let trustedBrandDomains: Set<String> = [
        "akbank.com",
        "garanti.com.tr",
        "halkbank.com.tr",
        "isbank.com.tr",
        "mhrs.gov.tr",
        "ptt.gov.tr",
        "qnb.com.tr",
        "turkiye.gov.tr",
        "turkcell.com.tr",
        "turktelekom.com.tr",
        "vakifbank.com.tr",
        "vodafone.com.tr",
        "yapikredi.com.tr",
        "ziraatbank.com.tr"
    ]

    // MARK: - Shared UserDefaults

    private var defaults: UserDefaults {
        UserDefaults(suiteName: Self.groupID) ?? .standard
    }

    // MARK: - Keys

    private enum Key {
        static let manualBlockedNumbers = "manualBlockedNumbers"
        static let remoteBlockedNumbers = "remoteBlockedNumbers"
        static let blockedSenderRules   = "blockedSenderRules"
        static let trustedSenderRules   = "trustedSenderRules"
        static let filterSensitivity    = "filterSensitivity"
        static let safeKeywords         = "safeKeywords"
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

    // MARK: - Sender rules

    var blockedSenderRules: Set<String> {
        get { Set(defaults.stringArray(forKey: Key.blockedSenderRules) ?? []) }
        set { defaults.set(Array(newValue), forKey: Key.blockedSenderRules) }
    }

    var trustedSenderRules: Set<String> {
        get { Set(defaults.stringArray(forKey: Key.trustedSenderRules) ?? []) }
        set { defaults.set(Array(newValue), forKey: Key.trustedSenderRules) }
    }

    func addBlockedSenderRule(_ rule: String) {
        var rules = blockedSenderRules
        rules.insert(normalizeSender(rule))
        blockedSenderRules = rules
    }

    func removeBlockedSenderRule(_ rule: String) {
        var rules = blockedSenderRules
        rules.remove(normalizeSender(rule))
        blockedSenderRules = rules
    }

    func addTrustedSenderRule(_ rule: String) {
        var rules = trustedSenderRules
        rules.insert(normalizeSender(rule))
        trustedSenderRules = rules
    }

    func removeTrustedSenderRule(_ rule: String) {
        var rules = trustedSenderRules
        rules.remove(normalizeSender(rule))
        trustedSenderRules = rules
    }

    func isBlockedSender(_ sender: String) -> Bool {
        matchesSenderRule(sender, rules: blockedSenderRules)
    }

    func isTrustedSenderIdentifier(_ sender: String) -> Bool {
        matchesSenderRule(sender, rules: trustedSenderRules)
    }

    // MARK: - Rule controls

    enum FilterSensitivity: String, CaseIterable, Identifiable {
        case relaxed
        case balanced
        case aggressive

        var id: String { rawValue }

        var title: String {
            switch self {
            case .relaxed:
                return "Hafif"
            case .balanced:
                return "Dengeli"
            case .aggressive:
                return "Agresif"
            }
        }

        var detail: String {
            switch self {
            case .relaxed:
                return "Yanlış pozitifleri azaltır, sadece güçlü bahis sinyallerini engeller."
            case .balanced:
                return "Günlük kullanım için önerilen orta seviye koruma."
            case .aggressive:
                return "Daha fazla şüpheli mesajı yakalar, yanlış pozitif riski artar."
            }
        }

        var keywordThreshold: Int {
            switch self {
            case .relaxed:
                return 5
            case .balanced:
                return 4
            case .aggressive:
                return 3
            }
        }

        var alphanumericThreshold: Int {
            switch self {
            case .relaxed:
                return 4
            case .balanced:
                return 2
            case .aggressive:
                return 1
            }
        }
    }

    var filterSensitivity: FilterSensitivity {
        get {
            FilterSensitivity(rawValue: defaults.string(forKey: Key.filterSensitivity) ?? "") ?? .balanced
        }
        set {
            defaults.set(newValue.rawValue, forKey: Key.filterSensitivity)
        }
    }

    var safeKeywords: Set<String> {
        get { Set(defaults.stringArray(forKey: Key.safeKeywords) ?? []) }
        set { defaults.set(Array(newValue).sorted(), forKey: Key.safeKeywords) }
    }

    func addSafeKeyword(_ keyword: String) {
        let normalized = Self.normalizeText(keyword)
        guard !normalized.isEmpty else { return }
        var keywords = safeKeywords
        keywords.insert(normalized)
        safeKeywords = keywords
    }

    func removeSafeKeyword(_ keyword: String) {
        var keywords = safeKeywords
        keywords.remove(Self.normalizeText(keyword))
        safeKeywords = keywords
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

    struct FilterDecision {
        let shouldBlock: Bool
        let reason: String
        let detail: String
        let matchedKeyword: String?
        let score: Int
    }

    func containsFraudKeyword(in body: String) -> Bool {
        decision(sender: nil, body: body).reason == "keyword"
    }

    func fraudScore(in body: String) -> Int {
        keywordEvaluation(in: body).score
    }

    struct LinkAnalysis {
        let input: String
        let hosts: [String]
        let score: Int
        let reasons: [String]

        var isRisky: Bool {
            score >= 4
        }
    }

    func analyzeLink(_ input: String) -> LinkAnalysis {
        let hosts = Self.extractHosts(from: input)
        var total = 0
        var reasons: [String] = []

        for host in hosts {
            let result = Self.linkRiskDetails(for: host)
            total += result.score
            reasons.append(contentsOf: result.reasons)
        }

        return LinkAnalysis(input: input, hosts: hosts, score: min(total, 10), reasons: reasons)
    }

    func decision(sender: String?, body: String?) -> FilterDecision {
        if let sender, (isTrusted(sender) || isTrustedSenderIdentifier(sender)) {
            return FilterDecision(
                shouldBlock: false,
                reason: "",
                detail: "",
                matchedKeyword: nil,
                score: 0
            )
        }

        if let sender, isBlockedNumber(sender) {
            return FilterDecision(
                shouldBlock: true,
                reason: "sender",
                detail: "Gönderici engellenen numara listesinde.",
                matchedKeyword: nil,
                score: 0
            )
        }

        if let sender, isBlockedSender(sender) {
            return FilterDecision(
                shouldBlock: true,
                reason: "sender_rule",
                detail: "Gönderici adı engellenen kural listesiyle eşleşti.",
                matchedKeyword: nil,
                score: 0
            )
        }

        let keywordEvaluation = keywordEvaluation(in: body ?? "")
        let linkEvaluation = suspiciousLinkEvaluation(in: body ?? "")
        let totalScore = keywordEvaluation.score + linkEvaluation.score
        if let safeKeyword = matchedSafeKeyword(in: body ?? "") {
            return FilterDecision(
                shouldBlock: false,
                reason: "safe_keyword",
                detail: "\"\(safeKeyword)\" güvenli kelime listesinde olduğu için içerik bazlı engel uygulanmadı.",
                matchedKeyword: safeKeyword,
                score: totalScore
            )
        }

        let sensitivity = filterSensitivity
        if linkEvaluation.score >= sensitivity.keywordThreshold {
            return FilterDecision(
                shouldBlock: true,
                reason: "link",
                detail: "Mesajdaki \"\(linkEvaluation.host ?? "link")\" bağlantısı riskli link kuralıyla eşleşti.",
                matchedKeyword: linkEvaluation.host,
                score: totalScore
            )
        }

        if totalScore >= sensitivity.keywordThreshold {
            return FilterDecision(
                shouldBlock: true,
                reason: "keyword",
                detail: "Mesaj \"\(keywordEvaluation.keyword ?? linkEvaluation.host ?? "spam")\" ifadesi nedeniyle işaretlendi.",
                matchedKeyword: keywordEvaluation.keyword ?? linkEvaluation.host,
                score: totalScore
            )
        }

        if let sender, isSuspiciousAlphanumeric(sender), totalScore >= sensitivity.alphanumericThreshold {
            return FilterDecision(
                shouldBlock: true,
                reason: "alphanumeric",
                detail: "Alfanümerik gönderici ve şüpheli içerik birlikte tespit edildi.",
                matchedKeyword: keywordEvaluation.keyword ?? linkEvaluation.host,
                score: totalScore
            )
        }

        return FilterDecision(
            shouldBlock: false,
            reason: "",
            detail: "",
            matchedKeyword: keywordEvaluation.keyword ?? linkEvaluation.host,
            score: totalScore
        )
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
        let detail: String
        let preview: String
        let date: Date

        private enum CodingKeys: String, CodingKey {
            case sender
            case reason
            case detail
            case preview
            case date
        }

        init(sender: String, reason: String, detail: String, preview: String, date: Date) {
            self.sender = sender
            self.reason = reason
            self.detail = detail
            self.preview = preview
            self.date = date
        }

        init(from decoder: Decoder) throws {
            let container = try decoder.container(keyedBy: CodingKeys.self)
            sender = try container.decode(String.self, forKey: .sender)
            reason = try container.decode(String.self, forKey: .reason)
            detail = try container.decodeIfPresent(String.self, forKey: .detail) ?? ""
            preview = try container.decodeIfPresent(String.self, forKey: .preview) ?? ""
            date = try container.decode(Date.self, forKey: .date)
        }
    }

    private enum HistoryKey {
        static let filteredMessages = "filteredMessages"
        static let spamReports = "spamReports"
        static let filteredStats = "filteredStats"
        static let filteredTotalCount = "filteredTotalCount"
        static let filterInvocationCount = "filterInvocationCount"
        static let lastFilterCheckDate = "lastFilterCheckDate"
        static let lastFilterCheckResult = "lastFilterCheckResult"
    }

    struct FilterStats: Codable {
        var total: Int
        var byReason: [String: Int]
        var byDay: [String: Int]

        static let empty = FilterStats(total: 0, byReason: [:], byDay: [:])

        mutating func record(reason: String, date: Date) {
            total += 1
            byReason[reason, default: 0] += 1
            byDay[Self.dayKey(for: date), default: 0] += 1
        }

        func count(since startDate: Date) -> Int {
            byDay.reduce(0) { partial, entry in
                guard let date = Self.dayFormatter.date(from: entry.key), date >= startDate else {
                    return partial
                }
                return partial + entry.value
            }
        }

        static func derived(from messages: [FilteredMessage]) -> FilterStats {
            messages.reduce(into: FilterStats.empty) { stats, message in
                stats.record(reason: message.reason, date: message.date)
            }
        }

        private static func dayKey(for date: Date) -> String {
            dayFormatter.string(from: date)
        }

        private static let dayFormatter: DateFormatter = {
            let formatter = DateFormatter()
            formatter.calendar = Calendar(identifier: .gregorian)
            formatter.locale = Locale(identifier: "en_US_POSIX")
            formatter.dateFormat = "yyyy-MM-dd"
            return formatter
        }()
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
                defaults.synchronize()
            }
        }
    }

    var filteredStats: FilterStats {
        get {
            let fallbackTotal = defaults.integer(forKey: HistoryKey.filteredTotalCount)
            if let data = defaults.data(forKey: HistoryKey.filteredStats),
               let decoded = try? JSONDecoder().decode(FilterStats.self, from: data),
               decoded.total > 0 || filteredMessages.isEmpty {
                if decoded.total < fallbackTotal {
                    var repaired = decoded
                    repaired.total = fallbackTotal
                    return repaired
                }
                return decoded
            }
            var derived = FilterStats.derived(from: filteredMessages)
            if derived.total < fallbackTotal {
                derived.total = fallbackTotal
            }
            return derived
        }
        set {
            if let data = try? JSONEncoder().encode(newValue) {
                defaults.set(data, forKey: HistoryKey.filteredStats)
                defaults.set(newValue.total, forKey: HistoryKey.filteredTotalCount)
                defaults.synchronize()
            }
        }
    }

    func recordFiltered(sender: String, reason: String, detail: String, preview: String) {
        var list = filteredMessages
        var stats = filteredStats
        let normalizedPreview = preview.trimmingCharacters(in: .whitespacesAndNewlines)
        if let last = list.last,
           last.sender == sender,
           last.reason == reason,
           last.preview == normalizedPreview,
           Date().timeIntervalSince(last.date) < 300 {
            return
        }
        list.append(FilteredMessage(sender: sender, reason: reason, detail: detail, preview: normalizedPreview, date: Date()))
        filteredMessages = list

        stats.record(reason: reason, date: Date())
        filteredStats = stats
    }

    var filterInvocationCount: Int {
        defaults.integer(forKey: HistoryKey.filterInvocationCount)
    }

    var lastFilterCheckDate: Date? {
        let timestamp = defaults.double(forKey: HistoryKey.lastFilterCheckDate)
        return timestamp > 0 ? Date(timeIntervalSince1970: timestamp) : nil
    }

    var lastFilterCheckResult: String {
        defaults.string(forKey: HistoryKey.lastFilterCheckResult) ?? "Henüz kontrol yok"
    }

    func recordFilterInvocation(sender: String?, decision: FilterDecision) {
        let result = decision.shouldBlock ? "Engellendi: \(decision.reason)" : "Geçti"
        defaults.set(filterInvocationCount + 1, forKey: HistoryKey.filterInvocationCount)
        defaults.set(Date().timeIntervalSince1970, forKey: HistoryKey.lastFilterCheckDate)
        defaults.set(result, forKey: HistoryKey.lastFilterCheckResult)
        defaults.synchronize()
    }

    struct SpamReport: Codable, Identifiable {
        let id: UUID
        let sender: String
        let message: String
        let note: String
        let date: Date
    }

    var spamReports: [SpamReport] {
        get {
            guard let data = defaults.data(forKey: HistoryKey.spamReports),
                  let decoded = try? JSONDecoder().decode([SpamReport].self, from: data)
            else { return [] }
            return decoded
        }
        set {
            let trimmed = Array(newValue.suffix(100))
            if let data = try? JSONEncoder().encode(trimmed) {
                defaults.set(data, forKey: HistoryKey.spamReports)
            }
        }
    }

    func addSpamReport(sender: String, message: String, note: String) {
        var reports = spamReports
        reports.append(
            SpamReport(
                id: UUID(),
                sender: sender.trimmingCharacters(in: .whitespacesAndNewlines),
                message: message.trimmingCharacters(in: .whitespacesAndNewlines),
                note: note.trimmingCharacters(in: .whitespacesAndNewlines),
                date: Date()
            )
        )
        spamReports = reports
    }

    // MARK: - Helpers

    private func normalize(_ number: String) -> String {
        var s = number.filter { $0.isNumber || $0 == "+" }
        if s.hasPrefix("0") { s = "+90" + s.dropFirst() }
        return s
    }

    private func normalizeSender(_ sender: String) -> String {
        Self.normalizeText(sender)
            .replacingOccurrences(of: " ", with: "")
            .uppercased()
    }

    private static func normalizeText(_ text: String) -> String {
        text
            .folding(options: [.caseInsensitive, .diacriticInsensitive], locale: Locale(identifier: "tr_TR"))
            .trimmingCharacters(in: .whitespacesAndNewlines)
    }

    private func keywordEvaluation(in body: String) -> (score: Int, keyword: String?) {
        let normalized = Self.normalizeText(body)
        var total = 0
        var matched: String?

        for keyword in Self.scoredKeywords {
            guard Self.matchesKeyword(normalized, keyword: keyword.text) else { continue }
            total += keyword.score
            if matched == nil {
                matched = keyword.text
            }
        }

        return (total, matched)
    }

    private func matchedSafeKeyword(in body: String) -> String? {
        let normalized = Self.normalizeText(body)
        return safeKeywords.sorted().first { keyword in
            Self.matchesKeyword(normalized, keyword: keyword)
        }
    }

    private func suspiciousLinkEvaluation(in body: String) -> (score: Int, host: String?) {
        let hosts = Self.extractHosts(from: body)
        var total = 0
        var matched: String?

        for host in hosts {
            let hostScore = Self.linkScore(for: host)
            guard hostScore > 0 else { continue }
            total += hostScore
            if matched == nil {
                matched = host
            }
        }

        return (total, matched)
    }

    private static func extractHosts(from body: String) -> [String] {
        let normalized = normalizeText(body)
        let pattern = #"(?i)\b(?:https?://)?(?:www\.)?([a-z0-9-]+(?:\.[a-z0-9-]+)+)\b"#
        guard let regex = try? NSRegularExpression(pattern: pattern) else { return [] }
        let range = NSRange(normalized.startIndex..<normalized.endIndex, in: normalized)
        return regex.matches(in: normalized, range: range).compactMap { match in
            guard let hostRange = Range(match.range(at: 1), in: normalized) else { return nil }
            return String(normalized[hostRange]).trimmingCharacters(in: CharacterSet(charactersIn: ".,;:!?)("))
        }
    }

    private static func linkScore(for host: String) -> Int {
        linkRiskDetails(for: host).score
    }

    private static func linkRiskDetails(for host: String) -> (score: Int, reasons: [String]) {
        let registeredDomain = registrableDomain(for: host)
        if trustedBrandDomains.contains(registeredDomain) {
            return (0, ["\(registeredDomain) güvenilir domain listesinde."])
        }

        let parts = host.split(separator: ".").map(String.init)
        let tld = parts.last ?? ""
        var score = 0
        var reasons: [String] = []

        if shortLinkHosts.contains(host) {
            score += 4
            reasons.append("\(host) kısa link servisi olarak işaretlendi.")
        }
        if riskyLinkTLDs.contains(tld) {
            score += 3
            reasons.append(".\(tld) uzantısı riskli link uzantıları arasında.")
        }
        if let hint = riskyLinkHints.first(where: { host.contains($0) }) {
            score += 3
            reasons.append("Domain içinde \"\(hint)\" risk sinyali var.")
        }
        if let spoofedDomain = spoofedTrustedDomain(for: registeredDomain) {
            score += 6
            reasons.append("\(registeredDomain), \(spoofedDomain) adresine benziyor; marka taklidi olabilir.")
        }

        return (min(score, 6), reasons)
    }

    private static func spoofedTrustedDomain(for domain: String) -> String? {
        for trustedDomain in trustedBrandDomains where domain != trustedDomain {
            let trustedBrand = trustedDomain.split(separator: ".").first.map(String.init) ?? trustedDomain
            let domainBrand = domain.split(separator: ".").first.map(String.init) ?? domain
            if domain.contains(trustedBrand) || editDistance(domainBrand, trustedBrand) <= 1 {
                return trustedDomain
            }
        }
        return nil
    }

    private static func registrableDomain(for host: String) -> String {
        let parts = host.split(separator: ".").map(String.init)
        guard parts.count >= 2 else { return host }
        let twoLevelTR = ["com", "gov", "net", "org", "edu", "bel", "k12"]
        if parts.count >= 3, parts.last == "tr", twoLevelTR.contains(parts[parts.count - 2]) {
            return parts.suffix(3).joined(separator: ".")
        }
        return parts.suffix(2).joined(separator: ".")
    }

    private static func editDistance(_ lhs: String, _ rhs: String) -> Int {
        let left = Array(lhs)
        let right = Array(rhs)
        var previous = Array(0...right.count)

        for (i, leftCharacter) in left.enumerated() {
            var current = [i + 1]
            for (j, rightCharacter) in right.enumerated() {
                let cost = leftCharacter == rightCharacter ? 0 : 1
                current.append(min(previous[j + 1] + 1, current[j] + 1, previous[j] + cost))
            }
            previous = current
        }

        return previous.last ?? max(lhs.count, rhs.count)
    }

    private static func matchesKeyword(_ text: String, keyword: String) -> Bool {
        if exactTokenKeywords.contains(keyword) {
            return matchesExactToken(text, keyword: keyword)
        }
        return matchesAtWordStart(text, keyword: keyword)
    }

    private static func matchesExactToken(_ text: String, keyword: String) -> Bool {
        var searchRange = text.startIndex..<text.endIndex
        while let found = text.range(of: keyword, options: .literal, range: searchRange) {
            let isLeftBoundary = found.lowerBound == text.startIndex || !text[text.index(before: found.lowerBound)].isLetter
            let isRightBoundary = found.upperBound == text.endIndex || !text[found.upperBound].isLetter
            if isLeftBoundary && isRightBoundary {
                return true
            }
            guard found.upperBound < text.endIndex else { break }
            searchRange = found.upperBound..<text.endIndex
        }
        return false
    }

    private func isSuspiciousAlphanumeric(_ sender: String) -> Bool {
        let normalized = Self.normalizeText(sender)
        let hasLetter = normalized.contains { $0.isLetter }
        let hasDigit = normalized.contains { $0.isNumber }
        return hasLetter && (!hasDigit || Self.senderKeywordHints.contains(where: { normalized.contains($0) }))
    }

    private func matchesSenderRule(_ sender: String, rules: Set<String>) -> Bool {
        let normalizedSender = normalizeSender(sender)
        return rules.contains { rule in
            !rule.isEmpty && normalizedSender.contains(rule)
        }
    }
}
