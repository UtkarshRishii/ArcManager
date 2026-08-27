package com.arcmanager.presentation.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.arcmanager.core.util.CurrencyUtils
import com.arcmanager.core.util.DateUtils
import com.arcmanager.core.util.Result
import com.arcmanager.domain.model.PaymentSchedule
import com.arcmanager.domain.repository.PaymentScheduleRepository
import com.arcmanager.presentation.components.*
import com.arcmanager.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CalendarUiState(
    val isLoading: Boolean = true,
    val upcomingSchedules: List<PaymentSchedule> = emptyList(),
    val overdueSchedules: List<PaymentSchedule> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class PaymentCalendarViewModel @Inject constructor(
    private val scheduleRepository: PaymentScheduleRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadSchedules()
    }

    fun loadSchedules() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            var upcoming = emptyList<PaymentSchedule>()
            var overdue = emptyList<PaymentSchedule>()

            scheduleRepository.getUpcomingSchedules(50).collect { result ->
                if (result is Result.Success) upcoming = result.data
            }

            scheduleRepository.getOverdueSchedules().collect { result ->
                if (result is Result.Success) overdue = result.data
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    upcomingSchedules = upcoming,
                    overdueSchedules = overdue
                )
            }
        }
    }

    fun toggleScheduleReceived(scheduleId: String, markReceived: Boolean) {
        viewModelScope.launch {
            when (scheduleRepository.toggleScheduleReceived(scheduleId, markReceived)) {
                is Result.Success -> loadSchedules()
                is Result.Error -> {}
                is Result.Loading -> {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentCalendarScreen(
    onNavigateBack: () -> Unit,
    viewModel: PaymentCalendarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadSchedules()
    }

    LiquidMeshBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Payment Schedule Calendar",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = TextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            if (uiState.isLoading && uiState.upcomingSchedules.isEmpty() && uiState.overdueSchedules.isEmpty()) {
                LoadingState(message = "Loading schedules...")
            } else if (uiState.upcomingSchedules.isEmpty() && uiState.overdueSchedules.isEmpty()) {
                EmptyState(
                    title = "No Upcoming Due Dates",
                    description = "When you create projects with payment schedules or installments, they will appear here.",
                    icon = Icons.Outlined.CalendarMonth,
                    modifier = Modifier.padding(Dimens.ScreenPadding).padding(padding)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(horizontal = Dimens.ScreenPadding, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (uiState.overdueSchedules.isNotEmpty()) {
                        item {
                            Text("OVERDUE", style = MaterialTheme.typography.labelSmall, color = StatusDangerBright)
                        }
                        items(uiState.overdueSchedules) { schedule ->
                            ScheduleCalendarCard(
                                schedule = schedule,
                                onToggleReceived = { markReceived ->
                                    viewModel.toggleScheduleReceived(schedule.id, markReceived)
                                }
                            )
                        }
                    }

                    if (uiState.upcomingSchedules.isNotEmpty()) {
                        item {
                            Text("UPCOMING MILESTONES", style = MaterialTheme.typography.labelSmall, color = TextTertiary)
                        }
                        items(uiState.upcomingSchedules) { schedule ->
                            ScheduleCalendarCard(
                                schedule = schedule,
                                onToggleReceived = { markReceived ->
                                    viewModel.toggleScheduleReceived(schedule.id, markReceived)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleCalendarCard(
    schedule: PaymentSchedule,
    onToggleReceived: (Boolean) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0x1C1C1C2E))
            .border(1.dp, Brush.verticalGradient(listOf(Color(0x35FFFFFF), Color(0x0CFFFFFF))), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                PaymentReceivedTickButton(
                    isReceived = schedule.effectiveStatus == "paid",
                    onToggle = onToggleReceived
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = schedule.title ?: "Milestone",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Text(
                        text = DateUtils.formatDueText(schedule.dueDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (schedule.isOverdue) StatusDanger else PrimaryVioletLight
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyUtils.formatAmount(schedule.amount, schedule.currency),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                StatusBadge(status = schedule.effectiveStatus)
            }
        }
    }
}
