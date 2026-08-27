package com.arcmanager.domain.repository

import com.arcmanager.core.util.Result
import com.arcmanager.domain.model.PaymentSchedule
import kotlinx.coroutines.flow.Flow

interface PaymentScheduleRepository {
    fun getSchedulesByProject(projectId: String): Flow<Result<List<PaymentSchedule>>>
    fun getSchedulesByClient(clientId: String): Flow<Result<List<PaymentSchedule>>>
    fun getUpcomingSchedules(limit: Int = 10): Flow<Result<List<PaymentSchedule>>>
    fun getOverdueSchedules(): Flow<Result<List<PaymentSchedule>>>
    suspend fun getScheduleById(scheduleId: String): Result<PaymentSchedule>
    suspend fun createSchedule(schedule: PaymentSchedule): Result<PaymentSchedule>
    suspend fun createSchedules(schedules: List<PaymentSchedule>): Result<List<PaymentSchedule>>
    suspend fun updateSchedule(schedule: PaymentSchedule): Result<PaymentSchedule>
    suspend fun toggleScheduleReceived(scheduleId: String, markReceived: Boolean): Result<PaymentSchedule>
    suspend fun deleteSchedule(scheduleId: String): Result<Unit>
    suspend fun deleteSchedulesByProject(projectId: String): Result<Unit>
}
