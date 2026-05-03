package com.callshield.app.ui.screen.history

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.callshield.app.domain.model.BlockEvent
import com.callshield.app.domain.model.RiskLevel
import com.callshield.app.ui.component.EmptyState
import com.callshield.app.ui.component.RiskChip
import com.callshield.app.ui.theme.BEngelAccent
import com.callshield.app.ui.theme.BEngelGreen
import com.callshield.app.ui.theme.DarkBg
import com.callshield.app.ui.theme.DarkSurface
import com.callshield.app.ui.theme.DarkSurfaceVar
import com.callshield.app.ui.theme.OnDarkPrimary
import com.callshield.app.ui.theme.OnDarkSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockHistoryScreen(
    onBack: () -> Unit,
    viewModel: BlockHistoryViewModel = hiltViewModel(),
) {
    val events by viewModel.events.collectAsStateWithLifecycle()
    val currentFilter by viewModel.filter.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = DarkBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text("Engelleme Geçmişi", color = OnDarkPrimary, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 20.sp, color = OnDarkPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(padding),
        ) {
            // Filter chips
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val filters = listOf(
                    HistoryFilter.ALL  to "Tümü",
                    HistoryFilter.SMS  to "📩 SMS",
                    HistoryFilter.CALL to "📞 Arama",
                )
                items(filters) { (f, label) ->
                    FilterChip(
                        selected = currentFilter == f,
                        onClick = { viewModel.setFilter(f) },
                        label = { Text(label, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BEngelAccent,
                            selectedLabelColor = Color.White,
                            containerColor = DarkSurfaceVar,
                            labelColor = OnDarkSecondary,
                        ),
                    )
                }
            }

            if (events.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Shield,
                    title = "Henüz engelleme yok",
                    subtitle = "Şüpheli arama veya SMS tespit edildiğinde\nburada görünecek",
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item { Spacer(Modifier.height(4.dp)) }
                    items(events, key = { it.id }) { event ->
                        BlockEventRow(
                            event = event,
                            onNotSpam = if (event.sender.isNotBlank()) {
                                {
                                    viewModel.markAsNotSpam(event)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("${event.sender} güvenilir listeye eklendi")
                                    }
                                }
                            } else null,
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun BlockEventRow(event: BlockEvent, onNotSpam: (() -> Unit)? = null) {
    val typeEmoji = if (event.type == "SMS") "📩" else "📞"
    val riskEmoji = when (event.riskLevel) {
        RiskLevel.BLOCKED -> "🔴"
        RiskLevel.HIGH    -> "🟠"
        RiskLevel.MEDIUM  -> "🟡"
        RiskLevel.LOW     -> "🟢"
        RiskLevel.SAFE    -> "🟢"
    }
    val categoryLabel = when (event.category) {
        "BETTING"  -> "🎰 Bahis"
        "PHISHING" -> "🎣 Oltalama"
        "LEGAL"    -> "⚖️ Hukuki"
        "SOCIAL"   -> "🎭 Sosyal"
        "INVESTMENT" -> "💰 Yatırım"
        else       -> "❓ Bilinmiyor"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVar)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurface),
            contentAlignment = Alignment.Center,
        ) {
            Text("$typeEmoji", fontSize = 20.sp)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$riskEmoji ${event.type}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnDarkPrimary,
                )
                Text(
                    text = "  ·  $categoryLabel",
                    fontSize = 12.sp,
                    color = OnDarkSecondary,
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = "ID: #${event.senderHash}  ·  ${formatDateTime(event.timestamp)}",
                fontSize = 11.sp,
                color = OnDarkSecondary,
            )
        }

        RiskChip(
            riskLevel = event.riskLevel,
            modifier = Modifier.padding(start = 8.dp),
        )
    }

    if (onNotSpam != null) {
        TextButton(
            onClick = onNotSpam,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 4.dp),
        ) {
            Text(
                text = "Bu spam değildi — güvenilir listeye ekle",
                fontSize = 11.sp,
                color = BEngelGreen,
            )
        }
    }
}

private fun formatDateTime(timestamp: Long): String =
    SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault()).format(Date(timestamp))
