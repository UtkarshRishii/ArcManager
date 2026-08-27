package com.arcmanager.domain.model

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

data class Payment(
    val id: String,
    val userId: String,
    val clientId: String,
    val projectId: String? = null,
    val scheduleId: String? = null,
    val amount: BigDecimal,
    val currency: String = "INR",
    val paymentDate: Instant,
    val paymentType: PaymentType = PaymentType.OTHER,
    val paymentMethod: PaymentMethod = PaymentMethod.OTHER,
    val bankAccountId: String? = null,
    val transactionReference: String? = null,
    val notes: String? = null,
    val status: String = "received",
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    // Populated from joins
    var clientName: String = ""
    var projectName: String? = null
    var bankAccountDisplay: String? = null
}

enum class PaymentType(val value: String, val displayName: String) {
    ADVANCE("ADVANCE", "Advance"),
    MILESTONE("MILESTONE", "Milestone"),
    INSTALLMENT("INSTALLMENT", "Installment"),
    FINAL("FINAL", "Final"),
    MONTHLY("MONTHLY", "Monthly"),
    OTHER("OTHER", "Other");

    companion object {
        fun fromValue(value: String?): PaymentType {
            return entries.firstOrNull { it.value == value } ?: OTHER
        }
    }
}

enum class PaymentMethod(val value: String, val displayName: String) {
    BANK_TRANSFER("BANK_TRANSFER", "Bank Transfer"),
    UPI("UPI", "UPI"),
    CASH("CASH", "Cash"),
    CARD("CARD", "Card"),
    PAYPAL("PAYPAL", "PayPal"),
    WISE("WISE", "Wise"),
    SWIFT("SWIFT", "SWIFT"),
    OTHER("OTHER", "Other");

    companion object {
        fun fromValue(value: String?): PaymentMethod {
            return entries.firstOrNull { it.value == value } ?: OTHER
        }
    }
}
