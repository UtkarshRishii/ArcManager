package com.arcmanager.presentation.screens.payments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.arcmanager.core.util.ValidationUtils
import com.arcmanager.domain.model.PaymentModel
import com.arcmanager.domain.model.PaymentSchedule
import com.arcmanager.domain.model.Project
import com.arcmanager.domain.repository.PaymentScheduleRepository
import com.arcmanager.domain.repository.ProjectRepository
import com.arcmanager.domain.usecase.SaveProjectScheduleUseCase
import com.arcmanager.presentation.screens.clients.inputFieldColors
import com.arcmanager.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import javax.inject.Inject

data class EditableMilestone(
    val id: String = java.util.UUID.randomUUID().toString(),
    var title: String = "",
    var amount: String = "",
    var dueDate: LocalDate = LocalDate.now(),
    var paymentType: String = "MILESTONE",
)

data class ScheduleBuilderUiState(
    val isLoading: Boolean = true,
    val project: Project? = null,
    val milestones: List<EditableMilestone> = emptyList(),
    val error: String? = null,
    val isSaving: Boolean = false,
) {
    val totalProjectAmount: BigDecimal
        get() = project?.totalAmount ?: BigDecimal.ZERO

    val scheduledAmount: BigDecimal
        get() = milestones.sumOf { ValidationUtils.parseAmount(it.amount) ?: BigDecimal.ZERO }

    val remainingToSchedule: BigDecimal
        get() = totalProjectAmount.subtract(scheduledAmount)

    val isBalanced: Boolean
        get() = totalProjectAmount.compareTo(scheduledAmount) == 0
}

