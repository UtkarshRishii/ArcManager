package com.arcmanager.domain.model

import java.time.Instant

data class Client(
    val id: String,
    val userId: String,
    val name: String,
    val companyName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val telegram: String? = null,
    val whatsapp: String? = null,
    val country: String? = null,
    val currency: String = "INR",
    val notes: String? = null,
    val status: String = "active",
    val tags: List<String> = emptyList(),
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    // Calculated fields — populated from payments, not stored
    var totalBilled: java.math.BigDecimal = java.math.BigDecimal.ZERO
    var totalReceived: java.math.BigDecimal = java.math.BigDecimal.ZERO
    var totalPending: java.math.BigDecimal = java.math.BigDecimal.ZERO
    var activeProjects: Int = 0

    val paymentPercentage: Int
        get() = if (totalBilled > java.math.BigDecimal.ZERO) {
            totalReceived.multiply(java.math.BigDecimal(100))
                .divide(totalBilled, 0, java.math.RoundingMode.HALF_UP)
                .toInt()
        } else 0

    val displayStatus: String
        get() = when {
            totalBilled == java.math.BigDecimal.ZERO -> "NO BILLING"
            totalPending == java.math.BigDecimal.ZERO -> "FULLY PAID"
            totalReceived > java.math.BigDecimal.ZERO -> "PARTIALLY PAID"
            else -> "UNPAID"
        }
}
