import SwiftUI

struct SettingsView: View {
    @EnvironmentObject private var sync: FraudSyncService
    @EnvironmentObject private var store: StoreManager
    @AppStorage("filterSetupConfirmed") private var filterSetupConfirmed = false

    var body: some View {
        ScrollView {
            VStack(spacing: 20) {
                premiumSection
                smsFilterSection
                senderRulesSection
                reportSection
                syncSection
                appSection
                Spacer(minLength: 40)
            }
            .padding(.horizontal, 16)
            .padding(.top, 24)
        }
        .background(CS.bg.ignoresSafeArea())
        .navigationTitle("Ayarlar")
        .navigationBarTitleDisplayMode(.large)
    }

    // MARK: - Premium
    private var premiumSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader("PREMIUM")

            NavigationLink(destination: PaywallView()) {
                HStack(spacing: 14) {
                    Image(systemName: store.isPremium ? "checkmark.seal.fill" : "sparkles")
                        .font(.system(size: 22))
                        .foregroundStyle(store.isPremium ? CS.accent : CS.orange)
                        .frame(width: 28)

                    VStack(alignment: .leading, spacing: 2) {
                        Text(store.isPremium ? "Premium Aktif" : "7 Gün Ücretsiz Dene")
                            .font(.subheadline.weight(.semibold))
                            .foregroundStyle(CS.primary)
                        Text(store.isPremium ? "Reklamsız kullanım ve premium özellikler açık" : "Reklamları kaldır, sınırsız korumayı aç")
                            .font(.caption)
                            .foregroundStyle(CS.secondary)
                    }

                    Spacer()
                    Image(systemName: "chevron.right")
                        .font(.caption)
                        .foregroundStyle(CS.disabled)
                }
                .padding(16)
                .background(CS.surface)
                .clipShape(RoundedRectangle(cornerRadius: 14))
            }
        }
    }

    // MARK: - SMS Filter setup
    private var smsFilterSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader("SMS FİLTRESİ")

            HStack(spacing: 12) {
                Image(systemName: filterSetupConfirmed ? "checkmark.shield.fill" : "exclamationmark.triangle.fill")
                    .font(.system(size: 22))
                    .foregroundStyle(filterSetupConfirmed ? CS.accent : CS.orange)
                VStack(alignment: .leading, spacing: 4) {
                    Text(filterSetupConfirmed ? "Kurulum Onaylandı" : "Kurulum Onayı Bekleniyor")
                        .font(.subheadline.weight(.semibold))
                        .foregroundStyle(CS.primary)
                    Text(filterSetupConfirmed
                         ? "Siper filtresini etkinleştirdiğini işaretledin."
                         : "Filtreyi Ayarlar > Mesajlar > Bilinmeyen ve Spam bölümünden etkinleştirip aşağıdaki onayı ver.")
                        .font(.caption)
                        .foregroundStyle(CS.secondary)
                }
                Spacer()
                Button(filterSetupConfirmed ? "Sıfırla" : "Onayla") {
                    filterSetupConfirmed.toggle()
                }
                .font(.caption.weight(.semibold))
                .foregroundStyle(filterSetupConfirmed ? CS.orange : CS.accent)
            }
            .padding(16)
            .background(CS.surface)
            .clipShape(RoundedRectangle(cornerRadius: 14))

            NavigationLink(destination: FilterHistoryView()) {
                historyRow
            }

            NavigationLink(destination: SetupGuideView()) {
                HStack(spacing: 14) {
                    Image(systemName: "questionmark.circle.fill")
                        .font(.system(size: 22))
                        .foregroundStyle(CS.accent)
                        .frame(width: 28)

                    VStack(alignment: .leading, spacing: 2) {
                        Text("SMS Filtresi Nasıl Aktif Edilir?")
                            .font(.subheadline).fontWeight(.medium)
                            .foregroundStyle(CS.primary)
                        Text("Kurulum rehberini görüntüle")
                            .font(.caption)
                            .foregroundStyle(CS.secondary)
                    }

                    Spacer()
                    Image(systemName: "chevron.right")
                        .font(.caption)
                        .foregroundStyle(CS.disabled)
                }
                .padding(16)
                .background(CS.surface)
                .clipShape(RoundedRectangle(cornerRadius: 14))
            }

            NavigationLink(destination: LinkTestView()) {
                HStack(spacing: 14) {
                    Image(systemName: "link.badge.plus")
                        .font(.system(size: 22))
                        .foregroundStyle(CS.red)
                        .frame(width: 28)

                    VStack(alignment: .leading, spacing: 2) {
                        Text("Link Testi")
                            .font(.subheadline.weight(.medium))
                            .foregroundStyle(CS.primary)
                        Text("Şüpheli linki yapıştır, risk sinyalini gör")
                            .font(.caption)
                            .foregroundStyle(CS.secondary)
                    }

                    Spacer()
                    Image(systemName: "chevron.right")
                        .font(.caption)
                        .foregroundStyle(CS.disabled)
                }
                .padding(16)
                .background(CS.surface)
                .clipShape(RoundedRectangle(cornerRadius: 14))
            }

            NavigationLink(destination: FilterRulesView()) {
                HStack(spacing: 14) {
                    Image(systemName: "slider.horizontal.3")
                        .font(.system(size: 22))
                        .foregroundStyle(CS.accent)
                        .frame(width: 28)

                    VStack(alignment: .leading, spacing: 2) {
                        Text("Kural Hassasiyeti")
                            .font(.subheadline.weight(.medium))
                            .foregroundStyle(CS.primary)
                        Text("Filtre seviyesini ve güvenli kelimeleri yönet")
                            .font(.caption)
                            .foregroundStyle(CS.secondary)
                    }

                    Spacer()
                    Image(systemName: "chevron.right")
                        .font(.caption)
                        .foregroundStyle(CS.disabled)
                }
                .padding(16)
                .background(CS.surface)
                .clipShape(RoundedRectangle(cornerRadius: 14))
            }
        }
    }

    private var historyRow: some View {
        HStack(spacing: 14) {
            Image(systemName: "nosign")
                .font(.system(size: 22))
                .foregroundStyle(CS.red)
                .frame(width: 28)
            VStack(alignment: .leading, spacing: 2) {
                Text("Engellenen SMS Geçmişi")
                    .font(.subheadline).fontWeight(.medium)
                    .foregroundStyle(CS.primary)
                Text("Uygulama içi filtre kayıtlarını görüntüle")
                    .font(.caption)
                    .foregroundStyle(CS.secondary)
            }
            Spacer()
            Image(systemName: "chevron.right")
                .font(.caption)
                .foregroundStyle(CS.disabled)
        }
        .padding(16)
        .background(CS.surface)
        .clipShape(RoundedRectangle(cornerRadius: 14))
    }

    private var senderRulesSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader("GÖNDERİCİ KURALLARI")

            NavigationLink(destination: SenderRulesView()) {
                HStack(spacing: 14) {
                    Image(systemName: "text.badge.minus")
                        .font(.system(size: 22))
                        .foregroundStyle(CS.orange)
                        .frame(width: 28)
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Başlık Kuralları")
                            .font(.subheadline).fontWeight(.medium)
                            .foregroundStyle(CS.primary)
                        Text("INNOVIX, BETXYZ gibi gönderici adları için kural ekle")
                            .font(.caption)
                            .foregroundStyle(CS.secondary)
                    }
                    Spacer()
                    Image(systemName: "chevron.right")
                        .font(.caption)
                        .foregroundStyle(CS.disabled)
                }
                .padding(16)
                .background(CS.surface)
                .clipShape(RoundedRectangle(cornerRadius: 14))
            }
        }
    }

    private var reportSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader("GERİ BİLDİRİM")

            NavigationLink(destination: SpamReportView()) {
                HStack(spacing: 14) {
                    Image(systemName: "exclamationmark.bubble.fill")
                        .font(.system(size: 22))
                        .foregroundStyle(CS.red)
                        .frame(width: 28)
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Spam Raporla")
                            .font(.subheadline).fontWeight(.medium)
                            .foregroundStyle(CS.primary)
                        Text("Kaçan mesajları yerel olarak not al")
                            .font(.caption)
                            .foregroundStyle(CS.secondary)
                    }
                    Spacer()
                    Image(systemName: "chevron.right")
                        .font(.caption)
                        .foregroundStyle(CS.disabled)
                }
                .padding(16)
                .background(CS.surface)
                .clipShape(RoundedRectangle(cornerRadius: 14))
            }
        }
    }

    // MARK: - Sync section
    private var syncSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader("DOLANDIRICI LİSTESİ")

            VStack(spacing: 0) {
                infoRow(label: "Engellenen Numara", value: "\(sync.blockedCount)")
                Divider().background(CS.surfaceVar).padding(.leading, 16)
                infoRow(label: "Son Güncelleme", value: lastSyncText)
                Divider().background(CS.surfaceVar).padding(.leading, 16)

                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text("Listeyi Güncelle")
                            .font(.subheadline).fontWeight(.medium)
                            .foregroundStyle(CS.primary)
                        Text("24 saatte bir otomatik güncellenir")
                            .font(.caption)
                            .foregroundStyle(CS.secondary)
                    }
                    Spacer()
                    if sync.isSyncing {
                        ProgressView()
                            .tint(CS.accent)
                    } else {
                        Button("Güncelle") {
                            Task { await sync.sync() }
                        }
                        .font(.subheadline.weight(.medium))
                        .foregroundStyle(CS.accent)
                    }
                }
                .padding(16)

                if let err = sync.lastError {
                    Divider().background(CS.surfaceVar).padding(.leading, 16)
                    HStack {
                        Image(systemName: "exclamationmark.circle.fill")
                            .foregroundStyle(CS.red)
                        Text(err)
                            .font(.caption)
                            .foregroundStyle(CS.red)
                    }
                    .padding(16)
                }
            }
            .background(CS.surface)
            .clipShape(RoundedRectangle(cornerRadius: 14))
        }
    }

    // MARK: - App info
    private var appSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader("UYGULAMA")

            VStack(spacing: 0) {
                infoRow(label: "Sürüm", value: "1.0.0")
                Divider().background(CS.surfaceVar).padding(.leading, 16)
                infoRow(label: "Platform", value: "iOS 16+")
            }
            .background(CS.surface)
            .clipShape(RoundedRectangle(cornerRadius: 14))

            Text("Siper · Türkiye Dolandırıcılık Koruma")
                .font(.caption2)
                .foregroundStyle(CS.disabled)
                .frame(maxWidth: .infinity, alignment: .center)
                .padding(.top, 4)
        }
    }

    // MARK: - Helpers
    private func sectionHeader(_ text: String) -> some View {
        Text(text)
            .font(.system(size: 11, weight: .semibold))
            .foregroundStyle(CS.secondary)
    }

    private func infoRow(label: String, value: String) -> some View {
        HStack {
            Text(label)
                .font(.subheadline).fontWeight(.medium)
                .foregroundStyle(CS.primary)
            Spacer()
            Text(value)
                .font(.subheadline)
                .foregroundStyle(CS.secondary)
        }
        .padding(16)
    }

    private var lastSyncText: String {
        guard let date = sync.lastSyncDate else { return "Hiç güncellenmedi" }
        let diff = Date().timeIntervalSince(date)
        if diff < 60    { return "Az önce" }
        if diff < 3600  { return "\(Int(diff / 60)) dakika önce" }
        if diff < 86400 { return "\(Int(diff / 3600)) saat önce" }
        return "\(Int(diff / 86400)) gün önce"
    }
}

