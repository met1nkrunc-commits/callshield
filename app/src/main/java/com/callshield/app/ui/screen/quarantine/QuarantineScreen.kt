package com.callshield.app.ui.screen.quarantine

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.callshield.app.domain.model.BlockedSms
import com.callshield.app.domain.model.RiskLevel
import com.callshield.app.ui.component.EmptyState
import com.callshield.app.ui.theme.BEngelAccent
import com.callshield.app.ui.theme.BEngelGreen
import com.callshield.app.ui.theme.BEngelGreenDark
import com.callshield.app.ui.theme.DarkBg
import com.callshield.app.ui.theme.DarkSurface
import com.callshield.app.ui.theme.DarkSurfaceVar
import com.callshield.app.ui.theme.OnDarkPrimary
import com.callshield.app.ui.theme.OnDarkSecondary
import com.callshield.app.ui.theme.StatusBlocked
import com.callshield.app.ui.theme.StatusHigh
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuarantineScreen(
    onBack: () -> Unit,
    viewModel: QuarantineViewModel = hiltViewModel(),
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = DarkBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Karantina", color = OnDarkPrimary, fontWeight = FontWeight.Bold)
                        Text(
                            "${messages.size} engellenen SMS",
                            fontSize = 12.sp,
                            color = OnDarkSecondary,
                        )
                    }
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
        if (messages.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Lock,
                title = "Karantina boş",
                subtitle = "Engellenen SMS'ler burada görünecek.\nİçeriklerini görmek veya geri almak için buraya gelin.",
                modifier = Modifier.padding(padding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBg)
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item { Spacer(Modifier.height(8.dp)) }
                items(messages, key = { it.id }) { sms ->
                    QuarantineSmsCard(
                        sms = sms,
                        onNotSpam = {
                            viewModel.markAsNotSpam(sms)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "${sms.sender} güvenilir listeye eklendi, mesaj gelen kutusuna geri eklendi"
                                )
                            }
                        },
                        onDelete = { viewModel.deleteFromQuarantine(sms) },
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun QuarantineSmsCard(
    sms: BlockedSms,
    onNotSpam: () -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var showConfirmDelete by remember { mutableStateOf(false) }

    val riskColor = when (sms.riskLevel) {
        RiskLevel.BLOCKED -> StatusBlocked
        else              -> StatusHigh
    }

    val categoryLabel = when (sms.category) {
        "BETTING"    -> "Bahis/Kumar"
        "PHISHING"   -> "Oltalama"
        "LEGAL"      -> "Hukuki Tehdit"
        "SOCIAL"     -> "Sosyal Mühendislik"
        "INVESTMENT" -> "Yatırım Dolandırıcılığı"
        else         -> "Spam"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceVar),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(riskColor),
                    )
                    Text(
                        text = sms.sender.ifBlank { "Bilinmeyen" },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnDarkPrimary,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "$categoryLabel · ${formatDateTime(sms.timestamp)}",
                    fontSize = 11.sp,
                    color = OnDarkSecondary,
                )
            }
            TextButton(onClick = { expanded = !expanded }) {
                Text(
                    text = if (expanded) "Gizle" else "İçeriği Gör",
                    fontSize = 12.sp,
                    color = BEngelAccent,
                )
            }
        }

        // Expanded body
        if (expanded) {
            Text(
                text = sms.body,
                fontSize = 13.sp,
                color = OnDarkSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                lineHeight = 19.sp,
            )
        } else {
            Text(
                text = sms.body,
                fontSize = 12.sp,
                color = OnDarkSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 8.dp),
            )
        }

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedButton(
                onClick = { showConfirmDelete = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Spamı Onayla", fontSize = 12.sp, color = OnDarkSecondary)
            }
            Button(
                onClick = onNotSpam,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BEngelGreenDark,
                    contentColor = androidx.compose.ui.graphics.Color.White,
                ),
            ) {
                Text("Bu spam değildi", fontSize = 12.sp)
            }
        }
    }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            containerColor = DarkSurface,
            title = {
                Text("Karantinadan sil?", color = OnDarkPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Bu mesaj kalıcı olarak silinecek ve bir daha görüntülenemeyecek.",
                    color = OnDarkSecondary,
                    fontSize = 14.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = { onDelete(); showConfirmDelete = false }) {
                    Text("Sil", color = StatusBlocked)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) {
                    Text("İptal", color = BEngelAccent)
                }
            },
        )
    }
}

private fun formatDateTime(timestamp: Long): String =
    SimpleDateFormat("dd.MM.yy HH:mm", Locale.getDefault()).format(Date(timestamp))
