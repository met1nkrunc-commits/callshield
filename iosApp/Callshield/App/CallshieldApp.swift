import GoogleMobileAds
import SwiftUI

@main
struct CallshieldApp: App {
    @StateObject private var syncService = FraudSyncService()
    @StateObject private var store = StoreManager()
    @AppStorage("onboardingDone") private var onboardingDone = false

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(syncService)
                .environmentObject(store)
                .task {
                    await MobileAds.shared.start()
                    await syncService.syncIfNeeded()
                    await store.configure()
                }
        }
    }
}
