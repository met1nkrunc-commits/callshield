# Callshield Gizlilik Politikası (Kişisel Verilerin Korunması Aydınlatma Metni)

**Son güncelleme:** 11 Mart 2026
**Yürürlük tarihi:** 11 Mart 2026

Bu aydınlatma metni; 6698 sayılı **Kişisel Verilerin Korunması Kanunu (KVKK)** ve Avrupa Genel Veri Koruma Tüzüğü **(GDPR)** çerçevesinde, Callshield uygulamasını geliştiren ve işleten **Callshield** ("Veri Sorumlusu") tarafından hazırlanmıştır.

---

## 1. VERİ SORUMLUSU

| | |
|---|---|
| **Unvan** | Callshield |
| **E-posta** | privacy@callshield.app |
| **Web** | https://callshield.app/privacy |
| **İletişim** | legal@callshield.app |

---

## 2. İŞLENEN KİŞİSEL VERİLER VE İŞLEME AMAÇLARI

### 2.1 Cihaz Üzerinde İşlenen Veriler (Sunucuya Gönderilmez)

| Veri Türü | İşlenme Amacı | Saklama Yeri | Sunucuya Gönderim |
|-----------|--------------|-------------|-------------------|
| Gelen SMS içeriği | Spam/dolandırıcı tespiti için yerel analiz | Cihaz RAM (geçici) | ❌ Asla |
| Arayan telefon numarası | Engelleme listesiyle yerel karşılaştırma | Cihaz Room DB | ❌ Asla |
| Arama günlüğü | Gerçek zamanlı tarama (oturum süresi) | Cihaz RAM (geçici) | ❌ Asla |
| Engelleme olayları | Uygulama içi istatistik | Cihaz Room DB | ❌ Asla |
| Kullanıcının eklediği numaralar | Kişisel engelleme listesi | Cihaz Room DB | ❌ Asla |

### 2.2 Sunucuyla Paylaşılan Veriler

| Veri Türü | Kiminle Paylaşıldığı | Amaç | Hukuki Dayanak |
|-----------|---------------------|------|----------------|
| Arayan telefon numarası | IPQualityScore.com (IPQS) | Gerçek zamanlı spam skoru (yalnızca **Premium** kullanıcılar, opt-in) | KVKK m.5/2-f (meşru menfaat) / Açık rıza |
| Anonim hata raporları (varsa) | Firebase Crashlytics | Uygulama stabilitesi | KVKK m.5/2-c (sözleşme ifası) |

> **Not:** IPQS'e veri iletimi yalnızca **Premium** planına abone olan ve bu özelliği etkinleştiren kullanıcılara uygulanır. Ücretsiz kullanıcıların verileri hiçbir üçüncü tarafla paylaşılmaz.

### 2.3 Güncelleme Verisi (GitHub CDN)

Uygulama, dolandırıcı numara listesini 24 saatte bir GitHub Raw sunucusundan günceller. Bu işlem esnasında yalnızca IP adresi GitHub'a iletilir (standart HTTP isteği); kişisel veri gönderilmez.

---

## 3. VERİLERİN SAKLANMASI VE SİLİNMESİ

| Veri | Saklama Süresi | Silme Yöntemi |
|------|---------------|--------------|
| SMS içeriği | İşlem süresince RAM'de; analiz tamamlanınca silinir | Otomatik |
| Room veritabanı (engelleme olayları, liste) | Uygulama kaldırılana kadar | Uygulama kaldırma veya uygulama içi "Veriyi Temizle" |
| IPQS sorgu kayıtları | IPQS'in kendi politikasına göre | IPQS hesabınızdan talep |
| Firebase Crashlytics | 90 gün | Geliştirici paneli |

Bulut yedeklemesi **devre dışı** bırakılmıştır (`data_extraction_rules.xml`). Room veritabanı ve DataStore, Google/Apple bulutuna yedeklenmez.

---

## 4. KULLANICININ HAKLARI (KVKK m.11)

KVKK'nın 11. maddesi kapsamında aşağıdaki haklara sahipsiniz:

- **(a)** Kişisel verilerinizin işlenip işlenmediğini öğrenme
- **(b)** İşlenen verileriniz hakkında bilgi talep etme
- **(c)** Verilerin işlenme amacını ve bunların amacına uygun kullanılıp kullanılmadığını öğrenme
- **(d)** Yurt içinde veya yurt dışında aktarıldığı üçüncü kişileri öğrenme
- **(e)** Verilerin eksik veya yanlış işlenmesi hâlinde düzeltilmesini talep etme
- **(f)** KVKK m.7 çerçevesinde silinmesini veya yok edilmesini talep etme
- **(g)** (e) ve (f) bentleri kapsamındaki işlemlerin verilerin aktarıldığı üçüncü kişilere bildirilmesini isteme
- **(h)** Yalnızca otomatik sistemler aracılığıyla işlenmesi nedeniyle aleyhinize çıkan sonuca itiraz etme
- **(ı)** Zararın giderilmesini talep etme

