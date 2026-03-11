package com.callshield.app.core.util

object Constants {
    const val GITHUB_OWNER  = "met1nkrunc-commits"
    const val GITHUB_REPO   = "b-engel-fraud-numbers"
    const val GITHUB_BRANCH = "main"
    val GITHUB_RAW_URL = "https://raw.githubusercontent.com/$GITHUB_OWNER/$GITHUB_REPO/$GITHUB_BRANCH/fraud_numbers.json"
}

object TurkishFraudConstants {

    val BTK_SPAM_CODES: Set<String> = setOf(
        "B214", "B217", "B250", "B312", "B445",
    )

    val BETTING_KEYWORDS: Set<String> = setOf(
        // Core Turkish
        "bahis", "bahiis", "bahiss", "bahıs", "bah1s", "bah!s", "b-a-h-i-s", "bahîs",
        "casino", "casin0", "cas1no", "kasino", "kazino", "casiino", "casin", "casino'",
        "kumar", "slot", "rulet", "poker", "blackjack", "jackpot", "ikramiye",

        // Short English terms
        "bet", "bets", "betting", "bettor",
        "free bet", "freebet", "free-bet",
        "bet365", "bet-365",
        "1xbet", "1x bet", "1xbahis",
        "22bet", "22 bet",
        "win", "winwin", "winning",
        "odds", "odd",
        "stake", "stakes",
        "wager", "wagering",
        "punt", "punter",
        "accumulator", "acca",
        "parlay", "combo bet",
        "handicap", "asian handicap",
        "live bet", "livebet", "in-play",
        "cashout", "cash out", "cash-out",
        "arbitrage", "arb bet", "sure bet", "surebet",
        "matched bet", "matchedbet",

        // Deneme variations
        "deneme", "deneme bonusu", "deneme bonus", "d3neme", "d-e-n-e-m-e",
        "déneme", "déneme bonusu", "deneme b0nusu",
        "ücretsiz deneme", "ucretsiz deneme", "bedava deneme",
        "deneme fırsatı", "deneme firsati", "ilk deneme",
        "deneme süresi", "deneme suresi", "trial bonus",

        // Bonus variations
        "bonus", "bonusu", "bonuss", "b0nus", "bonüs", "b-o-n-u-s",
        "hoşgeldin bonusu", "hosgeldin bonusu", "hoş geldin bonusu",
        "hoşgeldın bonusu", "hosgeldın bonusu", "hoşgeldin", "hosgeldin",
        "ilk yatırım bonusu", "ilk yatirim bonusu",
        "yatırım bonusu", "yatirim bonusu",
        "çevrimsiz bonus", "cevrimsiz bonus", "çevrimsız bonus",
        "çevrim şartı yok", "cevrim sarti yok",
        "yatırımsız bonus", "yatirimsiz bonus",
        "kayıpsız bonus", "kayipsiz bonus",
        "iade bonusu", "cashback bonus", "geri ödeme bonusu",
        "kayıp bonusu", "kayip bonusu",
        "vip bonus", "vip üyelik", "vip uyelik",
        "özel bonus", "ozel bonus", "ekstra bonus",
        "günlük bonus", "gunluk bonus", "haftalık bonus", "haftalik bonus",
        "doğum günü bonusu", "dogum gunu bonusu",
        "arkadaş getir bonusu", "arkadasini getir",
        "combo bonus", "çoklu bonus", "coklu bonus",
        "anında bonus", "aninda bonus", "otomatik bonus",
        "bonus kodu", "promosyon kodu", "promo kod",
        "kupon kodu", "referans kodu",
        "%100 bonus", "yüzde yüz bonus", "çift bonus",

        // Free spin variations
        "freespin", "free spin", "free-spin", "frispın", "fri spin",
        "freespın", "free spın", "fríspin",
        "bedava dönüş", "bedava donus",

        // Yatırım/çekim
        "yatırım", "yatirim", "yatırın", "yatirin", "yat1r", "yatır",
        "para yatir", "para yatır",
        "çekim", "cekim", "çekım", "cekım", "para çek", "para cek",
        "hızlı çekim", "anında ödeme", "7/24 ödeme",
        "güvenli ödeme", "papara", "p-a-p-a-r-a", "papar@", "p@p@r@",

        // Kripto/ödeme
        "kripto", "krıpto", "kript0", "bitcoin", "b1tcoin", "btc",
        "usdt", "u-s-d-t", "tether",

        // Oyna/giriş
        "oyna", "oyn4", "0yna", "oynaa", "hemen oyna",
        "siteye gir", "siteye girin",
        "giriş yap", "giris yap", "gırıs yap", "girış yap",
        "giriş linki", "giris linki",
        "kayıt ol", "kayit ol", "kay1t ol",
        "üye ol", "uye ol", "üye olun", "uye olun",
        "hesap aç", "hesap ac",

        // Para kazanma
        "para kazan", "para kazanın", "para kazanin",
        "kazanç", "kazanc", "kazançlı", "kazancli",
        "günlük kazanç", "gunluk kazanc",
        "garantili kazanç", "garantili kazanc",

        // Turkish short forms
        "bahse gir", "bahse girin",
        "oranlar", "oran", "yüksek oran", "yuksek oran",
        "maç tahmini", "mac tahmini", "tahmin",
        "kupon", "kuponu doldur",
        "kombine", "sistem bahis",
        "canli", "canlı",
        "ganyan", "at yarışı", "at yarisi",
        "toto", "milli piyango", "piyango",
        "şans oyunu", "sans oyunu",
        "tombala", "bingo",

        // Bahis siteleri
        "vaycasino", "vay casino", "vay-casino",
        "betboo", "bet-boo", "bet boo",
        "bets10", "bet-s10", "bets 10",
        "superbahis", "super bahis", "super-bahis",
        "mobilbahis", "mobil bahis", "mobil-bahis",
        "nakitbahis", "nakit bahis", "nakit-bahis",
        "betturkey", "bet turkey", "bet-turkey",
        "tipobet", "tipo bet", "tipo-bet",
        "sultanbet", "sultan bet", "sultan-bet",
        "youwin", "tempobet", "betpas",
        "casinometropol", "casinomaxi", "retrobet",
        "bahsegel", "mariobet", "betvole", "rexbet",
        "betgram", "betlike", "hiperbet",
        "betcup", "betroad", "betorder",
        "casinotr", "casino tr",
        "bahigo", "bahi go",
        "betist", "bet ist",
        "betvakti", "bet vakti",
        "anadolucasino", "anadolu casino",
        "elexbet", "elex bet",
        "betnano", "bet nano",
        "vegabet", "vega bet",
        "goldenbahis", "golden bahis",
        "royalbet", "royal bet",
        "favoribahis", "favori bahis",
        "bettilt", "bet tilt",
        "betgaranti", "bet garanti",

        // Genel
        "iddaa", "canlı bahis", "canli bahis", "spor bahis",
        "canlı casino", "canli casino",
        "sınırlı süre", "sadece bugün", "son şans",
        "özel teklif", "büyük ikramiye",
    )

