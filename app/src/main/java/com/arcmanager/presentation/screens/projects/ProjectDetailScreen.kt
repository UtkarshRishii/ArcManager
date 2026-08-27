package com.arcmanager.presentation.screens.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.arcmanager.core.util.CurrencyUtils
import com.arcmanager.core.util.DateUtils
import com.arcmanager.core.util.Result
import com.arcmanager.domain.model.Payment
import com.arcmanager.domain.model.PaymentSchedule
import com.arcmanager.domain.model.Project
import com.arcmanager.domain.repository.PaymentRepository
import com.arcmanager.domain.repository.PaymentScheduleRepository
import com.arcmanager.domain.repository.ProjectRepository
import com.arcmanager.presentation.components.*
import com.arcmanager.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class ProjectDetailUiState(
    val isLoading: Boolean = true,
    val project: Project? = null,
    val schedules: List<PaymentSchedule> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val paymentRepository: PaymentRepository,
    private val scheduleRepository: PaymentScheduleRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val projectId: String = checkNotNull(savedStateHandle["projectId"])
    private val _uiState = MutableStateFlow(ProjectDetailUiState())
    val uiState: StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()

    init {
        loadProjectDetails()
    }

    fun loadProjectDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val projRes = projectRepository.getProjectById(projectId)
            if (projRes is Result.Success) {
                val project = projRes.data
                val totalRec = paymentRepository.getTotalReceivedByProject(projectId).getOrNull() ?: BigDecimal.ZERO
                project.receivedAmount = totalRec

                var schedules = emptyList<PaymentSchedule>()
                scheduleRepository.getSchedulesByProject(projectId).collect { sRes ->
                    if (sRes is Result.Success) schedules = sRes.data
                }

                var payments = emptyList<Payment>()
                paymentRepository.getPaymentsByProject(projectId).collect { payRes ->
                    if (payRes is Result.Success) payments = payRes.data
                }

                _uiState.update {
                    it.copy(isLoading = false, project = project, schedules = schedules, payments = payments)
                }
            } else if (projRes is Result.Error) {
                _uiState.update { it.copy(isLoading = false, error = projRes.message) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    onNavigateToScheduleBuilder: () -> Unit,
    onNavigateToAddPayment: (String) -> Unit,
    viewModel: ProjectDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val project = uiState.project
    val currency = project?.currency ?: "INR"

    androidx.compose.runtime.LaunchedEffect(projectId) {
        viewModel.loadProjectDetails()
    }

    if (uiState.isLoading || project == null) {
        LoadingState(message = "Loading project details...")
        return
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text(project.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // 1. Project Financial Card
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                        .padding(Dimens.CardPadding)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = project.name, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                                Text(text = project.paymentModel.displayName, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            StatusBadge(status = project.displayStatus)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurface)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("TOTAL VALUE", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextTertiary)
                                Text(CurrencyUtils.formatAmount(project.totalAmount, currency), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                            }
                            Column {
                                Text("RECEIVED", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextTertiary)
                                Text(CurrencyUtils.formatAmount(project.receivedAmount, currency), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = StatusSuccess)
                            }
                            Column {
                                Text("PENDING", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = TextTertiary)
                                Text(CurrencyUtils.formatAmount(project.pendingAmount, currency), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = StatusWarning)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { (project.completionPercentage / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = PrimaryViolet,
                            trackColor = DarkSurfaceElevated,
                        )
                    }
                }
            }

            // 2. Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onNavigateToAddPayment(project.clientId) },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet, contentColor = TextOnPrimary)
                    ) {
                        Text("+ Record Payment", style = MaterialTheme.typography.labelMedium)
                    }
                    OutlinedButton(
                        onClick = onNavigateToScheduleBuilder,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryVioletLight)
                    ) {
                        Text("Edit Schedule", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // 3. Payment Schedule Breakdown
            item {
                Text("PAYMENT SCHEDULE", style = MaterialTheme.typography.labelSmall, color = TextTertiary, letterSpacing = 1.sp)
            }

            if (uiState.schedules.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(DarkSurface).padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No schedule plan defined yet", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    }
                }
            } else {
                items(uiState.schedules) { schedule ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkSurfaceCard)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(schedule.title ?: "Milestone", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                                Text("Due ${DateUtils.formatDisplayDate(schedule.dueDate)}", style = MaterialTheme.typography.bodySmall, color = if (schedule.isOverdue) StatusDanger else TextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(CurrencyUtils.formatAmount(schedule.amount, currency), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                                StatusBadge(status = schedule.effectiveStatus)
                            }
                        }
                    }
                }
            }

            // 4. Project Payments Ledger
            item {
                Text("RECORDED PAYMENTS (${uiState.payments.size})", style = MaterialTheme.typography.labelSmall, color = TextTertiary, letterSpacing = 1.sp)
            }

            if (uiState.payments.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(DarkSurface).padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No payments recorded yet", style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                    }
                }
            } else {
                items(uiState.payments) { payment ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(DarkSurfaceCard)
                            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(payment.paymentType.displayName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                                Text(DateUtils.formatRelativeDateTime(payment.paymentDate), style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("+ ${CurrencyUtils.formatAmount(payment.amount, currency)}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = StatusSuccess)
                                Text(payment.paymentMethod.displayName, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                            }
                        }
                    }
                }
            }
        }
    }
}
