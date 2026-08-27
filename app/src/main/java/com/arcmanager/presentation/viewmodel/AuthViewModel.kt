package com.arcmanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcmanager.core.util.Result
import com.arcmanager.core.util.ValidationUtils
import com.arcmanager.domain.model.User
import com.arcmanager.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val isCheckingSession: Boolean = true,
    val resetEmailSent: Boolean = false,
    // Form fields
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val nameError: String? = null,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkSession()
    }

    fun checkSession() {
        viewModelScope.launch {
            val loggedIn = authRepository.isLoggedIn()
            if (loggedIn) {
                when (val result = authRepository.getCurrentUser()) {
                    is Result.Success -> _uiState.update {
                        it.copy(isLoggedIn = true, user = result.data, isCheckingSession = false)
                    }
                    else -> _uiState.update { it.copy(isCheckingSession = false) }
                }
            } else {
                _uiState.update { it.copy(isCheckingSession = false) }
            }
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null, error = null) }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null, error = null) }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(fullName = name, nameError = null, error = null) }
    }

    fun login() {
        val state = _uiState.value
        val emailError = ValidationUtils.validateLoginEmail(state.email)
        val passwordError = ValidationUtils.validatePassword(state.password)

        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.login(state.email, state.password)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, isLoggedIn = true, user = result.data)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    fun register() {
        val state = _uiState.value
        val nameError = if (state.fullName.isBlank()) "Name is required" else null
        val emailError = ValidationUtils.validateLoginEmail(state.email)
        val passwordError = ValidationUtils.validatePassword(state.password)

        if (nameError != null || emailError != null || passwordError != null) {
            _uiState.update { it.copy(nameError = nameError, emailError = emailError, passwordError = passwordError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = authRepository.register(state.email, state.password, state.fullName)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, isLoggedIn = true, user = result.data)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    fun forgotPassword() {
        val state = _uiState.value
        val emailError = ValidationUtils.validateLoginEmail(state.email)
        if (emailError != null) {
            _uiState.update { it.copy(emailError = emailError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (authRepository.forgotPassword(state.email)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, resetEmailSent = true)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = "Failed to send reset email")
                }
                is Result.Loading -> {}
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { AuthUiState(isCheckingSession = false) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
