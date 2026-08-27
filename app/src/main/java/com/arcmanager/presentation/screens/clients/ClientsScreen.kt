package com.arcmanager.presentation.screens.clients

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
import androidx.compose.material.icons.outlined.PersonAdd
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arcmanager.core.util.CurrencyUtils
import com.arcmanager.domain.model.Client
import com.arcmanager.presentation.components.*
import com.arcmanager.presentation.theme.*
import com.arcmanager.presentation.viewmodel.ClientsViewModel

@Composable
fun ClientsScreen(
    onNavigateToAddClient: () -> Unit,
    onNavigateToClientDetail: (String) -> Unit,
    viewModel: ClientsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.loadClients()
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
                text = "Clients",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            LiquidGlassButton(
                onClick = onNavigateToAddClient,
                modifier = Modifier.height(40.dp)
            ) {
                Text("+ Add Client", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Liquid Glass Search Bar
        ArcSearchBar(
            query = uiState.searchQuery,
            onQueryChange = viewModel::onSearchQueryChange,
            placeholder = "Search by name, company, email..."
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips
        val filterOptions = listOf("All", "Active", "Paid", "Pending", "Archived")
        ArcFilterChips(
            items = filterOptions,
            selectedItem = uiState.selectedFilter,
            onItemSelected = viewModel::onFilterSelected,
            labelProvider = { it }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Content
        when {
            uiState.isLoading && uiState.clients.isEmpty() -> {
                LoadingState(message = "Loading clients...")
            }
            uiState.filteredClients.isEmpty() -> {
                EmptyState(
                    title = if (uiState.searchQuery.isNotEmpty()) "No matching clients" else "No clients yet",
                    description = if (uiState.searchQuery.isNotEmpty()) "Try adjusting your search terms" else "Add your first client to start tracking projects and payments",
                    icon = Icons.Outlined.PersonAdd,
                    actionLabel = if (uiState.searchQuery.isEmpty()) "+ Add Client" else null,
                    onActionClick = if (uiState.searchQuery.isEmpty()) onNavigateToAddClient else null
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 110.dp) // Floating dock clearance
                ) {
                    items(uiState.filteredClients, key = { it.id }) { client ->
                        ClientLiquidCard(
                            client = client,
                            onClick = { onNavigateToClientDetail(client.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientLiquidCard(
    client: Client,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .scale(if (isPressed) 0.97f else 1f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x1A1C1C2E))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color(0x35FFFFFF), Color(0x0CFFFFFF))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = client.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    if (!client.companyName.isNullOrBlank()) {
                        Text(
                            text = client.companyName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                StatusBadge(status = client.displayStatus)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Frosted Financial Summary Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x14FFFFFF))
                    .border(1.dp, Color(0x10FFFFFF), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FinancialMetricMini(label = "Billed", amount = client.totalBilled, currency = client.currency)
                FinancialMetricMini(label = "Received", amount = client.totalReceived, currency = client.currency, color = StatusSuccessBright)
                FinancialMetricMini(label = "Pending", amount = client.totalPending, currency = client.currency, color = StatusWarningBright)
            }

            // Progress bar
            if (client.totalBilled > java.math.BigDecimal.ZERO) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { (client.paymentPercentage / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = PrimaryVioletBright,
                        trackColor = Color(0x1EFFFFFF),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "${client.paymentPercentage}%",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryVioletBright
                    )
                }
            }
        }
    }
}
