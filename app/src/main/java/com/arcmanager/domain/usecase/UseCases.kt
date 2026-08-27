package com.arcmanager.domain.usecase

import com.arcmanager.core.util.DateUtils
import com.arcmanager.core.util.Result
import com.arcmanager.domain.model.*
import com.arcmanager.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.math.BigDecimal
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

// ──────────────────────────────────────────────
// Financial Overview / Dashboard UseCase
// ──────────────────────────────────────────────
data class DashboardOverview(
    val totalReceived: BigDecimal = BigDecimal.ZERO,
    val totalPending: BigDecimal = BigDecimal.ZERO,
    val expectedThisMonth: BigDecimal = BigDecimal.ZERO,
    val receivedThisMonth: BigDecimal = BigDecimal.ZERO,
    val totalOverdue: BigDecimal = BigDecimal.ZERO,
    val activeClientsCount: Int = 0,
    val activeProjectsCount: Int = 0,
    val recurringMonthlyRevenue: BigDecimal = BigDecimal.ZERO,
    val upcomingPayments: List<PaymentSchedule> = emptyList(),
    val recentTransactions: List<Payment> = emptyList(),
    val overduePayments: List<PaymentSchedule> = emptyList(),
)

class GetDashboardOverviewUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val clientRepository: ClientRepository,
    private val projectRepository: ProjectRepository,
    private val scheduleRepository: PaymentScheduleRepository,
) {
    operator fun invoke(): Flow<Result<DashboardOverview>> = flow {
        emit(Result.Loading)
        try {
            // 1. Total received
            val totalReceived = paymentRepository.getTotalReceived().getOrNull() ?: BigDecimal.ZERO
            val receivedThisMonth = paymentRepository.getTotalReceivedThisMonth().getOrNull() ?: BigDecimal.ZERO

            // 2. Fetch active clients & projects to calculate pending totals
            var allClients = emptyList<Client>()
            clientRepository.getClients("active").collect { result ->
                if (result is Result.Success) allClients = result.data
            }

            var allProjects = emptyList<Project>()
            projectRepository.getAllProjects().collect { result ->
                if (result is Result.Success) allProjects = result.data
            }

            // Calculate pending totals accurately across all projects
            var totalProjectValue = BigDecimal.ZERO
            allProjects.forEach { project ->
                totalProjectValue = totalProjectValue.add(project.totalAmount)
            }
            val totalPending = totalProjectValue.subtract(totalReceived).coerceAtLeast(BigDecimal.ZERO)

            // 3. Fetch upcoming & overdue schedules
            var upcoming = emptyList<PaymentSchedule>()
            scheduleRepository.getUpcomingSchedules(5).collect { result ->
                if (result is Result.Success) upcoming = result.data
            }

            var overdue = emptyList<PaymentSchedule>()
            scheduleRepository.getOverdueSchedules().collect { result ->
                if (result is Result.Success) overdue = result.data
            }

            var overdueTotal = BigDecimal.ZERO
            overdue.forEach { overdueTotal = overdueTotal.add(it.remainingAmount) }

            // 4. Calculate expected this month
            val currentMonth = LocalDate.now(ZoneId.of("Asia/Kolkata")).monthValue
            val currentYear = LocalDate.now(ZoneId.of("Asia/Kolkata")).year
            var expectedThisMonth = BigDecimal.ZERO
            upcoming.filter { it.dueDate.monthValue == currentMonth && it.dueDate.year == currentYear }
                .forEach { expectedThisMonth = expectedThisMonth.add(it.remainingAmount) }

            // 5. Recent payments
            var recent = emptyList<Payment>()
            paymentRepository.getRecentPayments(10).collect { result ->
                if (result is Result.Success) recent = result.data
            }

            val overview = DashboardOverview(
                totalReceived = totalReceived,
                totalPending = totalPending,
                expectedThisMonth = expectedThisMonth,
                receivedThisMonth = receivedThisMonth,
                totalOverdue = overdueTotal,
                activeClientsCount = allClients.size,
                activeProjectsCount = allProjects.filter { it.status == "active" }.size,
                recurringMonthlyRevenue = BigDecimal.ZERO, // Populated in recurring phase
                upcomingPayments = upcoming,
                recentTransactions = recent,
                overduePayments = overdue,
            )

            emit(Result.Success(overview))
        } catch (e: Exception) {
            emit(Result.Error("Failed to calculate dashboard metrics: ${e.message}", e))
        }
    }
}

// ──────────────────────────────────────────────
// Client UseCases
// ──────────────────────────────────────────────
class GetClientsWithCalculationsUseCase @Inject constructor(
    private val clientRepository: ClientRepository,
    private val projectRepository: ProjectRepository,
    private val paymentRepository: PaymentRepository,
) {
    operator fun invoke(status: String? = null): Flow<Result<List<Client>>> = flow {
        emit(Result.Loading)
        clientRepository.getClients(status).collect { clientResult ->
            if (clientResult is Result.Success) {
                val enrichedClients = clientResult.data.map { client ->
                    val totalReceived = paymentRepository.getTotalReceivedByClient(client.id).getOrNull() ?: BigDecimal.ZERO
                    client.totalReceived = totalReceived
                    // Calculate total billed from client's projects
                    var totalBilled = BigDecimal.ZERO
                    projectRepository.getProjectsByClient(client.id).collect { projResult ->
                        if (projResult is Result.Success) {
                            projResult.data.forEach { totalBilled = totalBilled.add(it.totalAmount) }
                            client.activeProjects = projResult.data.count { it.status == "active" }
                        }
                    }
                    client.totalBilled = totalBilled
                    client.totalPending = totalBilled.subtract(totalReceived).coerceAtLeast(BigDecimal.ZERO)
                    client
                }
                emit(Result.Success(enrichedClients))
            } else if (clientResult is Result.Error) {
                emit(clientResult)
            }
        }
    }
}

// ──────────────────────────────────────────────
// Project & Payment Schedule UseCases
// ──────────────────────────────────────────────
class SaveProjectScheduleUseCase @Inject constructor(
    private val scheduleRepository: PaymentScheduleRepository,
) {
    suspend operator fun invoke(projectId: String, schedules: List<PaymentSchedule>): Result<List<PaymentSchedule>> {
        // Delete existing draft schedules for project before saving new plan
        scheduleRepository.deleteSchedulesByProject(projectId)
        return scheduleRepository.createSchedules(schedules)
    }
}
