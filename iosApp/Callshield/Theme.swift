import SwiftUI

enum CS {
    static let bg          = Color(red: 0.051, green: 0.067, blue: 0.090)   // #0D1117
    static let surface     = Color(red: 0.098, green: 0.122, blue: 0.157)   // #191F28
    static let surfaceVar  = Color(red: 0.118, green: 0.149, blue: 0.196)   // #1E2632
    static let accent      = Color(red: 0.000, green: 0.784, blue: 0.325)   // #00C853
    static let accentDark  = Color(red: 0.000, green: 0.588, blue: 0.251)   // #009640
    static let primary     = Color.white
    static let secondary   = Color(white: 0.60)
    static let disabled    = Color(white: 0.35)
    static let red         = Color(red: 0.953, green: 0.259, blue: 0.212)   // #F34236
    static let orange      = Color(red: 1.000, green: 0.596, blue: 0.000)   // #FF9800
    static let yellow      = Color(red: 1.000, green: 0.863, blue: 0.000)   // #FFDC00
}

struct BrandMarkView: View {
    var size: CGFloat = 96
    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: size * 0.28, style: .continuous)
                .fill(
                    LinearGradient(
                        colors: [CS.surfaceVar, CS.bg],
                        startPoint: .topLeading,
                        endPoint: .bottomTrailing
                    )
                )

            RoundedRectangle(cornerRadius: size * 0.28, style: .continuous)
                .stroke(CS.accent.opacity(0.16), lineWidth: size * 0.03)

            ShieldGlyph()
                .fill(
                    LinearGradient(
                        colors: [CS.accent, CS.accentDark],
                        startPoint: .top,
                        endPoint: .bottom
                    )
                )
                .padding(size * 0.16)

            Text("S")
                .font(.system(size: size * 0.42, weight: .black, design: .rounded))
                .foregroundStyle(.white)
                .offset(y: size * 0.01)

            Circle()
                .fill(.white.opacity(0.12))
                .frame(width: size * 0.18, height: size * 0.18)
                .offset(x: -size * 0.18, y: -size * 0.18)
        }
        .frame(width: size, height: size)
        .shadow(color: CS.accent.opacity(0.22), radius: size * 0.12, y: size * 0.04)
    }
}

struct BrandWordmarkView: View {
    var body: some View {
        HStack(spacing: 10) {
            BrandMarkView(size: 34)
            Text("Siper")
                .font(.system(size: 26, weight: .black, design: .rounded))
                .foregroundStyle(CS.primary)
        }
    }
}

private struct ShieldGlyph: Shape {
    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.move(to: CGPoint(x: rect.midX, y: rect.minY))
        path.addCurve(
            to: CGPoint(x: rect.maxX, y: rect.minY + rect.height * 0.24),
            control1: CGPoint(x: rect.midX + rect.width * 0.20, y: rect.minY),
            control2: CGPoint(x: rect.maxX, y: rect.minY + rect.height * 0.08)
        )
        path.addLine(to: CGPoint(x: rect.maxX, y: rect.minY + rect.height * 0.58))
        path.addCurve(
            to: CGPoint(x: rect.midX, y: rect.maxY),
            control1: CGPoint(x: rect.maxX, y: rect.minY + rect.height * 0.82),
            control2: CGPoint(x: rect.midX + rect.width * 0.18, y: rect.maxY)
        )
        path.addCurve(
            to: CGPoint(x: rect.minX, y: rect.minY + rect.height * 0.58),
            control1: CGPoint(x: rect.midX - rect.width * 0.18, y: rect.maxY),
            control2: CGPoint(x: rect.minX, y: rect.minY + rect.height * 0.82)
        )
        path.addLine(to: CGPoint(x: rect.minX, y: rect.minY + rect.height * 0.24))
        path.addCurve(
            to: CGPoint(x: rect.midX, y: rect.minY),
            control1: CGPoint(x: rect.minX, y: rect.minY + rect.height * 0.08),
            control2: CGPoint(x: rect.midX - rect.width * 0.20, y: rect.minY)
        )
        return path
    }
}
