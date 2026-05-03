package com.callshield.app.ui.screen.smsinbox

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.google.android.play.core.review.ReviewManagerFactory
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.callshield.app.domain.model.RiskLevel
import com.callshield.app.domain.usecase.sms.ScannedSms
import com.callshield.app.ui.theme.BEngelAccent
import com.callshield.app.ui.theme.DarkBg
import com.callshield.app.ui.theme.DarkSurfaceVar
import com.callshield.app.ui.theme.OnDarkPrimary
import com.callshield.app.ui.theme.OnDarkSecondary
import com.callshield.app.ui.theme.StatusBlocked
import com.callshield.app.ui.theme.toColor
import com.callshield.app.ui.theme.toLabel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SmsInboxScreen(
    viewModel: SmsInboxViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val displayed = viewModel.displayedMessages(state)

    val context = LocalContext.current
    val reviewManager = remember { ReviewManagerFactory.create(context) }

    // In-App Review trigger
    LaunchedEffect(viewModel) {
        viewModel.triggerReview.collect {
            val activity = context as? Activity ?: return@collect
            reviewManager.requestReviewFlow().addOnCompleteListener { request ->
                if (request.isSuccessful) {
                    reviewManager.launchReviewFlow(activity, request.result)
                }
            }
        }
    }

    // Auto-scan on first open if READ_SMS is already granted
    LaunchedEffect(Unit) {
        if (!state.hasScanned &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS)
                == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.scan()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) viewModel.scan() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(16.dp))

        // ── Header ──
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "SMS Gelen Kutusu",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnDarkPrimary,
                )
                if (state.hasScanned) {
                    val riskyCount = state.messages.count {
                        it.riskLevel != RiskLevel.SAFE && it.riskLevel != RiskLevel.LOW
                    }
                    Text(
                        "${state.messages.size} mesaj · $riskyCount şüpheli",
                        fontSize = 12.sp,
                        color = OnDarkSecondary,
                    )
                }
            }
            Button(
                onClick = {
                    permissionLauncher.launch(Manifest.permission.READ_SMS)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BEngelAccent),
                enabled = !state.isLoading,
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = DarkBg, modifier = Modifier.size(16.dp))
                Text("  Tara", color = DarkBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Filter chips ──
        if (state.hasScanned) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = state.filter == InboxFilter.ALL,
                    onClick  = { viewModel.setFilter(InboxFilter.ALL) },
                    label    = { Text("Tümü") },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BEngelAccent,
                        selectedLabelColor     = DarkBg,
                    ),
                )
                FilterChip(
                    selected = state.filter == InboxFilter.RISKY_ONLY,
                    onClick  = { viewModel.setFilter(InboxFilter.RISKY_ONLY) },
                    label    = { Text("Şüpheli") },
                    colors   = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = StatusBlocked,
                        selectedLabelColor     = OnDarkPrimary,
                    ),
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        // ── Content ──
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = BEngelAccent)
                        Spacer(Modifier.height(12.dp))
                        Text("Gelen kutusu taranıyor…", color = OnDarkSecondary, fontSize = 13.sp)
                    }
                }
            }

            state.permissionDenied -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "SMS okuma izni gerekli.\nAyarlar → Uygulamalar → Callshield → İzinler",
                        color = OnDarkSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    )
                }
            }

            !state.hasScanned -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📱", fontSize = 48.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Gelen kutunuzu analiz edin",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OnDarkPrimary,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "\"Tara\" düğmesine basarak mevcut SMS'lerinizi\nşüpheli içerik için tarayın.",
                            fontSize = 13.sp,
                            color = OnDarkSecondary,
                            lineHeight = 18.sp,
                        )
                    }
                }
            }

            displayed.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("✅", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Şüpheli mesaj bulunamadı",
                            fontSize = 16.sp,
                            color = BEngelAccent,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            else -> {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(displayed, key = { it.message.id }) { sms ->
                        SmsMessageRow(sms)
                    }
                }
            }
        }
    }
}

private val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale("tr"))

@Composable
private fun SmsMessageRow(sms: ScannedSms) {
    val color = sms.riskLevel.toColor()
    val riskEmoji = when (sms.riskLevel) {
        RiskLevel.BLOCKED -> "🔴"
        RiskLevel.HIGH    -> "🟠"
        RiskLevel.MEDIUM  -> "🟡"
        RiskLevel.LOW     -> "🟢"
        RiskLevel.SAFE    -> "✅"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVar)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Risk dot
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(riskEmoji, fontSize = 16.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = sms.message.address.ifBlank { "Bilinmeyen" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = OnDarkPrimary,
                )
                Text(
                    text = dateFormat.format(Date(sms.message.date)),
                    fontSize = 11.sp,
                    color = OnDarkSecondary,
                )
            }
            Text(
                text = sms.message.body,
                fontSize = 12.sp,
                color = OnDarkSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
            )
            if (sms.riskLevel != RiskLevel.SAFE) {
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = sms.riskLevel.toLabel(),
                        fontSize = 10.sp,
                        color = color,
                        fontWeight = FontWeight.Bold,
                    )
                    if (sms.matchedKeywords.isNotEmpty()) {
                        Text("·", fontSize = 10.sp, color = OnDarkSecondary)
                        Text(
                            text = sms.matchedKeywords.joinToString(", "),
                            fontSize = 10.sp,
                            color = OnDarkSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
