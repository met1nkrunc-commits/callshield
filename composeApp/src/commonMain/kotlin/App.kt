import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.bengel.shared.domain.model.BlockEvent
import ui.screen.home.HomeScreen
import ui.screen.stats.StatsScreen
import ui.screen.stats.StatsUiState
import ui.screen.smsinbox.ConversationListScreen
import ui.screen.smsinbox.ConversationDetailScreen
import com.bengel.shared.domain.model.ConversationThread
import com.bengel.shared.domain.model.SmsMessage
import ui.screen.home.HomeUiState
import ui.theme.CallShieldTheme

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Home      : Screen("home",      "Ana Sayfa",  Icons.Default.Home)
    data object Stats     : Screen("stats",     "İstatistik", Icons.Default.BarChart)
    data object SmsInbox  : Screen("sms_inbox", "Gelen Kutusu", Icons.Default.Message)
    data object Blocklist : Screen("blocklist", "Engelliler", Icons.Default.Close)
    data object Settings  : Screen("settings",  "Ayarlar",    Icons.Default.Settings)
}

private val screens = listOf(
    Screen.Home, Screen.Stats, Screen.SmsInbox, Screen.Blocklist, Screen.Settings,
)

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var isProtectionEnabled by remember { mutableStateOf(true) }
    var selectedThreadId by remember { mutableStateOf<Long?>(null) }
    var selectedAddress by remember { mutableStateOf("") }

    CallShieldTheme {
        if (selectedThreadId != null) {
            ConversationDetailScreen(
                address = selectedAddress,
                messages = listOf(
                    SmsMessage(1, selectedThreadId!!, selectedAddress, "Selam, nasılsın?", 1714316400000, 1, true),
                    SmsMessage(2, selectedThreadId!!, selectedAddress, "İyiyim, sen?", 1714316500000, 2, true),
                    SmsMessage(3, selectedThreadId!!, selectedAddress, "Ben de iyiyim. Akşama müsait misin?", 1714316600000, 1, true),
                ),
                onBack = { selectedThreadId = null }
            )
        } else {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        screens.forEach { screen ->
                            NavigationBarItem(
                                selected = currentScreen == screen,
                                onClick = { currentScreen = screen },
                                icon  = { Icon(screen.icon, contentDescription = screen.label) },
                                label = { Text(screen.label) },
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    when (currentScreen) {
                        Screen.Home -> HomeScreen(
                            uiState = HomeUiState(
                                isProtectionEnabled = isProtectionEnabled,
                                totalBlockedCount = 124,
                                weeklyBlockedCount = 12,
                                recentEvents = listOf(
                                    BlockEvent(
                                        timestamp = 1714323600000,
                                        type = "SMS",
                                        riskLevel = com.bengel.shared.domain.model.RiskLevel.BLOCKED,
                                        category = "BETTING",
                                        senderHash = "BAHIS-123"
                                    ),
                                    BlockEvent(
                                        timestamp = 1714320000000,
                                        type = "SMS",
                                        riskLevel = com.bengel.shared.domain.model.RiskLevel.HIGH,
                                        category = "PHISHING",
                                        senderHash = "E-DEVLET"
                                    ),
                                    BlockEvent(
                                        timestamp = 1714316400000,
                                        type = "CALL",
                                        riskLevel = com.bengel.shared.domain.model.RiskLevel.MEDIUM,
                                        category = "SPAM",
                                        senderHash = "0850..."
                                    )
                                )
                            ),
                            onToggleProtection = { isProtectionEnabled = !isProtectionEnabled }
                        )
                        Screen.Stats -> StatsScreen(
                            uiState = StatsUiState(
                                totalBlocked = 124,
                                weeklyBlocked = 12,
                                dailyBlocked = 3,
                                totalSms = 89,
                                totalCalls = 35,
                                categoryStats = mapOf(
                                    "BETTING" to 45,
                                    "PHISHING" to 32,
                                    "LEGAL" to 12,
                                    "SOCIAL" to 5
                                ),
                                riskStats = mapOf(
                                    com.bengel.shared.domain.model.RiskLevel.BLOCKED to 124,
                                    com.bengel.shared.domain.model.RiskLevel.HIGH to 45,
                                    com.bengel.shared.domain.model.RiskLevel.MEDIUM to 12
                                ),
                                dailyData = listOf(
                                    "Pzt" to 5, "Sal" to 8, "Çar" to 3, "Per" to 12, "Cum" to 7, "Cmt" to 4, "Paz" to 2
                                )
                            ),
                            onNavigateToBlocklist = { currentScreen = Screen.Blocklist }
                        )
                        Screen.SmsInbox -> ConversationListScreen(
                            threads = listOf(
                                ConversationThread(1, "B002-BAHIS", "Tebrikler! 500 TL bonus kazandiniz. Tikla...", 1714323600000, 1, com.bengel.shared.domain.model.RiskLevel.BLOCKED),
                                ConversationThread(2, "E-DEVLET", "Adiniza tanimlanan yardim odemesini sorgulamak icin...", 1714320000000, 0, com.bengel.shared.domain.model.RiskLevel.HIGH),
                                ConversationThread(3, "0532 123 45 67", "Selam, aksam musait misin?", 1714316400000, 0, com.bengel.shared.domain.model.RiskLevel.SAFE),
                                ConversationThread(4, "KREDI-INFO", "Size ozel %0.99 faizli kredi firsatini kacirmayin!", 1714312800000, 5, com.bengel.shared.domain.model.RiskLevel.MEDIUM)
                            ),
                            isLoading = false,
                            onOpenThread = { id, addr -> 
                                selectedThreadId = id
                                selectedAddress = addr
                            },
                            onRefresh = {}
                        )
                        else -> {
                            // Placeholder for other screens
                            Text(
                                text = "${currentScreen.label} Ekranı Yakında Gelecek",
                                modifier = Modifier.padding(32.dp),
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
