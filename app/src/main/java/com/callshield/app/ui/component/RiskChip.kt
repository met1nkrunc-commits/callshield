package com.callshield.app.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callshield.app.domain.model.RiskLevel
import com.callshield.app.ui.theme.StatusBlocked
import com.callshield.app.ui.theme.StatusHigh
import com.callshield.app.ui.theme.StatusLow
import com.callshield.app.ui.theme.StatusMedium
import com.callshield.app.ui.theme.StatusSafe

@Composable
fun RiskChip(riskLevel: RiskLevel, modifier: Modifier = Modifier) {
    val (label, baseColor) = when (riskLevel) {
        RiskLevel.BLOCKED -> "ENGELLENDİ" to StatusBlocked
        RiskLevel.HIGH    -> "Yüksek Risk" to StatusHigh
        RiskLevel.MEDIUM  -> "Orta Risk"   to StatusMedium
        RiskLevel.LOW     -> "Düşük Risk"  to StatusLow
        RiskLevel.SAFE    -> "Güvenli"     to StatusSafe
    }

    val shouldAnimate = riskLevel == RiskLevel.BLOCKED || riskLevel == RiskLevel.HIGH

    val infiniteTransition = rememberInfiniteTransition(label = "riskPulse")

    val alpha by if (shouldAnimate) {
        infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "chipAlpha",
        )
    } else {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Restart,
            ),
            label = "chipAlphaStatic",
        )
    }

    val bgColor = if (shouldAnimate) baseColor.copy(alpha = alpha) else baseColor.copy(alpha = 0.2f)
    val textColor = if (shouldAnimate) Color.White else baseColor

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
        )
    }
}