**Başvuru yolu:** legal@callshield.app adresine KVKK m.13 kapsamında yazılı başvuru yapabilirsiniz. Başvurular 30 (otuz) gün içinde yanıtlanır.

**KVKK Kurulu'na şikâyet:** https://www.kvkk.gov.tr

---

## 5. GDPR KAPSAMINDA EK HAKLAR (AB/AEA Kullanıcıları)

GDPR'ın uygulandığı ülkelerdeki kullanıcılar ek olarak şu haklara sahiptir:

- **Taşınabilirlik hakkı (Art. 20):** İşlenen kişisel verilerinizi yapılandırılmış, yaygın olarak kullanılan formatta talep etme
- **Unutulma hakkı (Art. 17):** Belirli koşullarda verilerinizin silinmesini talep etme
- **İşlemenin kısıtlanması hakkı (Art. 18):** Belirli durumlarda işlemenin durdurulmasını talep etme
- **İtiraz hakkı (Art. 21):** Meşru menfaate dayanan işlemeye itiraz etme

**Denetim otoritesi şikâyeti:** Bulunduğunuz AB ülkesindeki veri koruma otoritesine şikâyet hakkınız saklıdır.

---

## 6. VERİ GÜVENLİĞİ

Verilerinizin güvenliğini sağlamak için aldığımız teknik ve idari tedbirler:

- ✅ Tüm ağ trafiği TLS/HTTPS ile şifrelenir
- ✅ Cihaz üzerindeki veriler Android işletim sistemi şifreleme katmanıyla korunur
- ✅ Bulut yedekleme devre dışı (`android:allowBackup` kısıtlaması)
- ✅ Kod gizleme ve yeniden yapılandırma (ProGuard/R8) uygulanır
- ✅ GitHub sertifikasına sertifika sabitleme uygulanır
- ✅ IPQS API anahtarı yalnızca build zamanında eklenir, kaynak kodda yer almaz

---

## 7. ÇOCUKLARIN GİZLİLİĞİ

Uygulama, 18 yaşın altındaki kişilere yönelik değildir. 18 yaşın altında olduğunu bildiğimiz kişilerden bilerek kişisel veri toplamıyoruz. Ebeveynler veya vasiler, çocuklarının verilerini topladığımızı düşünüyorlarsa legal@callshield.app adresinden bizimle iletişime geçebilir.

---

## 8. ÜÇÜNCÜ TARAF HİZMETLERİ

### 8.1 IPQualityScore.com (IPQS)
- **Amaç:** Premium kullanıcılar için gerçek zamanlı telefon numarası spam analizi
- **İletilen veri:** Yalnızca arayan telefon numarası
- **IPQS Gizlilik Politikası:** https://www.ipqualityscore.com/privacy-policy
- **IPQS Koşulları:** https://www.ipqualityscore.com/terms-of-service

### 8.2 GitHub (Microsoft)
- **Amaç:** Dolandırıcı numara listesi güncellemesi (CDN)
- **İletilen veri:** Standart HTTP isteği (IP adresi)
- **GitHub Gizlilik Politikası:** https://docs.github.com/en/site-policy/privacy-policies/github-privacy-statement

### 8.3 Google Play / Apple App Store
Uygulama mağazası aracılığıyla ödeme yapılması hâlinde ödeme bilgileriniz ilgili mağaza tarafından işlenir. Geliştirici ödeme bilgilerinize erişmez.

---

## 9. ÇEREZLER VE İZLEME

Uygulama **çerez (cookie) kullanmamaktadır**. Reklam takibi, davranışsal analiz veya üçüncü taraf reklam SDK'ları kullanılmamaktadır.

---

## 10. POLİTİKADA DEĞİŞİKLİKLER

Bu politikayı periyodik olarak güncelleyebiliriz. Önemli değişikliklerde uygulama içi bildirim göndereceğiz. Güncel versiyon her zaman https://callshield.app/privacy adresinde yayımlanacaktır.

---

## 11. İLETİŞİM VE BAŞVURU

| | |
|---|---|
| **Veri Sorumlusu Başvuruları** | legal@callshield.app |
| **Genel İletişim** | hello@callshield.app |
| **Web** | https://callshield.app |

---

*Bu aydınlatma metni, 6698 sayılı Kişisel Verilerin Korunması Kanunu, Aydınlatma Yükümlülüğünün Yerine Getirilmesinde Uyulacak Usul ve Esaslar Hakkında Tebliğ ve GDPR (AB) 2016/679 sayılı Tüzük kapsamında hazırlanmıştır.*
