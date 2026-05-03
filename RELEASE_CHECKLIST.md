# CallShield — Google Play Yayın Kontrol Listesi

## 🔑 İmzalama & Build
- [ ] `keystore.properties` dosyası oluşturuldu (production signing key)
- [ ] `./gradlew assembleRelease` hatasız derleniyor
- [ ] R8/ProGuard optimize edilmiş APK boyutu kontrol edildi
- [ ] Version code/name `build.gradle.kts`'de güncellendi
- [ ] `debuggable false` release build'de aktif

## 📋 Google Play Console
- [ ] Developer hesabı oluşturuldu ve doğrulandı
- [ ] Uygulama draft oluşturuldu
- [ ] Signing key Play App Signing'e yüklendi

## 📝 Mağaza Sayfası
- [ ] Uygulama adı (Türkçe + İngilizce)
- [ ] Kısa açıklama (max 80 karakter)
- [ ] Uzun açıklama (max 4000 karakter)
- [ ] Ekran görüntüleri (en az 2 adet, telefon boyutu)
- [ ] Feature graphic (1024x500 px)
- [ ] Uygulama ikonu (512x512 px, Play Store formatı)
- [ ] Kategori: İletişim veya Araçlar
- [ ] İçerik derecelendirmesi anketi tamamlandı

## 🔒 Data Safety Formu
- [ ] "Veri toplanmıyor" veya toplanan veriler doğru beyan edildi
- [ ] "Veriler şifreleme ile aktarılıyor" (HTTPS only) işaretlendi
- [ ] "Kullanıcı veri silme talebinde bulunabilir" işaretlendi
- [ ] Paylaşılan 3. parti servisler beyan edildi (IPQS — opsiyonel, premium only)

## 🛡️ Hassas İzin Beyanları
Google Play, aşağıdaki izinler için **özel form** doldurmayı zorunlu kılar:

| İzin | Gerekçe | Form |
|------|---------|------|
| `RECEIVE_SMS` / `READ_SMS` | SMS içeriğini cihaz üzerinde spam analizi için tarar | SMS İzni Beyan Formu |
| `READ_CALL_LOG` | Son 24 saatteki aramaları spam kontrolü için tarar | Arama Kaydı İzni Formu |
| `SYSTEM_ALERT_WINDOW` | Gelen şüpheli aramada ekranda uyarı gösterir | - |
| `BIND_NOTIFICATION_LISTENER_SERVICE` | SMS uygulamasının spam bildirimlerini bastırır | Bildirim Erişimi Formu |
| `ROLE_CALL_SCREENING` | Gelen aramaları gerçek zamanlı tarar | Arama Tarama Beyanı |

- [ ] SMS izni beyan formu dolduruldu
- [ ] Arama kaydı izni beyan formu dolduruldu
- [ ] Bildirim erişimi gerekçesi yazıldı
- [ ] Video demo hazırlandı (izinlerin nasıl kullanıldığını gösterir)

## 🌐 Gizlilik
- [ ] Privacy Policy URL aktif ve erişilebilir
- [ ] Privacy Policy uygulamada görüntülenebiliyor (Settings → Gizlilik Politikası)
- [ ] Privacy Policy'de toplanan/işlenen veriler açıkça listeleniyor
- [ ] "Veriler cihazda kalır, buluta gönderilmez" ifadesi mevcut

## ✅ Teknik Kontroller
- [ ] `network_security_config.xml` — cleartext traffic yok
- [ ] `data_extraction_rules.xml` — cloud backup'ta hassas veri yok
- [ ] `backup_rules.xml` — eski Android'ler için backup kuralları
- [ ] `allowBackup="false"` — ADB backup koruması
- [ ] ProGuard kuralları doğru (API model sınıfları korunuyor)
- [ ] TLS certificate pinning (IPQS API'ye) değerlendirildi
- [ ] Tüm DAO'larda `deleteAll()` mevcut (veri silme akışı)
- [ ] Settings'te "Tüm Verilerimi Sil" butonu çalışıyor

## 🧪 Test
- [ ] Yeni kurulum akışı test edildi (Onboarding)
- [ ] İzinler reddedildiğinde uygulama çökmüyor
- [ ] SMS spam algılama doğru çalışıyor
- [ ] Arama engelleme doğru çalışıyor
- [ ] Premium abonelik akışı (Play Billing) test edildi
- [ ] "Tüm Verilerimi Sil" akışı test edildi
- [ ] Farklı Android sürümlerinde test edildi (API 26-34)
- [ ] Database migration'lar doğru çalışıyor

## 📦 AAB Yükleme
- [ ] `./gradlew bundleRelease` ile AAB üretildi
- [ ] AAB, Play Console'a yüklendi
- [ ] Internal testing track'te test edildi
- [ ] Pre-launch report incelendi
- [ ] Production track'e terfi edildi

## 🚀 Yayın Sonrası
- [ ] ANR / crash raporları izleniyor (Play Console + Firebase Crashlytics)
- [ ] Kullanıcı geri bildirimleri takip ediliyor
- [ ] Hotfix planı hazır
