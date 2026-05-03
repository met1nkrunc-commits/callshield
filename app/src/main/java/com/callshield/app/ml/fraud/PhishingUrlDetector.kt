package com.callshield.app.ml.fraud

import com.callshield.app.domain.model.RiskLevel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fix #14: PhishingUrlDetector stub.
 * URL pattern eşleşmesini TurkishPatternMatcher'dan bağımsız olarak değerlendirir.
 * TFLite ile gelişmiş URL skor modeli entegrasyonu için yer tutucu.
 */
@Singleton
class PhishingUrlDetector @Inject constructor() {

    data class UrlRiskResult(
        val isPhishing: Boolean,
        val score: Float,
        val riskLevel: RiskLevel,
    )

    private val shortUrlDomains = setOf(
        "bit.ly", "tinyurl.com", "t.co", "goo.gl", "ow.ly",
        "buff.ly", "cutt.ly", "rebrand.ly", "tiny.cc",
        // Türkiye'ye özgü kısaltıcılar
        "ksa.to", "dln.io",
    )

    private val suspiciousTLDs = setOf(".xyz", ".top", ".click", ".loan", ".work", ".gq", ".tk", ".ml", ".cf")

    fun detect(url: String): UrlRiskResult {
        val lower = url.lowercase()
        val isShortUrl      = shortUrlDomains.any { lower.contains(it) }
        val hasSuspiciousTld = suspiciousTLDs.any { lower.contains(it) }
        val hasIpAddress    = Regex("""https?://\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}""").containsMatchIn(lower)
        val hasMimicDomain  = Regex("""e-devlet|banka|guvenli|hesap|giris|login|verify""").containsMatchIn(lower)

        val score = listOf(isShortUrl, hasSuspiciousTld, hasIpAddress, hasMimicDomain)
            .count { it }.toFloat() / 4f

        val riskLevel = when {
            hasIpAddress || hasMimicDomain -> RiskLevel.HIGH
            hasSuspiciousTld               -> RiskLevel.MEDIUM
            isShortUrl                     -> RiskLevel.LOW
            else                           -> RiskLevel.SAFE
        }

        return UrlRiskResult(
            isPhishing = score >= 0.5f,
            score      = score,
            riskLevel  = riskLevel,
        )
    }
}
