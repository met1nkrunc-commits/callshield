package com.callshield.app.ui.screen.lookup

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.callshield.app.core.billing.DailyQuotaManager
import com.callshield.app.domain.model.RiskLevel
import com.callshield.app.ui.component.RiskChip
import com.callshield.app.ui.theme.BEngelAccent
import com.callshield.app.ui.theme.DarkBg
import com.callshield.app.ui.theme.DarkSurfaceVar
import com.callshield.app.ui.theme.OnDarkPrimary
import com.callshield.app.ui.theme.OnDarkSecondary
import com.callshield.app.ui.theme.toColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NumberLookupScreen(
    onBack: () -> Unit,
    viewModel: NumberLookupViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val remainingQuota by viewModel.remainingQuota.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg),
    ) {
        TopAppBar(
            title = {
                Text("Numara Sorgula", color = OnDarkPrimary, fontWeight = FontWeight.SemiBold)
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = OnDarkPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(16.dp))

            val isPremiumUser = state.result?.isPremium ?: false
            Text(
                text = if (isPremiumUser) "Premium · IPQS gerçek zamanlı analiz aktif"
                       else "Ücretsiz · Günlük $remainingQuota/${DailyQuotaManager.FREE_DAILY_LIMIT} IPQS sorgu hakkı kaldı",
                fontSize = 13.sp,
                color = if (!isPremiumUser && remainingQuota == 0) androidx.compose.ui.graphics.Color(0xFFEF5350) else OnDarkSecondary,
                lineHeight = 18.sp,
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                label = { Text("Telefon numarası", color = OnDarkSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = OnDarkSecondary) },
                placeholder = { Text("+90 5xx xxx xx xx", color = OnDarkSecondary) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Search,
                ),
                keyboardActions = KeyboardActions(onSearch = { viewModel.lookup() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = BEngelAccent,
                    unfocusedBorderColor = OnDarkSecondary,
                    focusedTextColor     = OnDarkPrimary,
                    unfocusedTextColor   = OnDarkPrimary,
                    cursorColor          = BEngelAccent,
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = viewModel::lookup,
                enabled = state.query.trim().isNotBlank() && !state.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = BEngelAccent),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Sorgula", color = DarkBg, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(32.dp))

            if (state.isLoading) {
                CircularProgressIndicator(color = BEngelAccent)
            }

            state.result?.let { result ->
                LookupResultCard(result)
            }
        }
    }
}

@Composable
private fun LookupResultCard(result: LookupResult) {
    val color = result.riskLevel.toColor()
    val emoji = when (result.riskLevel) {
        RiskLevel.BLOCKED -> "⛔"
        RiskLevel.SAFE    -> "✅"
        RiskLevel.HIGH    -> "🟠"
        RiskLevel.MEDIUM  -> "🟡"
        else              -> "ℹ️"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceVar)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(emoji, fontSize = 22.sp)
            }
            Column {
                Text(
                    text = result.number,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnDarkPrimary,
                )
                RiskChip(riskLevel = result.riskLevel)
            }
        }
        Text(
            text = result.reason,
            fontSize = 13.sp,
            color = OnDarkSecondary,
            lineHeight = 18.sp,
        )
        if (result.entity?.reason?.isNotBlank() == true) {
            Text(
                text = "Kayıt nedeni: ${result.entity.reason}",
                fontSize = 12.sp,
                color = OnDarkSecondary,
            )
        }
        Text(
            text = if (result.isPremium) "Kaynak: Yerel DB + IPQS" else "Kaynak: Yerel veritabanı (ücretsiz)",
            fontSize = 11.sp,
            color = OnDarkSecondary,
        )
        if (result.prefixInfo != null) {
            Text("📞 ${result.prefixInfo}", fontSize = 12.sp, color = OnDarkSecondary)
        }
        if (result.ipqsDetail != null) {
            Spacer(Modifier.height(8.dp))
            Text("IPQS ANALİZİ", fontSize = 10.sp, color = OnDarkSecondary, letterSpacing = 1.5.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IpqsScoreChip("Dolandırıcılık", result.ipqsDetail.fraudScore)
                IpqsScoreChip("Spam", result.ipqsDetail.spamScore)
            }
            if (!result.ipqsDetail.carrier.isNullOrBlank()) {
                Text("Operatör: ${result.ipqsDetail.carrier}", fontSize = 12.sp, color = OnDarkSecondary)
            }
            if (!result.ipqsDetail.lineType.isNullOrBlank()) {
                Text("Hat tipi: ${result.ipqsDetail.lineType}", fontSize = 12.sp, color = OnDarkSecondary)
            }
        }
        if (!result.isPremium && result.ipqsDetail == null) {
            Spacer(Modifier.height(8.dp))
            Text("🔒 Premium'da IPQS gerçek zamanlı analiz", fontSize = 12.sp, color = OnDarkSecondary)
        }
    }
}

@Composable
private fun IpqsScoreChip(label: String, score: Int) {
    val color = when {
        score >= 75 -> androidx.compose.ui.graphics.Color(0xFFEF5350)
        score >= 50 -> androidx.compose.ui.graphics.Color(0xFFFFCA28)
        else -> BEngelAccent
    }
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVar)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(score.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 10.sp, color = OnDarkSecondary)
    }
}
