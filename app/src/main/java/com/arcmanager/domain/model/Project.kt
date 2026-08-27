package com.arcmanager.domain.model

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

data class Project(
    val id: String,
    val userId: String,
    val clientId: String,
    val name: String,
    val description: String? = null,
    val totalAmount: BigDecimal,
    val currency: String = "INR",
    val paymentModel: PaymentModel,
    val status: String = "active",
    val startDate: LocalDate? = null,
    val expectedCompletionDate: LocalDate? = null,
    val completedAt: Instant? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    // Calculated from payments
    var receivedAmount: BigDecimal = BigDecimal.ZERO
    var clientName: String = ""

    val pendingAmount: BigDecimal
        get() = totalAmount.subtract(receivedAmount).coerceAtLeast(BigDecimal.ZERO)

    val completionPercentage: Int
        get() = if (totalAmount > BigDecimal.ZERO) {
            receivedAmount.multiply(BigDecimal(100))
                .divide(totalAmount, 0, java.math.RoundingMode.HALF_UP)
                .toInt()
                .coerceAtMost(100)
        } else 0

    val displayStatus: String
        get() = when {
            status == "completed" -> "COMPLETED"
            status == "cancelled" -> "CANCELLED"
            status == "on_hold" -> "ON HOLD"
            pendingAmount == BigDecimal.ZERO && totalAmount > BigDecimal.ZERO -> "FULLY PAID"
            receivedAmount > BigDecimal.ZERO -> "PARTIALLY PAID"
            else -> "UNPAID"
        }
}

enum class PaymentModel(val value: String, val displayName: String) {
    ONE_TIME("ONE_TIME", "One-Time"),
    ADVANCE_FINAL("ADVANCE_FINAL", "Advance + Final"),
    INSTALLMENTS("INSTALLMENTS", "Installments"),
    MONTHLY_RECURRING("MONTHLY_RECURRING", "Monthly Recurring"),
    CUSTOM("CUSTOM", "Custom Schedule");

    companion object {
        fun fromValue(value: String): PaymentModel {
            return entries.firstOrNull { it.value == value } ?: CUSTOM
        }
    }
}
