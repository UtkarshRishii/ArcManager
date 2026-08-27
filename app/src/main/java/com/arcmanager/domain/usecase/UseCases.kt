package com.arcmanager.domain.usecase

import com.arcmanager.core.util.Result
import com.arcmanager.domain.model.*
import com.arcmanager.domain.repository.*
import kotlinx.coroutines.flow.Flow
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
            // 1. Financial Totals
            val totalReceived = try {
                paymentRepository.getTotalReceived().getOrNull() ?: BigDecimal.ZERO
            } catch (e: Exception) { BigDecimal.ZERO }

            val receivedThisMonth = try {
                paymentRepository.getTotalReceivedThisMonth().getOrNull() ?: BigDecimal.ZERO
            } catch (e: Exception) { BigDecimal.ZERO }

            // 2. Fetch Projects for Total Value & Pending
            var totalProjectValue = BigDecimal.ZERO
            var activeProjects = 0
            try {
                projectRepository.getAllProjects().collect { result ->
                    if (result is Result.Success) {
                        activeProjects = result.data.count { it.status == "active" }
                        result.data.forEach { project ->
                            totalProjectValue = totalProjectValue.add(project.totalAmount)
                        }
                    }
                }
            } catch (e: Exception) { /* continue with defaults */ }

            val totalPending = totalProjectValue.subtract(totalReceived).coerceAtLeast(BigDecimal.ZERO)

            // 3. Active Clients Count
            var activeClients = 0
            try {
                clientRepository.getClients("active").collect { result ->
                    if (result is Result.Success) activeClients = result.data.size
                }
            } catch (e: Exception) { /* continue */ }

            // 4. Schedules (Upcoming & Overdue)
            var upcoming = emptyList<PaymentSchedule>()
            try {
                scheduleRepository.getUpcomingSchedules(5).collect { result ->
                    if (result is Result.Success) upcoming = result.data
                }
            } catch (e: Exception) { /* continue */ }

            var overdue = emptyList<PaymentSchedule>()
            var overdueTotal = BigDecimal.ZERO
            try {
                scheduleRepository.getOverdueSchedules().collect { result ->
                    if (result is Result.Success) {
                        overdue = result.data
                        overdue.forEach { overdueTotal = overdueTotal.add(it.remainingAmount) }
                    }
                }
            } catch (e: Exception) { /* continue */ }

            // 5. Expected this month
            val currentMonth = LocalDate.now(ZoneId.of("Asia/Kolkata")).monthValue
            val currentYear = LocalDate.now(ZoneId.of("Asia/Kolkata")).year
            var expectedThisMonth = BigDecimal.ZERO
            upcoming.filter { it.dueDate.monthValue == currentMonth && it.dueDate.year == currentYear }
                .forEach { expectedThisMonth = expectedThisMonth.add(it.remainingAmount) }

            // 6. Recent Payments
            var recent = emptyList<Payment>()
            try {
                paymentRepository.getRecentPayments(10).collect { result ->
                    if (result is Result.Success) recent = result.data
                }
            } catch (e: Exception) { /* continue */ }

            val overview = DashboardOverview(
                totalReceived = totalReceived,
                totalPending = totalPending,
                expectedThisMonth = expectedThisMonth,
                receivedThisMonth = receivedThisMonth,
                totalOverdue = overdueTotal,
                activeClientsCount = activeClients,
                activeProjectsCount = activeProjects,
                recurringMonthlyRevenue = BigDecimal.ZERO,
                upcomingPayments = upcoming,
                recentTransactions = recent,
                overduePayments = overdue,
            )

            emit(Result.Success(overview))
        } catch (e: Exception) {
            emit(Result.Error("Dashboard load failed: ${e.message}", e))
        }
    }
}

// ──────────────────────────────────────────────
// Client UseCases (Fast, Non-blocking Calculation)
// ──────────────────────────────────────────────
class GetClientsWithCalculationsUseCase @Inject constructor(
    private val clientRepository: ClientRepository,
    private val projectRepository: ProjectRepository,
    private val paymentRepository: PaymentRepository,
) {
    operator fun invoke(status: String? = null): Flow<Result<List<Client>>> = flow {
        emit(Result.Loading)
        try {
            clientRepository.getClients(status).collect { clientResult ->
                when (clientResult) {
                    is Result.Loading -> emit(Result.Loading)
                    is Result.Error -> emit(clientResult)
                    is Result.Success -> {
                        val clients = clientResult.data
                        if (clients.isEmpty()) {
                            emit(Result.Success(emptyList()))
                            return@collect
                        }

                        // Load projects in batch to enrich clients synchronously
                        var allProjects = emptyList<Project>()
                        try {
                            projectRepository.getAllProjects().collect { pRes ->
                                if (pRes is Result.Success) allProjects = pRes.data
                            }
                        } catch (e: Exception) { /* fallback to empty */ }

                        // Load payments in batch
                        var allPayments = emptyList<Payment>()
                        try {
                            paymentRepository.getAllPayments().collect { payRes ->
                                if (payRes is Result.Success) allPayments = payRes.data
                            }
                        } catch (e: Exception) { /* fallback to empty */ }

                        val enrichedClients = clients.map { client ->
                            val clientProjects = allProjects.filter { it.clientId == client.id }
                            val clientPayments = allPayments.filter { it.clientId == client.id && it.status == "received" }

                            var totalBilled = BigDecimal.ZERO
                            clientProjects.forEach { totalBilled = totalBilled.add(it.totalAmount) }

                            var totalReceived = BigDecimal.ZERO
                            clientPayments.forEach { totalReceived = totalReceived.add(it.amount) }

                            client.totalBilled = totalBilled
                            client.totalReceived = totalReceived
                            client.totalPending = totalBilled.subtract(totalReceived).coerceAtLeast(BigDecimal.ZERO)
                            client.activeProjects = clientProjects.count { it.status == "active" }
                            client
                        }

                        emit(Result.Success(enrichedClients))
                    }
                }
            }
        } catch (e: Exception) {
            emit(Result.Error("Failed to load clients: ${e.message}", e))
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
        scheduleRepository.deleteSchedulesByProject(projectId)
        return scheduleRepository.createSchedules(schedules)
    }
}
