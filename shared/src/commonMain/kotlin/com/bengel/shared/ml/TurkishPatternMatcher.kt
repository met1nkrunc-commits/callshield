package com.bengel.shared.ml

import com.bengel.shared.core.TurkishFraudConstants
import com.bengel.shared.domain.model.RiskLevel

class TurkishPatternMatcher {

    private val btkCodeRegex = Regex("""B\d{3,4}""")

    // Weight 3: single match is conclusive
    private val HIGH_WEIGHT_BETTING = setOf(
        "freespin", "free spin", "free bet", "freebet",
        "deneme bonusu", "cevrimsiz bonus", "yatirimsiz bonus",
        "vaycasino", "bets10", "superbahis", "mobilbahis",
        "1xbet", "bet365", "nakitbahis", "betturkey",
        "bahsegel", "mariobet", "tipobet", "sultanbet",
        "betboo", "tempobet", "youwin", "betpas",
        "casinometropol", "casinomaxi", "retrobet",
        "goldenbahis", "royalbet", "favoribahis", "betgaranti",
        "vegabet", "betnano", "elexbet", "anadolucasino",
        "onwin", "fixbet", "sekabet", "jojobet", "perabet",
        "pusulabet", "sahabet", "matadorbet", "jetbahis",
        "imajbet", "padisahbet", "betpark", "artemisbet",
        "glorybahis", "betsat", "piabella", "betvole",
        "bahisnow", "casinoplus", "diskontcasino",
    )

    // Weight 2: two matches likely confirms
    private val MED_WEIGHT_BETTING = setOf(
        "casino", "bahis", "kumar", "bonus", "slot", "rulet",
        "bet", "betting", "odds", "stake", "jackpot",
        "para kazan", "kazanc", "giris yap",
        "uye ol", "kayit ol", "yatirim", "oyna", "iddaa",
        "freespin", "cashout", "cash out",
        "surebet", "sure bet", "arb bet", "parlay",
        "winning", "wager", "wagering",
        "ganyan", "piyango", "tombala", "bingo",
    )

    // ── Locale-safe normalization ────────────────────────────────────────────
    // Bug: Kotlin commonMain String.lowercase() uses ROOT locale where:
    //   'İ' (U+0130) → "i\u0307" (combining dot, NOT "i")  ← breaks "İcra"
    //   'ı' (U+0131) → "ı"  (stays dotless, NOT "i")       ← breaks "ıcra"
    // Fix: collapse ALL Turkish I-variants → 'i' BEFORE .lowercase().
    private fun String.normalizeForMatching(): String {
        return this
            .replace('\u0130', 'i')  // İ → i  (capital dotted I)
            .replace('\u0131', 'i')  // ı → i  (dotless i)
            .lowercase()             // handles remaining uppercase
            .replace('0', 'o')
            .replace('1', 'i')
            .replace('3', 'e')
            .replace('4', 'a')
            .replace('@', 'a')
            .replace(Regex("-(?=[a-záéíóúüöçşğ])"), "")  // b-a-h-i-s → bahis
            .replace(Regex("(.)\\1{2,}"), "$1$1")         // bonuuus → bonuus
            .trim()
    }
    // ────────────────────────────────────────────────────────────────────────

