package com.callshield.app.ui.screen.onboarding

import android.Manifest
import android.app.role.RoleManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneEnabled
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callshield.app.service.NotificationSuppressorService
import com.callshield.app.ui.theme.BEngelAccent
import com.callshield.app.ui.theme.BEngelGreen
import com.callshield.app.ui.theme.BEngelGreenDark
import com.callshield.app.ui.theme.DarkBg
import com.callshield.app.ui.theme.DarkBorder
import com.callshield.app.ui.theme.DarkSurfaceVar
import com.callshield.app.ui.theme.OnDarkDisabled
import com.callshield.app.ui.theme.OnDarkPrimary
import com.callshield.app.ui.theme.OnDarkSecondary

data class OnboardingStep(
    val index: Int,
    val icon: ImageVector,
    val title: String,
    val description: String,
    val buttonLabel: String,
    val isGranted: (Context) -> Boolean,
    val onGrant: (Context) -> Unit,
)

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    var currentStep by remember { mutableIntStateOf(0) }
    var grantedSteps by remember { mutableStateOf(setOf<Int>()) }

    fun autoAdvance(stepIndex: Int, totalSteps: Int) {
        scope.launch {
            delay(700)
            if (currentStep == stepIndex && stepIndex < totalSteps - 1) currentStep++
        }
    }

    // SMS permission launcher
    val smsPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.all { it }) {
            grantedSteps = grantedSteps + 0
            autoAdvance(0, 4)
        }
    }

    // POST_NOTIFICATIONS launcher
    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            grantedSteps = grantedSteps + 2
            autoAdvance(2, 4)
        }
    }

    // Call screening role launcher
    val callScreeningLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                grantedSteps = grantedSteps + 1
                autoAdvance(1, 4)
            }
        }
    }

    // Notification listener settings launcher
    val notifListenerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val cn = ComponentName(context, NotificationSuppressorService::class.java)
        val enabled = Settings.Secure.getString(
            context.contentResolver, "enabled_notification_listeners"
        )?.contains(cn.flattenToString()) == true
        if (enabled) {
            grantedSteps = grantedSteps + 3
            autoAdvance(3, 4)
        }
    }

    val steps = listOf(
        OnboardingStep(
            index       = 0,
            icon        = Icons.Default.Sms,
            title       = "SMS İzni",
            description = "Gelen SMS mesajlarını tarayabilmek için izin gerekiyor. İçerikler cihazınızda analiz edilir, dışarı gönderilmez.",
            buttonLabel = "SMS İznini Ver",
            isGranted   = { ctx ->
                ctx.checkSelfPermission(Manifest.permission.RECEIVE_SMS) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED
            },
            onGrant = {
                smsPermLauncher.launch(
                    arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
                )
            },
        ),
        OnboardingStep(
            index       = 1,
            icon        = Icons.Default.PhoneEnabled,
            title       = "Arama Tarama",
            description = "Gelen aramaları filtreleyebilmek için Arama Tarama rolü gerekiyor. Bu rol spam aramaları otomatik engeller.",
            buttonLabel = "Arama Taramayı Etkinleştir",
            isGranted   = { ctx ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val roleManager = ctx.getSystemService(RoleManager::class.java)
                    roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
                } else false
            },
            onGrant = { ctx ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val roleManager = ctx.getSystemService(RoleManager::class.java)
                    val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING)
                    callScreeningLauncher.launch(intent)
                }
            },
        ),
        OnboardingStep(
            index       = 2,
            icon        = Icons.Default.Notifications,
            title       = "Bildirim İzni",
            description = "Engellenen SMS ve aramalar için uyarı bildirimleri göndermek amacıyla gerekiyor.",
            buttonLabel = "Bildirimlere İzin Ver",
            isGranted   = { _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                            android.content.pm.PackageManager.PERMISSION_GRANTED
                } else true
            },
            onGrant = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
        ),
        OnboardingStep(
            index       = 3,
            icon        = Icons.Default.NotificationsActive,
            title       = "Bildirim Erişimi",
            description = "SMS uygulamasının bildirimlerini okuyarak spam mesajları gizlemek için Bildirim Erişimi gerekiyor.",
            buttonLabel = "Bildirim Erişimini Ver",
            isGranted   = { ctx ->
                val cn = ComponentName(ctx, NotificationSuppressorService::class.java)
                Settings.Secure.getString(
                    ctx.contentResolver, "enabled_notification_listeners"
                )?.contains(cn.flattenToString()) == true
            },
            onGrant = {
                notifListenerLauncher.launch(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            },
        ),
    )

    // Auto-detect already-granted permissions whenever the current step changes
    LaunchedEffect(currentStep) {
        val step = steps[currentStep]
        if (step.isGranted(context)) {
            grantedSteps = grantedSteps + currentStep
        }
    }

    val allGranted = steps.all { it.isGranted(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(48.dp))

        // App name
        Text(
            text          = "B-Engel",
            fontSize      = 26.sp,
            fontWeight    = FontWeight.Bold,
            color         = OnDarkPrimary,
            letterSpacing = 2.sp,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text          = "Kurulum",
            fontSize      = 13.sp,
            color         = OnDarkSecondary,
            letterSpacing = 1.sp,
        )

        Spacer(Modifier.height(32.dp))

        // Step dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            steps.forEachIndexed { index, step ->
                val isGranted = step.isGranted(context)
                val isActive  = index == currentStep
                Box(
                    modifier = Modifier
                        .size(if (isActive) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isGranted -> BEngelAccent
                                isActive  -> BEngelGreen
                                else      -> DarkBorder
                            }
                        )
                )
            }
        }

        Spacer(Modifier.height(48.dp))

        // Animated step content
        AnimatedContent(
            targetState  = currentStep,
            transitionSpec = {
                slideInHorizontally(tween(300)) { it } togetherWith
                        slideOutHorizontally(tween(300)) { -it }
            },
            label = "stepContent",
        ) { stepIndex ->
            val step    = steps[stepIndex]
            val granted = step.isGranted(context)

            Column(
                modifier              = Modifier.fillMaxWidth(),
                horizontalAlignment   = Alignment.CenterHorizontally,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            if (granted) BEngelAccent.copy(alpha = 0.15f) else DarkSurfaceVar
                        ),
                ) {
                    Icon(
                        imageVector     = step.icon,
                        contentDescription = null,
                        tint            = if (granted) BEngelAccent else OnDarkSecondary,
                        modifier        = Modifier.size(48.dp),
                    )
                }

                Spacer(Modifier.height(28.dp))

                Text(
                    text        = step.title,
                    fontSize    = 22.sp,
                    fontWeight  = FontWeight.Bold,
                    color       = OnDarkPrimary,
                    textAlign   = TextAlign.Center,
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text       = step.description,
                    fontSize   = 14.sp,
                    color      = OnDarkSecondary,
                    textAlign  = TextAlign.Center,
                    lineHeight = 21.sp,
                )

                Spacer(Modifier.height(16.dp))

                if (granted) {
                    Row(
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(BEngelAccent.copy(alpha = 0.15f))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint     = BEngelAccent,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            "Verildi",
                            fontSize   = 13.sp,
                            color      = BEngelAccent,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        val currentStepData  = steps[currentStep]
        val isCurrentGranted = currentStepData.isGranted(context)

        Button(
            onClick = {
                if (isCurrentGranted) {
                    if (currentStep < steps.size - 1) currentStep++
                    else if (allGranted) onComplete()
                } else {
                    currentStepData.onGrant(context)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape  = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isCurrentGranted) BEngelGreen else BEngelGreenDark,
                contentColor   = Color.White,
            ),
        ) {
            Text(
                text = when {
                    allGranted && currentStep == steps.size - 1 -> "Korumayı Başlat →"
                    isCurrentGranted                             -> "Devam →"
                    else                                         -> currentStepData.buttonLabel
                },
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(
                onClick = { if (currentStep > 0) currentStep-- },
                enabled = currentStep > 0,
            ) {
                Text(
                    "← Geri",
                    color    = if (currentStep > 0) OnDarkSecondary else Color.Transparent,
                    fontSize = 13.sp,
                )
            }
            TextButton(onClick = {
                if (currentStep < steps.size - 1) currentStep++
                else onComplete()
            }) {
                Text("Atla", color = OnDarkDisabled, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
