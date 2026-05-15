import GoogleMobileAds
import SwiftUI

struct AdMobBannerView: UIViewRepresentable {
    let adUnitID: String

    func makeUIView(context: Context) -> BannerView {
        let banner = BannerView(adSize: AdSizeBanner)
        banner.adUnitID = adUnitID
        banner.rootViewController = context.coordinator.rootViewController
        banner.load(Request())
        return banner
    }

    func updateUIView(_ uiView: BannerView, context: Context) {}

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    final class Coordinator {
        let rootViewController = UIViewController()
    }
}

struct MonetizedBanner: View {
    @EnvironmentObject private var store: StoreManager

    private let testAdUnitID = "ca-app-pub-3940256099942544/2934735716"

    var body: some View {
        if !store.isPremium {
            AdMobBannerView(adUnitID: Bundle.main.adMobBannerAdUnitID ?? testAdUnitID)
                .frame(width: 320, height: 50)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 6)
                .background(CS.bg)
        }
    }
}

private extension Bundle {
    var adMobBannerAdUnitID: String? {
        object(forInfoDictionaryKey: "GADBannerAdUnitID") as? String
    }
}
