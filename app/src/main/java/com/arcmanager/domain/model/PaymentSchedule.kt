package com.arcmanager.domain.model

import com.arcmanager.core.util.DateUtils
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

data class PaymentSchedule(
    val id: String,
    val userId: String,
    val projectId: String,
    val clientId: String,
    val title: String? = null,
    val amount: BigDecimal,
    val currency: String = "INR",
    val dueDate: LocalDate,
    val paymentType: String? = null,
    val status: String = "pending",
    val recurrenceType: String? = null,
    val recurrenceInterval: Int? = null,
    val parentScheduleId: String? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null,
) {
    // Calculated from payments
    var paidAmount: BigDecimal = BigDecimal.ZERO
    var clientName: String = ""
    var projectName: String = ""

    val remainingAmount: BigDecimal
        get() = amount.subtract(paidAmount).coerceAtLeast(BigDecimal.ZERO)

    val isOverdue: Boolean
        get() = DateUtils.isOverdue(dueDate) && remainingAmount > BigDecimal.ZERO

    val daysOverdue: Long
        get() = if (isOverdue) -DateUtils.daysUntil(dueDate) else 0

    val effectiveStatus: String
        get() = when {
            remainingAmount <= BigDecimal.ZERO -> "paid"
            isOverdue -> "overdue"
            paidAmount > BigDecimal.ZERO -> "partially_paid"
            status == "cancelled" -> "cancelled"
            else -> "pending"
        }
}
