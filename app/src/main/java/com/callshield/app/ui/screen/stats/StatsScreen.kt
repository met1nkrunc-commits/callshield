package com.callshield.app.ui.screen.stats

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.callshield.app.domain.model.RiskLevel
import com.callshield.app.ui.theme.BEngelAccent
import com.callshield.app.ui.theme.DarkBg
import com.callshield.app.ui.theme.DarkBorder
import com.callshield.app.ui.theme.DarkSurfaceVar
import com.callshield.app.ui.theme.OnDarkPrimary
import com.callshield.app.ui.theme.OnDarkSecondary
import com.callshield.app.ui.theme.toColor
import com.callshield.app.ui.theme.toLabel

// Ordered list of risk levels for the risk breakdown section
private val RISK_LEVEL_ORDER = listOf(
    RiskLevel.BLOCKED,
    RiskLevel.HIGH,
    RiskLevel.MEDIUM,
    RiskLevel.LOW,
)

@Composable
fun StatsScreen(
    // Fix #18: Kullanıcı false positive'i blocklist'ten kaldırabilmesi için navigasyon eklendi.
    onNavigateToBlocklist: () -> Unit = {},
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(24.dp))

        Text(
            text = "İstatistikler",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = OnDarkPrimary,
            letterSpacing = 1.sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Toplam engelleme verileri",
            fontSize = 13.sp,
            color = OnDarkSecondary,
        )
        Spacer(Modifier.height(8.dp))
        // Fix #18: Engellenenler listesine navigasyon — false positive'leri oradan kaldır.
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onNavigateToBlocklist() }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(text = "📋", fontSize = 12.sp)
            Text(
                text = "Engellenenler Listesi →",
                fontSize = 12.sp,
                color = BEngelAccent,
                fontWeight = FontWeight.Medium,
            )
        }
        Spacer(Modifier.height(16.dp))

        // ── Summary row ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                value = uiState.totalBlocked.toString(),
                label = "Toplam",
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                value = uiState.weeklyBlocked.toString(),
                label = "Bu Hafta",
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                value = uiState.dailyBlocked.toString(),
                label = "Bugün",
            )
        }

        Spacer(Modifier.height(24.dp))

        if (uiState.dailyData.isNotEmpty()) {
            WeeklyBarChart(uiState.dailyData)
            Spacer(Modifier.height(24.dp))
        }

        // ── SMS / Call split ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SummaryCard(
                modifier = Modifier.weight(1f),
                value = uiState.totalSms.toString(),
                label = "SMS",
            )
            SummaryCard(
                modifier = Modifier.weight(1f),
                value = uiState.totalCalls.toString(),
                label = "Arama",
            )
        }

        if (uiState.categoryStats.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))

            Text(
                text = "KATEGORİ DAĞILIMI",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnDarkSecondary,
                letterSpacing = 2.sp,
            )
            Spacer(Modifier.height(12.dp))

            val total = uiState.categoryStats.values.sum().coerceAtLeast(1)
            uiState.categoryStats.entries.sortedByDescending { it.value }.forEach { (category, count) ->
                CategoryBar(
                    category = category,
                    count = count,
                    fraction = count.toFloat() / total,
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        val nonEmptyRisk = RISK_LEVEL_ORDER.filter { (uiState.riskStats[it] ?: 0) > 0 }
        if (nonEmptyRisk.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))

            Text(
                text = "RİSK SEVİYESİ",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnDarkSecondary,
                letterSpacing = 2.sp,
            )
            Spacer(Modifier.height(12.dp))

            nonEmptyRisk.forEach { level ->
                RiskRow(level = level, count = uiState.riskStats[level] ?: 0)
                Spacer(Modifier.height(8.dp))
            }
        }

        if (uiState.totalBlocked == 0 && !uiState.isLoading) {
            Spacer(Modifier.height(64.dp))
            EmptyState()
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun WeeklyBarChart(dailyData: List<Pair<String, Int>>) {
    if (dailyData.isEmpty()) return
    val maxVal = dailyData.maxOf { it.second }.coerceAtLeast(1)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVar)
            .padding(16.dp),
    ) {
        Text(
            "HAFTALIK ENGELLEMELER",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = OnDarkSecondary,
            letterSpacing = 2.sp,
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
        ) {
            dailyData.forEach { (label, count) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    if (count > 0) Text(count.toString(), fontSize = 9.sp, color = BEngelAccent)
                    Spacer(Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .width(28.dp)
                            .height((80 * count / maxVal).coerceAtLeast(4).dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(if (count > 0) BEngelAccent else DarkSurfaceVar.copy(alpha = 0.3f)),
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(label, fontSize = 10.sp, color = OnDarkSecondary)
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(modifier: Modifier, value: String, label: String) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVar)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = BEngelAccent,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = OnDarkSecondary,
        )
    }
}

@Composable
private fun CategoryBar(category: String, count: Int, fraction: Float) {
    // Animate the bar width on first composition
    var triggered by remember { mutableStateOf(false) }
    LaunchedEffect(fraction) { triggered = true }
    val animatedFraction by animateFloatAsState(
        targetValue   = if (triggered) fraction else 0f,
        animationSpec = tween(durationMillis = 600),
        label         = "bar_$category",
    )

    val label = when (category.uppercase()) {
        "BETTING"  -> "Bahis / Kumar"
        "PHISHING" -> "Phishing"
        "LEGAL"    -> "Yasal Tehdit"
        "SOCIAL"   -> "Sosyal Mühendislik"
        else       -> category
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVar)
            .padding(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = OnDarkPrimary,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = count.toString(),
                fontSize = 13.sp,
                color = BEngelAccent,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(DarkBorder),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BEngelAccent),
            )
        }
    }
}

@Composable
private fun RiskRow(level: RiskLevel, count: Int) {
    val accentColor = level.toColor()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVar)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accentColor),
            )
            Text(
                text = level.toLabel(),
                fontSize = 13.sp,
                color = OnDarkPrimary,
            )
        }
        Text(
            text = count.toString(),
            fontSize = 15.sp,
            color = accentColor,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = "✅", fontSize = 48.sp)
        Text(
            text = "Henüz engellenen tehdit yok",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = OnDarkSecondary,
        )
        Text(
            text = "Uygulama aktif olarak koruyor.",
            fontSize = 12.sp,
            color = OnDarkSecondary,
        )
    }
}
