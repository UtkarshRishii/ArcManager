package com.arcmanager.presentation.screens.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
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
import com.arcmanager.domain.model.Payment
import com.arcmanager.domain.repository.PaymentRepository
import com.arcmanager.presentation.components.ConfirmDialog
import com.arcmanager.presentation.components.LoadingState
import com.arcmanager.presentation.components.StatusBadge
import com.arcmanager.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PaymentDetailUiState(
    val isLoading: Boolean = true,
    val payment: Payment? = null,
    val error: String? = null,
    val showDeleteConfirm: Boolean = false,
)

@HiltViewModel
class PaymentDetailViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val paymentId: String = checkNotNull(savedStateHandle["paymentId"])
    private val _uiState = MutableStateFlow(PaymentDetailUiState())
    val uiState: StateFlow<PaymentDetailUiState> = _uiState.asStateFlow()

    init {
        loadPayment()
    }

    private fun loadPayment() {
        viewModelScope.launch {
            when (val result = paymentRepository.getPaymentById(paymentId)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, payment = result.data) }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                is Result.Loading -> {}
            }
        }
    }

    fun setDeleteConfirm(show: Boolean) = _uiState.update { it.copy(showDeleteConfirm = show) }

    fun deletePayment(onDeleted: () -> Unit) {
        viewModelScope.launch {
            when (paymentRepository.deletePayment(paymentId)) {
                is Result.Success -> onDeleted()
                is Result.Error -> _uiState.update { it.copy(error = "Failed to delete payment") }
                is Result.Loading -> {}
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDetailScreen(
    paymentId: String,
    onNavigateBack: () -> Unit,
    viewModel: PaymentDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val payment = uiState.payment

    if (uiState.isLoading || payment == null) {
        LoadingState(message = "Loading payment...")
        return
    }

    if (uiState.showDeleteConfirm) {
        ConfirmDialog(
            title = "Delete Payment Record",
            message = "Are you sure you want to delete this payment of ${CurrencyUtils.formatAmount(payment.amount, payment.currency)}? This will recalculate all pending balances.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = { viewModel.deletePayment(onNavigateBack) },
            onDismiss = { viewModel.setDeleteConfirm(false) }
        )
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = { Text("Payment Details", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary) },
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amount Banner Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "+ ${CurrencyUtils.formatAmount(payment.amount, payment.currency)}",
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                        color = StatusSuccess
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    StatusBadge(status = payment.status)
                }
            }

            // Transaction Details Breakdown Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkSurfaceCard)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(16.dp))
                    .padding(Dimens.CardPadding)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("TRANSACTION DETAILS", style = MaterialTheme.typography.labelSmall, color = TextTertiary, letterSpacing = 1.sp)
                    HorizontalDivider(color = BorderSubtle)

                    DetailRow("Payment Type", payment.paymentType.displayName)
                    DetailRow("Payment Method", payment.paymentMethod.displayName)
                    DetailRow("Date & Time", DateUtils.formatRelativeDateTime(payment.paymentDate))
                    if (!payment.transactionReference.isNullOrBlank()) {
                        DetailRow("Reference ID", payment.transactionReference)
                    }
                    if (!payment.notes.isNullOrBlank()) {
                        DetailRow("Notes", payment.notes)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = TextPrimary)
    }
}
