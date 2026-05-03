# CallShield — Claude Code Başlangıç Prompt'u

## VS Code'da Claude Code'u açtıktan sonra bu prompt'u yapıştır:

---

```
Read ARCHITECTURE.md and INSTRUCTIONS.md completely before writing any code.

Project: CallShield - Turkey-specific fraud call & SMS blocker for Android
Package: com.callshield.app
Min SDK: 29, Target SDK: 34
Language: Kotlin 2.0.20

Start with ADIM 1 from INSTRUCTIONS.md:
Create the following files:
1. gradle/libs.versions.toml
2. build.gradle.kts (root)
3. app/build.gradle.kts
4. settings.gradle.kts

Requirements:
- Use exact versions from ARCHITECTURE.md tech stack
- Enable Compose, Hilt, KSP, Room in app/build.gradle.kts
- abiFilters: arm64-v8a only (for Whisper.cpp JNI)
- Add assets folder configuration for TFLite models

Do NOT write any Kotlin source files yet. Only gradle files.
After completing, show me the file tree and wait for my confirmation.
```

---

## Adım 2 onayladıktan sonra kullan:

```
ADIM 1 confirmed. Proceed to ADIM 2.
Create domain model files only:
- RiskLevel.kt (sealed class: SAFE, LOW, MEDIUM, HIGH, BLOCKED)
- AnalysisResult.kt
- BlockedNumber.kt  
- SmsLog.kt

Zero Android imports in this layer. Pure Kotlin only.
Wait for confirmation before ADIM 3.
```

---

## Takıldığında kullan:

```
I'm stuck on [DOSYA ADI]. 
The error is: [HATA MESAJI]
Current code: [KODU YAPISTIR]
Fix only this file, don't change other files.
```
