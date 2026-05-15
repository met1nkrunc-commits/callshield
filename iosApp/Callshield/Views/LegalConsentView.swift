import SwiftUI

struct LegalConsentView: View {
    @AppStorage("kvkkConsentAccepted") private var kvkkConsentAccepted = false
    @AppStorage("kvkkConsentAcceptedAt") private var kvkkConsentAcceptedAt = 0.0
    @AppStorage("kvkkConsentVersion") private var kvkkConsentVersion = ""

    @State private var acceptedPrivacy = false
    @State private var acceptedTerms = false

    private var canContinue: Bool {
        acceptedPrivacy && acceptedTerms
    }

    var body: some View {
        ZStack {
            CS.bg.ignoresSafeArea()

            ScrollView {
                VStack(alignment: .leading, spacing: 22) {
                    header
                    legalSummary
                    consentControls
                    continueButton
                    footer
                }
                .padding(.horizontal, 22)
                .padding(.top, 34)
                .padding(.bottom, 28)
            }
        }
        .preferredColorScheme(.dark)
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 12) {
            BrandMarkView(size: 64)

            Text("KVKK ve Kullanım Onayı")
                .font(.largeTitle.weight(.bold))
                .foregroundStyle(CS.primary)

            Text("Siper'i kullanmadan önce veri işleme, SMS filtreleme ve abonelik koşullarını okuyup onaylaman gerekir.")
                .font(.subheadline)
                .foregroundStyle(CS.secondary)
                .fixedSize(horizontal: false, vertical: true)
        }
    }

    private var legalSummary: some View {
        VStack(alignment: .leading, spacing: 14) {
            legalSection(
                title: "KVKK Aydınlatma Özeti",
                icon: "lock.shield.fill",
                rows: [
                    "SMS içeriği spam analizi için cihazda işlenir; temel filtreleme için sunucuya gönderilmez.",
                    "Engellenen/güvenilir numaralar ve filtre geçmişi cihazda veya App Group alanında saklanır.",
                    "Dolandırıcı numara listesi güncellemeleri için internet bağlantısı kullanılabilir.",
                    "Premium özelliklerde, açıkça kullanılan sorgular üçüncü taraf risk analiz servislerine gönderilebilir."
                ]
            )

            legalSection(
                title: "Reklam ve Satın Alma",
                icon: "creditcard.fill",
                rows: [
                    "Ücretsiz kullanımda Google AdMob ile reklam gösterilebilir.",
                    "Premium abonelik App Store üzerinden yönetilir ve aktifken uygulama içi banner reklamlar kaldırılır.",
                    "Deneme süresi, fiyat ve yenileme bilgileri Apple satın alma ekranında ayrıca gösterilir."
                ]
            )

            legalSection(
                title: "Sorumluluk Sınırı",
                icon: "exclamationmark.triangle.fill",
                rows: [
                    "Siper destekleyici güvenlik aracıdır; tüm dolandırıcılık girişimlerini kesin olarak yakalayacağını garanti etmez.",
                    "Yanlış pozitif veya kaçan tehdit riskleri tamamen ortadan kaldırılamaz."
                ]
            )
        }
    }

    private var consentControls: some View {
        VStack(spacing: 12) {
            ConsentToggle(
                isOn: $acceptedPrivacy,
                title: "KVKK aydınlatma metnini okudum ve kişisel verilerimin bu kapsamda işlenmesini kabul ediyorum."
            )

            ConsentToggle(
                isOn: $acceptedTerms,
                title: "Kullanım koşullarını, reklam gösterimini ve App Store abonelik koşullarını kabul ediyorum."
            )
        }
    }

    private var continueButton: some View {
        Button {
            kvkkConsentAcceptedAt = Date().timeIntervalSince1970
            kvkkConsentVersion = Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "1.0"
            kvkkConsentAccepted = true
        } label: {
            Text("Kabul Et ve Devam Et")
                .font(.headline)
                .foregroundStyle(canContinue ? .black : CS.disabled)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background(canContinue ? CS.accent : CS.surfaceVar)
                .clipShape(RoundedRectangle(cornerRadius: 14))
        }
        .disabled(!canContinue)
    }

    private var footer: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Onay vermeden uygulamaya devam edilemez. Detaylı metinler uygulama içinde Ayarlar bölümünden ve App Store sayfasından erişilebilir.")
                .font(.caption)
                .foregroundStyle(CS.disabled)

            Text("Son güncelleme: 13 Mayıs 2026")
                .font(.caption2)
                .foregroundStyle(CS.disabled)
        }
    }

    private func legalSection(title: String, icon: String, rows: [String]) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Label(title, systemImage: icon)
                .font(.headline)
                .foregroundStyle(CS.primary)

            ForEach(rows, id: \.self) { row in
                HStack(alignment: .top, spacing: 10) {
                    Circle()
                        .fill(CS.accent)
                        .frame(width: 6, height: 6)
                        .padding(.top, 7)
                    Text(row)
                        .font(.footnote)
                        .foregroundStyle(CS.secondary)
                        .fixedSize(horizontal: false, vertical: true)
                }
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(CS.surface)
        .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

private struct ConsentToggle: View {
    @Binding var isOn: Bool
    let title: String

    var body: some View {
        Button {
            isOn.toggle()
        } label: {
            HStack(alignment: .top, spacing: 12) {
                Image(systemName: isOn ? "checkmark.square.fill" : "square")
                    .font(.system(size: 22, weight: .semibold))
                    .foregroundStyle(isOn ? CS.accent : CS.secondary)

                Text(title)
                    .font(.footnote.weight(.medium))
                    .foregroundStyle(CS.primary)
                    .fixedSize(horizontal: false, vertical: true)

                Spacer(minLength: 0)
            }
            .padding(14)
            .background(CS.surface)
            .clipShape(RoundedRectangle(cornerRadius: 14))
        }
        .buttonStyle(.plain)
    }
}
