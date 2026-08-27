package com.arcmanager.presentation.screens.payments

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.arcmanager.core.util.Result
import com.arcmanager.core.util.ValidationUtils
import com.arcmanager.domain.model.*
import com.arcmanager.domain.repository.BankAccountRepository
import com.arcmanager.domain.repository.ClientRepository
import com.arcmanager.domain.repository.PaymentRepository
import com.arcmanager.domain.repository.ProjectRepository
import com.arcmanager.presentation.components.ArcFilterChips
import com.arcmanager.presentation.components.LiquidGlassButton
import com.arcmanager.presentation.components.LiquidMeshBackground
import com.arcmanager.presentation.screens.clients.inputFieldColors
import com.arcmanager.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.Instant
import javax.inject.Inject

data class AddPaymentUiState(
    val clients: List<Client> = emptyList(),
    val projects: List<Project> = emptyList(),
    val bankAccounts: List<BankAccount> = emptyList(),
    val selectedClientId: String = "",
    val selectedProjectId: String? = null,
    val selectedBankAccountId: String? = null,
    val amount: String = "",
    val currency: String = "INR",
    val paymentType: PaymentType = PaymentType.ADVANCE,
    val paymentMethod: PaymentMethod = PaymentMethod.UPI,
    val transactionRef: String = "",
    val notes: String = "",
    val amountError: String? = null,
    val clientError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AddPaymentViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val clientRepository: ClientRepository,
    private val projectRepository: ProjectRepository,
    private val bankAccountRepository: BankAccountRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddPaymentUiState())
    val uiState: StateFlow<AddPaymentUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    fun initPreselected(clientId: String?, projectId: String?) {
        if (!clientId.isNullOrBlank()) {
            _uiState.update { it.copy(selectedClientId = clientId) }
            loadProjectsForClient(clientId)
        }
        if (!projectId.isNullOrBlank()) {
            _uiState.update { it.copy(selectedProjectId = projectId) }
        }
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Load clients
            clientRepository.getClients("active").collect { result ->
                if (result is Result.Success) {
                    _uiState.update { state ->
                        val selected = if (state.selectedClientId.isEmpty() && result.data.isNotEmpty()) {
                            result.data.first().id
                        } else state.selectedClientId
                        state.copy(clients = result.data, selectedClientId = selected)
                    }
                    if (_uiState.value.selectedClientId.isNotEmpty()) {
                        loadProjectsForClient(_uiState.value.selectedClientId)
                    }
                }
            }
        }

        viewModelScope.launch {
            // Load bank accounts
            bankAccountRepository.getBankAccounts().collect { result ->
                if (result is Result.Success) {
                    _uiState.update { state ->
                        state.copy(
                            bankAccounts = result.data,
                            selectedBankAccountId = result.data.firstOrNull()?.id
                        )
                    }
                }
            }
        }
    }

    private fun loadProjectsForClient(clientId: String) {
        viewModelScope.launch {
            projectRepository.getProjectsByClient(clientId).collect { result ->
                if (result is Result.Success) {
                    _uiState.update { it.copy(projects = result.data) }
                }
            }
        }
    }

    fun onClientSelected(clientId: String) {
        _uiState.update { it.copy(selectedClientId = clientId, selectedProjectId = null, clientError = null) }
        loadProjectsForClient(clientId)
    }

    fun onProjectSelected(projectId: String?) = _uiState.update { it.copy(selectedProjectId = projectId) }
    fun onBankAccountSelected(accountId: String?) = _uiState.update { it.copy(selectedBankAccountId = accountId) }
    fun onAmountChange(amount: String) = _uiState.update { it.copy(amount = amount, amountError = null) }
    fun onPaymentTypeSelected(type: PaymentType) = _uiState.update { it.copy(paymentType = type) }
    fun onPaymentMethodSelected(method: PaymentMethod) = _uiState.update { it.copy(paymentMethod = method) }
    fun onTransactionRefChange(ref: String) = _uiState.update { it.copy(transactionRef = ref) }
    fun onNotesChange(notes: String) = _uiState.update { it.copy(notes = notes) }

    fun recordPayment(onSuccess: () -> Unit) {
        val state = _uiState.value
        val amountErr = ValidationUtils.validateAmount(state.amount)
        val clientErr = if (state.selectedClientId.isBlank()) "Client is required" else null

        if (amountErr != null || clientErr != null) {
            _uiState.update { it.copy(amountError = amountErr, clientError = clientErr) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val amount = ValidationUtils.parseAmount(state.amount) ?: BigDecimal.ZERO
            val payment = Payment(
                id = "",
                userId = "",
                clientId = state.selectedClientId,
                projectId = state.selectedProjectId?.ifBlank { null },
                amount = amount,
                currency = state.currency,
                paymentDate = Instant.now(),
                paymentType = state.paymentType,
                paymentMethod = state.paymentMethod,
                bankAccountId = state.selectedBankAccountId?.ifBlank { null },
                transactionReference = state.transactionRef.trim().ifEmpty { null },
                notes = state.notes.trim().ifEmpty { null },
                status = "received"
            )

            when (val result = paymentRepository.recordPayment(payment)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentScreen(
    preselectedClientId: String?,
    preselectedProjectId: String?,
    onNavigateBack: () -> Unit,
    onPaymentRecorded: () -> Unit,
    viewModel: AddPaymentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var clientDropdownExpanded by remember { mutableStateOf(false) }
    var projectDropdownExpanded by remember { mutableStateOf(false) }
    var bankDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(preselectedClientId, preselectedProjectId) {
        viewModel.initPreselected(preselectedClientId, preselectedProjectId)
    }

    LiquidMeshBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Record Payment", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = TextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = Dimens.ScreenPadding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AnimatedVisibility(visible = uiState.error != null) {
                    Box(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                            .background(StatusDangerSubtle).border(1.dp, StatusDanger.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(Dimens.CardPadding)
                    ) {
                        Text(uiState.error ?: "", style = MaterialTheme.typography.bodyMedium, color = StatusDangerBright)
                    }
                }

                // Amount Field (High Visual Impact)
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = viewModel::onAmountChange,
                    label = { Text("Amount Received (₹) *") },
                    placeholder = { Text("0.00") },
                    leadingIcon = { Text("₹", style = MaterialTheme.typography.headlineMedium, color = StatusSuccessBright, modifier = Modifier.padding(start = 14.dp)) },
                    isError = uiState.amountError != null,
                    supportingText = uiState.amountError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputFieldColors(),
                    shape = RoundedCornerShape(16.dp),
                )

                // Client Selector
                Text("Client *", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Box(modifier = Modifier.fillMaxWidth()) {
                    val selectedClientName = uiState.clients.firstOrNull { it.id == uiState.selectedClientId }?.name ?: "Select Client"
                    OutlinedTextField(
                        value = selectedClientName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { clientDropdownExpanded = true }) {
                                Icon(Icons.Outlined.ArrowDropDown, "Select Client", tint = TextSecondary)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { clientDropdownExpanded = true },
                        colors = inputFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                        isError = uiState.clientError != null,
                        supportingText = uiState.clientError?.let { { Text(it) } }
                    )
                    DropdownMenu(
                        expanded = clientDropdownExpanded,
                        onDismissRequest = { clientDropdownExpanded = false },
                        modifier = Modifier.background(DarkSurfaceElevated)
                    ) {
                        uiState.clients.forEach { client ->
                            DropdownMenuItem(
                                text = { Text(client.name, color = TextPrimary) },
                                onClick = {
                                    viewModel.onClientSelected(client.id)
                                    clientDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Project Selector (Optional)
                if (uiState.projects.isNotEmpty()) {
                    Text("Project (Optional)", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val selectedProjectName = uiState.projects.firstOrNull { it.id == uiState.selectedProjectId }?.name ?: "Select Project (Optional)"
                        OutlinedTextField(
                            value = selectedProjectName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { projectDropdownExpanded = true }) {
                                    Icon(Icons.Outlined.ArrowDropDown, "Select Project", tint = TextSecondary)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable { projectDropdownExpanded = true },
                            colors = inputFieldColors(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        DropdownMenu(
                            expanded = projectDropdownExpanded,
                            onDismissRequest = { projectDropdownExpanded = false },
                            modifier = Modifier.background(DarkSurfaceElevated)
                        ) {
                            DropdownMenuItem(
                                text = { Text("None", color = TextTertiary) },
                                onClick = {
                                    viewModel.onProjectSelected(null)
                                    projectDropdownExpanded = false
                                }
                            )
                            uiState.projects.forEach { project ->
                                DropdownMenuItem(
                                    text = { Text(project.name, color = TextPrimary) },
                                    onClick = {
                                        viewModel.onProjectSelected(project.id)
                                        projectDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Payment Type Chips
                Text("Payment Type", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                ArcFilterChips(
                    items = PaymentType.entries,
                    selectedItem = uiState.paymentType,
                    onItemSelected = viewModel::onPaymentTypeSelected,
                    labelProvider = { it.displayName }
                )

                // Payment Method Chips
                Text("Payment Method", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                ArcFilterChips(
                    items = PaymentMethod.entries,
                    selectedItem = uiState.paymentMethod,
                    onItemSelected = viewModel::onPaymentMethodSelected,
                    labelProvider = { it.displayName }
                )

                // Received Into Bank Account
                if (uiState.bankAccounts.isNotEmpty()) {
                    Text("Received Into Bank Account", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val selectedAccount = uiState.bankAccounts.firstOrNull { it.id == uiState.selectedBankAccountId }
                        val display = selectedAccount?.displayName ?: "Select Account"
                        OutlinedTextField(
                            value = display,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { bankDropdownExpanded = true }) {
                                    Icon(Icons.Outlined.ArrowDropDown, "Select Account", tint = TextSecondary)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().clickable { bankDropdownExpanded = true },
                            colors = inputFieldColors(),
                            shape = RoundedCornerShape(14.dp)
                        )
                        DropdownMenu(
                            expanded = bankDropdownExpanded,
                            onDismissRequest = { bankDropdownExpanded = false },
                            modifier = Modifier.background(DarkSurfaceElevated)
                        ) {
                            uiState.bankAccounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = { Text(acc.displayName, color = TextPrimary) },
                                    onClick = {
                                        viewModel.onBankAccountSelected(acc.id)
                                        bankDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Transaction Reference
                OutlinedTextField(
                    value = uiState.transactionRef,
                    onValueChange = viewModel::onTransactionRefChange,
                    label = { Text("Transaction Reference / UPI Ref / UTR") },
                    placeholder = { Text("e.g. UPI/123456789/HDFC") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputFieldColors(),
                    shape = RoundedCornerShape(14.dp)
                )

                // Notes
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChange,
                    label = { Text("Notes (Optional)") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputFieldColors(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                LiquidGlassButton(
                    onClick = { viewModel.recordPayment(onPaymentRecorded) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    gradientColors = GradientLiquidSuccess
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                    } else {
                        Text("RECORD PAYMENT", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
