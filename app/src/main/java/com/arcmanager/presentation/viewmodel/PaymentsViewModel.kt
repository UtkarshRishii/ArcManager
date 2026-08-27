package com.arcmanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcmanager.core.util.Result
import com.arcmanager.domain.model.Payment
import com.arcmanager.domain.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentsUiState(
    val isLoading: Boolean = true,
    val payments: List<Payment> = emptyList(),
    val filteredPayments: List<Payment> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: String = "All", // All, Advance, Milestone, Final, Monthly
    val error: String? = null,
)

@HiltViewModel
class PaymentsViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentsUiState())
    val uiState: StateFlow<PaymentsUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadPayments()
    }

    fun loadPayments() {
        viewModelScope.launch {
            paymentRepository.getAllPayments().collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Result.Success -> {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                payments = result.data,
                                filteredPayments = applyFilters(result.data, state.searchQuery, state.selectedFilter),
                                error = null
                            )
                        }
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            _uiState.update { state ->
                state.copy(filteredPayments = applyFilters(state.payments, query, state.selectedFilter))
            }
        }
    }

    fun onFilterSelected(filter: String) {
        _uiState.update { state ->
            state.copy(
                selectedFilter = filter,
                filteredPayments = applyFilters(state.payments, state.searchQuery, filter)
            )
        }
    }

    private fun applyFilters(payments: List<Payment>, query: String, filter: String): List<Payment> {
        return payments.filter { payment ->
            val matchesQuery = query.isBlank() ||
                payment.paymentType.displayName.contains(query, ignoreCase = true) ||
                payment.paymentMethod.displayName.contains(query, ignoreCase = true) ||
                (payment.transactionReference?.contains(query, ignoreCase = true) == true) ||
                (payment.notes?.contains(query, ignoreCase = true) == true)

            val matchesFilter = when (filter) {
                "All" -> true
                "Advance" -> payment.paymentType.value == "ADVANCE"
                "Milestone" -> payment.paymentType.value == "MILESTONE"
                "Final" -> payment.paymentType.value == "FINAL"
                "Monthly" -> payment.paymentType.value == "MONTHLY"
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }
}
