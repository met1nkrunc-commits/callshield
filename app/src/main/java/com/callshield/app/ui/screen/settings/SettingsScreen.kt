package com.callshield.app.ui.screen.settings

import android.app.role.RoleManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.callshield.app.ui.theme.BEngelAccent
import com.callshield.app.ui.theme.BEngelGreen
import com.callshield.app.ui.theme.BEngelGreenDark
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
    onNavigateToHistory: () -> Unit = {},
    onNavigateToTrusted: () -> Unit = {},
    onNavigateToPhishingHistory: () -> Unit = {},
    onNavigateToQuarantine: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Call screening role state
    var callScreeningGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.getSystemService(RoleManager::class.java)
                    .isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
            } else false
        )
    }

    // Default SMS app role state
    var smsDefaultGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                context.getSystemService(RoleManager::class.java)
                    .isRoleHeld(RoleManager.ROLE_SMS)
            } else false
        )
    }
    // SMS Default App Rationale
    var showSmsRationale by remember { mutableStateOf(false) }
    val smsDefaultLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        smsDefaultGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getSystemService(RoleManager::class.java)
                .isRoleHeld(RoleManager.ROLE_SMS)
        } else false
    }

    // Call Screening Rationale
    var showCallRationale by remember { mutableStateOf(false) }
    val callScreeningLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        callScreeningGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getSystemService(RoleManager::class.java)
                .isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
        } else false
    }

    if (showSmsRationale) {
        PermissionRationaleDialog(
            title       = "Varsayılan SMS Uygulaması",
            description = "Spam ve dolandırıcılık amaçlı SMS'leri tespit edip engelleyebilmek için Callshield'ın varsayılan SMS uygulaması olması gerekmektedir. Mesajlarınız sadece cihazınızda taranır.",
            onDismiss   = { showSmsRationale = false },
            onConfirm   = {
                showSmsRationale = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val rm = context.getSystemService(RoleManager::class.java)
                    smsDefaultLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_SMS))
                }
            }
        )
    }

    if (showCallRationale) {
        PermissionRationaleDialog(
            title       = "Arama Tarama Özelliği",
            description = "Şüpheli aramaları gerçek zamanlı tespit edip sizi uyarabilmek için Arama Tarama yetkisi gereklidir. Bu yetki ile arayan numaralar güvenli listelerle karşılaştırılır.",
            onDismiss   = { showCallRationale = false },
            onConfirm   = {
                showCallRationale = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val rm = context.getSystemService(RoleManager::class.java)
                    callScreeningLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
                }
            }
        )
    }

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

            SectionLabel("SMS FİLTRELEME")

            SmsDefaultRow(
                isGranted = smsDefaultGranted,
                onRequest = { showSmsRationale = true },
            )

            Spacer(Modifier.height(24.dp))

            SectionLabel("ARAMA TARAMA")

            CallScreeningRow(
                isGranted = callScreeningGranted,
                onRequest = { showCallRationale = true },
            )

            Spacer(Modifier.height(8.dp))
            SwitchRow(
                title       = "Yüksek risk aramalarını engelle",
                description = "HIGH olarak işaretlenen aramaları otomatik reddet",
                checked     = uiState.blockHighRisk,
                onChecked   = viewModel::toggleBlockHighRisk,
            )

            Spacer(Modifier.height(24.dp))

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

            SectionLabel("GÜVENİLİR NUMARALAR")

            ClickableInfoRow(
                label   = "Güvenilir Numara Listesi",
                onClick = onNavigateToTrusted,
            )

            Spacer(Modifier.height(24.dp))

            SectionLabel("GEÇMİŞ")

            ClickableInfoRow(
                label   = "Engelleme Geçmişi",
                onClick = onNavigateToHistory,
            )
            Spacer(Modifier.height(8.dp))
            ClickableInfoRow(
                label   = "Phishing URL Geçmişi",
                onClick = onNavigateToPhishingHistory,
            )
            Spacer(Modifier.height(8.dp))
            ClickableInfoRow(
                label   = "Karantina (Engellenen SMS'ler)",
                onClick = onNavigateToQuarantine,
            )

            Spacer(Modifier.height(24.dp))

            SectionLabel("DOLANDIRICI LİSTESİ")

            SyncRow(
                updateState = uiState.updateState,
                lastInfo    = uiState.lastUpdateInfo,
                onSync      = viewModel::syncNow,
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

            Spacer(Modifier.height(24.dp))

            // ── VERİ YÖNETİMİ — Google Play Data Safety zorunlu ──
            SectionLabel("VERİ YÖNETİMİ")

            var showDeleteDialog by remember { mutableStateOf(false) }

            DangerActionRow(
                label       = "Tüm Verilerimi Sil",
                description = if (uiState.clearSuccess)
                    "✓ Tüm veriler başarıyla silindi"
                else
                    "Engelleme geçmişi, karantina ve cache temizlenir",
                isLoading   = uiState.isClearing,
                isSuccess   = uiState.clearSuccess,
                onClick     = { showDeleteDialog = true },
            )

            if (showDeleteDialog) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    containerColor   = DarkSurfaceVar,
                    titleContentColor = OnDarkPrimary,
                    textContentColor  = OnDarkSecondary,
                    title = { Text("Tüm Verileri Sil?") },
                    text  = {
                        Text(
                            "Engelleme geçmişi, karantina SMS'leri, numara cache'i ve " +
                            "phishing URL geçmişi kalıcı olarak silinecektir.\n\n" +
                            "Engellenen numara listesi ve ayarlarınız da sıfırlanacaktır. " +
                            "Bu işlem geri alınamaz."
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeleteDialog = false
                                viewModel.clearAllUserData()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDC3545),
                                contentColor   = Color.White,
                            ),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text("Sil", fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Vazgeç", color = OnDarkSecondary)
                        }
                    },
                )
            }

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
private fun SyncRow(updateState: UpdateState, lastInfo: String, onSync: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVar)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Numara Listesi",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = OnDarkPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = when (updateState) {
                    UpdateState.RUNNING -> "Güncelleniyor..."
                    UpdateState.SUCCESS -> "✓ ${lastInfo}"
                    UpdateState.ERROR   -> "✗ ${lastInfo}"
                    UpdateState.IDLE    -> "24 saatte bir otomatik güncellenir"
                },
                fontSize = 12.sp,
                color = when (updateState) {
                    UpdateState.SUCCESS -> BEngelGreen
                    UpdateState.ERROR   -> androidx.compose.ui.graphics.Color.Red.copy(alpha = 0.7f)
                    else                -> OnDarkSecondary
                },
                lineHeight = 16.sp,
            )
        }

        if (updateState == UpdateState.RUNNING) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = BEngelAccent,
                strokeWidth = 2.dp,
            )
        } else {
            Button(
                onClick = onSync,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BEngelGreenDark,
                    contentColor = Color.White,
                ),
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Text("Güncelle", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun CallScreeningRow(isGranted: Boolean, onRequest: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVar)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Arama Tarama",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = OnDarkPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (isGranted) "Aktif — gelen aramalar filtreleniyor"
                       else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) "Android 10+ gerekli"
                       else "Devre dışı — aramaları engellemek için etkinleştirin",
                fontSize = 12.sp,
                color = if (isGranted) BEngelGreen else OnDarkSecondary,
                lineHeight = 16.sp,
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !isGranted) {
            Button(
                onClick = onRequest,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BEngelGreenDark,
                    contentColor = Color.White,
                ),
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Text("Etkinleştir", fontSize = 12.sp)
            }
        } else {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isGranted) BEngelGreen else OnDarkDisabled),
            )
        }
    }
}

