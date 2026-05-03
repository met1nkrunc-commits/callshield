package com.callshield.app.core.util

object BillingConstants {
    // Subscription product IDs — must match Play Console exactly
    const val PRODUCT_STANDARD_MONTHLY = "bengel_standard_monthly"
    const val PRODUCT_STANDARD_YEARLY  = "bengel_standard_yearly"
    const val PRODUCT_FAMILY_MONTHLY   = "bengel_family_monthly"
    const val PRODUCT_FAMILY_YEARLY    = "bengel_family_yearly"

    // Entitlement levels
    const val PLAN_FREE     = "free"
    const val PLAN_STANDARD = "standard"
    const val PLAN_FAMILY   = "family"

    // Feature gates
    val STANDARD_FEATURES = setOf(
        "ipqs_lookup",
        "unlimited_blocklist",
        "priority_updates",
        "phishtank_protection",
    )
    val FAMILY_FEATURES = STANDARD_FEATURES + setOf(
        "multi_device",     // up to 5 devices
        "family_dashboard",
    )
}
