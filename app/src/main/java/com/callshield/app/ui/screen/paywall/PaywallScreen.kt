package com.callshield.app.ui.screen.paywall

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callshield.app.ui.theme.BEngelAccent
import com.callshield.app.ui.theme.BEngelGreen
import com.callshield.app.ui.theme.DarkBg
import com.callshield.app.ui.theme.DarkSurfaceVar
import com.callshield.app.ui.theme.OnDarkDisabled
import com.callshield.app.ui.theme.OnDarkPrimary
import com.callshield.app.ui.theme.OnDarkSecondary

@Composable
fun PaywallScreen(
    onDismiss: () -> Unit,
    onSelectPlan: (String) -> Unit,   // passes product ID
) {
    var selectedPlan by remember { mutableStateOf("yearly") }  // yearly pre-selected

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))

        // Dismiss
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Kapat", tint = OnDarkSecondary)
            }
        }

        // Header
        Text(
            "B·Engel",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = OnDarkPrimary,
            letterSpacing = 2.sp,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Tam Koruma",
            fontSize = 16.sp,
            color = BEngelAccent,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(24.dp))

        // Feature list
        val features = listOf(
            "Numara spam kontrolü (IPQS)",
            "Sınırsız engelleme listesi",
            "Phishing URL koruması",
            "Otomatik liste güncellemesi",
            "Öncelikli yeni tehdit tespiti",
        )
        features.forEach { feature ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = BEngelAccent,
                    modifier = Modifier.size(18.dp),
                )
                Text(feature, fontSize = 14.sp, color = OnDarkPrimary)
            }
        }

        Spacer(Modifier.height(28.dp))

        // Plan toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceVar)
                .padding(4.dp),
        ) {
            PlanToggleButton(
                label    = "Aylık",
                sublabel = "₺39/ay",
                selected = selectedPlan == "monthly",
                modifier = Modifier.weight(1f),
                onClick  = { selectedPlan = "monthly" },
            )
            PlanToggleButton(
                label    = "Yıllık",
                sublabel = "₺299/yıl",
                badge    = "%36 indirim",
                selected = selectedPlan == "yearly",
                modifier = Modifier.weight(1f),
                onClick  = { selectedPlan = "yearly" },
            )
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = if (selectedPlan == "yearly") "Aylık ₺24.9'a denk" else "İstediğin zaman iptal",
            fontSize = 12.sp,
            color = OnDarkSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(24.dp))

        // CTA
        Button(
            onClick = {
                val productId = if (selectedPlan == "yearly")
                    "bengel_standard_yearly" else "bengel_standard_monthly"
                onSelectPlan(productId)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BEngelGreen,
                contentColor   = Color.White,
            ),
        ) {
            Text(
                text = if (selectedPlan == "yearly") "Yıllık Başla — ₺299" else "Aylık Başla — ₺39",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "7 gün ücretsiz deneme · İstediğin zaman iptal",
            fontSize = 11.sp,
            color = OnDarkDisabled,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun PlanToggleButton(
    label: String,
    sublabel: String,
    selected: Boolean,
    modifier: Modifier,
    badge: String? = null,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) BEngelGreen else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) Color.White else OnDarkSecondary,
            )
            Text(
                sublabel,
                fontSize = 12.sp,
                color = if (selected) Color.White.copy(alpha = 0.85f) else OnDarkDisabled,
            )
            if (badge != null) {
                Spacer(Modifier.height(3.dp))
                Text(badge, fontSize = 10.sp, color = BEngelAccent, fontWeight = FontWeight.Bold)
            }
        }
    }
}
