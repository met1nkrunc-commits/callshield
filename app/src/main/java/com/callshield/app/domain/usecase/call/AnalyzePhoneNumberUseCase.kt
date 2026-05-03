package com.callshield.app.domain.usecase.call

import com.callshield.app.domain.model.RiskLevel
import javax.inject.Inject

data class PrefixResult(
    val riskLevel: RiskLevel,
    val reason: String,
    val lineType: String,
)

/**
 * Türk telefon numaralarının ön eki / hat tipine göre risk skoru atar.
 * Numara listesine gerek duymadan anlık karar verir.
 */
class AnalyzePhoneNumberUseCase @Inject constructor() {

    // Scam kampanyalarında sık görülen yabancı ülke kodları
    private val highRiskCountryCodes = setOf(
        "355", // Arnavutluk — sahte yatırım
        "225", // Fildişi Sahili
        "234", // Nijerya
        "216",  // Tunus
        "212",  // Fas
        "963",  // Suriye
        "964",  // Irak
        "967",  // Yemen
        "380",  // Ukrayna (sahte piyango)
        "373",  // Moldova
        "375",  // Belarus
    )

    // Genellikle meşru ama zaman zaman dolandırıcılık için kullanılan yabancı kodlar
    private val mediumRiskCountryCodes = setOf(
        "44",  // İngiltere — sahte fintech/kripto
        "49",  // Almanya
        "31",  // Hollanda — sahte iş teklifleri
        "32",  // Belçika
        "33",  // Fransa
        "39",  // İtalya
        "7",   // Rusya
        "995", // Gürcistan
        "994", // Azerbaycan
        "996", // Kırgızistan
    )

    operator fun invoke(rawNumber: String): PrefixResult? {
        val n = rawNumber.replace(Regex("[\\s\\-().+]"), "")

        // +90 → 0, 90 → 0 normalizasyonu
        val normalized = when {
            n.startsWith("0090") -> "0" + n.drop(4)
            n.startsWith("90") && n.length == 12 -> "0" + n.drop(2)
            n.startsWith("0") -> n
            else -> n
        }

        return when {
            // ── Premium/ücretli hatlar ──────────────────────────────────────
            normalized.startsWith("0900") ->
                PrefixResult(RiskLevel.HIGH, "0900 premium hat — dolandırıcılık riski yüksek", "PREMIUM")

            normalized.startsWith("0888") ->
                PrefixResult(RiskLevel.MEDIUM, "0888 bilgi hattı", "PREMIUM")

            normalized.startsWith("0870") ->
                PrefixResult(RiskLevel.HIGH, "0870 ücretli hat", "PREMIUM")

            normalized.startsWith("0877") || normalized.startsWith("0878") ->
                PrefixResult(RiskLevel.HIGH, "Ücretli özel hat", "PREMIUM")

            // ── Çağrı merkezi / VoIP hatları ───────────────────────────────
            normalized.startsWith("0850") ->
                PrefixResult(RiskLevel.MEDIUM, "0850 çağrı merkezi hattı", "CALL_CENTER")

            normalized.startsWith("0444") ->
                PrefixResult(RiskLevel.LOW, "0444 çağrı merkezi (şehiriçi)", "CALL_CENTER")

            // 08xx kurumsal/servis hatları
            Regex("^08[0-9]{2}").containsMatchIn(normalized) &&
                    !normalized.startsWith("0850") && !normalized.startsWith("0888") ->
                PrefixResult(RiskLevel.LOW, "Kurumsal/servis hattı", "VOIP")

            // ── Ücretsiz hatlar ────────────────────────────────────────────
            normalized.startsWith("0800") ->
                PrefixResult(RiskLevel.LOW, "0800 ücretsiz hat", "TOLL_FREE")

            // ── Kısa kodlar ────────────────────────────────────────────────
            normalized.length in 1..3 ->
                PrefixResult(RiskLevel.LOW, "Operatör kısa kodu", "SHORT_CODE")

            normalized.length in 4..6 ->
                PrefixResult(RiskLevel.MEDIUM, "Kısa/gizli numara", "UNKNOWN")

            // ── Yurt dışı — "00" veya doğrudan ülke kodu ile başlayan ─────
            normalized.startsWith("00") -> {
                val withoutZeros = normalized.drop(2)
                when {
                    highRiskCountryCodes.any { withoutZeros.startsWith(it) } ->
                        PrefixResult(RiskLevel.HIGH, "Dolandırıcılık riski yüksek ülkeden arama", "INTERNATIONAL")
                    mediumRiskCountryCodes.any { withoutZeros.startsWith(it) } ->
                        PrefixResult(RiskLevel.MEDIUM, "Yurt dışı kaynaklı arama", "INTERNATIONAL")
                    else ->
                        PrefixResult(RiskLevel.LOW, "Yurt dışı kaynaklı arama", "INTERNATIONAL")
                }
            }

            // Yurt dışı direkt (0 olmadan, ülke koduyla başlayan uzun numara)
            normalized.length > 11 && !normalized.startsWith("0") -> {
                when {
                    highRiskCountryCodes.any { normalized.startsWith(it) } ->
                        PrefixResult(RiskLevel.HIGH, "Dolandırıcılık riski yüksek ülkeden arama", "INTERNATIONAL")
                    mediumRiskCountryCodes.any { normalized.startsWith(it) } ->
                        PrefixResult(RiskLevel.MEDIUM, "Yurt dışı kaynaklı arama", "INTERNATIONAL")
                    else -> null
                }
            }

            // ── Robocall / tekrarlayan rakam tespiti (#10) ─────────────────
            isRobocallPattern(normalized) ->
                PrefixResult(RiskLevel.MEDIUM, "Otomatik arama (robocall) şüphesi", "ROBOCALL")

            else -> null // Normal TR cep/sabit hat
        }
    }

    /**
     * Robocall kampanyalarında sıkça görülen numara kalıpları:
     * - Tüm rakamlar aynı: 05551111111
     * - Son 6 rakam sıralı artan/azalan: 0532123456
     * - Çağrı merkezi otomatik dialer kalıpları
     */
    private fun isRobocallPattern(normalized: String): Boolean {
        if (normalized.length < 10) return false
        val digits = normalized.filter { it.isDigit() }
        if (digits.length < 7) return false
        val tail = digits.takeLast(7)
        // Tüm son 7 rakam aynı: 1111111, 0000000
        if (tail.all { it == tail[0] }) return true
        // Son 6 rakam sıralı artan: 1234567, 2345678
        val isAscending  = tail.zipWithNext().all { (a, b) -> b.digitToInt() == a.digitToInt() + 1 }
        val isDescending = tail.zipWithNext().all { (a, b) -> b.digitToInt() == a.digitToInt() - 1 }
        return isAscending || isDescending
    }
}