@Composable
private fun SmsDefaultRow(isGranted: Boolean, onRequest: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVar)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Varsayılan SMS Uygulaması",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = OnDarkPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (isGranted)
                    "Aktif — spam SMS veritabanına yazılmıyor"
                else
                    "Tam SMS engelleme için gerekli — spam mesajlar uygulamada görünür",
                fontSize = 12.sp,
                color = if (isGranted) BEngelGreen else OnDarkSecondary,
                lineHeight = 16.sp,
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !isGranted) {
            Button(
                onClick = onRequest,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BEngelGreenDark,
                    contentColor = Color.White,
                ),
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Text("Etkinleştir", fontSize = 12.sp)
            }
        } else {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isGranted) BEngelGreen else OnDarkDisabled),
            )
        }
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

@Composable
private fun DangerActionRow(
    label: String,
    description: String,
    isLoading: Boolean,
    isSuccess: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceVar)
            .then(if (!isLoading) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSuccess) BEngelGreen else Color(0xFFDC3545),
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = if (isSuccess) BEngelGreen else OnDarkSecondary,
                lineHeight = 16.sp,
            )
        }
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color(0xFFDC3545),
                strokeWidth = 2.dp,
            )
        }
    }
}

@Composable
private fun PermissionRationaleDialog(
    title: String,
    description: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = DarkSurfaceVar,
        titleContentColor = OnDarkPrimary,
        textContentColor  = OnDarkSecondary,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text  = {
            Text(
                text       = description,
                fontSize   = 14.sp,
                lineHeight = 20.sp,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape   = RoundedCornerShape(8.dp),
                colors  = ButtonDefaults.buttonColors(
                    containerColor = BEngelGreenDark,
                    contentColor   = Color.White,
                ),
            ) {
                Text("Anladım")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Şimdi Değil", color = OnDarkSecondary)
            }
        },
    )
}
