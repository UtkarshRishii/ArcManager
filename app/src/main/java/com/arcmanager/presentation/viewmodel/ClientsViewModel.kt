package com.arcmanager.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arcmanager.core.util.Result
import com.arcmanager.core.util.ValidationUtils
import com.arcmanager.domain.model.Client
import com.arcmanager.domain.repository.ClientRepository
import com.arcmanager.domain.usecase.GetClientsWithCalculationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClientsUiState(
    val isLoading: Boolean = true,
    val clients: List<Client> = emptyList(),
    val filteredClients: List<Client> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: String = "All", // All, Active, Paid, Pending, Overdue, Archived
    val error: String? = null,
)

data class AddClientUiState(
    val name: String = "",
    val companyName: String = "",
    val email: String = "",
    val phone: String = "",
    val telegram: String = "",
    val whatsapp: String = "",
    val country: String = "India",
    val currency: String = "INR",
    val notes: String = "",
    val tags: List<String> = emptyList(),
    val tagInput: String = "",
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class ClientsViewModel @Inject constructor(
    private val getClientsWithCalculationsUseCase: GetClientsWithCalculationsUseCase,
    private val clientRepository: ClientRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientsUiState())
    val uiState: StateFlow<ClientsUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadClients()
    }

    fun loadClients() {
        viewModelScope.launch {
            getClientsWithCalculationsUseCase().collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is Result.Success -> {
                        _uiState.update { state ->
                            state.copy(
                                isLoading = false,
                                clients = result.data,
                                filteredClients = applyFilters(result.data, state.searchQuery, state.selectedFilter),
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
            delay(300) // Debounce search
            _uiState.update { state ->
                state.copy(filteredClients = applyFilters(state.clients, query, state.selectedFilter))
            }
        }
    }

    fun onFilterSelected(filter: String) {
        _uiState.update { state ->
            state.copy(
                selectedFilter = filter,
                filteredClients = applyFilters(state.clients, state.searchQuery, filter)
            )
        }
    }

    private fun applyFilters(clients: List<Client>, query: String, filter: String): List<Client> {
        return clients.filter { client ->
            val matchesQuery = query.isBlank() ||
                client.name.contains(query, ignoreCase = true) ||
                (client.companyName?.contains(query, ignoreCase = true) == true) ||
                (client.email?.contains(query, ignoreCase = true) == true) ||
                (client.phone?.contains(query, ignoreCase = true) == true) ||
                client.tags.any { it.contains(query, ignoreCase = true) }

            val matchesFilter = when (filter) {
                "All" -> client.status != "archived"
                "Active" -> client.status == "active"
                "Paid" -> client.displayStatus == "FULLY PAID"
                "Pending" -> client.totalPending > java.math.BigDecimal.ZERO
                "Archived" -> client.status == "archived"
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }
}