    val LEGAL_THREAT_KEYWORDS: Set<String> = setOf(
        "dava açılacak", "icra", "son uyarı", "tc kimlik",
        "mahkeme", "savcılık", "emniyet", "jandarma",
        "vergi borcu", "vergi borcunuz", "vergi cezası",
        "sgk borcu", "sgk borcunuz", "sgk cezası",
        "e-devlet bildirimi", "edevlet bildirimi",
        "resmi uyarı", "resmi uyari", "resmi bildirim",
        "yasal işlem", "yasal islem", "yasal süreç",
        "noter tebligatı", "noter teblıgati", "tebligat",
        "icra takibi", "icra takip", "icra müdürlüğü",
        "iflas", "iflâs", "haciz işlemi",
        "banka hesabınız", "banka hesabiniz", "hesabınız bloke",
        "kartınız iptal", "kartiniz iptal", "kartınız askıya",
        "hesabınız donduruldu", "hesabiniz donduruldu",
        "ödeme yapılmadı", "odeme yapilmadi", "gecikmiş ödeme",
        "son ödeme tarihi", "son odeme tarihi",
        "borcunuzu ödeyin", "borcunuzu odeyin",
    )

    val PHISHING_KEYWORDS: Set<String> = setOf(
        "e-devlet", "hesabınız askıya", "şifrenizi güncelleyin",
        "kimliğinizi doğrulayın", "hesap donduruldu", "acil güncelleme",
    )

    val SOCIAL_ENGINEERING_KEYWORDS: Set<String> = setOf(
        "sosyal yardım", "devlet yardımı", "burs", "hibe",
        "ücretsiz", "bedava", "hediye",
        "tebrikler", "tebrıkler", "tebr1kler",
        "kazanan siz oldunuz", "kazanan sizsiniz", "siz kazandınız",
        "numaranız seçildi", "numaraniz secildi",
        "ödülünüzü alın", "odulunuzu alin", "ödülünüz hazır",
        "hediyeniz hazır", "hediyeniz hazir", "hediye kazandınız",
        "kampanyaya katıl", "kampanyaya katil", "kampanyamıza katıl",
        "formu doldurun", "formu doldurunuz", "formu tamamlayın",
        "bilgilerinizi güncelleyin", "bilgilerinizi guncellyin",
        "hesabınızı doğrulayın", "hesabinizi dogrulayın",
        "kimliğinizi onaylayın", "kimliginizi onaylayin",
        "link tıklayın", "linke tıkla", "linki tıkla",
        "hemen tıkla", "hemen tikla", "buraya tıkla", "buraya tikla",
        "whatsapp'tan ulaşın", "whatsapptan ulasin",
        "telegram grubu", "telegram kanalı", "telegram kanali",
        "kanal linki", "gruba katıl", "gruba katil",
        "qr kod okut", "qr kodu tara",
    )

