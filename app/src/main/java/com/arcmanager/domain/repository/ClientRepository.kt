package com.arcmanager.domain.repository

import com.arcmanager.core.util.Result
import com.arcmanager.domain.model.Client
import kotlinx.coroutines.flow.Flow

interface ClientRepository {
    fun getClients(status: String? = null): Flow<Result<List<Client>>>
    suspend fun getClientById(clientId: String): Result<Client>
    suspend fun addClient(client: Client): Result<Client>
    suspend fun updateClient(client: Client): Result<Client>
    suspend fun deleteClient(clientId: String): Result<Unit>
    suspend fun archiveClient(clientId: String): Result<Unit>
    fun searchClients(query: String): Flow<Result<List<Client>>>
}
