package com.arcmanager.data.repository

import com.arcmanager.core.di.IoDispatcher
import com.arcmanager.core.security.EncryptionHelper
import com.arcmanager.core.util.Constants
import com.arcmanager.core.util.Result
import com.arcmanager.data.mapper.toDomain
import com.arcmanager.data.remote.dto.BankAccountDto
import com.arcmanager.domain.model.BankAccount
import com.arcmanager.domain.repository.BankAccountRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BankAccountRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    private val encryptionHelper: EncryptionHelper,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : BankAccountRepository {

    private val userId: String
        get() = supabase.auth.currentUserOrNull()?.id ?: throw IllegalStateException("Not authenticated")

    override fun getBankAccounts(): Flow<Result<List<BankAccount>>> = flow {
        emit(Result.Loading)
        try {
            val response = supabase.postgrest[Constants.TABLE_BANK_ACCOUNTS]
                .select {
                    filter { eq("user_id", userId); eq("is_active", true) }
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
            val accounts = response.decodeList<BankAccountDto>().map { it.toDomain() }
            emit(Result.success(accounts))
        } catch (e: Exception) {
            emit(Result.error("Failed to load bank accounts: ${e.message}", e))
        }
    }.flowOn(dispatcher)

    override suspend fun getBankAccountById(accountId: String): Result<BankAccount> {
        return try {
            val dto = supabase.postgrest[Constants.TABLE_BANK_ACCOUNTS]
                .select { filter { eq("id", accountId); eq("user_id", userId) } }
                .decodeSingle<BankAccountDto>()
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.error("Failed to load bank account: ${e.message}", e)
        }
    }

    override suspend fun addBankAccount(
        accountName: String,
        bankName: String,
        accountHolderName: String?,
        accountNumber: String?,
        ifsc: String?,
        currency: String,
    ): Result<BankAccount> {
        return try {
            // Encrypt sensitive data before storing
            val encryptedAccountNumber = accountNumber?.let { encryptionHelper.encrypt(it) }
            val accountLast4 = accountNumber?.let { encryptionHelper.extractLast4(it) }
            val encryptedIfsc = ifsc?.let { encryptionHelper.encrypt(it) }

            val dto = BankAccountDto(
                userId = userId,
                accountName = accountName,
                bankName = bankName,
                accountHolderName = accountHolderName,
                encryptedAccountNumber = encryptedAccountNumber,
                accountLast4 = accountLast4,
                encryptedIfsc = encryptedIfsc,
                currency = currency,
            )
            val response = supabase.postgrest[Constants.TABLE_BANK_ACCOUNTS]
                .insert(dto) { select() }
                .decodeSingle<BankAccountDto>()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.error("Failed to add bank account: ${e.message}", e)
        }
    }

    override suspend fun updateBankAccount(account: BankAccount): Result<BankAccount> {
        return try {
            val dto = BankAccountDto(
                id = account.id,
                accountName = account.accountName,
                bankName = account.bankName,
                accountHolderName = account.accountHolderName,
                currency = account.currency,
            )
            supabase.postgrest[Constants.TABLE_BANK_ACCOUNTS]
                .update(dto) { filter { eq("id", account.id); eq("user_id", userId) } }
            Result.success(account)
        } catch (e: Exception) {
            Result.error("Failed to update bank account: ${e.message}", e)
        }
    }

    override suspend fun deleteBankAccount(accountId: String): Result<Unit> {
        return try {
            // Soft delete by marking inactive
            supabase.postgrest[Constants.TABLE_BANK_ACCOUNTS]
                .update(mapOf("is_active" to false)) {
                    filter { eq("id", accountId); eq("user_id", userId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Failed to delete bank account: ${e.message}", e)
        }
    }
}
