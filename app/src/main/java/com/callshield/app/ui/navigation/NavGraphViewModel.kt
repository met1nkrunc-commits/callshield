package com.callshield.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callshield.app.core.billing.OnboardingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NavGraphViewModel @Inject constructor(
    private val onboardingManager: OnboardingManager,
) : ViewModel() {

    // null = DataStore henüz yüklenmedi (splash screen bekletilir)
    val isOnboardingDone: StateFlow<Boolean?> = onboardingManager.isOnboardingDone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun markOnboardingDone() {
        viewModelScope.launch { onboardingManager.markDone() }
    }
}