    fun analyze(text: String, sender: String): PatternMatchResult {
        if (TurkishFraudConstants.TRUSTED_SENDER_IDS.any { it.equals(sender, ignoreCase = true) }) {
            return PatternMatchResult(
                riskLevel       = RiskLevel.SAFE,
                matchedPatterns = emptyList(),
                hasBtkCode      = false,
                hasPhishingUrl  = false,
            )
        }

        val normalized = text.normalizeForMatching()
        val matchedPatterns = mutableListOf<String>()

        val btkMatch = btkCodeRegex.find(text)
        val hasBtkCode = btkMatch != null
        if (btkMatch != null) matchedPatterns.add(btkMatch.value)

        val bettingMatches    = TurkishFraudConstants.BETTING_KEYWORDS
            .filter { normalized.contains(it.normalizeForMatching()) }
        val legalMatches      = TurkishFraudConstants.LEGAL_THREAT_KEYWORDS
            .filter { normalized.contains(it.normalizeForMatching()) }
        val phishingMatches   = TurkishFraudConstants.PHISHING_KEYWORDS
            .filter { normalized.contains(it.normalizeForMatching()) }
        val socialMatches     = TurkishFraudConstants.SOCIAL_ENGINEERING_KEYWORDS
            .filter { normalized.contains(it.normalizeForMatching()) }
        val investmentMatches = TurkishFraudConstants.INVESTMENT_SCAM_KEYWORDS
            .filter { normalized.contains(it.normalizeForMatching()) }

        matchedPatterns.addAll(bettingMatches)
        matchedPatterns.addAll(legalMatches)
        matchedPatterns.addAll(phishingMatches)
        matchedPatterns.addAll(socialMatches)
        matchedPatterns.addAll(investmentMatches)

        val hasPhishingUrl =
            TurkishFraudConstants.SHORT_URL_PATTERNS.any { it.containsMatchIn(normalized) } ||
            TurkishFraudConstants.SUSPICIOUS_DOMAIN_PATTERNS.any { it.containsMatchIn(normalized) }
        if (hasPhishingUrl) matchedPatterns.add("suspicious_url")

        val senderIsPhoneNumber = sender.startsWith("+") || sender.all { it.isDigit() }

        val bettingScore: Int = bettingMatches.fold(0) { acc, keyword ->
            val n = keyword.normalizeForMatching()
            acc + when {
                HIGH_WEIGHT_BETTING.any { n.contains(it.normalizeForMatching()) } -> 3
                MED_WEIGHT_BETTING.any  { n.contains(it.normalizeForMatching()) } -> 2
                else -> 1
            }
        }

        val investmentScore: Int = investmentMatches.fold(0) { acc, kw ->
            acc + when {
                kw.contains("garantili") || kw.contains("sinyal") -> 3
                else -> 2
            }
        }

        val urlAlone = hasPhishingUrl &&
                bettingMatches.isEmpty() && legalMatches.isEmpty() &&
                phishingMatches.isEmpty() && socialMatches.isEmpty() &&
                investmentMatches.isEmpty() && !hasBtkCode

        val riskLevel = when {
            hasBtkCode                                                             -> RiskLevel.BLOCKED
            senderIsPhoneNumber && phishingMatches.isNotEmpty()                   -> RiskLevel.BLOCKED
            bettingScore >= 6                                                      -> RiskLevel.BLOCKED
            investmentScore >= 6                                                   -> RiskLevel.BLOCKED
            legalMatches.isNotEmpty() || phishingMatches.isNotEmpty() || urlAlone -> RiskLevel.HIGH
            hasPhishingUrl                                                         -> RiskLevel.HIGH
            bettingScore >= 3 || investmentScore >= 3                             -> RiskLevel.HIGH
            bettingScore >= 2 || socialMatches.isNotEmpty()
                    || investmentScore >= 2                                        -> RiskLevel.MEDIUM
            bettingScore >= 1 || investmentScore >= 1                             -> RiskLevel.LOW
            matchedPatterns.isNotEmpty()                                          -> RiskLevel.LOW
            else                                                                   -> RiskLevel.SAFE
        }

        return PatternMatchResult(
            riskLevel       = riskLevel,
            matchedPatterns = matchedPatterns,
            hasBtkCode      = hasBtkCode,
            hasPhishingUrl  = hasPhishingUrl,
        )
    }

    fun detectCategory(matchedPatterns: List<String>, hasPhishingUrl: Boolean): String {
        val set = matchedPatterns.toSet()
        return when {
            set.any { TurkishFraudConstants.PHISHING_KEYWORDS.contains(it) }
                || hasPhishingUrl || set.contains("suspicious_url")              -> "PHISHING"
            set.any { TurkishFraudConstants.LEGAL_THREAT_KEYWORDS.contains(it) } -> "LEGAL"
            set.any { TurkishFraudConstants.INVESTMENT_SCAM_KEYWORDS.contains(it) } -> "INVESTMENT"
            set.any { TurkishFraudConstants.BETTING_KEYWORDS.contains(it) }      -> "BETTING"
            set.any { TurkishFraudConstants.SOCIAL_ENGINEERING_KEYWORDS.contains(it) } -> "SOCIAL"
            else                                                                  -> "UNKNOWN"
        }
    }
}

data class PatternMatchResult(
    val riskLevel: RiskLevel,
    val matchedPatterns: List<String>,
    val hasBtkCode: Boolean,
    val hasPhishingUrl: Boolean,
)
