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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.callshield.app.core.billing.BillingState
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
    onSelectPlan: (String) -> Unit,
    viewModel: PaywallViewModel = hiltViewModel(),
) {
    val billingState by viewModel.billingState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedPlan by remember { mutableStateOf("yearly") }

    // Satın alma başarılıysa kapat
    LaunchedEffect(billingState) {
        if (billingState is BillingState.Success) onSelectPlan((billingState as BillingState.Success).plan)
    }

    val isLoading = billingState is BillingState.Launching
    val errorMsg = (billingState as? BillingState.Error)?.message

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Kapat", tint = OnDarkSecondary)
            }
        }

        Text(
            "B-Engel",
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

        val standardFeatures = listOf(
            "Numara spam kontrolü (IPQS)",
            "Sınırsız engelleme listesi",
            "Phishing URL koruması",
            "Otomatik liste güncellemesi",
            "Öncelikli yeni tehdit tespiti",
            "Günlük sınırsız sorgu",
        )
        val familyFeatures = listOf(
            "Standart özelliklerin tamamı",
            "5 cihaza kadar koruma",
            "Aile paneli (yakında)",
            "Öncelikli destek",
        )

        val isFamily = selectedPlan == "family_yearly"
        val features = if (isFamily) familyFeatures else standardFeatures

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
                    tint = if (isFamily) BEngelAccent else BEngelGreen,
                    modifier = Modifier.size(18.dp),
                )
                Text(feature, fontSize = 14.sp, color = OnDarkPrimary)
            }
        }

        Spacer(Modifier.height(28.dp))

        // Plan toggle — 3 options
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
            PlanToggleButton(
                label    = "Aile",
                sublabel = "₺499/yıl",
                badge    = "5 cihaz",
                selected = selectedPlan == "family_yearly",
                modifier = Modifier.weight(1f),
                onClick  = { selectedPlan = "family_yearly" },
                accentColor = BEngelAccent,
            )
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = when (selectedPlan) {
                "yearly"        -> "Aylık ₺24.9'a denk"
                "family_yearly" -> "Kişi başı ₺8.3/ay · 5 cihaz"
                else            -> "İstediğin zaman iptal"
            },
            fontSize = 12.sp,
            color = OnDarkSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(24.dp))

        if (errorMsg != null) {
            Text(
                text = "Hata: $errorMsg",
                fontSize = 12.sp,
                color = Color.Red.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        Button(
            onClick = {
                val productId = when (selectedPlan) {
                    "yearly"        -> "bengel_standard_yearly"
                    "family_yearly" -> "bengel_family_yearly"
                    else            -> "bengel_standard_monthly"
                }
                val activity = context as? android.app.Activity
                if (activity != null) viewModel.purchase(activity, productId)
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedPlan == "family_yearly") BEngelAccent else BEngelGreen,
                contentColor   = if (selectedPlan == "family_yearly") DarkBg else Color.White,
            ),
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(
                    text = when (selectedPlan) {
                        "yearly"        -> "Yıllık Başla — ₺299"
                        "family_yearly" -> "Aile Planı — ₺499/yıl"
                        else            -> "Aylık Başla — ₺39"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = { viewModel.restore() }) {
            Text("Satın alımları geri yükle", fontSize = 12.sp, color = OnDarkSecondary)
        }

        Spacer(Modifier.height(4.dp))
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
    accentColor: Color = BEngelGreen,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) accentColor else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) (if (accentColor == BEngelGreen) Color.White else DarkBg) else OnDarkSecondary,
            )
            Text(
                sublabel,
                fontSize = 12.sp,
                color = if (selected) (if (accentColor == BEngelGreen) Color.White.copy(alpha = 0.85f) else DarkBg.copy(alpha = 0.8f)) else OnDarkDisabled,
            )
            if (badge != null) {
                Spacer(Modifier.height(3.dp))
                Text(badge, fontSize = 10.sp, color = if (selected) Color.White.copy(alpha = 0.9f) else BEngelAccent, fontWeight = FontWeight.Bold)
            }
        }
    }
}
