package com.arcmanager.data.repository

import com.arcmanager.core.di.IoDispatcher
import com.arcmanager.core.util.Constants
import com.arcmanager.core.util.Result
import com.arcmanager.data.mapper.toDomain
import com.arcmanager.data.mapper.toDto
import com.arcmanager.data.remote.dto.PaymentDto
import com.arcmanager.domain.model.Payment
import com.arcmanager.domain.repository.PaymentRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : PaymentRepository {

    private val userId: String
        get() = supabase.auth.currentUserOrNull()?.id ?: throw IllegalStateException("Not authenticated")

    override fun getAllPayments(): Flow<Result<List<Payment>>> = flow {
        emit(Result.Loading)
        try {
            val response = supabase.postgrest[Constants.TABLE_PAYMENTS]
                .select {
                    filter { eq("user_id", userId) }
                    order("payment_date", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
            val payments = response.decodeList<PaymentDto>().map { it.toDomain() }
            emit(Result.success(payments))
        } catch (e: Exception) {
            emit(Result.error("Failed to load payments: ${e.message}", e))
        }
    }.flowOn(dispatcher)

    override fun getPaymentsByClient(clientId: String): Flow<Result<List<Payment>>> = flow {
        emit(Result.Loading)
        try {
            val response = supabase.postgrest[Constants.TABLE_PAYMENTS]
                .select {
                    filter { eq("user_id", userId); eq("client_id", clientId) }
                    order("payment_date", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
            val payments = response.decodeList<PaymentDto>().map { it.toDomain() }
            emit(Result.success(payments))
        } catch (e: Exception) {
            emit(Result.error("Failed to load payments: ${e.message}", e))
        }
    }.flowOn(dispatcher)

    override fun getPaymentsByProject(projectId: String): Flow<Result<List<Payment>>> = flow {
        emit(Result.Loading)
        try {
            val response = supabase.postgrest[Constants.TABLE_PAYMENTS]
                .select {
                    filter { eq("user_id", userId); eq("project_id", projectId) }
                    order("payment_date", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
            val payments = response.decodeList<PaymentDto>().map { it.toDomain() }
            emit(Result.success(payments))
        } catch (e: Exception) {
            emit(Result.error("Failed to load payments: ${e.message}", e))
        }
    }.flowOn(dispatcher)

    override fun getRecentPayments(limit: Int): Flow<Result<List<Payment>>> = flow {
        emit(Result.Loading)
        try {
            val response = supabase.postgrest[Constants.TABLE_PAYMENTS]
                .select {
                    filter { eq("user_id", userId); eq("status", "received") }
                    order("payment_date", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(limit.toLong())
                }
            val payments = response.decodeList<PaymentDto>().map { it.toDomain() }
            emit(Result.success(payments))
        } catch (e: Exception) {
            emit(Result.error("Failed to load recent payments: ${e.message}", e))
        }
    }.flowOn(dispatcher)

    override suspend fun getPaymentById(paymentId: String): Result<Payment> {
        return try {
            val dto = supabase.postgrest[Constants.TABLE_PAYMENTS]
                .select { filter { eq("id", paymentId); eq("user_id", userId) } }
                .decodeSingle<PaymentDto>()
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.error("Failed to load payment: ${e.message}", e)
        }
    }

    override suspend fun recordPayment(payment: Payment): Result<Payment> {
        return try {
            val dto = payment.copy(userId = userId).toDto()
            val response = supabase.postgrest[Constants.TABLE_PAYMENTS]
                .insert(dto) { select() }
                .decodeSingle<PaymentDto>()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.error("Failed to record payment: ${e.message}", e)
        }
    }

    override suspend fun updatePayment(payment: Payment): Result<Payment> {
        return try {
            val dto = payment.toDto()
            supabase.postgrest[Constants.TABLE_PAYMENTS]
                .update(dto) { filter { eq("id", payment.id); eq("user_id", userId) } }
            Result.success(payment)
        } catch (e: Exception) {
            Result.error("Failed to update payment: ${e.message}", e)
        }
    }

    override suspend fun deletePayment(paymentId: String): Result<Unit> {
        return try {
            supabase.postgrest[Constants.TABLE_PAYMENTS]
                .delete { filter { eq("id", paymentId); eq("user_id", userId) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Failed to delete payment: ${e.message}", e)
        }
    }

    // ── Financial Calculations (always from records) ──

    override suspend fun getTotalReceived(): Result<BigDecimal> {
        return sumPayments { eq("user_id", userId); eq("status", "received") }
    }

    override suspend fun getTotalReceivedByClient(clientId: String): Result<BigDecimal> {
        return sumPayments { eq("user_id", userId); eq("client_id", clientId); eq("status", "received") }
    }

    override suspend fun getTotalReceivedByProject(projectId: String): Result<BigDecimal> {
        return sumPayments { eq("user_id", userId); eq("project_id", projectId); eq("status", "received") }
    }

    override suspend fun getTotalReceivedByBankAccount(bankAccountId: String): Result<BigDecimal> {
        return sumPayments { eq("user_id", userId); eq("bank_account_id", bankAccountId); eq("status", "received") }
    }

    override suspend fun getTotalReceivedThisMonth(): Result<BigDecimal> {
        return try {
            val now = LocalDate.now(ZoneId.of("Asia/Kolkata"))
            val startOfMonth = now.withDayOfMonth(1).atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant()
            val response = supabase.postgrest[Constants.TABLE_PAYMENTS]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("status", "received")
                        gte("payment_date", startOfMonth.toString())
                    }
                }
            val total = response.decodeList<PaymentDto>()
                .sumOf { BigDecimal.valueOf(it.amount) }
            Result.success(total)
        } catch (e: Exception) {
            Result.error("Failed to calculate: ${e.message}", e)
        }
    }

    override suspend fun getPaymentCountByBankAccount(bankAccountId: String): Result<Int> {
        return try {
            val response = supabase.postgrest[Constants.TABLE_PAYMENTS]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("bank_account_id", bankAccountId)
                        eq("status", "received")
                    }
                }
            Result.success(response.decodeList<PaymentDto>().size)
        } catch (e: Exception) {
            Result.error("Failed to count: ${e.message}", e)
        }
    }

    private suspend fun sumPayments(
        filterBlock: io.github.jan.supabase.postgrest.query.filter.PostgrestFilterBuilder.() -> Unit
    ): Result<BigDecimal> {
        return try {
            val response = supabase.postgrest[Constants.TABLE_PAYMENTS]
                .select { filter(filterBlock) }
            val total = response.decodeList<PaymentDto>()
                .sumOf { BigDecimal.valueOf(it.amount) }
            Result.success(total)
        } catch (e: Exception) {
            Result.error("Failed to calculate: ${e.message}", e)
        }
    }
}
