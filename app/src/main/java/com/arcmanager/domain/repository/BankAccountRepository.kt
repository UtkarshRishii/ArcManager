package com.arcmanager.domain.repository

import com.arcmanager.core.util.Result
import com.arcmanager.domain.model.BankAccount
import kotlinx.coroutines.flow.Flow

interface BankAccountRepository {
    fun getBankAccounts(): Flow<Result<List<BankAccount>>>
    suspend fun getBankAccountById(accountId: String): Result<BankAccount>
    suspend fun addBankAccount(
        accountName: String,
        bankName: String,
        accountHolderName: String?,
        accountNumber: String?,
        ifsc: String?,
        currency: String,
    ): Result<BankAccount>
    suspend fun updateBankAccount(account: BankAccount): Result<BankAccount>
    suspend fun deleteBankAccount(accountId: String): Result<Unit>
}
