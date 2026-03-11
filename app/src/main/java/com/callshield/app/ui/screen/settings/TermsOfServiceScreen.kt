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
fun TermsOfServiceScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Text("Kullanım Koşulları", color = OnDarkPrimary, fontWeight = FontWeight.Bold)
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
                body  = "11 Mart 2026 · Versiyon 1.0",
            )
            PolicySection(
                title = "1. Lisans ve Kullanım Hakkı",
                body  = """
Callshield, yalnızca kişisel ve ticari olmayan kullanım için sınırlı bir lisans kapsamında sunulmaktadır.

Şunları yapamazsınız:
• Uygulamayı tersine mühendislik yöntemiyle analiz etme
• Kopyalama, değiştirme veya türev çalışma oluşturma
• Üçüncü kişilere satma, kiralama veya lisanslama
• Yasadışı veya hileli amaçlarla kullanma

Uygulama ve içindeki tüm fikri mülkiyet hakları münhasıran Callshield'e aittir.
                """.trimIndent(),
            )
            PolicySection(
                title = "2. Hizmetin Sınırları ve Garanti Yokluğu",
                body  = """
Callshield bir YARDIMCI ARAÇTIR; kesin güvenlik güvencesi vermez.

Uygulama "olduğu gibi" sunulmaktadır. Geliştirici şunları garanti etmez:
• Tüm dolandırıcı içeriklerin eksiksiz tespiti
• Kesintisiz veya hatasız çalışma
• Belirli cihaz/sürüm uyumluluğu

Yanlış pozitif (meşru içeriğin engellenmesi) veya yanlış negatif (dolandırıcının geçmesi) hâllerinden Geliştirici sorumlu değildir.
                """.trimIndent(),
            )
            PolicySection(
                title = "3. Sorumluluk Sınırlaması",
                body  = """
Geliştirici'nin azami sorumluluğu, son 12 ay içinde ödediğiniz abonelik ücretiyle sınırlıdır.

Ücretsiz kullanıcılar bakımından bu sınır SIFIR Türk Lirası'dır.

Geliştirici şu zararlardan kesinlikle sorumlu değildir:
• Kaçırılan arama veya SMS kaynaklı zararlar
• Tespit edilemeyen dolandırıcılıktan kaynaklanan mali kayıplar
• Üçüncü taraf hizmetlerinden kaynaklanan zararlar
• Dolaylı, arızi veya cezai zararlar
                """.trimIndent(),
            )
            PolicySection(
                title = "4. Kullanıcı Yükümlülükleri",
                body  = """
Uygulamayı kullanarak şunları kabul edersiniz:
• 18 yaşından büyük olduğunuzu
• Yalnızca yasal amaçlarla kullanacağınızı
• Elde ettiğiniz bilgileri başkalarını zarar vermek için kullanmayacağınızı
• Kötüye kullanımdan doğan zararları tazmin edeceğinizi
                """.trimIndent(),
            )
            PolicySection(
                title = "5. Ücretli Hizmetler (Premium)",
                body  = """
• Abonelikler Google Play / App Store üzerinden yönetilir
• İptal, mevcut fatura döneminin sonunda geçerli olur
• Kısmi iade yapılmaz
• Fiyatlar önceden bildirimle değiştirilebilir
• İadeler, ilgili mağazanın politikasına tabidir
                """.trimIndent(),
            )
            PolicySection(
                title = "6. Uygulanacak Hukuk",
                body  = """
Bu Sözleşme Türkiye Cumhuriyeti hukukuna tabidir.

Uyuşmazlıklarda Türkiye Cumhuriyeti Mahkemeleri ve İcra Daireleri yetkilidir.

Tüketici işlemlerinde 6502 sayılı Tüketicinin Korunması Hakkında Kanun kapsamındaki haklar saklıdır.
                """.trimIndent(),
            )
            PolicySection(
                title = "7. Değişiklikler ve Fesih",
                body  = """
• Geliştirici, koşulları istediği zaman değiştirebilir; değişiklikler uygulama içinde duyurulur
• Uygulamayı kullanmaya devam etmeniz güncel koşulları kabul ettiğiniz anlamına gelir
• Kabul etmiyorsanız uygulamayı kaldırınız
• Geliştirici, gerekçe göstermeksizin erişimi sona erdirebilir
                """.trimIndent(),
            )
            PolicySection(
                title = "8. İletişim",
                body  = "Kullanım koşullarına ilişkin sorularınız için:\nlegal@callshield.app",
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}
