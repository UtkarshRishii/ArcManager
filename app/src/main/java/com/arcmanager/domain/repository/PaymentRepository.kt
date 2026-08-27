package com.arcmanager.domain.repository

import com.arcmanager.core.util.Result
import com.arcmanager.domain.model.Payment
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

interface PaymentRepository {
    fun getAllPayments(): Flow<Result<List<Payment>>>
    fun getPaymentsByClient(clientId: String): Flow<Result<List<Payment>>>
    fun getPaymentsByProject(projectId: String): Flow<Result<List<Payment>>>
    fun getRecentPayments(limit: Int = 10): Flow<Result<List<Payment>>>
    suspend fun getPaymentById(paymentId: String): Result<Payment>
    suspend fun recordPayment(payment: Payment): Result<Payment>
    suspend fun updatePayment(payment: Payment): Result<Payment>
    suspend fun togglePaymentStatus(paymentId: String, newStatus: String): Result<Payment>
    suspend fun deletePayment(paymentId: String): Result<Unit>

    // Financial calculations — always derived from actual payment records
    suspend fun getTotalReceived(): Result<BigDecimal>
    suspend fun getTotalReceivedByClient(clientId: String): Result<BigDecimal>
    suspend fun getTotalReceivedByProject(projectId: String): Result<BigDecimal>
    suspend fun getTotalReceivedByBankAccount(bankAccountId: String): Result<BigDecimal>
    suspend fun getTotalReceivedThisMonth(): Result<BigDecimal>
    suspend fun getPaymentCountByBankAccount(bankAccountId: String): Result<Int>
}
