import SwiftUI

struct FilterHistoryView: View {
    @State private var messages: [AppGroupStorage.FilteredMessage] = []

    var body: some View {
        Group {
            if messages.isEmpty {
                emptyState
            } else {
                list
            }
        }
        .background(CS.bg.ignoresSafeArea())
        .navigationTitle("Engellenen SMS")
        .navigationBarTitleDisplayMode(.large)
        .onAppear { messages = AppGroupStorage.shared.filteredMessages.reversed() }
    }

    private var list: some View {
        ScrollView {
            LazyVStack(spacing: 10) {
                ForEach(Array(messages.enumerated()), id: \.offset) { _, msg in
                    HStack(spacing: 14) {
                        Image(systemName: "nosign")
                            .font(.system(size: 20))
                            .foregroundStyle(CS.red)
                            .frame(width: 28)

                        VStack(alignment: .leading, spacing: 4) {
                            Text(msg.sender.isEmpty ? "Bilinmeyen" : msg.sender)
                                .font(.subheadline).fontWeight(.medium)
                                .foregroundStyle(CS.primary)

                            Text(msg.reason == "sender" ? "Engellenen gönderici" : "Spam anahtar kelime")
                                .font(.caption)
                                .foregroundStyle(CS.secondary)
                        }

                        Spacer()

                        Text(msg.date, style: .relative)
                            .font(.caption2)
                            .foregroundStyle(CS.disabled)
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
            Text("Spam SMS filtrelendiğinde burada görünür.")
                .font(.subheadline)
                .foregroundStyle(CS.secondary)
                .multilineTextAlignment(.center)
        }
        .padding(40)
    }
}
