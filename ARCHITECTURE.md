# CallShield — Mimari Referans Belgesi

## Proje Kimliği
- **Uygulama Adı:** CallShield
- **Amaç:** Türkiye'ye özel bahis, dolandırıcılık ve spam içerikli SMS/arama engelleme
- **Platform:** Android (Kotlin)
- **Min SDK:** 29 (Android 10)
- **Target SDK:** 34
- **Paket Adı:** com.callshield.app

---

## Tech Stack

| Katman | Teknoloji |
|--------|-----------|
| Dil | Kotlin 2.0.20 |
| UI | Jetpack Compose + Material3 |
| DI | Hilt 2.52 |
| Veritabanı | Room 2.6.1 |
| Async | Coroutines + Flow |
| Network | Retrofit 2.11.0 + OkHttp 4.12.0 |
| ML | TFLite 0.4.4 (metin + ses) |
| STT | Whisper.cpp (JNI, on-device) |
| Background | WorkManager 2.9.1 + ForegroundService |
| Ayarlar | DataStore Preferences |
| Build | Gradle 8.5.2 + libs.versions.toml |

---

## Klasör Yapısı

```
app/src/main/java/com/callshield/
│
├── core/
│   ├── di/
│   │   ├── AppModule.kt
│   │   ├── DatabaseModule.kt
│   │   ├── NetworkModule.kt
│   │   └── MlModule.kt
│   ├── util/
│   │   ├── Extensions.kt
│   │   ├── Constants.kt          ← BTK kodları, TR bahis pattern'leri
│   │   └── PhoneNumberUtils.kt
│   └── base/
│       └── BaseViewModel.kt
│
├── data/
│   ├── local/
│   │   ├── db/
│   │   │   ├── CallShieldDatabase.kt
│   │   │   ├── dao/
│   │   │   │   ├── BlockedNumberDao.kt
│   │   │   │   ├── SmsLogDao.kt
│   │   │   │   └── CallLogDao.kt
│   │   │   └── entity/
│   │   │       ├── BlockedNumberEntity.kt
│   │   │       ├── SmsLogEntity.kt
│   │   │       └── CallLogEntity.kt
│   │   └── datastore/
│   │       └── SettingsDataStore.kt
│   ├── remote/
│   │   ├── api/
│   │   │   └── FraudDataApi.kt   ← GitHub raw JSON çeker
│   │   └── dto/
│   │       └── FraudNumberDto.kt
│   └── repository/
│       ├── BlockedNumberRepositoryImpl.kt
│       ├── SmsAnalysisRepositoryImpl.kt
│       └── CallAnalysisRepositoryImpl.kt
│
├── domain/
│   ├── model/
│   │   ├── RiskLevel.kt          ← SAFE / LOW / MEDIUM / HIGH / BLOCKED
│   │   ├── AnalysisResult.kt
│   │   ├── BlockedNumber.kt
│   │   └── SmsLog.kt
│   ├── repository/
│   │   ├── BlockedNumberRepository.kt
│   │   ├── SmsAnalysisRepository.kt
│   │   └── CallAnalysisRepository.kt
│   └── usecase/
│       ├── call/
│       │   ├── CheckNumberUseCase.kt
│       │   └── AnalyzeCallAudioUseCase.kt
│       └── sms/
│           ├── AnalyzeSmsContentUseCase.kt
│           ├── CheckPhishingUrlUseCase.kt
│           └── DetectBtkCodeUseCase.kt   ← TR'ye özel
│
├── ml/
│   ├── whisper/
│   │   ├── WhisperEngine.kt
│   │   └── WhisperConfig.kt
│   ├── fraud/
│   │   ├── FraudTextClassifier.kt    ← TFLite NLP (Türkçe)
│   │   ├── TurkishPatternMatcher.kt  ← Regex: bahis, dava, icra, SMS kalıpları
│   │   └── PhishingUrlDetector.kt
│   └── audio/
│       └── AudioFraudDetector.kt
│
├── service/
│   ├── CallMonitorService.kt         ← ForegroundService, orkestratör
│   ├── CallScreeningServiceImpl.kt   ← API 29+ native çağrı filtresi
│   ├── SmsReceiver.kt                ← BroadcastReceiver
│   ├── AudioCaptureManager.kt
│   ├── NumberUpdateWorker.kt         ← WorkManager, günlük DB güncelleme
│   └── OverlayManager.kt
│
└── ui/
    ├── MainActivity.kt
    ├── navigation/
    │   └── NavGraph.kt
    └── screen/
        ├── home/
        │   ├── HomeScreen.kt
        │   └── HomeViewModel.kt
        ├── history/
        │   ├── HistoryScreen.kt
        │   └── HistoryViewModel.kt
        ├── blocklist/
        │   ├── BlocklistScreen.kt
        │   └── BlocklistViewModel.kt
        ├── settings/
        │   ├── SettingsScreen.kt
        │   └── SettingsViewModel.kt
        └── onboarding/
            └── PermissionOnboardingScreen.kt
```

