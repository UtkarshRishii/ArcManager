package com.arcmanager.data.repository

import com.arcmanager.core.di.IoDispatcher
import com.arcmanager.core.util.Constants
import com.arcmanager.core.util.Result
import com.arcmanager.data.mapper.toDomain
import com.arcmanager.data.mapper.toDto
import com.arcmanager.data.remote.dto.ClientDto
import com.arcmanager.domain.model.Client
import com.arcmanager.domain.repository.ClientRepository
import com.arcmanager.domain.repository.PaymentRepository
import com.arcmanager.domain.repository.ProjectRepository
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
class ClientRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : ClientRepository {

    private val userId: String
        get() = supabase.auth.currentUserOrNull()?.id ?: throw IllegalStateException("Not authenticated")

    override fun getClients(status: String?): Flow<Result<List<Client>>> = flow {
        emit(Result.Loading)
        try {
            val response = supabase.postgrest[Constants.TABLE_CLIENTS]
                .select {
                    filter {
                        eq("user_id", userId)
                        if (status != null) eq("status", status)
                    }
                    order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
            val clients = response.decodeList<ClientDto>().map { it.toDomain() }
            emit(Result.success(clients))
        } catch (e: Exception) {
            emit(Result.error("Failed to load clients: ${e.message}", e))
        }
    }.flowOn(dispatcher)

    override suspend fun getClientById(clientId: String): Result<Client> {
        return try {
            val dto = supabase.postgrest[Constants.TABLE_CLIENTS]
                .select { filter { eq("id", clientId); eq("user_id", userId) } }
                .decodeSingle<ClientDto>()
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.error("Failed to load client: ${e.message}", e)
        }
    }

    override suspend fun addClient(client: Client): Result<Client> {
        return try {
            val dto = client.copy(userId = userId).toDto()
            val response = supabase.postgrest[Constants.TABLE_CLIENTS]
                .insert(dto) { select() }
                .decodeSingle<ClientDto>()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.error("Failed to add client: ${e.message}", e)
        }
    }

    override suspend fun updateClient(client: Client): Result<Client> {
        return try {
            val dto = client.toDto()
            supabase.postgrest[Constants.TABLE_CLIENTS]
                .update(dto) { filter { eq("id", client.id); eq("user_id", userId) } }
            Result.success(client)
        } catch (e: Exception) {
            Result.error("Failed to update client: ${e.message}", e)
        }
    }

    override suspend fun deleteClient(clientId: String): Result<Unit> {
        return try {
            supabase.postgrest[Constants.TABLE_CLIENTS]
                .delete { filter { eq("id", clientId); eq("user_id", userId) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Failed to delete client: ${e.message}", e)
        }
    }

    override suspend fun archiveClient(clientId: String): Result<Unit> {
        return try {
            supabase.postgrest[Constants.TABLE_CLIENTS]
                .update(mapOf("status" to "archived")) {
                    filter { eq("id", clientId); eq("user_id", userId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Failed to archive client: ${e.message}", e)
        }
    }

    override fun searchClients(query: String): Flow<Result<List<Client>>> = flow {
        emit(Result.Loading)
        try {
            val response = supabase.postgrest[Constants.TABLE_CLIENTS]
                .select {
                    filter {
                        eq("user_id", userId)
                        or {
                            ilike("name", "%$query%")
                            ilike("company_name", "%$query%")
                            ilike("email", "%$query%")
                            ilike("phone", "%$query%")
                        }
                    }
                }
            val clients = response.decodeList<ClientDto>().map { it.toDomain() }
            emit(Result.success(clients))
        } catch (e: Exception) {
            emit(Result.error("Search failed: ${e.message}", e))
        }
    }.flowOn(dispatcher)
}
