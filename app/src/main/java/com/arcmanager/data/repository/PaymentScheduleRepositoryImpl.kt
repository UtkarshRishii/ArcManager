package com.arcmanager.data.repository

import com.arcmanager.core.di.IoDispatcher
import com.arcmanager.core.util.Constants
import com.arcmanager.core.util.Result
import com.arcmanager.data.mapper.toDomain
import com.arcmanager.data.mapper.toDto
import com.arcmanager.data.remote.dto.PaymentScheduleDto
import com.arcmanager.domain.model.PaymentSchedule
import com.arcmanager.domain.repository.PaymentScheduleRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentScheduleRepositoryImpl @Inject constructor(
    private val supabase: SupabaseClient,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
) : PaymentScheduleRepository {

    private val userId: String
        get() = supabase.auth.currentUserOrNull()?.id ?: throw IllegalStateException("Not authenticated")

    override fun getSchedulesByProject(projectId: String): Flow<Result<List<PaymentSchedule>>> = flow {
        emit(Result.Loading)
        try {
            val response = supabase.postgrest[Constants.TABLE_PAYMENT_SCHEDULES]
                .select {
                    filter { eq("user_id", userId); eq("project_id", projectId) }
                    order("due_date", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                }
            val schedules = response.decodeList<PaymentScheduleDto>().map { it.toDomain() }
            emit(Result.success(schedules))
        } catch (e: Exception) {
            emit(Result.error("Failed to load schedules: ${e.message}", e))
        }
    }.flowOn(dispatcher)

    override fun getSchedulesByClient(clientId: String): Flow<Result<List<PaymentSchedule>>> = flow {
        emit(Result.Loading)
        try {
            val response = supabase.postgrest[Constants.TABLE_PAYMENT_SCHEDULES]
                .select {
                    filter { eq("user_id", userId); eq("client_id", clientId) }
                    order("due_date", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                }
            val schedules = response.decodeList<PaymentScheduleDto>().map { it.toDomain() }
            emit(Result.success(schedules))
        } catch (e: Exception) {
            emit(Result.error("Failed to load schedules: ${e.message}", e))
        }
    }.flowOn(dispatcher)

    override fun getUpcomingSchedules(limit: Int): Flow<Result<List<PaymentSchedule>>> = flow {
        emit(Result.Loading)
        try {
            val today = LocalDate.now().toString()
            val response = supabase.postgrest[Constants.TABLE_PAYMENT_SCHEDULES]
                .select {
                    filter {
                        eq("user_id", userId)
                        gte("due_date", today)
                        neq("status", "paid")
                        neq("status", "cancelled")
                    }
                    order("due_date", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                    limit(limit.toLong())
                }
            val schedules = response.decodeList<PaymentScheduleDto>().map { it.toDomain() }
            emit(Result.success(schedules))
        } catch (e: Exception) {
            emit(Result.error("Failed to load upcoming schedules: ${e.message}", e))
        }
    }.flowOn(dispatcher)

    override fun getOverdueSchedules(): Flow<Result<List<PaymentSchedule>>> = flow {
        emit(Result.Loading)
        try {
            val today = LocalDate.now().toString()
            val response = supabase.postgrest[Constants.TABLE_PAYMENT_SCHEDULES]
                .select {
                    filter {
                        eq("user_id", userId)
                        lt("due_date", today)
                        neq("status", "paid")
                        neq("status", "cancelled")
                    }
                    order("due_date", io.github.jan.supabase.postgrest.query.Order.ASCENDING)
                }
            val schedules = response.decodeList<PaymentScheduleDto>().map { it.toDomain() }
            emit(Result.success(schedules))
        } catch (e: Exception) {
            emit(Result.error("Failed to load overdue schedules: ${e.message}", e))
        }
    }.flowOn(dispatcher)

    override suspend fun getScheduleById(scheduleId: String): Result<PaymentSchedule> {
        return try {
            val dto = supabase.postgrest[Constants.TABLE_PAYMENT_SCHEDULES]
                .select { filter { eq("id", scheduleId); eq("user_id", userId) } }
                .decodeSingle<PaymentScheduleDto>()
            Result.success(dto.toDomain())
        } catch (e: Exception) {
            Result.error("Failed to load schedule: ${e.message}", e)
        }
    }

    override suspend fun createSchedule(schedule: PaymentSchedule): Result<PaymentSchedule> {
        return try {
            val dto = schedule.copy(userId = userId).toDto()
            val response = supabase.postgrest[Constants.TABLE_PAYMENT_SCHEDULES]
                .insert(dto) { select() }
                .decodeSingle<PaymentScheduleDto>()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.error("Failed to create schedule: ${e.message}", e)
        }
    }

    override suspend fun createSchedules(schedules: List<PaymentSchedule>): Result<List<PaymentSchedule>> {
        return try {
            val dtos = schedules.map { it.copy(userId = userId).toDto() }
            val response = supabase.postgrest[Constants.TABLE_PAYMENT_SCHEDULES]
                .insert(dtos) { select() }
                .decodeList<PaymentScheduleDto>()
            Result.success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Result.error("Failed to create schedules: ${e.message}", e)
        }
    }

    override suspend fun updateSchedule(schedule: PaymentSchedule): Result<PaymentSchedule> {
        return try {
            val dto = schedule.toDto()
            supabase.postgrest[Constants.TABLE_PAYMENT_SCHEDULES]
                .update(dto) { filter { eq("id", schedule.id); eq("user_id", userId) } }
            Result.success(schedule)
        } catch (e: Exception) {
            Result.error("Failed to update schedule: ${e.message}", e)
        }
    }

    override suspend fun deleteSchedule(scheduleId: String): Result<Unit> {
        return try {
            supabase.postgrest[Constants.TABLE_PAYMENT_SCHEDULES]
                .delete { filter { eq("id", scheduleId); eq("user_id", userId) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Failed to delete schedule: ${e.message}", e)
        }
    }

    override suspend fun deleteSchedulesByProject(projectId: String): Result<Unit> {
        return try {
            supabase.postgrest[Constants.TABLE_PAYMENT_SCHEDULES]
                .delete { filter { eq("project_id", projectId); eq("user_id", userId) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Failed to delete schedules: ${e.message}", e)
        }
    }
}
