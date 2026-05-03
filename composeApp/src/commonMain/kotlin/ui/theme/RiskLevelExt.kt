package ui.theme

import androidx.compose.ui.graphics.Color
import com.bengel.shared.domain.model.RiskLevel

fun RiskLevel.toColor(): Color = when (this) {
    RiskLevel.BLOCKED -> StatusBlocked
    RiskLevel.HIGH    -> StatusHigh
    RiskLevel.MEDIUM  -> StatusMedium
    RiskLevel.LOW     -> StatusLow
    RiskLevel.SAFE    -> StatusSafe
}

fun RiskLevel.toLabel(): String = when (this) {
    RiskLevel.BLOCKED -> "ENGELLENDİ"
    RiskLevel.HIGH    -> "Yüksek Risk"
    RiskLevel.MEDIUM  -> "Orta Risk"
    RiskLevel.LOW     -> "Düşük Risk"
    RiskLevel.SAFE    -> "Güvenli"
}
