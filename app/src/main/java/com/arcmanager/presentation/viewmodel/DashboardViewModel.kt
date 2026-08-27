package com.arcmanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcmanager.core.util.Result
import com.arcmanager.domain.model.User
import com.arcmanager.domain.repository.AuthRepository
import com.arcmanager.domain.usecase.DashboardOverview
import com.arcmanager.domain.usecase.GetDashboardOverviewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val overview: DashboardOverview = DashboardOverview(),
    val selectedTimeFilter: String = "30 Days",
    val error: String? = null,
) {
    val greeting: String
        get() {
            val hour = LocalTime.now().hour
            return when {
                hour < 12 -> "Good morning"
                hour < 17 -> "Good afternoon"
                else -> "Good evening"
            }
        }
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardOverviewUseCase: GetDashboardOverviewUseCase,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            // Load user profile
            val userResult = authRepository.getCurrentUser()
            if (userResult is Result.Success) {
                _uiState.update { it.copy(user = userResult.data) }
            }

            // Load financial metrics
            getDashboardOverviewUseCase().collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Result.Success -> _uiState.update {
                        it.copy(isLoading = false, overview = result.data, error = null)
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }

    fun onTimeFilterSelected(filter: String) {
        _uiState.update { it.copy(selectedTimeFilter = filter) }
    }
}
