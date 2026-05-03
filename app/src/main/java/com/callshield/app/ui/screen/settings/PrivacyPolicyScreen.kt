package com.callshield.app.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.callshield.app.ui.theme.DarkBg
import com.callshield.app.ui.theme.DarkSurface
import com.callshield.app.ui.theme.OnDarkPrimary
import com.callshield.app.ui.theme.OnDarkSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Text("Gizlilik Politikası", color = OnDarkPrimary, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = OnDarkPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBg)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            PolicySection(
                title = "Son Güncelleme",
                body  = "1 Ocak 2025",
            )
            PolicySection(
                title = "Toplanan Veriler",
                body  = """
Callshield aşağıdaki verilere erişir ve bunları yalnızca cihazınızda işler:

• SMS içeriği — spam analizi için yerel olarak taranır; hiçbir metin sunucuya gönderilmez.
• Gelen arama numaraları — engelleme veya uyarı için yerel veri tabanıyla karşılaştırılır.
• Arama kaydı — aramayı yapanın geçmişini kontrol etmek için kullanılır.
• Engellenen numara listesi — yalnızca cihazınızda saklanır.
                """.trimIndent(),
            )
            PolicySection(
                title = "Üçüncü Taraf Hizmetler",
                body  = """
• GitHub (raw.githubusercontent.com) — Güncel dolandırıcı numara listesi indirilir. Hiçbir kişisel veri gönderilmez.

• IPQS (ipqualityscore.com) — Yalnızca Premium üyeler için; sorguladığınız telefon numarası IPQS sunucularına gönderilir ve anlık spam skoru alınır. Bu özellik isteğe bağlıdır; devre dışı bırakabilirsiniz.
                """.trimIndent(),
            )
            PolicySection(
                title = "Veri Saklama",
                body  = """
• Tüm veriler yalnızca cihazınızdaki yerel veritabanında tutulur.
• Bulut yedeklemesi devre dışıdır; verileriniz Google sunucularına yüklenmez.
• Uygulamayı kaldırdığınızda tüm veriler kalıcı olarak silinir.
                """.trimIndent(),
            )
            PolicySection(
                title = "İzin Açıklamaları",
                body  = """
• RECEIVE_SMS / READ_SMS — Spam SMS'leri tespit etmek ve analiz etmek için.
• READ_PHONE_STATE / ANSWER_PHONE_CALLS / READ_CALL_LOG — Gelen aramaları filtrelemek için.
• SYSTEM_ALERT_WINDOW — Şüpheli arama geldiğinde uyarı göstermek için.
• INTERNET — Dolandırıcı numara listesini GitHub'dan güncellemek için.
                """.trimIndent(),
            )
            PolicySection(
                title = "Çocukların Gizliliği",
                body  = "Callshield, 13 yaşın altındaki kişilere yönelik değildir ve bu kişilerden bilerek veri toplamaz.",
            )
            PolicySection(
                title = "İletişim",
                body  = "Gizlilik ile ilgili sorularınız için:\ncallshield.app@gmail.com",
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

