import SwiftUI
import UserNotifications

struct OnboardingView: View {
    @AppStorage("onboardingDone") private var onboardingDone = false
    @AppStorage("filterSetupConfirmed") private var filterSetupConfirmed = false
    @State private var page = 0

    var body: some View {
        ZStack {
            CS.bg.ignoresSafeArea()
            VStack(spacing: 0) {
                TabView(selection: $page) {
                    WelcomePage().tag(0)
                    SmsFilterPage().tag(1)
                    NotificationPage().tag(2)
                }
                .tabViewStyle(.page(indexDisplayMode: .never))

                // Dots
                HStack(spacing: 8) {
                    ForEach(0..<3) { i in
                        Circle()
                            .fill(i == page ? CS.accent : CS.disabled)
                            .frame(width: i == page ? 10 : 6, height: i == page ? 10 : 6)
                            .animation(.spring(response: 0.3), value: page)
                    }
                }
                .padding(.bottom, 20)

                Button(action: next) {
                    Text(buttonTitle)
                        .font(.headline)
                        .foregroundStyle(.black)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 16)
                        .background(CS.accent)
                        .clipShape(RoundedRectangle(cornerRadius: 14))
                }
                .padding(.horizontal, 24)
                .padding(.bottom, 40)
            }
        }
    }

    private var buttonTitle: String {
        switch page {
        case 2: return "Bildirim İzni Ver"
        case 1: return filterSetupConfirmed ? "Devam Et" : "Önce Filtreyi Onayla"
        default: return "Devam Et"
        }
    }

    private func next() {
        if page == 1 && !filterSetupConfirmed {
            return
        }

        if page < 2 {
            withAnimation { page += 1 }
        } else {
            Task {
                _ = try? await UNUserNotificationCenter.current()
                    .requestAuthorization(options: [.alert, .sound, .badge])
                await MainActor.run {
                    onboardingDone = true
                }
            }
        }
    }
}

// MARK: - Pages
private struct WelcomePage: View {
    @State private var scale: CGFloat = 1.0
    var body: some View {
        VStack(spacing: 28) {
            Spacer()
            ZStack {
                Circle().fill(CS.accent.opacity(0.12)).frame(width: 148, height: 148).scaleEffect(scale)
                BrandMarkView(size: 104)
            }
            .onAppear {
                withAnimation(.easeInOut(duration: 1.8).repeatForever(autoreverses: true)) { scale = 1.15 }
            }
            VStack(spacing: 12) {
                Text("Siper'e Hoş Geldin").font(.title).fontWeight(.bold).foregroundStyle(CS.primary)
                Text("Spam SMS'leri ve sahte aramaları tespit eden, Türkiye'ye özel koruma uygulaması.")
                    .font(.subheadline).foregroundStyle(CS.secondary).multilineTextAlignment(.center)
            }
            featureRow(icon: "message.fill",       color: CS.accent,  text: "SMS spam filtreleme")
            featureRow(icon: "shield.lefthalf.filled", color: CS.orange, text: "Engellenen numara listesi")
            featureRow(icon: "arrow.clockwise",     color: CS.accent,  text: "Otomatik liste güncellemesi")
            Spacer()
        }
        .padding(.horizontal, 32)
    }
    private func featureRow(icon: String, color: Color, text: String) -> some View {
        HStack(spacing: 14) {
            Image(systemName: icon).foregroundStyle(color).frame(width: 24)
            Text(text).font(.subheadline).foregroundStyle(CS.primary)
            Spacer()
        }
        .padding(14).background(CS.surface).clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

private struct SmsFilterPage: View {
    @Environment(\.openURL) private var openURL
    @AppStorage("filterSetupConfirmed") private var filterSetupConfirmed = false

    var body: some View {
        VStack(spacing: 24) {
            Spacer()
            Image(systemName: "message.badge.filled.fill").font(.system(size: 64)).foregroundStyle(CS.accent)
            VStack(spacing: 10) {
                Text("SMS Filtresini Aktif Et").font(.title2).fontWeight(.bold).foregroundStyle(CS.primary)
                Text("Spam SMS'lerin otomatik filtrelenmesi için Mesajlar ayarlarında Siper'i seçmen gerekiyor.")
                    .font(.subheadline).foregroundStyle(CS.secondary).multilineTextAlignment(.center)
            }
            Text("iOS bu özellik için doğrudan Bilinmeyen ve Spam ekranını açtırmaz. Buton yalnızca uygulama ayarlarını açar; aşağıdaki yolu manuel izlemen gerekir.")
                .font(.footnote)
                .foregroundStyle(CS.secondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 4)

            Button {
                guard let url = URL(string: UIApplication.openSettingsURLString) else { return }
                openURL(url)
            } label: {
                Label("Uygulama Ayarlarını Aç", systemImage: "arrow.up.right.square")
                    .font(.headline)
                    .foregroundStyle(.black)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(CS.accent)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }

            VStack(spacing: 0) {
                ForEach(Array(steps.enumerated()), id: \.offset) { idx, step in
                    HStack(spacing: 14) {
                        ZStack {
                            Circle().fill(CS.accent).frame(width: 26, height: 26)
                            Text("\(idx+1)").font(.caption.bold()).foregroundStyle(.black)
                        }
                        Text(step).font(.subheadline).foregroundStyle(CS.primary)
                        Spacer()
                    }
                    .padding(14)
                    if idx < steps.count - 1 { Divider().background(CS.surfaceVar).padding(.leading, 54) }
                }
            }
            .background(CS.surface).clipShape(RoundedRectangle(cornerRadius: 14))

            Button {
                filterSetupConfirmed.toggle()
            } label: {
                Label(
                    filterSetupConfirmed ? "Etkinleştirme Onaylı" : "Siper'i Seçtim",
                    systemImage: filterSetupConfirmed ? "checkmark.circle.fill" : "checkmark.circle"
                )
                .font(.headline)
                .foregroundStyle(filterSetupConfirmed ? .black : CS.primary)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
                .background(filterSetupConfirmed ? CS.accent : CS.surface)
                .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            Spacer()
        }
        .padding(.horizontal, 28)
    }
    private let steps = ["Ayarlar → Mesajlar", "Bilinmeyen ve Spam", "SMS Filtresi", "Siper'i seç"]
}

private struct NotificationPage: View {
    var body: some View {
        VStack(spacing: 28) {
            Spacer()
            Image(systemName: "bell.badge.fill").font(.system(size: 64)).foregroundStyle(CS.accent)
            VStack(spacing: 10) {
                Text("Bildirimlere İzin Ver").font(.title2).fontWeight(.bold).foregroundStyle(CS.primary)
                Text("Önemli güvenlik uyarılarını kaçırmamak için bildirimleri etkinleştir.")
                    .font(.subheadline).foregroundStyle(CS.secondary).multilineTextAlignment(.center)
            }
            VStack(alignment: .leading, spacing: 12) {
                infoRow("Spam listesi güncellemesi")
                infoRow("Yeni güvenlik uyarıları")
            }
            .padding(16).background(CS.surface).clipShape(RoundedRectangle(cornerRadius: 14))
            Spacer()
        }
        .padding(.horizontal, 28)
    }
    private func infoRow(_ text: String) -> some View {
        HStack(spacing: 10) {
            Image(systemName: "checkmark.circle.fill").foregroundStyle(CS.accent)
            Text(text).font(.subheadline).foregroundStyle(CS.primary)
        }
    }
}
