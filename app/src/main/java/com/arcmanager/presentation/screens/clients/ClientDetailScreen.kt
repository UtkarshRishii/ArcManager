package com.arcmanager.presentation.screens.clients

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
import com.arcmanager.domain.model.Client
import com.arcmanager.domain.model.Payment
import com.arcmanager.domain.model.Project
import com.arcmanager.domain.repository.ClientRepository
import com.arcmanager.domain.repository.PaymentRepository
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

data class ClientDetailUiState(
    val isLoading: Boolean = true,
    val client: Client? = null,
    val projects: List<Project> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val selectedTab: Int = 0, // 0: Overview, 1: Projects, 2: Payments, 3: Notes
    val error: String? = null,
    val showDeleteConfirm: Boolean = false,
)

@HiltViewModel
class ClientDetailViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val projectRepository: ProjectRepository,
    private val paymentRepository: PaymentRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val clientId: String = checkNotNull(savedStateHandle["clientId"])
    private val _uiState = MutableStateFlow(ClientDetailUiState())
    val uiState: StateFlow<ClientDetailUiState> = _uiState.asStateFlow()

    init {
        loadClientDetails()
    }

    fun loadClientDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val clientRes = clientRepository.getClientById(clientId)
            if (clientRes is Result.Success) {
                val client = clientRes.data
                val totalRec = paymentRepository.getTotalReceivedByClient(clientId).getOrNull() ?: BigDecimal.ZERO
                client.totalReceived = totalRec

                // Load projects
                var projects = emptyList<Project>()
                projectRepository.getProjectsByClient(clientId).collect { pRes ->
                    if (pRes is Result.Success) projects = pRes.data
                }

                var totalBilled = BigDecimal.ZERO
                projects.forEach { totalBilled = totalBilled.add(it.totalAmount) }
                client.totalBilled = totalBilled
                client.totalPending = totalBilled.subtract(totalRec).coerceAtLeast(BigDecimal.ZERO)

                // Load payments
                var payments = emptyList<Payment>()
                paymentRepository.getPaymentsByClient(clientId).collect { payRes ->
                    if (payRes is Result.Success) payments = payRes.data
                }

                _uiState.update {
                    it.copy(isLoading = false, client = client, projects = projects, payments = payments)
                }
            } else if (clientRes is Result.Error) {
                _uiState.update { it.copy(isLoading = false, error = clientRes.message) }
            }
        }
    }

    fun onTabSelected(tab: Int) = _uiState.update { it.copy(selectedTab = tab) }
    fun setDeleteConfirm(show: Boolean) = _uiState.update { it.copy(showDeleteConfirm = show) }

    fun deleteClient(onDeleted: () -> Unit) {
        viewModelScope.launch {
            when (clientRepository.deleteClient(clientId)) {
                is Result.Success -> onDeleted()
                is Result.Error -> _uiState.update { it.copy(error = "Failed to delete client") }
                is Result.Loading -> {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    clientId: String,
    onNavigateBack: () -> Unit,
    onNavigateToProject: (String) -> Unit,
    onNavigateToCreateProject: () -> Unit,
    onNavigateToAddPayment: (String, String?) -> Unit,
    viewModel: ClientDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val client = uiState.client

    androidx.compose.runtime.LaunchedEffect(clientId) {
        viewModel.loadClientDetails()
    }

    if (uiState.isLoading || client == null) {
        LoadingState(message = "Loading client details...")
        return
    }

    if (uiState.showDeleteConfirm) {
        ConfirmDialog(
            title = "Delete Client",
            message = "Are you sure you want to delete ${client.name}? This will also delete associated project records.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = { viewModel.deleteClient(onNavigateBack) },
            onDismiss = { viewModel.setDeleteConfirm(false) }
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text(client.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setDeleteConfirm(true) }) {
                        Icon(Icons.Outlined.Delete, "Delete", tint = StatusDanger)
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
            // Header Card
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
                            Text(
                                text = client.name,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            if (!client.companyName.isNullOrBlank()) {
                                Text(text = client.companyName, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                        StatusBadge(status = client.displayStatus)
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
                        FinancialMetricMini(label = "Billed", amount = client.totalBilled, currency = client.currency)
                        FinancialMetricMini(label = "Received", amount = client.totalReceived, currency = client.currency, color = StatusSuccess)
                        FinancialMetricMini(label = "Pending", amount = client.totalPending, currency = client.currency, color = StatusWarning)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onNavigateToCreateProject,
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet, contentColor = TextOnPrimary)
                ) {
                    Text("+ Project", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = { onNavigateToAddPayment(client.id, null) },
                    modifier = Modifier.weight(1f).height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryVioletLight)
                ) {
                    Text("+ Payment", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Bar
            val tabs = listOf("Overview", "Projects (${uiState.projects.size})", "Payments (${uiState.payments.size})", "Notes")
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = DarkBackground,
                contentColor = PrimaryViolet,
                divider = { HorizontalDivider(color = BorderSubtle) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.onTabSelected(index) },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (uiState.selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (uiState.selectedTab == index) PrimaryVioletLight else TextTertiary
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tab Content
            when (uiState.selectedTab) {
                0 -> ClientOverviewTab(client = client)
                1 -> ClientProjectsTab(
                    projects = uiState.projects,
                    currency = client.currency,
                    onNavigateToProject = onNavigateToProject,
                    onCreateProject = onNavigateToCreateProject
                )
                2 -> ClientPaymentsTab(
                    payments = uiState.payments,
                    currency = client.currency,
                    onAddPayment = { onNavigateToAddPayment(client.id, null) }
                )
                3 -> ClientNotesTab(client = client)
            }
        }
    }
}

@Composable
private fun ClientOverviewTab(client: Client) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 30.dp)
    ) {
        item {
            InfoCard(title = "Contact Info") {
                if (!client.email.isNullOrBlank()) InfoRow(label = "Email", value = client.email)
                if (!client.phone.isNullOrBlank()) InfoRow(label = "Phone", value = client.phone)
                if (!client.telegram.isNullOrBlank()) InfoRow(label = "Telegram", value = client.telegram)
                if (!client.whatsapp.isNullOrBlank()) InfoRow(label = "WhatsApp", value = client.whatsapp)
                if (!client.country.isNullOrBlank()) InfoRow(label = "Country", value = client.country)
            }
        }
    }
}

@Composable
private fun ClientProjectsTab(
    projects: List<Project>,
    currency: String,
    onNavigateToProject: (String) -> Unit,
    onCreateProject: () -> Unit,
) {
    if (projects.isEmpty()) {
        EmptyState(
            title = "No projects yet",
            description = "Create a project to start setting payment plans",
            icon = Icons.Outlined.FolderOpen,
            actionLabel = "+ Create Project",
            onActionClick = onCreateProject
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 30.dp)
        ) {
            items(projects) { project ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurfaceCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                        .clickable { onNavigateToProject(project.id) }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = project.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                            Text(text = project.paymentModel.displayName, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = CurrencyUtils.formatAmount(project.totalAmount, currency), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                            StatusBadge(status = project.status)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientPaymentsTab(
    payments: List<Payment>,
    currency: String,
    onAddPayment: () -> Unit,
) {
    if (payments.isEmpty()) {
        EmptyState(
            title = "No payments recorded",
            description = "Record your first payment from this client",
            icon = Icons.Outlined.Payments,
            actionLabel = "+ Record Payment",
            onActionClick = onAddPayment
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 30.dp)
        ) {
            items(payments) { payment ->
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
                            Text(text = payment.paymentType.displayName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold), color = TextPrimary)
                            Text(text = DateUtils.formatRelativeDateTime(payment.paymentDate), style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "+ ${CurrencyUtils.formatAmount(payment.amount, currency)}", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = StatusSuccess)
                            Text(text = payment.paymentMethod.displayName, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientNotesTab(client: Client) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Text(
            text = if (!client.notes.isNullOrBlank()) client.notes else "No notes added for this client.",
            style = MaterialTheme.typography.bodyMedium,
            color = if (!client.notes.isNullOrBlank()) TextPrimary else TextTertiary
        )
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title.uppercase(), style = MaterialTheme.typography.labelSmall, color = TextTertiary, letterSpacing = 1.sp)
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = TextPrimary)
    }
}