private struct FilterRulesView: View {
    @State private var sensitivity = AppGroupStorage.shared.filterSensitivity
    @State private var safeKeywords: [String] = []
    @State private var showAddSafeKeyword = false
    @State private var draftKeyword = ""

    var body: some View {
        ScrollView {
            VStack(spacing: 18) {
                sensitivityCard
                safeKeywordsCard
            }
            .padding(16)
        }
        .background(CS.bg.ignoresSafeArea())
        .navigationTitle("Kural Hassasiyeti")
        .navigationBarTitleDisplayMode(.large)
        .onAppear(perform: reload)
        .alert("Güvenli Kelime Ekle", isPresented: $showAddSafeKeyword) {
            TextField("Örn. betimleme", text: $draftKeyword)
                .textInputAutocapitalization(.never)
            Button("Ekle") { addSafeKeyword() }
            Button("İptal", role: .cancel) { draftKeyword = "" }
        } message: {
            Text("Bu kelimeyi içeren mesajlarda içerik bazlı bahis engeli uygulanmaz.")
        }
    }

    private var sensitivityCard: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("Filtre Seviyesi")
                .font(.headline)
                .foregroundStyle(CS.primary)

            Picker("Filtre Seviyesi", selection: $sensitivity) {
                ForEach(AppGroupStorage.FilterSensitivity.allCases) { level in
                    Text(level.title).tag(level)
                }
            }
            .pickerStyle(.segmented)
            .onChange(of: sensitivity) { newValue in
                AppGroupStorage.shared.filterSensitivity = newValue
            }

