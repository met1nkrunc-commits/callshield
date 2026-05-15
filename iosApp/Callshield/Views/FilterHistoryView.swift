import SwiftUI

struct FilterHistoryView: View {
    @Environment(\.scenePhase) private var scenePhase
    @State private var messages: [AppGroupStorage.FilteredMessage] = []
    @State private var statusMessage: String?

    var body: some View {
        Group {
            if messages.isEmpty {
                emptyState
            } else {
                list
            }
        }
        .background(CS.bg.ignoresSafeArea())
        .navigationTitle("Filtre Kayıtları")
        .navigationBarTitleDisplayMode(.large)
        .onAppear(perform: reload)
        .onChange(of: scenePhase) { phase in
            if phase == .active {
                reload()
            }
        }
    }

    private var list: some View {
        ScrollView {
            LazyVStack(spacing: 10) {
                if let statusMessage {
                    Text(statusMessage)
                        .font(.caption.weight(.semibold))
                        .foregroundStyle(CS.accent)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(12)
                        .background(CS.accent.opacity(0.12))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }

                ForEach(Array(messages.enumerated()), id: \.offset) { _, msg in
                    VStack(alignment: .leading, spacing: 12) {
                        HStack(spacing: 14) {
                            Image(systemName: "nosign")
                                .font(.system(size: 20))
                                .foregroundStyle(CS.red)
                                .frame(width: 28)

                            VStack(alignment: .leading, spacing: 4) {
                                Text(msg.sender.isEmpty ? "Bilinmeyen" : msg.sender)
                                    .font(.subheadline.weight(.medium))
                                    .foregroundStyle(CS.primary)

                                Text(reasonTitle(for: msg))
                                    .font(.caption)
                                    .foregroundStyle(CS.secondary)

                                if !msg.detail.isEmpty {
                                    Text(msg.detail)
                                        .font(.caption2)
                                        .foregroundStyle(CS.disabled)
                                }

                                if !msg.preview.isEmpty {
                                    Text(msg.preview)
                                        .font(.caption2)
                                        .foregroundStyle(CS.secondary)
                                        .lineLimit(2)
                                }
                            }

                            Spacer()

                            Text(msg.date, style: .relative)
                                .font(.caption2)
                                .foregroundStyle(CS.disabled)
                        }

                        if !msg.sender.isEmpty {
                            HStack(spacing: 10) {
                                Button {
                                    markAsFalsePositive(msg)
                                } label: {
                                    Label("Yanlış engellendi", systemImage: "checkmark.shield")
                                        .font(.caption.weight(.semibold))
                                }
                                .foregroundStyle(CS.accent)

                                Spacer()

                                Button {
                                    blockSimilar(msg)
                                } label: {
                                    Label("Benzerlerini engelle", systemImage: "plus.circle")
                                        .font(.caption.weight(.semibold))
                                }
                                .foregroundStyle(CS.orange)
                            }
                            .padding(.top, 2)
                        }
                    }
                    .padding(14)
                    .background(CS.surface)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 16)
        }
    }

    private var emptyState: some View {
        VStack(spacing: 16) {
            Image(systemName: "checkmark.shield.fill")
                .font(.system(size: 52))
                .foregroundStyle(CS.accent)
            Text("Engellenen SMS yok")
                .font(.title3).fontWeight(.semibold)
                .foregroundStyle(CS.primary)
            Text("Filtre motorunun uygulama içine yazabildiği son kayıtlar burada görünür.")
                .font(.subheadline)
                .foregroundStyle(CS.secondary)
                .multilineTextAlignment(.center)
        }
        .padding(40)
    }

    private func reasonTitle(for message: AppGroupStorage.FilteredMessage) -> String {
        switch message.reason {
        case "sender":
            return "Engellenen gönderici"
        case "sender_rule":
            return "Gönderici kuralı"
        case "alphanumeric":
            return "Alfanümerik spam gönderici"
        case "link":
            return "Riskli link"
        default:
            return "Spam anahtar kelime"
        }
    }

    private func reload() {
        messages = AppGroupStorage.shared.filteredMessages.reversed()
    }

    private func markAsFalsePositive(_ message: AppGroupStorage.FilteredMessage) {
        let sender = message.sender.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !sender.isEmpty else { return }

        if isPhoneLike(sender) {
            AppGroupStorage.shared.addTrusted(sender)
            statusMessage = "\(sender) güvenilir numaralara eklendi."
        } else {
            AppGroupStorage.shared.addTrustedSenderRule(sender)
            statusMessage = "\(sender) güvenilir başlıklara eklendi."
        }
    }

    private func blockSimilar(_ message: AppGroupStorage.FilteredMessage) {
        let sender = message.sender.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !sender.isEmpty else { return }

        if isPhoneLike(sender) {
            AppGroupStorage.shared.addBlocked(sender)
            statusMessage = "\(sender) engellenen numaralara eklendi."
        } else {
            AppGroupStorage.shared.addBlockedSenderRule(sender)
            statusMessage = "\(sender) engellenen başlıklara eklendi."
        }
    }

    private func isPhoneLike(_ sender: String) -> Bool {
        sender.contains { $0.isNumber } && !sender.contains { $0.isLetter }
    }
}
