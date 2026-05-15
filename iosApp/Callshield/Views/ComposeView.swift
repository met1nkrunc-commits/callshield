import SwiftUI

struct ComposeView: View {
    @AppStorage("onboardingDone") private var onboardingDone = false
    @AppStorage("initialTrialOfferShown") private var initialTrialOfferShown = false
    @EnvironmentObject private var store: StoreManager
    @State private var showInitialTrialOffer = false

    var body: some View {
        Group {
            if onboardingDone {
                MainTabView()
                    .onAppear(perform: presentInitialTrialOfferIfNeeded)
                    .fullScreenCover(isPresented: $showInitialTrialOffer) {
                        NavigationView {
                            PaywallView()
                        }
                        .environmentObject(store)
                        .preferredColorScheme(.dark)
                    }
            } else {
                OnboardingView()
            }
        }
    }

    private func presentInitialTrialOfferIfNeeded() {
        guard !initialTrialOfferShown, !store.isPremium else { return }
        initialTrialOfferShown = true
        showInitialTrialOffer = true
    }
}
