package com.callshield.app.core.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.callshield.app.core.util.BillingConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val planManager: PlanManager,
) : PurchasesUpdatedListener {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _products = MutableStateFlow<List<ProductDetails>>(emptyList())
    val products: StateFlow<List<ProductDetails>> = _products

    private val _billingState = MutableStateFlow<BillingState>(BillingState.Idle)
    val billingState: StateFlow<BillingState> = _billingState

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    init {
        connect()
    }

    private var reconnectAttempts = 0

    private fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                reconnectAttempts = 0
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch {
                        queryProducts()
                        restorePurchases()
                    }
                }
            }
            // Fix #4: Google Play Billing bağlantıyı kestiğinde yeniden bağlan.
            // "OS will retry automatically" YANLIŞ — geliştirici yeniden çağırmalı.
            override fun onBillingServiceDisconnected() {
                if (reconnectAttempts < 3) {
                    reconnectAttempts++
                    scope.launch {
                        kotlinx.coroutines.delay(reconnectAttempts * 2_000L)
                        connect()
                    }
                }
            }
        })
    }

    private suspend fun queryProducts() {
        val productList = listOf(
            BillingConstants.PRODUCT_STANDARD_MONTHLY,
            BillingConstants.PRODUCT_STANDARD_YEARLY,
            BillingConstants.PRODUCT_FAMILY_MONTHLY,
            BillingConstants.PRODUCT_FAMILY_YEARLY,
        ).map { id ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(id)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        // Use callback wrapped in coroutine — avoids overload ambiguity with billing-ktx 7
        val result = suspendCancellableCoroutine { cont ->
            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                cont.resume(Pair(billingResult, productDetailsList))
            }
        }
        if (result.first.responseCode == BillingClient.BillingResponseCode.OK) {
            _products.value = result.second
        }
    }

    suspend fun restorePurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        // Wrap callback in suspend — avoids overload ambiguity with billing-ktx 7
        val purchases = suspendCancellableCoroutine<List<Purchase>> { cont ->
            billingClient.queryPurchasesAsync(
                params,
                PurchasesResponseListener { _, purchaseList -> cont.resume(purchaseList) }
            )
        }

        val activePurchase = purchases
            .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED }
            .maxByOrNull { it.purchaseTime }

        if (activePurchase != null) {
            handlePurchase(activePurchase)
        } else {
            planManager.setFree()
        }
    }

    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
        val offerToken = productDetails.subscriptionOfferDetails
            ?.firstOrNull()?.offerToken ?: return

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)
            .setOfferToken(offerToken)
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        _billingState.value = BillingState.Launching
        billingClient.launchBillingFlow(activity, flowParams)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK ->
                purchases?.forEach { purchase ->
                    scope.launch { handlePurchase(purchase) }
                }
            BillingClient.BillingResponseCode.USER_CANCELED ->
                _billingState.value = BillingState.Cancelled
            else ->
                _billingState.value = BillingState.Error(result.debugMessage)
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return

        if (!purchase.isAcknowledged) {
            val ackParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            // Callback-based to avoid overload ambiguity
            suspendCancellableCoroutine<Unit> { cont ->
                billingClient.acknowledgePurchase(ackParams) { cont.resume(Unit) }
            }
        }

        val plan = when {
            purchase.products.any { it.contains("family") }   -> BillingConstants.PLAN_FAMILY
            purchase.products.any { it.contains("standard") } -> BillingConstants.PLAN_STANDARD
            else -> BillingConstants.PLAN_FREE
        }

        planManager.setPlan(plan)
        _billingState.value = BillingState.Success(plan)
    }
}

sealed class BillingState {
    object Idle      : BillingState()
    object Launching : BillingState()
    data class Success(val plan: String) : BillingState()
    object Cancelled : BillingState()
    data class Error(val message: String) : BillingState()
}
