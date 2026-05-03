package com.callshield.app.domain.usecase.sms

import com.bengel.shared.ml.TurkishPatternMatcher
import com.callshield.app.domain.model.RiskLevel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AnalyzeSmsContentUseCaseTest {

    private lateinit var useCase: AnalyzeSmsContentUseCase

    @Before
    fun setUp() {
        useCase = AnalyzeSmsContentUseCase(TurkishPatternMatcher())
    }

    @Test
    fun `phishing link in SMS returns HIGH or BLOCKED risk`() {
        val result = useCase("12345", "Hesabınız askıya alındı. Hemen giriş yapın: http://bankafake.xyz/giris")
        assertTrue(result.riskLevel == RiskLevel.HIGH || result.riskLevel == RiskLevel.BLOCKED)
    }

    @Test
    fun `normal SMS returns SAFE or LOW risk`() {
        val result = useCase("0532XXXXXXX", "Merhaba, yarın buluşalım mı?")
        assertTrue(result.riskLevel == RiskLevel.SAFE || result.riskLevel == RiskLevel.LOW)
    }

    @Test
    fun `lottery scam SMS returns HIGH risk`() {
        val result = useCase("0850XXXXXXX", "Tebrikler! 50.000 TL kazandınız. Ödülünüzü almak için hemen tıklayın.")
        assertTrue(result.riskLevel != RiskLevel.SAFE)
    }
}
