package com.arcmanager.presentation.screens.projects

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.arcmanager.core.util.Result
import com.arcmanager.core.util.ValidationUtils
import com.arcmanager.domain.model.Client
import com.arcmanager.domain.model.PaymentModel
import com.arcmanager.domain.model.Project
import com.arcmanager.domain.repository.ClientRepository
import com.arcmanager.domain.repository.ProjectRepository
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
import java.time.LocalDate
import javax.inject.Inject

data class CreateProjectUiState(
    val clients: List<Client> = emptyList(),
    val selectedClientId: String = "",
    val name: String = "",
    val description: String = "",
    val totalAmount: String = "",
    val currency: String = "INR",
    val paymentModel: PaymentModel = PaymentModel.ADVANCE_FINAL,
    val startDate: LocalDate? = null,
    val completionDate: LocalDate? = null,
    val nameError: String? = null,
    val amountError: String? = null,
    val clientError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class CreateProjectViewModel @Inject constructor(
    private val projectRepository: ProjectRepository,
    private val clientRepository: ClientRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateProjectUiState())
    val uiState: StateFlow<CreateProjectUiState> = _uiState.asStateFlow()

    init {
        loadClients()
    }

    fun initPreselectedClient(clientId: String?) {
        if (!clientId.isNullOrBlank()) {
            _uiState.update { it.copy(selectedClientId = clientId) }
        }
    }

    private fun loadClients() {
        viewModelScope.launch {
            clientRepository.getClients("active").collect { result ->
                if (result is Result.Success) {
                    _uiState.update { state ->
                        val selected = if (state.selectedClientId.isEmpty() && result.data.isNotEmpty()) {
                            result.data.first().id
                        } else state.selectedClientId
                        state.copy(clients = result.data, selectedClientId = selected)
                    }
                }
            }
        }
    }

    fun onClientSelected(clientId: String) = _uiState.update { it.copy(selectedClientId = clientId, clientError = null) }
    fun onNameChange(name: String) = _uiState.update { it.copy(name = name, nameError = null) }
    fun onDescriptionChange(desc: String) = _uiState.update { it.copy(description = desc) }
    fun onAmountChange(amount: String) = _uiState.update { it.copy(totalAmount = amount, amountError = null) }
    fun onCurrencyChange(currency: String) = _uiState.update { it.copy(currency = currency) }
    fun onPaymentModelSelected(model: PaymentModel) = _uiState.update { it.copy(paymentModel = model) }

    fun createProject(onSuccess: (String) -> Unit) {
        val state = _uiState.value
        val nameErr = ValidationUtils.validateProjectName(state.name)
        val amountErr = ValidationUtils.validateAmount(state.totalAmount)
        val clientErr = if (state.selectedClientId.isBlank()) "Please select a client" else null

        if (nameErr != null || amountErr != null || clientErr != null) {
            _uiState.update { it.copy(nameError = nameErr, amountError = amountErr, clientError = clientErr) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val amount = ValidationUtils.parseAmount(state.totalAmount) ?: BigDecimal.ZERO
            val project = Project(
                id = "",
                userId = "",
                clientId = state.selectedClientId,
                name = state.name.trim(),
                description = state.description.trim().ifEmpty { null },
                totalAmount = amount,
                currency = state.currency,
                paymentModel = state.paymentModel,
                startDate = state.startDate,
                expectedCompletionDate = state.completionDate,
            )

            when (val result = projectRepository.createProject(project)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess(result.data.id)
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
fun CreateProjectScreen(
    preselectedClientId: String?,
    onNavigateBack: () -> Unit,
    onProjectCreated: (String) -> Unit,
    viewModel: CreateProjectViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current
    var clientDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(preselectedClientId) {
        viewModel.initPreselectedClient(preselectedClientId)
    }

    LiquidMeshBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Create Project", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary) },
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

                // Project Name *
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Project Name *") },
                    placeholder = { Text("e.g. Falcon Android App") },
                    isError = uiState.nameError != null,
                    supportingText = uiState.nameError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputFieldColors(),
                    shape = RoundedCornerShape(14.dp),
                )

                // Total Amount *
                OutlinedTextField(
                    value = uiState.totalAmount,
                    onValueChange = viewModel::onAmountChange,
                    label = { Text("Total Project Value (₹) *") },
                    placeholder = { Text("e.g. 50,000") },
                    leadingIcon = { Text("₹", style = MaterialTheme.typography.titleMedium, color = PrimaryVioletBright, modifier = Modifier.padding(start = 12.dp)) },
                    isError = uiState.amountError != null,
                    supportingText = uiState.amountError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputFieldColors(),
                    shape = RoundedCornerShape(14.dp),
                )

                // Payment Model Selector
                Text("Payment Plan Model *", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PaymentModel.entries.forEach { model ->
                        val isSelected = uiState.paymentModel == model
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) PrimaryViolet.copy(alpha = 0.18f) else Color(0x10FFFFFF))
                                .border(
                                    1.dp,
                                    if (isSelected) Brush.verticalGradient(listOf(PrimaryVioletLight, PrimaryVioletDark)) else Brush.linearGradient(listOf(Color(0x18FFFFFF), Color(0x18FFFFFF))),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { viewModel.onPaymentModelSelected(model) }
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = model.displayName,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (isSelected) PrimaryVioletBright else TextPrimary
                                    )
                                    val desc = when (model) {
                                        PaymentModel.ONE_TIME -> "Single full payment upon completion"
                                        PaymentModel.ADVANCE_FINAL -> "Advance upfront + Final balance upon delivery"
                                        PaymentModel.INSTALLMENTS -> "Divided into equal installment milestones"
                                        PaymentModel.MONTHLY_RECURRING -> "Fixed recurring monthly retainer billing"
                                        PaymentModel.CUSTOM -> "Flexible custom dates & amounts"
                                    }
                                    Text(text = desc, style = MaterialTheme.typography.bodySmall, color = TextTertiary)
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.onPaymentModelSelected(model) },
                                    colors = RadioButtonDefaults.colors(selectedColor = PrimaryVioletBright)
                                )
                            }
                        }
                    }
                }

                // Description
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text("Project Scope / Description (Optional)") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputFieldColors(),
                    shape = RoundedCornerShape(14.dp),
                )

                Spacer(modifier = Modifier.height(10.dp))

                LiquidGlassButton(
                    onClick = { viewModel.createProject(onProjectCreated) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                    } else {
                        Text("Continue to Schedule Builder →", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
