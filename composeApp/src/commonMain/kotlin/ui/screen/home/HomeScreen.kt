package ui.screen.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bengel.shared.domain.model.BlockEvent
import com.bengel.shared.domain.model.RiskLevel
import ui.component.RiskChip
import ui.theme.*

data class HomeUiState(
    val isProtectionEnabled: Boolean = true,
    val totalBlockedCount: Int = 0,
    val weeklyBlockedCount: Int = 0,
    val recentEvents: List<BlockEvent> = emptyList(),
    val isOffline: Boolean = false,
    val showSmsBanner: Boolean = false,
)

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onToggleProtection: () -> Unit,
    onNavigateToLookup: () -> Unit = {},
    onNavigateToCallLog: () -> Unit = {},
) {
    val scrollState = rememberScrollState()
    val isActive = uiState.isProtectionEnabled

    // Pulse animation for active shield
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue  = 1.08f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseScale",
    )

    val shieldColor by animateColorAsState(
        targetValue   = if (isActive) BEngelAccent else StatusBlocked,
        animationSpec = tween(600),
        label         = "shieldColor",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))

        // ── Top bar ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Callshield",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = OnDarkPrimary,
                letterSpacing = 2.sp,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (uiState.isOffline) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StatusMedium.copy(alpha = 0.18f))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = "✈ Offline — yerel koruma aktif",
                            fontSize = 10.sp,
                            color = StatusMedium,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    Spacer(Modifier.size(8.dp))
                }
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isActive) BEngelAccent else StatusBlocked),
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        // ── Hero Shield ──
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(160.dp)
                .scale(if (isActive) pulseScale else 1f),
        ) {
            // Glow ring
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                shieldColor.copy(alpha = 0.15f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
            // Inner circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVar),
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Koruma Kalkanı",
                    tint = shieldColor,
                    modifier = Modifier.size(64.dp),
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Status text ──
        Text(
            text = if (isActive) "KORUMA AKTİF" else "KORUMA KAPALI",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = shieldColor,
            letterSpacing = 3.sp,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (isActive) "SMS ve aramalar taranıyor" else "Korumayı aktif edin",
            fontSize = 13.sp,
            color = OnDarkSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(28.dp))

        // ── Toggle switch ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceVar)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Koruma",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnDarkPrimary,
                )
                Text(
                    text = if (isActive) "Aktif" else "Devre dışı",
                    fontSize = 12.sp,
                    color = if (isActive) BEngelAccent else OnDarkSecondary,
                )
            }
            Switch(
                checked = isActive,
                onCheckedChange = { onToggleProtection() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor   = DarkBg,
                    checkedTrackColor   = BEngelAccent,
                    uncheckedThumbColor = OnDarkSecondary,
                    uncheckedTrackColor = DarkSurface,
                ),
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── Stat cards ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(
                modifier    = Modifier.weight(1f),
                value       = uiState.totalBlockedCount.toString(),
                label       = "Toplam\nEngellenen",
                icon        = Icons.Default.Shield,
                accentColor = BEngelAccent,
            )
            StatCard(
                modifier    = Modifier.weight(1f),
                value       = uiState.weeklyBlockedCount.toString(),
                label       = "Bu Hafta\nEngellenen",
                icon        = Icons.Default.BarChart,
                accentColor = StatusMedium,
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── Recent events ──
        if (uiState.recentEvents.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceVar)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "SON ENGELLENENLER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnDarkSecondary,
                    letterSpacing = 1.5.sp,
                )
                uiState.recentEvents.take(3).forEach { event ->
                    RecentEventRow(event = event)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Shortcut cards ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ShortcutCard(
                modifier = Modifier.weight(1f),
                title = "Numara Sorgula",
                subtitle = "Ücretsiz · Yerel DB",
                icon = Icons.Default.Search,
                onClick = onNavigateToLookup,
            )
            ShortcutCard(
                modifier = Modifier.weight(1f),
                title = "Son Aramalar",
                subtitle = "Risk analizi",
                icon = Icons.Default.Phone,
                onClick = onNavigateToCallLog,
            )
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ShortcutCard(
    modifier: Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVar)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = OnDarkPrimary)
            Text(subtitle, fontSize = 11.sp, color = OnDarkSecondary)
        }
        Icon(icon, contentDescription = null, tint = BEngelAccent, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun StatCard(
    modifier: Modifier,
    value: String,
    label: String,
    icon: ImageVector,
    accentColor: Color,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVar)
            .padding(16.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = value,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = OnDarkPrimary,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = OnDarkSecondary,
            lineHeight = 15.sp,
        )
    }
}

@Composable
private fun RecentEventRow(event: BlockEvent) {
    val accentColor = event.riskLevel.toColor()
    val senderLabel = "#${event.senderHash}"
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(accentColor),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${event.type} · $senderLabel",
                fontSize = 13.sp,
                color = OnDarkPrimary,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = event.category ?: "",
                fontSize = 11.sp,
                color = OnDarkSecondary,
            )
        }
        RiskChip(riskLevel = event.riskLevel)
    }
}
