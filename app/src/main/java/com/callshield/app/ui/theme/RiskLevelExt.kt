package com.callshield.app.ui.theme

import androidx.compose.ui.graphics.Color
import com.callshield.app.domain.model.RiskLevel

fun RiskLevel.toColor(): Color = when (this) {
    RiskLevel.SAFE    -> StatusSafe
    RiskLevel.LOW     -> StatusLow
    RiskLevel.MEDIUM  -> StatusMedium
    RiskLevel.HIGH    -> StatusHigh
    RiskLevel.BLOCKED -> StatusBlocked
}

fun RiskLevel.toLabel(): String = when (this) {
    RiskLevel.SAFE    -> "Güvenli"
    RiskLevel.LOW     -> "Düşük Risk"
    RiskLevel.MEDIUM  -> "Orta Risk"
    RiskLevel.HIGH    -> "Yüksek Risk"
    RiskLevel.BLOCKED -> "Engellendi"
}
