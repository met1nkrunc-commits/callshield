import SwiftUI

struct ContentView: View {
    @AppStorage("kvkkConsentAccepted") private var kvkkConsentAccepted = false

    var body: some View {
        Group {
            if kvkkConsentAccepted {
                ComposeView()
            } else {
                LegalConsentView()
            }
        }
    }
}
