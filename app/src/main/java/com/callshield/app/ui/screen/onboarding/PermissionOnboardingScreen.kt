package com.callshield.app.ui.screen.onboarding

import android.Manifest
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PermissionOnboardingScreen(
    isScreeningServiceActive: Boolean,
    onFinish: () -> Unit,
) {
    val context = LocalContext.current

    val runtimePermissions = buildList {
        add(Manifest.permission.RECEIVE_SMS)
        add(Manifest.permission.READ_PHONE_STATE)
        add(Manifest.permission.ANSWER_PHONE_CALLS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val runtimePermissionsState = rememberMultiplePermissionsState(permissions = runtimePermissions)

    var overlayGranted by remember {
        mutableStateOf(Settings.canDrawOverlays(context))
    }

    var notificationListenerGranted by remember {
        mutableStateOf(isNotificationListenerEnabled(context))
    }

    val overlayLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        overlayGranted = Settings.canDrawOverlays(context)
    }

    val notificationListenerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        notificationListenerGranted = isNotificationListenerEnabled(context)
    }

    val screeningRoleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { /* isScreeningServiceActive is recomputed in MainActivity and passed down */ }

    val allGranted = runtimePermissionsState.allPermissionsGranted &&
            overlayGranted &&
            notificationListenerGranted &&
            isScreeningServiceActive

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("CallShield Kurulum") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Dolandırıcı aramaları ve SMS'leri engellemek için aşağıdaki izinlere ihtiyaç var.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(4.dp))

            // SMS permission
            val smsState = rememberPermissionState(Manifest.permission.RECEIVE_SMS)
            PermissionItem(
                icon = Icons.AutoMirrored.Filled.Message,
                title = "SMS Okuma",
                description = "Gelen SMS mesajlarını analiz ederek dolandırıcılık içeriğini tespit eder.",
                granted = smsState.status.isGranted,
                buttonLabel = if (smsState.status.isGranted) "Verildi" else "İzin Ver",
                buttonEnabled = !smsState.status.isGranted,
                onButtonClick = { runtimePermissionsState.launchMultiplePermissionRequest() },
            )

            // READ_PHONE_STATE permission
            val phoneStateGranted = runtimePermissionsState.permissions
                .first { it.permission == Manifest.permission.READ_PHONE_STATE }
                .status.isGranted
            PermissionItem(
                icon = Icons.Default.Phone,
                title = "Telefon Durumu",
                description = "Gelen aramaların numarasını okuyarak engellenmiş numaraları kontrol eder.",
                granted = phoneStateGranted,
                buttonLabel = if (phoneStateGranted) "Verildi" else "İzin Ver",
                buttonEnabled = !phoneStateGranted,
                onButtonClick = { runtimePermissionsState.launchMultiplePermissionRequest() },
            )

            // ANSWER_PHONE_CALLS permission
            val answerCallsGranted = runtimePermissionsState.permissions
                .first { it.permission == Manifest.permission.ANSWER_PHONE_CALLS }
                .status.isGranted
            PermissionItem(
                icon = Icons.Default.Call,
                title = "Aramaları Yönetme",
                description = "Engellenen numaralardan gelen aramaları otomatik olarak reddeder.",
                granted = answerCallsGranted,
                buttonLabel = if (answerCallsGranted) "Verildi" else "İzin Ver",
                buttonEnabled = !answerCallsGranted,
                onButtonClick = { runtimePermissionsState.launchMultiplePermissionRequest() },
            )

            // POST_NOTIFICATIONS (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val notifGranted = runtimePermissionsState.permissions
                    .first { it.permission == Manifest.permission.POST_NOTIFICATIONS }
                    .status.isGranted
                PermissionItem(
                    icon = Icons.Default.Warning,
                    title = "Bildirimler",
                    description = "Dolandırıcı SMS veya arama tespit edildiğinde bildirim göndermek için gereklidir.",
                    granted = notifGranted,
                    buttonLabel = if (notifGranted) "Verildi" else "İzin Ver",
                    buttonEnabled = !notifGranted,
                    onButtonClick = { runtimePermissionsState.launchMultiplePermissionRequest() },
                )
            }

            // CallScreeningService role
            PermissionItem(
                icon = Icons.Default.Settings,
                title = "Arama Tarama Servisi",
                description = "Uygulamanın sistem seviyesinde aramaları tarayabilmesi için varsayılan tarama servisi olarak ayarlanması gerekir.",
                granted = isScreeningServiceActive,
                buttonLabel = if (isScreeningServiceActive) "Etkin" else "Ayarla",
                buttonEnabled = !isScreeningServiceActive,
                onButtonClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val roleManager = context.getSystemService(RoleManager::class.java)
                        val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                        screeningRoleLauncher.launch(intent)
                    } else {
                        val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
                        context.startActivity(intent)
                    }
                },
            )

            // SYSTEM_ALERT_WINDOW overlay
            PermissionItem(
                icon = Icons.Default.Warning,
                title = "Ekran Üstü Gösterim",
                description = "Tehlikeli arama geldiğinde ekranın üzerinde uyarı göstermek için gereklidir.",
                granted = overlayGranted,
                buttonLabel = if (overlayGranted) "Verildi" else "Ayarla",
                buttonEnabled = !overlayGranted,
                onButtonClick = {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}"),
                    )
                    overlayLauncher.launch(intent)
                },
            )

            // NotificationListenerService
            PermissionItem(
                icon = Icons.Default.CheckCircle,
                title = "Bildirim Erişimi",
                description = "Şüpheli SMS bildirimlerini gizleyip yerine uyarı göstermek için gereklidir.",
                granted = notificationListenerGranted,
                buttonLabel = if (notificationListenerGranted) "Verildi" else "Ayarla",
                buttonEnabled = !notificationListenerGranted,
                onButtonClick = {
                    val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    notificationListenerLauncher.launch(intent)
                },
            )

            Spacer(Modifier.height(8.dp))

            // Primary CTA
            Button(
                onClick = { runtimePermissionsState.launchMultiplePermissionRequest() },
                modifier = Modifier.fillMaxWidth(),
                enabled = !runtimePermissionsState.allPermissionsGranted,
            ) {
                Text("Tüm İzinleri Ver")
            }

            // Navigate to app
            OutlinedButton(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (allGranted) "Uygulamayı Başlat" else "Şimdilik Atla")
            }

            if (!allGranted) {
                Text(
                    text = "Bazı izinler verilmeden uygulama tam koruma sağlayamaz.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PermissionItem(
    icon: ImageVector,
    title: String,
    description: String,
    granted: Boolean,
    buttonLabel: String,
    buttonEnabled: Boolean,
    onButtonClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (granted)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (granted)
                    MaterialTheme.colorScheme.onSecondaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (granted) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(8.dp))
            TextButton(
                onClick = onButtonClick,
                enabled = buttonEnabled,
            ) {
                Text(buttonLabel)
            }
        }
    }
}

private fun isNotificationListenerEnabled(context: Context): Boolean {
    val flat = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners",
    ) ?: return false
    val component = ComponentName(context, "com.callshield.app.service.NotificationSuppressorService")
    return flat.split(":").any { entry ->
        ComponentName.unflattenFromString(entry) == component
    }
}
