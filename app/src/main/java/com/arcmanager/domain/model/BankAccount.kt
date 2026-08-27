package com.arcmanager.domain.model

import java.math.BigDecimal
import java.time.Instant

data class BankAccount(
    val id: String,
    val userId: String,
    val accountName: String,
    val bankName: String,
    val accountHolderName: String? = null,
    val accountLast4: String? = null,
    val currency: String = "INR",
    val isActive: Boolean = true,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    // Calculated from payments
    var trackedReceived: BigDecimal = BigDecimal.ZERO
    var transactionCount: Int = 0

    /**
     * Display format: "HDFC Bank •••• 4821"
     */
    val displayName: String
        get() {
            val masked = if (accountLast4 != null) "•••• $accountLast4" else ""
            return "$bankName $masked".trim()
        }
}
