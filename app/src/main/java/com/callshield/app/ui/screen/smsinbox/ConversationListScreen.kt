package com.callshield.app.ui.screen.smsinbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.callshield.app.domain.model.RiskLevel
import com.callshield.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ConversationListScreen(
    onOpenThread: (threadId: Long, address: String) -> Unit,
    viewModel: ConversationListViewModel = hiltViewModel(),
) {
    val threads   by viewModel.threads.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        // ── Başlık ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Mesajlar",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = OnDarkPrimary,
            )
            IconButton(onClick = { viewModel.loadThreads() }) {
                Text("↻", fontSize = 18.sp, color = BEngelAccent)
            }
        }

        HorizontalDivider(color = DarkSurfaceVar)

        when {
            isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BEngelAccent)
                }
            }
            threads.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💬", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Mesaj yok", fontSize = 16.sp, color = OnDarkSecondary)
                    }
                }
            }
            else -> {
                LazyColumn {
                    items(threads, key = { it.threadId }) { thread ->
                        ThreadRow(
                            thread = thread,
                            onClick = { onOpenThread(thread.threadId, thread.address) },
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 72.dp),
                            color = DarkSurfaceVar.copy(alpha = 0.5f),
                        )
                    }
                }
            }
        }
    }
}

private val timeFormat = SimpleDateFormat("HH:mm", Locale("tr"))
private val dateFormat = SimpleDateFormat("dd MMM", Locale("tr"))

@Composable
private fun ThreadRow(thread: ConversationThread, onClick: () -> Unit) {
    val riskColor = when (thread.riskLevel) {
        RiskLevel.BLOCKED -> StatusBlocked
        RiskLevel.HIGH    -> StatusHigh
        RiskLevel.MEDIUM  -> StatusMedium
        else              -> Color.Transparent
    }
    val showRisk = thread.riskLevel != RiskLevel.SAFE && thread.riskLevel != RiskLevel.LOW

    val cal = Calendar.getInstance()
    val msgCal = Calendar.getInstance().apply { timeInMillis = thread.date }
    val timeStr = if (cal.get(Calendar.DATE) == msgCal.get(Calendar.DATE))
        timeFormat.format(Date(thread.date))
    else
        dateFormat.format(Date(thread.date))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Avatar
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVar),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = thread.address.firstOrNull()?.uppercase() ?: "?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BEngelAccent,
                )
            }
            if (showRisk) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(riskColor)
                        .align(Alignment.BottomEnd)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = thread.address.ifBlank { "Bilinmeyen" },
                    fontSize = 15.sp,
                    fontWeight = if (thread.unreadCount > 0) FontWeight.Bold else FontWeight.Normal,
                    color = OnDarkPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = timeStr,
                    fontSize = 11.sp,
                    color = if (thread.unreadCount > 0) BEngelAccent else OnDarkSecondary,
                )
            }
            Spacer(Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = thread.snippet,
                    fontSize = 13.sp,
                    color = if (showRisk) riskColor else OnDarkSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (thread.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(BEngelAccent),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = thread.unreadCount.toString(),
                            fontSize = 10.sp,
                            color = DarkBg,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            if (showRisk) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = when (thread.riskLevel) {
                        RiskLevel.BLOCKED -> "🔴 Engellendi"
                        RiskLevel.HIGH    -> "🟠 Yüksek Risk"
                        RiskLevel.MEDIUM  -> "🟡 Şüpheli"
                        else -> ""
                    },
                    fontSize = 11.sp,
                    color = riskColor,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