@HiltViewModel
class PaymentScheduleBuilderViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val saveProjectScheduleUseCase: SaveProjectScheduleUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val projectId: String = checkNotNull(savedStateHandle["projectId"])
    private val _uiState = MutableStateFlow(ScheduleBuilderUiState())
    val uiState: StateFlow<ScheduleBuilderUiState> = _uiState.asStateFlow()

    init {
        loadProject()
    }

    private fun loadProject() {
        viewModelScope.launch {
            when (val result = projectRepository.getProjectById(projectId)) {
                is Result.Success -> {
                    val project = result.data
                    val defaultMilestones = generateInitialMilestones(project)
                    _uiState.update {
                        it.copy(isLoading = false, project = project, milestones = defaultMilestones)
                    }
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> {}
            }
        }
    }

    private fun generateInitialMilestones(project: Project): List<EditableMilestone> {
        val total = project.totalAmount
        val today = LocalDate.now()

        return when (project.paymentModel) {
            PaymentModel.ONE_TIME -> listOf(
                EditableMilestone(title = "Full Payment", amount = total.toPlainString(), dueDate = today.plusDays(30), paymentType = "FINAL")
            )
            PaymentModel.ADVANCE_FINAL -> {
                val advance = total.multiply(BigDecimal("0.30")).setScale(0, RoundingMode.HALF_UP)
                val finalAmt = total.subtract(advance)
                listOf(
                    EditableMilestone(title = "Advance", amount = advance.toPlainString(), dueDate = today, paymentType = "ADVANCE"),
                    EditableMilestone(title = "Final Delivery", amount = finalAmt.toPlainString(), dueDate = today.plusDays(30), paymentType = "FINAL")
                )
            }
            PaymentModel.INSTALLMENTS -> {
                val part = total.divide(BigDecimal(3), 0, RoundingMode.HALF_UP)
                val part3 = total.subtract(part.multiply(BigDecimal(2)))
                listOf(
                    EditableMilestone(title = "Installment 1", amount = part.toPlainString(), dueDate = today, paymentType = "INSTALLMENT"),
                    EditableMilestone(title = "Installment 2", amount = part.toPlainString(), dueDate = today.plusDays(30), paymentType = "INSTALLMENT"),
                    EditableMilestone(title = "Installment 3", amount = part3.toPlainString(), dueDate = today.plusDays(60), paymentType = "INSTALLMENT")
                )
            }
            PaymentModel.MONTHLY_RECURRING -> listOf(
                EditableMilestone(title = "Month 1 Retainer", amount = total.toPlainString(), dueDate = today, paymentType = "MONTHLY"),
                EditableMilestone(title = "Month 2 Retainer", amount = total.toPlainString(), dueDate = today.plusMonths(1), paymentType = "MONTHLY")
            )
            PaymentModel.CUSTOM -> listOf(
                EditableMilestone(title = "Milestone 1", amount = total.toPlainString(), dueDate = today.plusDays(15), paymentType = "MILESTONE")
            )
        }
    }

    fun addMilestone() {
        val current = _uiState.value.milestones
        val remaining = _uiState.value.remainingToSchedule.coerceAtLeast(BigDecimal.ZERO)
        val newMilestone = EditableMilestone(
            title = "Milestone ${current.size + 1}",
            amount = if (remaining > BigDecimal.ZERO) remaining.toPlainString() else "",
            dueDate = LocalDate.now().plusDays((current.size + 1) * 15L),
            paymentType = "MILESTONE"
        )
        _uiState.update { it.copy(milestones = current + newMilestone) }
    }

    fun removeMilestone(index: Int) {
        val current = _uiState.value.milestones.toMutableList()
        if (current.size > 1 && index in current.indices) {
            current.removeAt(index)
            _uiState.update { it.copy(milestones = current) }
        }
    }

    fun updateMilestoneTitle(index: Int, title: String) {
        val current = _uiState.value.milestones.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(title = title)
            _uiState.update { it.copy(milestones = current) }
        }
    }

    fun updateMilestoneAmount(index: Int, amount: String) {
        val current = _uiState.value.milestones.toMutableList()
        if (index in current.indices) {
            current[index] = current[index].copy(amount = amount)
            _uiState.update { it.copy(milestones = current) }
        }
    }

    fun saveSchedule(onSuccess: () -> Unit) {
        val state = _uiState.value
        val project = state.project ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val scheduleEntities = state.milestones.map { m ->
                PaymentSchedule(
                    id = "",
                    userId = "",
                    projectId = project.id,
                    clientId = project.clientId,
                    title = m.title.ifBlank { "Milestone" },
                    amount = ValidationUtils.parseAmount(m.amount) ?: BigDecimal.ZERO,
                    currency = project.currency,
                    dueDate = m.dueDate,
                    paymentType = m.paymentType,
                )
            }

            when (val result = saveProjectScheduleUseCase(projectId, scheduleEntities)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isSaving = false) }
                    onSuccess()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isSaving = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScheduleBuilderScreen(
    projectId: String,
    onNavigateBack: () -> Unit,
    onScheduleSaved: () -> Unit,
    viewModel: PaymentScheduleBuilderViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val project = uiState.project
    val currency = project?.currency ?: "INR"

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Payment Schedule", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = Dimens.ScreenPadding)
        ) {
            // Balance Card
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
                        Text("PROJECT VALUE", style = MaterialTheme.typography.labelSmall, color = TextTertiary, letterSpacing = 1.sp)
                        Text(
                            text = CurrencyUtils.formatAmount(uiState.totalProjectAmount, currency),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryVioletLight
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("SCHEDULED", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            Text(
                                text = CurrencyUtils.formatAmount(uiState.scheduledAmount, currency),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (uiState.isBalanced) StatusSuccess else TextPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("REMAINING", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                            Text(
                                text = CurrencyUtils.formatAmount(uiState.remainingToSchedule, currency),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = if (uiState.remainingToSchedule == BigDecimal.ZERO) StatusSuccess else StatusWarning
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Milestones Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PAYMENT PLAN MILESTONES", style = MaterialTheme.typography.labelSmall, color = TextTertiary, letterSpacing = 1.sp)
                TextButton(onClick = viewModel::addMilestone) {
                    Text("+ Add Milestone", color = PrimaryViolet)
                }
            }

            // Milestone List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                itemsIndexed(uiState.milestones, key = { _, m -> m.id }) { index, milestone ->
                    MilestoneCard(
                        index = index + 1,
                        milestone = milestone,
                        onTitleChange = { viewModel.updateMilestoneTitle(index, it) },
                        onAmountChange = { viewModel.updateMilestoneAmount(index, it) },
                        onDelete = { viewModel.removeMilestone(index) },
                        canDelete = uiState.milestones.size > 1
                    )
                }
            }

            // Save Schedule Button
            Button(
                onClick = { viewModel.saveSchedule(onScheduleSaved) },
                enabled = !uiState.isSaving && uiState.milestones.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.ButtonHeight),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet, contentColor = TextOnPrimary)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = TextOnPrimary, strokeWidth = 2.dp)
                } else {
                    Text("Confirm Payment Schedule", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MilestoneCard(
    index: Int,
    milestone: EditableMilestone,
    onTitleChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onDelete: () -> Unit,
    canDelete: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("%02d", index),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryVioletLight
                )
                if (canDelete) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Outlined.Close, "Remove milestone", tint = TextTertiary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            OutlinedTextField(
                value = milestone.title,
                onValueChange = onTitleChange,
                label = { Text("Milestone Title") },
                placeholder = { Text("e.g. Advance / Milestone 1 / Final") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = inputFieldColors(),
                shape = RoundedCornerShape(10.dp)
            )

            OutlinedTextField(
                value = milestone.amount,
                onValueChange = onAmountChange,
                label = { Text("Amount (₹)") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = inputFieldColors(),
                shape = RoundedCornerShape(10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Due: ${DateUtils.formatDisplayDate(milestone.dueDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
