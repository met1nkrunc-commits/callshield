package com.callshield.app.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.callshield.app.ui.theme.BEngelAccent
import com.callshield.app.ui.theme.BEngelGreen
import com.callshield.app.ui.theme.DarkBg
import com.callshield.app.ui.theme.DarkSurface
import com.callshield.app.ui.theme.DarkSurfaceVar
import com.callshield.app.ui.theme.OnDarkDisabled
import com.callshield.app.ui.theme.OnDarkPrimary
import com.callshield.app.ui.theme.OnDarkSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToPaywall: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ayarlar",
                        color = OnDarkPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = DarkSurface,
                    titleContentColor = OnDarkPrimary,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(20.dp))

            SectionLabel("ABONELİK")

            PremiumRow(onClick = onNavigateToPaywall)

            Spacer(Modifier.height(24.dp))

            SectionLabel("BİLDİRİMLER")

            SwitchRow(
                title       = "Uyarı bildirimleri",
                description = "Şüpheli SMS ve aramalar için bildirim göster",
                checked     = uiState.notificationsEnabled,
                onChecked   = viewModel::toggleNotifications,
            )

            Spacer(Modifier.height(24.dp))

            SectionLabel("GÜVENİLİR GÖNDERENLER")

            InfoCard(
                title = "Beyaz liste",
                body  = "Aşağıdaki gönderenlerden gelen SMS'ler analiz edilmez ve her zaman güvenli kabul edilir:\n\n" +
                        "Akbank, GarantiBBVA, İşBankası, Yapıkredi, Ziraat, Halkbank, Vakıfbank, " +
                        "DenizBank, Finansbank, TEB, Turkcell, Vodafone, TurkTelekom, " +
                        "PTT, Trendyol, Hepsiburada, Amazon, Getir, Yemeksepeti ve diğerleri.",
            )

            Spacer(Modifier.height(24.dp))

            SectionLabel("UYGULAMA")

            InfoRow(label = "Sürüm",       value = "1.0.0")
            Spacer(Modifier.height(8.dp))
            InfoRow(label = "Uygulama Adı", value = "Callshield")

            Spacer(Modifier.height(24.dp))

            SectionLabel("HUKUKİ")

            ClickableInfoRow(
                label   = "Gizlilik Politikası",
                onClick = onNavigateToPrivacy,
            )
            Spacer(Modifier.height(8.dp))
            ClickableInfoRow(
                label   = "Kullanım Koşulları",
                onClick = onNavigateToTerms,
            )

            Spacer(Modifier.height(40.dp))

            Text(
                text      = "Callshield · Türkiye Dolandırıcılık Koruma\nv1.0.0",
                fontSize  = 12.sp,
                color     = OnDarkDisabled,
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth(),
                lineHeight = 18.sp,
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text         = text,
        fontSize     = 11.sp,
        fontWeight   = FontWeight.SemiBold,
        color        = OnDarkSecondary,
        letterSpacing = 2.sp,
        modifier     = Modifier.padding(bottom = 10.dp),
    )
}

@Composable
private fun SwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVar)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = title,
                fontSize   = 15.sp,
                fontWeight = FontWeight.Medium,
                color      = OnDarkPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text       = description,
                fontSize   = 12.sp,
                color      = OnDarkSecondary,
                lineHeight = 16.sp,
            )
        }
        Switch(
            checked         = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(
                checkedThumbColor   = DarkBg,
                checkedTrackColor   = BEngelAccent,
                uncheckedThumbColor = OnDarkSecondary,
                uncheckedTrackColor = DarkSurface,
            ),
        )
    }
}

@Composable
private fun InfoCard(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVar)
            .padding(16.dp),
    ) {
        Text(
            text       = title,
            fontSize   = 15.sp,
            fontWeight = FontWeight.Medium,
            color      = OnDarkPrimary,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text       = body,
            fontSize   = 12.sp,
            color      = OnDarkSecondary,
            lineHeight = 18.sp,
        )
    }
}

@Composable
private fun PremiumRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVar)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = BEngelGreen,
            modifier = Modifier.padding(end = 12.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Premium'a Geç",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = OnDarkPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "IPQS spam kontrolü · 7 gün ücretsiz",
                fontSize = 12.sp,
                color = BEngelAccent,
            )
        }
        Text("›", fontSize = 20.sp, color = OnDarkSecondary)
    }
}

@Composable
private fun ClickableInfoRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVar)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text     = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color    = OnDarkPrimary,
            modifier = Modifier.weight(1f),
        )
        Text("›", fontSize = 20.sp, color = OnDarkSecondary)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVar)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text       = label,
            fontSize   = 15.sp,
            fontWeight = FontWeight.Medium,
            color      = OnDarkPrimary,
            modifier   = Modifier.weight(1f),
        )
        Text(
            text     = value,
            fontSize = 13.sp,
            color    = OnDarkSecondary,
        )
    }
}