---

## Veri Akış Pipeline'ları

### Arama Kanalı
```
Gelen Arama
  → CallScreeningServiceImpl
      → CheckNumberUseCase (Room DB'den local lookup)
          ├─ BLOCKED → anında reddet
          └─ UNKNOWN → CallMonitorService başlat
                → AudioCaptureManager (PCM stream)
                → WhisperEngine (Flow<String> transcript)
                → FraudTextClassifier + AudioFraudDetector
                → RiskLevel → OverlayManager (kullanıcı uyarısı)
```

### SMS Kanalı
```
Gelen SMS
  → SmsReceiver (BroadcastReceiver, android.provider.Telephony.SMS_RECEIVED)
      → AnalyzeSmsContentUseCase
          → DetectBtkCodeUseCase     (B214, B217, B250 → anında BLOCKED)
          → TurkishPatternMatcher    (regex: bahis, dava, icra kalıpları)
          → FraudTextClassifier      (TFLite skor)
          → CheckPhishingUrlUseCase  (URL varsa)
          → RiskLevel
              ├─ BLOCKED → SMS'i engelle + bildirim göster
              ├─ HIGH    → uyarı bildirimi
              └─ MEDIUM  → bildirimde etiket
```

### Güncelleme Kanalı
```
NumberUpdateWorker (WorkManager, günde 1x, sadece WiFi)
  → FraudDataApi (GitHub raw JSON)
  → Room DB → BlockedNumberEntity güncelle
```

---

## Katman Kuralları (SOLID)
1. `UI` → sadece `ViewModel`'ı bilir
2. `ViewModel` → sadece `UseCase`'leri bilir
3. `UseCase` → sadece `Repository interface`'leri bilir
4. `Repository impl` → `Room DAO` + `Retrofit API` + `ML engine`'leri bilir
5. `Service` katmanı → UI'ı doğrudan değil, `SharedFlow` üzerinden günceller
6. `ML motorları` → hiçbir zaman doğrudan UI veya Service tarafından çağrılmaz, UseCase üzerinden erişilir

---

## Türkiye'ye Özel Bileşenler

### BTK Yönlendirme Kodları (Constants.kt)
```kotlin
val BTK_SPAM_CODES = setOf("B214", "B217", "B250", "B312", "B445")
// B214 = Asya SMS, B217 = Sazak GSM, B250 = Ayaz GSM
```

### Türkçe Dolandırıcılık Pattern'leri (TurkishPatternMatcher.kt)
```
Bahis kalıpları   : "bahis", "casino", "canlı bahis", "bonus", "yatır kazan"
Dava/icra kalıpları: "dava açılacak", "icra", "ceza dosyası", "son gün", "avukat"
Phishing kalıpları : "e-devlet", "hesabınız askıya", "şifrenizi güncelleyin"
Sosyal yardım      : "sosyal yardım", "burs", "destek ödemesi"
```

### Numara Veritabanı Kaynağı
- Kaynak: GitHub'da yayımlanan açık JSON liste
- Format: `{ "numbers": ["+905xxxxxxxxx"], "updated_at": "..." }`
- Güncelleme: WorkManager, günde 1 kez, yalnızca WiFi'de
- Fallback: Local asset olarak paketlenmiş başlangıç listesi

---

## İzin Listesi (AndroidManifest.xml)
```xml
<!-- Temel -->
<uses-permission android:name="android.permission.RECEIVE_SMS"/>
<uses-permission android:name="android.permission.READ_SMS"/>
<uses-permission android:name="android.permission.READ_PHONE_STATE"/>
<uses-permission android:name="android.permission.READ_CALL_LOG"/>
<uses-permission android:name="android.permission.ANSWER_PHONE_CALLS"/>

<!-- Overlay -->
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>

<!-- Arka plan -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>

<!-- Network -->
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>

<!-- Ses -->
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
<uses-permission android:name="android.permission.CAPTURE_AUDIO_OUTPUT"/>
```

---

## Geliştirme Sırası (Aşama Aşama)
1. `libs.versions.toml` + `build.gradle.kts`
2. Domain modeller (`RiskLevel`, `AnalysisResult`, `BlockedNumber`, `SmsLog`)
3. Room DB (`entity` + `DAO` + `Database`)
4. Repository (`interface` + `impl`)
5. `TurkishPatternMatcher` + `DetectBtkCodeUseCase` (TR'ye özel motor)
6. `FraudTextClassifier` (TFLite)
7. `SmsReceiver` + `AnalyzeSmsContentUseCase`
8. `CallScreeningServiceImpl` + `CheckNumberUseCase`
9. `WhisperEngine` + `CallMonitorService`
10. `OverlayManager`
11. `NumberUpdateWorker` + `FraudDataApi`
12. UI ekranları (Home, History, Blocklist, Settings)
13. Permission Onboarding
