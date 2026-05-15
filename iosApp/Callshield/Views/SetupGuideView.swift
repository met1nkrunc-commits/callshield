import SwiftUI

struct SetupGuideView: View {
    @AppStorage("filterSetupConfirmed") private var filterSetupConfirmed = false
    private let steps: [(String, String)] = [
        ("Ayarlar'ı Açın",          "gear"),
        ("Mesajlar'a girin",         "message.fill"),
        ("Bilinmeyen ve Spam",       "person.fill.questionmark"),
        ("SMS Filtresi'ne dokunun",  "checkmark.shield"),
        ("Siper'i seçin",            "shield.fill"),
    ]

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // Info card
                HStack(spacing: 14) {
                    Image(systemName: "info.circle.fill")
                        .font(.title2)
                        .foregroundStyle(CS.accent)
                    Text("SMS filtrelemeyi aktif etmek için bu adımları izleyin. Uygulama yalnızca kendi ayar sayfasını açabilir; Mesajlar > Bilinmeyen ve Spam bölümüne manuel girmen gerekir.")
                        .font(.subheadline)
                        .foregroundStyle(CS.secondary)
                }
                .padding(16)
                .background(CS.surface)
                .clipShape(RoundedRectangle(cornerRadius: 14))

                // Steps
                VStack(spacing: 0) {
                    ForEach(Array(steps.enumerated()), id: \.offset) { idx, step in
                        HStack(spacing: 16) {
                            ZStack {
                                Circle()
                                    .fill(CS.accent)
                                    .frame(width: 30, height: 30)
                                Text("\(idx + 1)")
                                    .font(.caption.bold())
                                    .foregroundStyle(.black)
                            }
                            Label(step.0, systemImage: step.1)
                                .font(.subheadline.weight(.medium))
                                .foregroundStyle(CS.primary)
                            Spacer()
                        }
                        .padding(16)

                        if idx < steps.count - 1 {
                            Divider().background(CS.surfaceVar).padding(.leading, 62)
                        }
                    }
                }
                .background(CS.surface)
                .clipShape(RoundedRectangle(cornerRadius: 14))

                // Open Settings button
                Button {
                    if let url = URL(string: UIApplication.openSettingsURLString) {
                        UIApplication.shared.open(url)
                    }
                } label: {
                    Label("Uygulama Ayarlarını Aç", systemImage: "arrow.up.right.square")
                        .font(.headline)
                        .foregroundStyle(.black)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(CS.accent)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }

                Text("Sonraki adım: Ayarlar uygulamasında Mesajlar > Bilinmeyen ve Spam > SMS Filtresi yolunu manuel aç.")
                    .font(.caption)
                    .foregroundStyle(CS.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 12)

                Button {
                    filterSetupConfirmed.toggle()
                } label: {
                    Label(
                        filterSetupConfirmed ? "Kurulum Onayını Kaldır" : "Filtreyi Etkinleştirdim",
                        systemImage: filterSetupConfirmed ? "xmark.circle" : "checkmark.circle.fill"
                    )
                    .font(.headline)
                    .foregroundStyle(filterSetupConfirmed ? CS.primary : .black)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(filterSetupConfirmed ? CS.surfaceVar : CS.accent)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }

                Spacer(minLength: 32)
            }
            .padding(.horizontal, 16)
            .padding(.top, 20)
        }
        .background(CS.bg.ignoresSafeArea())
        .navigationTitle("Kurulum Rehberi")
        .navigationBarTitleDisplayMode(.inline)
    }
}
