package com.callshield.app.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.callshield.app.ui.screen.blocklist.BlocklistScreen
import com.callshield.app.ui.screen.calllog.CallLogScreen
import com.callshield.app.ui.screen.history.BlockHistoryScreen
import com.callshield.app.ui.screen.home.HomeScreen
import com.callshield.app.ui.screen.lookup.NumberLookupScreen
import com.callshield.app.ui.screen.onboarding.OnboardingScreen
import com.callshield.app.ui.screen.paywall.PaywallScreen
import com.callshield.app.ui.screen.quarantine.QuarantineScreen
import com.callshield.app.ui.screen.settings.PhishingHistoryScreen
import com.callshield.app.ui.screen.settings.PrivacyPolicyScreen
import com.callshield.app.ui.screen.settings.SettingsScreen
import com.callshield.app.ui.screen.settings.TermsOfServiceScreen
import com.callshield.app.ui.screen.smsinbox.ConversationDetailScreen
import com.callshield.app.ui.screen.smsinbox.ConversationListScreen
import com.callshield.app.ui.screen.stats.StatsScreen
import com.callshield.app.ui.screen.trusted.TrustedNumbersScreen

private const val ROUTE_ONBOARDING       = "onboarding"
private const val ROUTE_PAYWALL          = "paywall"
private const val ROUTE_PRIVACY          = "privacy_policy"
private const val ROUTE_TERMS            = "terms_of_service"
private const val ROUTE_NUMBER_LOOKUP    = "number_lookup"
private const val ROUTE_HISTORY          = "block_history"
private const val ROUTE_TRUSTED          = "trusted_numbers"
private const val ROUTE_PHISHING_HISTORY = "phishing_history"
private const val ROUTE_CALL_LOG         = "call_log"
private const val ROUTE_QUARANTINE       = "quarantine"
private const val ROUTE_CONVERSATION     = "conversation/{threadId}/{address}"

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

private val fullscreenRoutes = setOf(
    ROUTE_ONBOARDING, ROUTE_PAYWALL, ROUTE_PRIVACY, ROUTE_TERMS,
    ROUTE_NUMBER_LOOKUP, ROUTE_HISTORY, ROUTE_TRUSTED, ROUTE_PHISHING_HISTORY,
    ROUTE_CALL_LOG, ROUTE_QUARANTINE, ROUTE_CONVERSATION,
)

@Composable
fun NavGraph(
    viewModel: NavGraphViewModel = hiltViewModel(),
) {
    val onboardingDone    by viewModel.isOnboardingDone.collectAsStateWithLifecycle()
    val navController      = rememberNavController()
    val backStackEntry    by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // null = DataStore henüz okunmadı; içerik gösterme (splash screen devam eder)
    if (onboardingDone == null) return

    val startDestination = if (onboardingDone == true) Screen.Home.route else ROUTE_ONBOARDING
    val showBottomBar    = currentDestination?.route !in fullscreenRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    screens.forEach { screen ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy
                                ?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState    = true
                                }
                            },
                            icon  = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) },
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        NavHost(
            navController    = navController,
            startDestination = startDestination,
            modifier         = Modifier.padding(paddingValues),
        ) {
            composable(ROUTE_ONBOARDING) {
                OnboardingScreen(
                    onComplete = {
                        viewModel.markOnboardingDone()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(ROUTE_ONBOARDING) { inclusive = true }
                        }
                    },
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToLookup  = { navController.navigate(ROUTE_NUMBER_LOOKUP) },
                    onNavigateToCallLog = { navController.navigate(ROUTE_CALL_LOG) },
                )
            }
            composable(Screen.Stats.route) {
                // Fix #18: Stats → Blocklist navigasyonu — false positive'leri kaldırabilmek için.
                StatsScreen(
                    onNavigateToBlocklist = { navController.navigate(Screen.Blocklist.route) }
                )
            }
            composable(Screen.SmsInbox.route) {
                ConversationListScreen(
                    onOpenThread = { threadId, address ->
                        navController.navigate(
                            "conversation/$threadId/${Uri.encode(address)}"
                        )
                    },
                )
            }
            composable(Screen.Blocklist.route) { BlocklistScreen() }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToPaywall         = { navController.navigate(ROUTE_PAYWALL) },
                    onNavigateToPrivacy         = { navController.navigate(ROUTE_PRIVACY) },
                    onNavigateToTerms           = { navController.navigate(ROUTE_TERMS) },
                    onNavigateToHistory         = { navController.navigate(ROUTE_HISTORY) },
                    onNavigateToTrusted         = { navController.navigate(ROUTE_TRUSTED) },
                    onNavigateToPhishingHistory = { navController.navigate(ROUTE_PHISHING_HISTORY) },
                    onNavigateToQuarantine      = { navController.navigate(ROUTE_QUARANTINE) },
                )
            }
            composable(ROUTE_PAYWALL) {
                PaywallScreen(
                    onDismiss    = { navController.popBackStack() },
                    onSelectPlan = { navController.popBackStack() },
                )
            }
            composable(ROUTE_PRIVACY) {
                PrivacyPolicyScreen(onBack = { navController.popBackStack() })
            }
            composable(ROUTE_TERMS) {
                TermsOfServiceScreen(onBack = { navController.popBackStack() })
            }
            composable(ROUTE_NUMBER_LOOKUP) {
                NumberLookupScreen(onBack = { navController.popBackStack() })
            }
            composable(ROUTE_HISTORY) {
                BlockHistoryScreen(onBack = { navController.popBackStack() })
            }
            composable(ROUTE_TRUSTED) {
                TrustedNumbersScreen(onBack = { navController.popBackStack() })
            }
            composable(ROUTE_PHISHING_HISTORY) {
                PhishingHistoryScreen(onBack = { navController.popBackStack() })
            }
            composable(ROUTE_CALL_LOG) {
                CallLogScreen(onBack = { navController.popBackStack() })
            }
            composable(ROUTE_QUARANTINE) {
                QuarantineScreen(onBack = { navController.popBackStack() })
            }
            composable(ROUTE_CONVERSATION) { backStack ->
                val threadId = backStack.arguments?.getString("threadId")?.toLongOrNull() ?: return@composable
                val address  = Uri.decode(backStack.arguments?.getString("address").orEmpty())
                ConversationDetailScreen(
                    threadId = threadId,
                    address  = address,
                    onBack   = { navController.popBackStack() },
                )
            }
        }
    }
}
