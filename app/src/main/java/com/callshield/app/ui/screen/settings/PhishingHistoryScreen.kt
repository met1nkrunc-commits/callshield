package com.callshield.app.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import com.callshield.app.ui.component.EmptyState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bengel.shared.domain.model.RiskLevel
import com.callshield.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhishingHistoryScreen(
    onBack: () -> Unit,
    viewModel: PhishingHistoryViewModel = hiltViewModel(),
) {
    val urls by viewModel.urls.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("Phishing URL Geçmişi", color = OnDarkPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = OnDarkPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface, titleContentColor = OnDarkPrimary),
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(12.dp))

            // ── Manuel URL kontrol alanı ──
            Text("URL Kontrol Et", fontSize = 13.sp, color = OnDarkSecondary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = viewModel.urlInput,
                    onValueChange = viewModel::onUrlInputChange,
                    placeholder = { Text("https://ornek.com/link", fontSize = 13.sp, color = OnDarkDisabled) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BEngelAccent,
                        unfocusedBorderColor = DarkSurfaceVar,
                        focusedTextColor = OnDarkPrimary,
                        unfocusedTextColor = OnDarkPrimary,
                        cursorColor = BEngelAccent,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { viewModel.checkUrl() }),
                    shape = RoundedCornerShape(10.dp),
                )
                IconButton(
                    onClick = viewModel::checkUrl,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(BEngelAccent)
                        .size(48.dp),
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Kontrol et", tint = DarkBg)
                }
            }

            // ── Kontrol sonucu ──
            viewModel.checkResult?.let { result ->
                Spacer(Modifier.height(8.dp))
                val (bgColor, textColor, label) = when (result.riskLevel) {
                    RiskLevel.BLOCKED -> Triple(Color(0x33EF5350), StatusBlocked, "ENGELLENDI")
                    RiskLevel.HIGH    -> Triple(Color(0x33FF7043), Color(0xFFFF7043), "YÜKSEK RİSK")
                    RiskLevel.MEDIUM  -> Triple(Color(0x33FFB300), Color(0xFFFFB300), "ORTA RİSK")
                    RiskLevel.LOW     -> Triple(Color(0x3366BB6A), Color(0xFF66BB6A), "DÜŞÜK RİSK")
                    RiskLevel.SAFE    -> Triple(Color(0x334CAF50), Color(0xFF4CAF50), "GÜVENLİ")
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(bgColor)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(label, fontSize = 12.sp, color = textColor, fontWeight = FontWeight.Bold)
                        Text(result.reason, fontSize = 11.sp, color = OnDarkSecondary, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = DarkSurfaceVar)
            Spacer(Modifier.height(12.dp))

            Text("Tespit Geçmişi", fontSize = 13.sp, color = OnDarkSecondary, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))

            if (urls.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Link,
                    title = "Şüpheli URL tespit edilmedi",
                    subtitle = "Gelen SMS'lerde veya manuel kontrolde\nphishing linki tespit edildiğinde burada görünecek",
                    modifier = Modifier.weight(1f),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp),
                ) {
                    items(urls) { entry ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurfaceVar)
                                .padding(14.dp)
                        ) {
                            Text(entry.url, fontSize = 13.sp, color = Color(0xFFEF5350), fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(4.dp))
                            Text("Gönderen: ${entry.sender}", fontSize = 12.sp, color = OnDarkSecondary)
                            Spacer(Modifier.height(2.dp))
                            Text(entry.snippet, fontSize = 11.sp, color = OnDarkDisabled, maxLines = 2)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                SimpleDateFormat("dd MMM yyyy HH:mm", Locale("tr")).format(Date(entry.detectedAt)),
                                fontSize = 11.sp,
                                color = OnDarkDisabled,
                            )
                        }
                    }
                }
            }
        }
    }
}
