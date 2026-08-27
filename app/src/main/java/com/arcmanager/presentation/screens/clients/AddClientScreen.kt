package com.arcmanager.presentation.screens.clients

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
import com.arcmanager.domain.repository.ClientRepository
import com.arcmanager.presentation.components.LiquidGlassButton
import com.arcmanager.presentation.components.LiquidMeshBackground
import com.arcmanager.presentation.theme.*
import com.arcmanager.presentation.viewmodel.AddClientUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddClientViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddClientUiState())
    val uiState: StateFlow<AddClientUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) = _uiState.update { it.copy(name = name, nameError = null) }
    fun onCompanyNameChange(company: String) = _uiState.update { it.copy(companyName = company) }
    fun onEmailChange(email: String) = _uiState.update { it.copy(email = email, emailError = null) }
    fun onPhoneChange(phone: String) = _uiState.update { it.copy(phone = phone, phoneError = null) }
    fun onTelegramChange(tg: String) = _uiState.update { it.copy(telegram = tg) }
    fun onWhatsappChange(wa: String) = _uiState.update { it.copy(whatsapp = wa) }
    fun onCountryChange(country: String) = _uiState.update { it.copy(country = country) }
    fun onCurrencyChange(currency: String) = _uiState.update { it.copy(currency = currency) }
    fun onNotesChange(notes: String) = _uiState.update { it.copy(notes = notes) }

    fun addTag(tag: String) {
        if (tag.isNotBlank() && !uiState.value.tags.contains(tag.trim())) {
            _uiState.update { it.copy(tags = it.tags + tag.trim(), tagInput = "") }
        }
    }

    fun removeTag(tag: String) {
        _uiState.update { it.copy(tags = it.tags - tag) }
    }

    fun onTagInputChange(input: String) = _uiState.update { it.copy(tagInput = input) }

    fun saveClient(onSuccess: (String) -> Unit) {
        val state = _uiState.value
        val nameErr = ValidationUtils.validateClientName(state.name)
        val emailErr = ValidationUtils.validateEmail(state.email)
        val phoneErr = ValidationUtils.validatePhone(state.phone)

        if (nameErr != null || emailErr != null || phoneErr != null) {
            _uiState.update { it.copy(nameError = nameErr, emailError = emailErr, phoneError = phoneErr) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val client = Client(
                id = "",
                userId = "",
                name = state.name.trim(),
                companyName = state.companyName.trim().ifEmpty { null },
                email = state.email.trim().ifEmpty { null },
                phone = state.phone.trim().ifEmpty { null },
                telegram = state.telegram.trim().ifEmpty { null },
                whatsapp = state.whatsapp.trim().ifEmpty { null },
                country = state.country.trim().ifEmpty { null },
                currency = state.currency,
                notes = state.notes.trim().ifEmpty { null },
                tags = state.tags
            )

            when (val result = clientRepository.addClient(client)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
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
fun AddClientScreen(
    onNavigateBack: () -> Unit,
    onClientAdded: (String) -> Unit,
    viewModel: AddClientViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LiquidMeshBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("New Client", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary) },
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

                // Client Name *
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Client Name *") },
                    placeholder = { Text("e.g. Acme Corp / Alex Smith") },
                    isError = uiState.nameError != null,
                    supportingText = uiState.nameError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputFieldColors(),
                    shape = RoundedCornerShape(14.dp),
                )

                // Company Name
                OutlinedTextField(
                    value = uiState.companyName,
                    onValueChange = viewModel::onCompanyNameChange,
                    label = { Text("Company Name (Optional)") },
                    placeholder = { Text("e.g. Acme Technologies") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputFieldColors(),
                    shape = RoundedCornerShape(14.dp),
                )

                // Email
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Outlined.Email, null, tint = PrimaryVioletLight) },
                    isError = uiState.emailError != null,
                    supportingText = uiState.emailError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputFieldColors(),
                    shape = RoundedCornerShape(14.dp),
                )

                // Phone
                OutlinedTextField(
                    value = uiState.phone,
                    onValueChange = viewModel::onPhoneChange,
                    label = { Text("Phone") },
                    leadingIcon = { Icon(Icons.Outlined.Phone, null, tint = PrimaryVioletLight) },
                    isError = uiState.phoneError != null,
                    supportingText = uiState.phoneError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputFieldColors(),
                    shape = RoundedCornerShape(14.dp),
                )

                // Telegram & WhatsApp Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.telegram,
                        onValueChange = viewModel::onTelegramChange,
                        label = { Text("Telegram") },
                        placeholder = { Text("@handle") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = inputFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                    )
                    OutlinedTextField(
                        value = uiState.whatsapp,
                        onValueChange = viewModel::onWhatsappChange,
                        label = { Text("WhatsApp") },
                        placeholder = { Text("+91...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = inputFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                    )
                }

                // Currency & Country Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.currency,
                        onValueChange = viewModel::onCurrencyChange,
                        label = { Text("Currency") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = inputFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                    )
                    OutlinedTextField(
                        value = uiState.country,
                        onValueChange = viewModel::onCountryChange,
                        label = { Text("Country") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = inputFieldColors(),
                        shape = RoundedCornerShape(14.dp),
                    )
                }

                // Notes
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::onNotesChange,
                    label = { Text("Notes (Preferences, payment habits)") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputFieldColors(),
                    shape = RoundedCornerShape(14.dp),
                )

                Spacer(modifier = Modifier.height(10.dp))

                LiquidGlassButton(
                    onClick = { viewModel.saveClient(onClientAdded) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                    } else {
                        Text("Save Client", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun inputFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryViolet,
    unfocusedBorderColor = Color(0x24FFFFFF),
    focusedLabelColor = PrimaryVioletBright,
    unfocusedLabelColor = TextSecondary,
    cursorColor = PrimaryVioletBright,
    focusedContainerColor = Color(0x18FFFFFF),
    unfocusedContainerColor = Color(0x0CFFFFFF),
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    errorBorderColor = StatusDanger,
    errorLabelColor = StatusDangerBright
)
