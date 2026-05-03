package com.callshield.app.ui.screen.calllog

import android.Manifest
import android.content.pm.PackageManager
import android.provider.CallLog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallMissed
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.callshield.app.domain.model.RiskLevel
import com.callshield.app.ui.component.EmptyState
import com.callshield.app.ui.component.RiskChip
import com.callshield.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallLogScreen(
    onBack: () -> Unit,
    viewModel: CallLogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) viewModel.load() }

    LaunchedEffect(Unit) {
        if (!uiState.hasLoaded &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG)
                == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.load()
        }
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Son Aramalar", color = OnDarkPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = OnDarkPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface),
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = BEngelAccent)
                        Spacer(Modifier.height(12.dp))
                        Text("Aramalar analiz ediliyor…", color = OnDarkSecondary, fontSize = 13.sp)
                    }
                }
            }

            uiState.permissionDenied -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp),
                    ) {
                        Text("📵", fontSize = 48.sp)
                        Spacer(Modifier.height(16.dp))
                        Text("Çağrı Günlüğü İzni Gerekli", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnDarkPrimary)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Geçmiş aramaları analiz etmek için izin verin.",
                            fontSize = 13.sp, color = OnDarkSecondary,
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = { permLauncher.launch(Manifest.permission.READ_CALL_LOG) },
                            colors = ButtonDefaults.buttonColors(containerColor = BEngelAccent),
                        ) {
                            Text("İzin Ver", color = DarkBg, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            !uiState.hasLoaded -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                        Text("📞", fontSize = 48.sp)
                        Spacer(Modifier.height(16.dp))
                        Text("Son Aramaları Analiz Et", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = OnDarkPrimary)
                        Spacer(Modifier.height(8.dp))
                        Text("Son 100 aramanızı dolandırıcılık\nveri tabanıyla karşılaştırın.", fontSize = 13.sp, color = OnDarkSecondary)
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = { permLauncher.launch(Manifest.permission.READ_CALL_LOG) },
                            colors = ButtonDefaults.buttonColors(containerColor = BEngelAccent),
                        ) {
                            Text("Analiz Et", color = DarkBg, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            uiState.entries.isEmpty() -> {
                EmptyState(
                    icon = Icons.Default.Phone,
                    title = "Arama geçmişi bulunamadı",
                    subtitle = "Cihazınızda kayıtlı arama bulunamadı.",
                    modifier = Modifier.padding(padding),
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                ) {
                    // Summary header
                    item {
                        val riskyCount = uiState.entries.count {
                            it.riskLevel != RiskLevel.SAFE && it.riskLevel != RiskLevel.LOW
                        }
                        Text(
                            "${uiState.entries.size} arama · $riskyCount şüpheli",
                            fontSize = 12.sp,
                            color = OnDarkSecondary,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                    items(uiState.entries, key = { "${it.number}_${it.date}" }) { entry ->
                        CallEntryRow(entry)
                    }
                }
            }
        }
    }
}

private val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale("tr"))

@Composable
private fun CallEntryRow(entry: CallLogEntry) {
    val callIcon = when (entry.callType) {
        CallLog.Calls.OUTGOING_TYPE -> Icons.Default.CallMade
        CallLog.Calls.MISSED_TYPE   -> Icons.Default.CallMissed
        else                         -> Icons.Default.CallReceived
    }
    val callColor = when (entry.callType) {
        CallLog.Calls.MISSED_TYPE -> StatusBlocked
        CallLog.Calls.OUTGOING_TYPE -> OnDarkSecondary
        else -> BEngelAccent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVar)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(callColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(callIcon, contentDescription = null, tint = callColor, modifier = Modifier.size(18.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.number,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnDarkPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(dateFormat.format(Date(entry.date)), fontSize = 11.sp, color = OnDarkSecondary)
                if (entry.callType != CallLog.Calls.MISSED_TYPE && entry.duration > 0) {
                    Text("·", fontSize = 11.sp, color = OnDarkDisabled)
                    Text(formatDuration(entry.duration), fontSize = 11.sp, color = OnDarkSecondary)
                }
            }
            if (entry.riskLevel != RiskLevel.SAFE && entry.riskLevel != RiskLevel.LOW && !entry.reason.isNullOrBlank()) {
                Text(entry.reason, fontSize = 10.sp, color = entry.riskLevel.toColor(), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        if (entry.riskLevel != RiskLevel.SAFE) {
            RiskChip(riskLevel = entry.riskLevel)
        }
    }
}

private fun formatDuration(seconds: Long): String = when {
    seconds < 60 -> "${seconds}s"
    else -> "${seconds / 60}d ${seconds % 60}s"
}
