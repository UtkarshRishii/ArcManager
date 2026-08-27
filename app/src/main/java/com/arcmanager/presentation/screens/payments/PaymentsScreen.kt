package com.arcmanager.presentation.screens.payments

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arcmanager.core.util.CurrencyUtils
import com.arcmanager.core.util.DateUtils
import com.arcmanager.domain.model.Payment
import com.arcmanager.presentation.components.*
import com.arcmanager.presentation.theme.*
import com.arcmanager.presentation.viewmodel.PaymentsViewModel

@Composable
fun PaymentsScreen(
    onNavigateToAddPayment: () -> Unit,
    onNavigateToPaymentDetail: (String) -> Unit,
    viewModel: PaymentsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadPayments()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Dimens.ScreenPadding)
            .padding(top = 20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Payments Ledger",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            LiquidGlassButton(
                onClick = onNavigateToAddPayment,
                modifier = Modifier.height(40.dp),
                gradientColors = GradientLiquidSuccess
            ) {
                Text("+ Record", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        ArcSearchBar(
            query = uiState.searchQuery,
            onQueryChange = viewModel::onSearchQueryChange,
            placeholder = "Search payments, references, notes..."
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips
        val filterOptions = listOf("All", "Advance", "Milestone", "Final", "Monthly")
        ArcFilterChips(
            items = filterOptions,
            selectedItem = uiState.selectedFilter,
            onItemSelected = viewModel::onFilterSelected,
            labelProvider = { it }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Content
        when {
            uiState.isLoading && uiState.payments.isEmpty() -> {
                LoadingState(message = "Loading transactions...")
            }
            uiState.filteredPayments.isEmpty() -> {
                EmptyState(
                    title = if (uiState.searchQuery.isNotEmpty()) "No matching payments" else "No payments yet",
                    description = if (uiState.searchQuery.isNotEmpty()) "Try adjusting your search query" else "Record your first received client payment to track your income",
                    icon = Icons.Outlined.Payments,
                    actionLabel = if (uiState.searchQuery.isEmpty()) "+ Record Payment" else null,
                    onActionClick = if (uiState.searchQuery.isEmpty()) onNavigateToAddPayment else null
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 110.dp) // Floating dock clearance
                ) {
                    items(uiState.filteredPayments, key = { it.id }) { payment ->
                        PaymentLedgerLiquidCard(
                            payment = payment,
                            onClick = { onNavigateToPaymentDetail(payment.id) },
                            onToggleReceived = { isRec ->
                                viewModel.togglePaymentReceived(payment.id, isRec)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentLedgerLiquidCard(
    payment: Payment,
    onClick: () -> Unit,
    onToggleReceived: (Boolean) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .scale(if (isPressed) 0.98f else 1f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0x1818182A))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color(0x35FFFFFF), Color(0x0CFFFFFF))
                ),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                PaymentReceivedTickButton(
                    isReceived = payment.status == "received",
                    onToggle = onToggleReceived
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = payment.paymentType.displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Text(
                        text = "${payment.paymentMethod.displayName} • ${DateUtils.formatRelativeDateTime(payment.paymentDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (payment.status == "received") "+ " else "") + CurrencyUtils.formatAmount(payment.amount, payment.currency),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (payment.status == "received") StatusSuccessBright else StatusWarningBright
                )
                if (!payment.transactionReference.isNullOrBlank()) {
                    Text(
                        text = "Ref: ${payment.transactionReference}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
        }
    }
}
