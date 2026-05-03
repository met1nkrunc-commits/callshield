# CallShield — Claude Code Talimatları

## Rolün
Sen bu projede Senior Android Developer'sın. Her adımda:
- Modern Kotlin idiom'larını kullan (data class, sealed class, extension functions)
- Coroutines + Flow — hiçbir zaman callback veya RxJava kullanma
- Hilt ile DI — manual instantiation yok
- Clean Architecture katman kurallarına kesinlikle uy
- Her dosyayı yazmadan önce ARCHITECTURE.md'yi referans al

---

## Altın Kurallar

1. **Onay olmadan bir sonraki adıma geçme.** Her adım biter, bekle.
2. **Tek seferde tüm kodu yazma.** Adım adım ilerle.
3. **TODO veya placeholder bırakma.** Her yazdığın kod çalışır olmalı.
4. **Her yeni dosya için önce** hangi katmanda, hangi amaca hizmet ettiğini 1 satırda açıkla.
5. **Türkçe yorum yok, İngilizce yorum yok** — kod kendini açıklamalı. Yalnızca karmaşık iş mantığını kısa İngilizce yorum ile açıkla.

---

## Adım Adım Geliştirme Planı

### ADIM 1 — Proje Kurulumu
**Dosyalar:**
- `gradle/libs.versions.toml`
- `build.gradle.kts` (root)
- `app/build.gradle.kts`
- `settings.gradle.kts`

**Kontrol:** `./gradlew assembleDebug` hatasız geçmeli.

---

### ADIM 2 — Domain Modelleri
**Dosyalar:**
- `domain/model/RiskLevel.kt`
- `domain/model/AnalysisResult.kt`
- `domain/model/BlockedNumber.kt`
- `domain/model/SmsLog.kt`

**Kural:** Bu katmanda sıfır Android import. Saf Kotlin.

---

### ADIM 3 — Room Veritabanı
**Dosyalar:**
- `data/local/db/entity/BlockedNumberEntity.kt`
- `data/local/db/entity/SmsLogEntity.kt`
- `data/local/db/entity/CallLogEntity.kt`
- `data/local/db/dao/BlockedNumberDao.kt`
- `data/local/db/dao/SmsLogDao.kt`
- `data/local/db/dao/CallLogDao.kt`
- `data/local/db/CallShieldDatabase.kt`

**Kural:** DAO'lar Flow döndürmeli. suspend fun için `@Transaction` kullan.

---

### ADIM 4 — Repository Katmanı
**Dosyalar:**
- `domain/repository/BlockedNumberRepository.kt` (interface)
- `domain/repository/SmsAnalysisRepository.kt` (interface)
- `domain/repository/CallAnalysisRepository.kt` (interface)
- `data/repository/BlockedNumberRepositoryImpl.kt`
- `data/repository/SmsAnalysisRepositoryImpl.kt`
- `data/repository/CallAnalysisRepositoryImpl.kt`

---

### ADIM 5 — Türkiye'ye Özel ML Motoru
**Dosyalar:**
- `core/util/Constants.kt` (BTK kodları + TR pattern listesi)
- `ml/fraud/TurkishPatternMatcher.kt`
- `domain/usecase/sms/DetectBtkCodeUseCase.kt`
- `domain/usecase/sms/AnalyzeSmsContentUseCase.kt`

**Bu adım kritik:** Türkçe kalıplar şunları kapsamalı:
- Bahis/kumar kelimeleri
- Dava/icra/avukat tehditleri
- BTK B-kodları (B214, B217, B250 vb.)
- Sosyal mühendislik kalıpları ("son gün", "hesabınız askıya")
- Phishing URL pattern'leri (bit.ly, kısaltılmış linkler, sahte domain'ler)

---

### ADIM 6 — TFLite Fraud Classifier
**Dosyalar:**
- `ml/fraud/FraudTextClassifier.kt`
- `ml/fraud/PhishingUrlDetector.kt`

**Not:** TFLite model assets klasörüne eklenecek.
`app/src/main/assets/models/fraud_classifier.tflite`

---

### ADIM 7 — SMS Servisi
**Dosyalar:**
- `service/SmsReceiver.kt`
- `domain/usecase/sms/CheckPhishingUrlUseCase.kt`

**Kural:** BroadcastReceiver içinde async iş yapma — hemen bir coroutine scope'a devret.

---

### ADIM 8 — Çağrı Tarama Servisi
**Dosyalar:**
- `domain/usecase/call/CheckNumberUseCase.kt`
- `service/CallScreeningServiceImpl.kt`

**Kural:** `CallScreeningService.respondToCall()` max 5 saniye içinde çağrılmalı.

---

### ADIM 9 — Ses Analizi (Whisper + CallMonitor)
**Dosyalar:**
- `ml/whisper/WhisperConfig.kt`
- `ml/whisper/WhisperEngine.kt`
- `service/AudioCaptureManager.kt`
- `domain/usecase/call/AnalyzeCallAudioUseCase.kt`
- `service/CallMonitorService.kt`

**Kural:** Whisper.cpp JNI bağlaması — `libwhisper.so` ARM64 için build edilmiş olmalı.

---

### ADIM 10 — Overlay Sistemi
**Dosyalar:**
- `service/OverlayManager.kt`
- `ui/overlay/FraudAlertOverlay.kt`

**Kural:** Compose overlay WindowManager üzerinden açılacak. Service lifecycle'ına bağlı.

---

### ADIM 11 — Numara Güncelleme Worker
**Dosyalar:**
- `data/remote/api/FraudDataApi.kt`
- `data/remote/dto/FraudNumberDto.kt`
- `service/NumberUpdateWorker.kt`
- `core/di/NetworkModule.kt`

**Kural:** Sadece WiFi bağlantısında çalışsın. Başarısız olursa local liste geçerli kalır.

---

### ADIM 12 — UI Ekranları
**Sıra:** Home → History → Blocklist → Settings → Onboarding

Her ekran için:
- Screen (Composable)
- ViewModel (HiltViewModel)
- UiState (sealed class)

---

### ADIM 13 — Hilt Modülleri + Manifest
**Dosyalar:**
- `core/di/AppModule.kt`
- `core/di/DatabaseModule.kt`
- `core/di/NetworkModule.kt`
- `core/di/MlModule.kt`
- `AndroidManifest.xml`

---

## Her Adım Sonrası Kontrol Listesi
- [ ] `./gradlew assembleDebug` hatasız geçiyor mu?
- [ ] Katman ihlali var mı? (UI → UseCase direkt mi?)
- [ ] Memory leak riski? (Service'lerde lifecycle dikkat)
- [ ] Null safety tam mı?
- [ ] Flow'lar düzgün cancel ediliyor mu?

---

## Başlamak İçin İlk Komut
```
Read ARCHITECTURE.md completely, then start with ADIM 1.
Create libs.versions.toml and build.gradle.kts files.
Do not proceed to ADIM 2 until I confirm.
```
