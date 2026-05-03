package com.callshield.app.ml.fraud

import android.content.Context
import com.callshield.app.domain.model.RiskLevel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fix #14: TFLite tabanlı metin sınıflandırıcı stub.
 * ARCHITECTURE.md'de tanımlı ama implement edilmemiş.
 *
 * Tam implementasyon için:
 * 1. `app/src/main/assets/models/fraud_classifier.tflite` dosyasını ekle.
 * 2. TFLite Interpreter'ı başlat.
 * 3. Türkçe tokenizasyon ve skorlama mantığını yaz.
 */
@Singleton
class FraudTextClassifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    data class ClassificationResult(
        val score: Float,       // 0.0 = safe, 1.0 = fraud
        val riskLevel: RiskLevel,
    )

    private val modelLoaded: Boolean by lazy {
        // Model dosyası yoksa sessizce degraded modda çalış.
        context.assets.list("models")?.contains("fraud_classifier.tflite") == true
    }

    /**
     * Metni sınıflandır. Model yoksa SAFE döner (fail-open).
     * TurkishPatternMatcher zaten çalıştırılmış olduğundan bu ek bir ML katmanıdır.
     */
    fun classify(text: String): ClassificationResult {
        if (!modelLoaded) return ClassificationResult(score = 0f, riskLevel = RiskLevel.SAFE)
        // TODO: TFLite inference buraya gelecek.
        return ClassificationResult(score = 0f, riskLevel = RiskLevel.SAFE)
    }
}
