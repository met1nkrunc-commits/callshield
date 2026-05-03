package com.callshield.app.ui.screen.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.callshield.app.core.billing.BillingManager
import com.callshield.app.core.billing.BillingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billingManager: BillingManager,
) : ViewModel() {

    val products = billingManager.products
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val billingState = billingManager.billingState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BillingState.Idle)

    fun purchase(activity: Activity, productId: String) {
        val product = products.value.find { it.productId == productId } ?: return
        billingManager.launchBillingFlow(activity, product)
    }

    fun restore() {
        viewModelScope.launch { billingManager.restorePurchases() }
    }
}