    val SHORT_URL_PATTERNS: List<Regex> = listOf(
        Regex("""bit\.ly/\S+"""),
        Regex("""tinyurl\.com/\S+"""),
        Regex("""t\.co/\S+"""),
        Regex("""ow\.ly/\S+"""),
        Regex("""rebrand\.ly/\S+"""),
        Regex("""tr\.im/\S+"""),
        Regex("""kisa\.link/\S+"""),
        Regex("""cutt\.ly/\S+"""),
    )

    // Matches lookalike domains impersonating Turkish government or banking sites.
    // Legitimate domains (e.g., turkiye.gov.tr, garantibbva.com.tr) do not match
    // because they do not contain hyphens or extra substrings after the brand name.
    val SUSPICIOUS_DOMAIN_PATTERNS: List<Regex> = listOf(
        Regex("""e-?devlet[a-z0-9\-]*\.(com|net|org|xyz|info|co)""", RegexOption.IGNORE_CASE),
        Regex("""garanti[a-z0-9\-]+\.(com|net|org|xyz)""", RegexOption.IGNORE_CASE),
        Regex("""isbank[a-z0-9\-]+\.(com|net|org|xyz)""", RegexOption.IGNORE_CASE),
        Regex("""akbank[a-z0-9\-]+\.(com|net|org|xyz)""", RegexOption.IGNORE_CASE),
        Regex("""ziraat[a-z0-9\-]+\.(com|net|org|xyz)""", RegexOption.IGNORE_CASE),
        Regex("""vakifbank[a-z0-9\-]+\.(net|org|xyz|co)""", RegexOption.IGNORE_CASE),
        Regex("""enpara[a-z0-9\-]+\.(com|net|org|xyz)""", RegexOption.IGNORE_CASE),
    )

    // Alphanumeric sender IDs used by legitimate Turkish institutions.
    // If the SMS sender exactly matches one of these (case-insensitive), skip fraud analysis.
    val TRUSTED_SENDER_IDS: Set<String> = setOf(
        // Banks
        "Akbank", "AkbankSMS", "AKBANK",
        "Garanti", "GarantiBBVA", "GARANTIBBVA",
        "İşBankası", "Isbank", "ISBANK",
        "Yapıkredi", "Yapikredi", "YAPIKREDI",
        "Ziraat", "ZiraatBankasi", "ZIRAAT",
        "Halkbank", "HALKBANK",
        "Vakıfbank", "Vakifbank", "VAKIFBANK",
        "DenizBank", "Denizbank", "DENIZBANK",
        "QNBFinansbank", "Finansbank", "FINANSBANK",
        "HSBC", "HSBCBank",
        "ING", "INGBank",
        "Odeabank", "ODEABANK",
        "TEB", "CEPTETEB",
        "Şekerbank", "Sekerbank",
        "Fibabanka", "FIBABANKA",
        "Enpara",
        // Operators
        "Turkcell", "TURKCELL",
        "Vodafone", "VODAFONE",
        "TurkTelekom", "Turk Telekom", "TURKTELEKOM",
        "Bimcell", "BIMCELL",
        // E-commerce / cargo
        "Trendyol", "TRENDYOL",
        "Hepsiburada", "HEPSIBURADA",
        "Amazon", "AmazonTR",
        "n11", "N11",
        "GittiGidiyor", "GITTIGIDIYOR",
        "Morhipo", "MORHIPO",
        "PTT", "PTTAVM", "PTTKargo",
        "Yurtiçi", "YurticiKargo", "YURTICI", "Yurtici",
        "Aras", "ArasKargo", "ARASKARGO",
        "MNG", "MNGKargo",
        "Sürat", "SuratKargo", "Surat",
        "Sendeo", "SENDEO",
        "DHL", "UPS", "FedEx",
        // Government / utility
        "e-Devlet", "eDevlet", "EDEVLET",
        "SGK", "BTK", "BDDK",
        "Vergi", "GIB",
        "TEDAŞ", "TEDAS", "AYEDAS",
        "İGDAŞ", "IGDAS", "GAZDAS",
        "İSKİ", "ISKI", "ASKİ",
        "Belediye",
        // Common services
        "Netflix", "Spotify", "YouTube",
        "Google", "Apple", "Microsoft",
        "PayPal", "PayTR", "İyzico", "Iyzico",
        "BiTaksi", "Uber", "Bolt",
        "Yemeksepeti", "Getir", "Migros",
        "A101", "BİM", "ŞOK",
        "YapiKredi",
    )
}
