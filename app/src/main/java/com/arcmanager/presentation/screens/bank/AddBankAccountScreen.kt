package com.arcmanager.presentation.screens.bank

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.arcmanager.domain.repository.BankAccountRepository
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
import javax.inject.Inject

data class AddBankAccountUiState(
    val accountName: String = "",
    val bankName: String = "",
    val accountHolderName: String = "",
    val accountNumber: String = "",
    val ifsc: String = "",
    val currency: String = "INR",
    val accountNameError: String? = null,
    val bankNameError: String? = null,
    val accountNumberError: String? = null,
    val ifscError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AddBankAccountViewModel @Inject constructor(
    private val bankAccountRepository: BankAccountRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddBankAccountUiState())
    val uiState: StateFlow<AddBankAccountUiState> = _uiState.asStateFlow()

    fun onAccountNameChange(name: String) = _uiState.update { it.copy(accountName = name, accountNameError = null) }
    fun onBankNameChange(name: String) = _uiState.update { it.copy(bankName = name, bankNameError = null) }
    fun onHolderNameChange(name: String) = _uiState.update { it.copy(accountHolderName = name) }
    fun onAccountNumberChange(num: String) = _uiState.update { it.copy(accountNumber = num, accountNumberError = null) }
    fun onIfscChange(ifsc: String) = _uiState.update { it.copy(ifsc = ifsc.uppercase(), ifscError = null) }
    fun onCurrencyChange(currency: String) = _uiState.update { it.copy(currency = currency) }

    fun saveBankAccount(onSuccess: () -> Unit) {
        val state = _uiState.value
        val nameErr = ValidationUtils.validateBankAccountName(state.accountName)
        val bankErr = ValidationUtils.validateBankName(state.bankName)
        val numErr = ValidationUtils.validateAccountNumber(state.accountNumber)
        val ifscErr = ValidationUtils.validateIFSC(state.ifsc)

        if (nameErr != null || bankErr != null || numErr != null || ifscErr != null) {
            _uiState.update {
                it.copy(
                    accountNameError = nameErr,
                    bankNameError = bankErr,
                    accountNumberError = numErr,
                    ifscError = ifscErr
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = bankAccountRepository.addBankAccount(
                accountName = state.accountName.trim(),
                bankName = state.bankName.trim(),
                accountHolderName = state.accountHolderName.trim().ifEmpty { null },
                accountNumber = state.accountNumber.trim().ifEmpty { null },
                ifsc = state.ifsc.trim().ifEmpty { null },
                currency = state.currency.trim().ifEmpty { "INR" }
            )

            when (result) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true) }
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
fun AddBankAccountScreen(
    onNavigateBack: () -> Unit,
    onAccountAdded: () -> Unit,
    viewModel: AddBankAccountViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    LiquidMeshBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Add Bank Account",
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(StatusDangerSubtle)
                            .border(1.dp, StatusDanger.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .padding(Dimens.CardPadding)
                    ) {
                        Text(uiState.error ?: "", style = MaterialTheme.typography.bodyMedium, color = StatusDangerBright)
                    }
                }

                // Account Name / Nickname *
                OutlinedTextField(
                    value = uiState.accountName,
                    onValueChange = viewModel::onAccountNameChange,
                    label = { Text("Account Label / Nickname *") },
                    placeholder = { Text("e.g. HDFC Salary / ICICI Primary") },
                    leadingIcon = { Icon(Icons.Outlined.AccountBalance, null, tint = PrimaryVioletLight) },
                    isError = uiState.accountNameError != null,
                    supportingText = uiState.accountNameError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputFieldColors(),
                    shape = RoundedCornerShape(14.dp)
                )

                // Bank Name *
                OutlinedTextField(
                    value = uiState.bankName,
                    onValueChange = viewModel::onBankNameChange,
                    label = { Text("Bank Name *") },
                    placeholder = { Text("e.g. HDFC Bank / State Bank of India") },
                    isError = uiState.bankNameError != null,
                    supportingText = uiState.bankNameError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputFieldColors(),
                    shape = RoundedCornerShape(14.dp)
                )

                // Account Holder Name
                OutlinedTextField(
                    value = uiState.accountHolderName,
                    onValueChange = viewModel::onHolderNameChange,
                    label = { Text("Account Holder Name (Optional)") },
                    placeholder = { Text("e.g. Alex Smith") },
                    leadingIcon = { Icon(Icons.Outlined.Person, null, tint = PrimaryVioletLight) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputFieldColors(),
                    shape = RoundedCornerShape(14.dp)
                )

                // Account Number
                OutlinedTextField(
                    value = uiState.accountNumber,
                    onValueChange = viewModel::onAccountNumberChange,
                    label = { Text("Account Number (Optional - Encrypted with AES)") },
                    placeholder = { Text("e.g. 5010049281726") },
                    leadingIcon = { Icon(Icons.Outlined.CreditCard, null, tint = PrimaryVioletLight) },
                    isError = uiState.accountNumberError != null,
                    supportingText = uiState.accountNumberError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputFieldColors(),
                    shape = RoundedCornerShape(14.dp)
                )

                // IFSC Code & Currency Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.ifsc,
                        onValueChange = viewModel::onIfscChange,
                        label = { Text("IFSC Code") },
                        placeholder = { Text("HDFC0001234") },
                        isError = uiState.ifscError != null,
                        supportingText = uiState.ifscError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        singleLine = true,
                        modifier = Modifier.weight(1.2f),
                        colors = inputFieldColors(),
                        shape = RoundedCornerShape(14.dp)
                    )
                    OutlinedTextField(
                        value = uiState.currency,
                        onValueChange = viewModel::onCurrencyChange,
                        label = { Text("Currency") },
                        singleLine = true,
                        modifier = Modifier.weight(0.8f),
                        colors = inputFieldColors(),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                LiquidGlassButton(
                    onClick = { viewModel.saveBankAccount(onAccountAdded) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                    } else {
                        Text("Save Bank Account", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
