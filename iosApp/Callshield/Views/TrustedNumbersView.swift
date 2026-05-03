import SwiftUI

struct TrustedNumbersView: View {
    @State private var numbers: [String] = []
    @State private var showAdd = false
    @State private var newNumber = ""

    var body: some View {
        ZStack {
            CS.bg.ignoresSafeArea()
            if numbers.isEmpty { emptyState } else { list }
        }
        .navigationTitle("Güvenilir Numaralar")
        .navigationBarTitleDisplayMode(.large)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button { showAdd = true } label: {
                    Image(systemName: "plus").foregroundStyle(CS.accent)
                }
            }
        }
        .alert("Güvenilir Numara Ekle", isPresented: $showAdd) {
            TextField("+90 5XX XXX XX XX", text: $newNumber).keyboardType(.phonePad)
            Button("Ekle") { addNumber() }
            Button("İptal", role: .cancel) { newNumber = "" }
        }
        .onAppear { reload() }
    }

    private var list: some View {
        ScrollView {
            LazyVStack(spacing: 10) {
                ForEach(numbers, id: \.self) { number in
                    HStack(spacing: 14) {
                        Image(systemName: "checkmark.shield.fill")
                            .font(.system(size: 18)).foregroundStyle(CS.accent).frame(width: 24)
                        Text(number)
                            .font(.subheadline).fontWeight(.medium).foregroundStyle(CS.primary)
                        Spacer()
                        Button {
                            AppGroupStorage.shared.removeTrusted(number); reload()
                        } label: {
                            Image(systemName: "trash").font(.system(size: 15)).foregroundStyle(CS.red.opacity(0.8))
                        }
                    }
                    .padding(14).background(CS.surface).clipShape(RoundedRectangle(cornerRadius: 12))
                }
            }
            .padding(.horizontal, 16).padding(.top, 16)
        }
    }

    private var emptyState: some View {
        VStack(spacing: 16) {
            Image(systemName: "person.crop.circle.badge.checkmark")
                .font(.system(size: 52)).foregroundStyle(CS.accent)
            Text("Güvenilir numara yok")
                .font(.title3).fontWeight(.semibold).foregroundStyle(CS.primary)
            Text("Güvendiğin numaraları ekle, spam olarak işaretlenmez.")
                .font(.subheadline).foregroundStyle(CS.secondary).multilineTextAlignment(.center)
        }
        .padding(40)
    }

    private func addNumber() {
        let n = newNumber.trimmingCharacters(in: .whitespaces)
        guard !n.isEmpty else { return }
        AppGroupStorage.shared.addTrusted(n); newNumber = ""; reload()
    }

    private func reload() { numbers = Array(AppGroupStorage.shared.trustedNumbers).sorted() }
}