            Text(sensitivity.detail)
                .font(.subheadline)
                .foregroundStyle(CS.secondary)

            Text("Eşik: içerik skoru \(sensitivity.keywordThreshold), alfanümerik gönderici skoru \(sensitivity.alphanumericThreshold).")
                .font(.caption)
                .foregroundStyle(CS.disabled)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(CS.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    private var safeKeywordsCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Label("Güvenli Kelimeler", systemImage: "checkmark.seal.fill")
                    .font(.headline)
                    .foregroundStyle(CS.accent)
                Spacer()
                Button("Ekle") {
                    showAddSafeKeyword = true
                }
                .font(.caption.weight(.semibold))
                .foregroundStyle(CS.accent)
            }

            Text("Yanlış pozitifleri azaltmak için kelime ekle. Engelli numara veya engelli başlık kuralları bundan etkilenmez.")
                .font(.subheadline)
                .foregroundStyle(CS.secondary)

            if safeKeywords.isEmpty {
                Text("Henüz güvenli kelime yok.")
                    .font(.subheadline)
                    .foregroundStyle(CS.secondary)
            } else {
                ForEach(safeKeywords, id: \.self) { keyword in
                    HStack {
                        Text(keyword)
                            .font(.subheadline.weight(.medium))
                            .foregroundStyle(CS.primary)
                        Spacer()
                        Button {
                            AppGroupStorage.shared.removeSafeKeyword(keyword)
                            reload()
                        } label: {
                            Image(systemName: "trash")
                                .foregroundStyle(CS.red)
                        }
                    }
                    .padding(.vertical, 4)
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(CS.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    private func addSafeKeyword() {
        AppGroupStorage.shared.addSafeKeyword(draftKeyword)
        draftKeyword = ""
        reload()
    }

    private func reload() {
        sensitivity = AppGroupStorage.shared.filterSensitivity
        safeKeywords = Array(AppGroupStorage.shared.safeKeywords).sorted()
    }
}

private struct LinkTestView: View {
    @State private var input = "https://bit.ly/b0nuswin"

    private let examples = [
        "https://bit.ly/b0nuswin",
        "bonus-firsat.xyz",
        "akkbank.com",
        "ptt.gov.tr",
        "akbank.com"
    ]

    private var analysis: AppGroupStorage.LinkAnalysis {
        AppGroupStorage.shared.analyzeLink(input)
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                examplesCard
                inputCard
                resultCard
            }
            .padding(16)
        }
        .background(CS.bg.ignoresSafeArea())
        .navigationTitle("Link Testi")
        .navigationBarTitleDisplayMode(.large)
    }

    private var examplesCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Hazır Linkler")
                .font(.headline)
                .foregroundStyle(CS.primary)

            LazyVGrid(columns: [GridItem(.adaptive(minimum: 130), spacing: 10)], spacing: 10) {
                ForEach(examples, id: \.self) { example in
                    Button {
                        input = example
                    } label: {
                        Text(example)
                            .font(.caption.weight(.semibold))
                            .foregroundStyle(CS.primary)
                            .lineLimit(1)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 10)
                            .background(CS.surfaceVar)
                            .clipShape(RoundedRectangle(cornerRadius: 10))
                    }
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(CS.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    private var inputCard: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Kontrol Edilecek Link")
                .font(.caption)
                .foregroundStyle(CS.secondary)

            TextField("https://...", text: $input)
                .textInputAutocapitalization(.never)
                .keyboardType(.URL)
                .padding(14)
                .background(CS.surfaceVar)
                .clipShape(RoundedRectangle(cornerRadius: 14))

            Text("Bu kontrol sadece yerel kural analizi yapar; link açılmaz ve sunucuya gönderilmez.")
                .font(.caption)
                .foregroundStyle(CS.disabled)
        }
        .padding(16)
        .background(CS.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    private var resultCard: some View {
        VStack(alignment: .leading, spacing: 14) {
            Label(
                analysis.isRisky ? "Riskli görünüyor" : "Belirgin risk bulunmadı",
                systemImage: analysis.isRisky ? "exclamationmark.triangle.fill" : "checkmark.shield.fill"
            )
            .font(.headline)
            .foregroundStyle(analysis.isRisky ? CS.red : CS.accent)

            HStack {
                Text("Risk skoru")
                    .font(.caption)
                    .foregroundStyle(CS.secondary)
                Spacer()
                Text("\(analysis.score) / 10")
                    .font(.caption.weight(.semibold))
                    .foregroundStyle(analysis.isRisky ? CS.red : CS.primary)
            }

            if analysis.hosts.isEmpty {
                Text("Geçerli bir domain bulunamadı. Linki veya domaini tam yazmayı dene.")
                    .font(.subheadline)
                    .foregroundStyle(CS.secondary)
            } else {
                ForEach(analysis.hosts, id: \.self) { host in
                    HStack {
                        Text("Domain")
                            .font(.caption)
                            .foregroundStyle(CS.secondary)
                        Spacer()
                        Text(host)
                            .font(.caption.weight(.semibold))
                            .foregroundStyle(CS.primary)
                    }
                }
            }

            if analysis.reasons.isEmpty && !analysis.hosts.isEmpty {
                Text("Kısa link, riskli uzantı veya bahis odaklı domain sinyali bulunmadı.")
                    .font(.subheadline)
                    .foregroundStyle(CS.secondary)
            } else {
                ForEach(analysis.reasons, id: \.self) { reason in
                    Label(reason, systemImage: "smallcircle.filled.circle")
                        .font(.caption)
                        .foregroundStyle(CS.secondary)
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(CS.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

private struct FilterTestView: View {
    @State private var sender = "INNOVIXLTD"
    @State private var message = "Bugun sana ozel bonus ve freebet tanimlandi. Hemen uye ol."

    private let examples: [(title: String, sender: String, message: String)] = [
        (
            title: "Bahis SMS",
            sender: "INNOVIXLTD",
            message: "Bugun sana ozel bonus ve freebet tanimlandi. Hemen uye ol."
        ),
        (
            title: "Riskli Link",
            sender: "BETFIRSAT",
            message: "Kuponun hazir. Hemen tikla: bit.ly/b0nuswin"
        ),
        (
            title: "Yanlış Pozitif",
            sender: "OKUL",
            message: "Bugunku ders konusu betimleme ve anlatim teknikleri."
        ),
        (
            title: "Temiz SMS",
            sender: "PTT",
            message: "Kargonuz dagitima cikmistir. Detaylar icin resmi kanallari kontrol ediniz."
        )
    ]

    private var decision: AppGroupStorage.FilterDecision {
        AppGroupStorage.shared.decision(sender: sender, body: message)
    }

    private var sensitivity: AppGroupStorage.FilterSensitivity {
        AppGroupStorage.shared.filterSensitivity
    }

    private var scoreRatio: Double {
        guard sensitivity.keywordThreshold > 0 else { return 0 }
        return min(Double(decision.score) / Double(sensitivity.keywordThreshold), 1)
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                examplesCard

                VStack(alignment: .leading, spacing: 10) {
                    Text("Gönderici")
                        .font(.caption)
                        .foregroundStyle(CS.secondary)
                    TextField("INNOVIXLTD", text: $sender)
                        .textInputAutocapitalization(.characters)
                        .padding(14)
                        .background(CS.surface)
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                }

                VStack(alignment: .leading, spacing: 10) {
                    Text("Mesaj")
                        .font(.caption)
                        .foregroundStyle(CS.secondary)
                    TextEditor(text: $message)
                        .frame(minHeight: 150)
                        .padding(10)
                        .background(CS.surface)
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                }

                resultCard
                recommendationCard
            }
            .padding(16)
        }
        .background(CS.bg.ignoresSafeArea())
        .navigationTitle("Filtre Testi")
        .navigationBarTitleDisplayMode(.large)
    }

    private var examplesCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Hazır Testler")
                .font(.headline)
                .foregroundStyle(CS.primary)

            LazyVGrid(columns: [GridItem(.adaptive(minimum: 130), spacing: 10)], spacing: 10) {
                ForEach(examples, id: \.title) { example in
                    Button {
                        sender = example.sender
                        message = example.message
                    } label: {
                        Text(example.title)
                            .font(.caption.weight(.semibold))
                            .foregroundStyle(CS.primary)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 10)
                            .background(CS.surfaceVar)
                            .clipShape(RoundedRectangle(cornerRadius: 10))
                    }
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(CS.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    private var resultCard: some View {
        VStack(alignment: .leading, spacing: 14) {
            Label(
                decision.shouldBlock ? "Bu mesaj engellenir" : "Bu mesaj geçer",
                systemImage: decision.shouldBlock ? "nosign" : "checkmark.shield"
            )
            .font(.headline)
            .foregroundStyle(decision.shouldBlock ? CS.red : CS.accent)

            Text(reasonTitle)
                .font(.subheadline.weight(.semibold))
                .foregroundStyle(CS.primary)

            Text(resultDetail)
                .font(.subheadline)
                .foregroundStyle(CS.secondary)

            VStack(alignment: .leading, spacing: 8) {
                HStack {
                    Text("Risk skoru")
                        .font(.caption)
                        .foregroundStyle(CS.secondary)
                    Spacer()
                    Text("\(decision.score) / \(sensitivity.keywordThreshold)")
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(decision.shouldBlock ? CS.red : CS.primary)
                }

                GeometryReader { geo in
                    ZStack(alignment: .leading) {
                        RoundedRectangle(cornerRadius: 4)
                            .fill(CS.surfaceVar)
                            .frame(height: 7)
                        RoundedRectangle(cornerRadius: 4)
                            .fill(decision.shouldBlock ? CS.red : CS.accent)
                            .frame(width: geo.size.width * scoreRatio, height: 7)
                    }
                }
                .frame(height: 7)
            }

            analysisRow(label: "Aktif hassasiyet", value: sensitivity.title)

            if let matched = decision.matchedKeyword {
                analysisRow(label: "İlk eşleşme", value: matched)
            }

            analysisRow(label: "Karar sebebi", value: reasonLabel)
        }
        .padding(16)
        .background(CS.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    private var recommendationCard: some View {
        VStack(alignment: .leading, spacing: 10) {
            Label("Öneri", systemImage: "lightbulb.fill")
                .font(.headline)
                .foregroundStyle(CS.orange)

            Text(recommendationText)
                .font(.subheadline)
                .foregroundStyle(CS.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(CS.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    private func analysisRow(label: String, value: String) -> some View {
        HStack {
            Text(label)
                .font(.caption)
                .foregroundStyle(CS.secondary)
            Spacer()
            Text(value)
                .font(.caption.weight(.semibold))
                .foregroundStyle(CS.primary)
        }
    }

    private var reasonTitle: String {
        switch decision.reason {
        case "sender":
            return "Gönderici doğrudan engelli listede."
        case "sender_rule":
            return "Gönderici başlık kuralıyla eşleşti."
        case "alphanumeric":
            return "Alfanümerik gönderici ve içerik birlikte şüpheli."
        case "link":
            return "Mesaj riskli link içeriyor."
        case "safe_keyword":
            return "Güvenli kelime mesajı korudu."
        case "":
            return "Yeterli risk sinyali oluşmadı."
        default:
            return "Mesaj içeriği spam anahtar kelimeleri taşıyor."
        }
    }

    private var resultDetail: String {
        if decision.detail.isEmpty {
            return "Şu anki kurallarla mesaj engelleme eşiğine ulaşmadı."
        }
        return decision.detail
    }

    private var reasonLabel: String {
        switch decision.reason {
        case "sender":
            return "Engelli numara"
        case "sender_rule":
            return "Başlık kuralı"
        case "alphanumeric":
            return "Alfanümerik + içerik"
        case "link":
            return "Riskli link"
        case "safe_keyword":
            return "Güvenli kelime"
        case "keyword":
            return "Anahtar kelime"
        default:
            return "Geçti"
        }
    }

    private var recommendationText: String {
        switch decision.reason {
        case "safe_keyword":
            return "Bu kelime yanlış pozitifleri azaltıyor. Eğer mesaj yine de spam ise güvenli kelime listesinden kaldır."
        case "link":
            return "Riskli link kuralı çalışıyor. Güvenilir kurum linkleri yanlış yakalanırsa göndericiyi güvenilir başlıklara ekle."
        case "sender_rule", "sender":
            return "Gönderici bazlı engel çalışıyor. Yanlışsa ilgili numara veya başlığı güvenilir listeye taşı."
        case "alphanumeric", "keyword":
            return "Bu örnek engellenir. Kaçan benzer SMS varsa gönderen başlığını da engellenen başlıklara eklemek yakalama oranını artırır."
        default:
            return "Bu örnek geçer. Bahis içeriği olmasına rağmen geçiyorsa hassasiyeti Agresif yap veya eksik kelimeyi spam raporuna ekle."
        }
    }
}

private struct SenderRulesView: View {
    @State private var blockedRules: [String] = []
    @State private var trustedRules: [String] = []
    @State private var showAddBlocked = false
    @State private var showAddTrusted = false
    @State private var draftRule = ""

    private let trustedPresets = [
        "AKBANK",
        "GARANTI",
        "ISBANK",
        "YAPIKREDI",
        "QNB",
        "EDEVLET",
        "PTT",
        "MNGKARGO",
        "YURTICIKARGO",
        "HEPSIJET",
        "ARASKARGO",
        "TRENDYOLEXPRESS"
    ]

    var body: some View {
        ScrollView {
            VStack(spacing: 18) {
                infoCard
                rulesCard(
                    title: "Engellenen Başlıklar",
                    icon: "nosign",
                    color: CS.red,
                    items: blockedRules,
                    emptyText: "Henüz engellenen gönderici kuralı yok.",
                    addAction: { showAddBlocked = true },
                    removeAction: removeBlockedRule
                )
                rulesCard(
                    title: "Güvenilir Başlıklar",
                    icon: "checkmark.shield.fill",
                    color: CS.accent,
                    items: trustedRules,
                    emptyText: "Henüz güvenilir gönderici kuralı yok.",
                    addAction: { showAddTrusted = true },
                    removeAction: removeTrustedRule
                )
                presetsCard
            }
            .padding(16)
        }
        .background(CS.bg.ignoresSafeArea())
        .navigationTitle("Başlık Kuralları")
        .navigationBarTitleDisplayMode(.large)
        .onAppear(perform: reload)
        .alert("Engellenen Başlık Ekle", isPresented: $showAddBlocked) {
            TextField("INNOVIX, BETXYZ", text: $draftRule)
                .textInputAutocapitalization(.characters)
            Button("Ekle") { addBlockedRule() }
            Button("İptal", role: .cancel) { draftRule = "" }
        }
        .alert("Güvenilir Başlık Ekle", isPresented: $showAddTrusted) {
            TextField("BANKA, E-DEVLET", text: $draftRule)
                .textInputAutocapitalization(.characters)
            Button("Ekle") { addTrustedRule() }
            Button("İptal", role: .cancel) { draftRule = "" }
        }
    }

    private var infoCard: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Gönderici başlıkları tam eşleşmek zorunda değil.")
                .font(.subheadline.weight(.semibold))
                .foregroundStyle(CS.primary)
            Text("Örneğin `INNOVIX` kuralı, `INNOVIXLTD` gibi alfanümerik başlıkları da yakalar.")
                .font(.subheadline)
                .foregroundStyle(CS.secondary)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(CS.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    private var presetsCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Güvenilir Preset'ler")
                .font(.headline)
                .foregroundStyle(CS.primary)
            Text("Banka, kargo ve resmi servis başlıklarını tek dokunuşla ekle.")
                .font(.subheadline)
                .foregroundStyle(CS.secondary)

            LazyVGrid(columns: [GridItem(.adaptive(minimum: 120), spacing: 10)], spacing: 10) {
                ForEach(trustedPresets, id: \.self) { preset in
                    Button {
                        AppGroupStorage.shared.addTrustedSenderRule(preset)
                        reload()
                    } label: {
                        Text(preset)
                            .font(.caption.weight(.semibold))
                            .foregroundStyle(CS.primary)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 10)
                            .background(CS.surfaceVar)
                            .clipShape(RoundedRectangle(cornerRadius: 10))
                    }
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(CS.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    private func rulesCard(
        title: String,
        icon: String,
        color: Color,
        items: [String],
        emptyText: String,
        addAction: @escaping () -> Void,
        removeAction: @escaping (String) -> Void
    ) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Label(title, systemImage: icon)
                    .font(.headline)
                    .foregroundStyle(color)
                Spacer()
                Button("Ekle", action: addAction)
                    .font(.caption.weight(.semibold))
                    .foregroundStyle(CS.accent)
            }

            if items.isEmpty {
                Text(emptyText)
                    .font(.subheadline)
                    .foregroundStyle(CS.secondary)
            } else {
                ForEach(items, id: \.self) { item in
                    HStack {
                        Text(item)
                            .font(.subheadline.weight(.medium))
                            .foregroundStyle(CS.primary)
                        Spacer()
                        Button {
                            removeAction(item)
                        } label: {
                            Image(systemName: "trash")
                                .foregroundStyle(CS.red)
                        }
                    }
                    .padding(.vertical, 4)
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(CS.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }

    private func addBlockedRule() {
        let rule = draftRule.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !rule.isEmpty else { return }
        AppGroupStorage.shared.addBlockedSenderRule(rule)
        draftRule = ""
        reload()
    }

    private func addTrustedRule() {
        let rule = draftRule.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !rule.isEmpty else { return }
        AppGroupStorage.shared.addTrustedSenderRule(rule)
        draftRule = ""
        reload()
    }

    private func removeBlockedRule(_ rule: String) {
        AppGroupStorage.shared.removeBlockedSenderRule(rule)
        reload()
    }

    private func removeTrustedRule(_ rule: String) {
        AppGroupStorage.shared.removeTrustedSenderRule(rule)
        reload()
    }

    private func reload() {
        blockedRules = Array(AppGroupStorage.shared.blockedSenderRules).sorted()
        trustedRules = Array(AppGroupStorage.shared.trustedSenderRules).sorted()
    }
}

private struct SpamReportView: View {
    @State private var sender = ""
    @State private var message = ""
    @State private var note = ""
    @State private var reports: [AppGroupStorage.SpamReport] = []

    var body: some View {
        ScrollView {
            VStack(spacing: 18) {
                VStack(alignment: .leading, spacing: 10) {
                    Text("Gönderici")
                        .font(.caption)
                        .foregroundStyle(CS.secondary)
                    TextField("INNOVIXLTD veya +90...", text: $sender)
                        .textInputAutocapitalization(.characters)
                        .padding(14)
                        .background(CS.surface)
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                }

                VStack(alignment: .leading, spacing: 10) {
                    Text("Mesaj")
                        .font(.caption)
                        .foregroundStyle(CS.secondary)
                    TextEditor(text: $message)
                        .frame(minHeight: 140)
                        .padding(10)
                        .background(CS.surface)
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                }

                VStack(alignment: .leading, spacing: 10) {
                    Text("Not")
                        .font(.caption)
                        .foregroundStyle(CS.secondary)
                    TextField("Örn. filtre kaçırdı, bahis dili yoğun", text: $note)
                        .padding(14)
                        .background(CS.surface)
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                }

                Button("Yerel Rapora Ekle") {
                    addReport()
                }
                .font(.headline)
                .foregroundStyle(.black)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
                .background(CS.accent)
                .clipShape(RoundedRectangle(cornerRadius: 14))

                VStack(alignment: .leading, spacing: 12) {
                    Text("Son Raporlar")
                        .font(.headline)
                        .foregroundStyle(CS.primary)

                    if reports.isEmpty {
                        Text("Henüz yerel spam raporu yok.")
                            .font(.subheadline)
                            .foregroundStyle(CS.secondary)
                    } else {
                        ForEach(reports.reversed()) { report in
                            VStack(alignment: .leading, spacing: 6) {
                                Text(report.sender.isEmpty ? "Bilinmeyen" : report.sender)
                                    .font(.subheadline.weight(.semibold))
                                    .foregroundStyle(CS.primary)
                                if !report.message.isEmpty {
                                    Text(report.message)
                                        .font(.caption)
                                        .foregroundStyle(CS.secondary)
                                        .lineLimit(2)
                                }
                                if !report.note.isEmpty {
                                    Text(report.note)
                                        .font(.caption2)
                                        .foregroundStyle(CS.disabled)
                                }
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(14)
                            .background(CS.surface)
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                        }
                    }
                }
            }
            .padding(16)
        }
        .background(CS.bg.ignoresSafeArea())
        .navigationTitle("Spam Raporla")
        .navigationBarTitleDisplayMode(.large)
        .onAppear { reports = AppGroupStorage.shared.spamReports }
    }

    private func addReport() {
        guard !sender.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || !message.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else { return }
        AppGroupStorage.shared.addSpamReport(sender: sender, message: message, note: note)
        sender = ""
        message = ""
        note = ""
        reports = AppGroupStorage.shared.spamReports
    }
}
