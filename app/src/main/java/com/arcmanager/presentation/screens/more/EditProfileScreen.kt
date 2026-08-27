package com.arcmanager.presentation.screens.more

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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Email
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.arcmanager.core.util.Constants
import com.arcmanager.core.util.Result
import com.arcmanager.domain.model.User
import com.arcmanager.domain.repository.AuthRepository
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
import javax.inject.Inject

data class EditProfileUiState(
    val fullName: String = "",
    val email: String = "",
    val defaultCurrency: String = "INR",
    val timezone: String = "Asia/Kolkata",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private var currentUser: User? = null

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            when (val result = authRepository.getCurrentUser()) {
                is Result.Success -> {
                    val user = result.data
                    currentUser = user
                    if (user != null) {
                        _uiState.update {
                            it.copy(
                                fullName = user.fullName ?: "",
                                email = user.email ?: "",
                                defaultCurrency = user.defaultCurrency,
                                timezone = user.timezone
                            )
                        }
                    }
                }
                is Result.Error -> _uiState.update { it.copy(error = result.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun onFullNameChange(name: String) = _uiState.update { it.copy(fullName = name) }
    fun onCurrencyChange(currency: String) = _uiState.update { it.copy(defaultCurrency = currency) }
    fun onTimezoneChange(tz: String) = _uiState.update { it.copy(timezone = tz) }

    fun saveProfile(onSuccess: () -> Unit) {
        val user = currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val updatedUser = user.copy(
                fullName = _uiState.value.fullName.trim().ifEmpty { null },
                defaultCurrency = _uiState.value.defaultCurrency,
                timezone = _uiState.value.timezone
            )
            when (val result = authRepository.updateProfile(updatedUser)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
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
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel(),
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
                            text = "Account Profile",
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
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

                // Full Name
                OutlinedTextField(
                    value = uiState.fullName,
                    onValueChange = viewModel::onFullNameChange,
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Outlined.Person, null, tint = PrimaryVioletLight) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputFieldColors(),
                    shape = RoundedCornerShape(14.dp)
                )

                // Email (Read-only)
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Registered Email") },
                    leadingIcon = { Icon(Icons.Outlined.Email, null, tint = TextTertiary) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputFieldColors(),
                    shape = RoundedCornerShape(14.dp)
                )

                // Default Currency Selection
                Text("Default Currency", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                ArcFilterChips(
                    items = Constants.SUPPORTED_CURRENCIES,
                    selectedItem = uiState.defaultCurrency,
                    onItemSelected = viewModel::onCurrencyChange,
                    labelProvider = { "$it (${Constants.CURRENCY_SYMBOLS[it] ?: ""})" }
                )

                // Timezone
                OutlinedTextField(
                    value = uiState.timezone,
                    onValueChange = viewModel::onTimezoneChange,
                    label = { Text("Timezone") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = inputFieldColors(),
                    shape = RoundedCornerShape(14.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                LiquidGlassButton(
                    onClick = { viewModel.saveProfile(onNavigateBack) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.5.dp)
                    } else {
                        Text("Save Profile", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}
