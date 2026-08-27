package com.arcmanager.presentation.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.arcmanager.core.util.CurrencyUtils
import com.arcmanager.core.util.DateUtils
import com.arcmanager.domain.model.Payment
import com.arcmanager.domain.model.PaymentSchedule
import com.arcmanager.presentation.components.*
import com.arcmanager.presentation.navigation.Screen
import com.arcmanager.presentation.theme.*
import com.arcmanager.presentation.viewmodel.DashboardViewModel
import java.math.BigDecimal

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val overview = uiState.overview
    val currency = uiState.user?.defaultCurrency ?: "INR"

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    if (uiState.isLoading && overview.totalReceived == BigDecimal.ZERO) {
        LoadingState(message = "Calculating financial overview...")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.ScreenPadding,
            end = Dimens.ScreenPadding,
            top = 20.dp,
            bottom = 110.dp // Clear the floating liquid dock comfortably
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── 1. Greeting Header with Frosted Glass Profile Avatar ──
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${uiState.greeting}, ${uiState.user?.fullName ?: "User"}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Your financial overview",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                // Liquid Frosted Avatar (Clickable to Edit Profile)
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(8.dp, CircleShape, spotColor = PrimaryViolet)
                        .clip(CircleShape)
                        .background(PrimaryVioletSubtle)
                        .border(
                            width = 1.5.dp,
                            brush = Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.6f), PrimaryVioletLight.copy(alpha = 0.3f))
                            ),
                            shape = CircleShape
                        )
                        .clickable { navController.navigate(Screen.Profile.route) },
                    contentAlignment = Alignment.Center
                ) {
                    val initial = uiState.user?.fullName?.take(1)?.uppercase() ?: "U"
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryVioletBright
                    )
                }
            }
        }

        // ── 2. Overdue Warning Banner (If any) ──
        if (overview.totalOverdue > BigDecimal.ZERO) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(18.dp), spotColor = StatusDanger)
                        .clip(RoundedCornerShape(18.dp))
                        .background(StatusDangerSubtle)
                        .border(
                            width = 1.dp,
                            brush = Brush.horizontalGradient(
                                listOf(StatusDanger.copy(alpha = 0.7f), StatusDangerSubtle, Color.Transparent)
                            ),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(StatusDanger.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.WarningAmber,
                                contentDescription = "Overdue warning",
                                tint = StatusDangerBright,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "${CurrencyUtils.formatAmount(overview.totalOverdue, currency)} OVERDUE",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = StatusDangerBright
                            )
                            Text(
                                text = "${overview.overduePayments.size} payment schedule(s) overdue",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // ── 3. Main Financial Metrics 2x2 Grid with Liquid Glass ──
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FinancialCard(
                        title = "Total Received",
                        amount = overview.totalReceived,
                        currencyCode = currency,
                        highlightColor = StatusSuccessBright,
                        modifier = Modifier.weight(1f)
                    )
                    FinancialCard(
                        title = "Pending",
                        amount = overview.totalPending,
                        currencyCode = currency,
                        highlightColor = StatusWarningBright,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FinancialCard(
                        title = "Expected This Month",
                        amount = overview.expectedThisMonth,
                        currencyCode = currency,
                        highlightColor = SecondaryBlueLight,
                        modifier = Modifier.weight(1f)
                    )
                    FinancialCard(
                        title = "Received This Month",
                        amount = overview.receivedThisMonth,
                        currencyCode = currency,
                        highlightColor = PrimaryVioletBright,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ── 4. Quick Stats Frosted Pill Capsule ──
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x16161628))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color(0x28FFFFFF), Color(0x08FFFFFF))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(vertical = 16.dp, horizontal = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    QuickStatItem(
                        label = "Active Clients",
                        value = overview.activeClientsCount.toString(),
                        icon = Icons.Outlined.People
                    )
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0x18FFFFFF)))
                    QuickStatItem(
                        label = "Projects",
                        value = overview.activeProjectsCount.toString(),
                        icon = Icons.Outlined.FolderOpen
                    )
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color(0x18FFFFFF)))
                    QuickStatItem(
                        label = "Overdue",
                        value = overview.overduePayments.size.toString(),
                        icon = Icons.Outlined.Schedule,
                        valueColor = if (overview.overduePayments.isNotEmpty()) StatusDangerBright else TextPrimary
                    )
                }
            }
        }

        // ── 5. Time Filter Selector ──
        item {
            val filters = listOf("7 Days", "30 Days", "3 Months", "6 Months", "1 Year", "All Time")
            ArcFilterChips(
                items = filters,
                selectedItem = uiState.selectedTimeFilter,
                onItemSelected = viewModel::onTimeFilterSelected,
                labelProvider = { it }
            )
        }

        // ── 6. Upcoming Payments Section ──
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "UPCOMING PAYMENTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    letterSpacing = 1.2.sp
                )
            }
        }

        if (overview.upcomingPayments.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0x0CFFFFFF))
                        .border(1.dp, Color(0x12FFFFFF), RoundedCornerShape(18.dp))
                        .padding(22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No upcoming payments scheduled",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
        } else {
            items(overview.upcomingPayments) { schedule ->
                UpcomingPaymentLiquidCard(
                    schedule = schedule,
                    currencyCode = currency,
                    onClick = {
                        navController.navigate(
                            Screen.AddPayment.createRoute(schedule.clientId, schedule.projectId)
                        )
                    }
                )
            }
        }

        // ── 7. Recent Transactions Section ──
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT TRANSACTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "View all",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryVioletBright,
                    modifier = Modifier.clickable { navController.navigate(Screen.Payments.route) }
                )
            }
        }

        if (overview.recentTransactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0x0CFFFFFF))
                        .border(1.dp, Color(0x12FFFFFF), RoundedCornerShape(18.dp))
                        .padding(22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions recorded yet",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }
        } else {
            items(overview.recentTransactions) { payment ->
                RecentTransactionLiquidCard(
                    payment = payment,
                    currencyCode = currency,
                    onClick = { navController.navigate(Screen.PaymentDetail.createRoute(payment.id)) }
                )
            }
        }
    }
}

@Composable
private fun QuickStatItem(
    label: String,
    value: String,
    icon: ImageVector,
    valueColor: Color = TextPrimary,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextTertiary)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = valueColor
        )
    }
}

@Composable
private fun UpcomingPaymentLiquidCard(
    schedule: PaymentSchedule,
    currencyCode: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .scale(if (isPressed) 0.97f else 1f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0x1818182A))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color(0x33FFFFFF), Color(0x0AFFFFFF))
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
            Column {
                Text(
                    text = schedule.title ?: "Payment Due",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = DateUtils.formatDueText(schedule.dueDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (schedule.isOverdue) StatusDangerBright else PrimaryVioletLight
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyUtils.formatAmount(schedule.remainingAmount, currencyCode),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                StatusBadge(status = schedule.effectiveStatus)
            }
        }
    }
}

@Composable
private fun RecentTransactionLiquidCard(
    payment: Payment,
    currencyCode: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = Modifier
            .scale(if (isPressed) 0.97f else 1f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0x1818182A))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(Color(0x33FFFFFF), Color(0x0AFFFFFF))
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(StatusSuccessSubtle)
                        .border(1.dp, StatusSuccess.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ArrowDownward,
                        contentDescription = "Received",
                        tint = StatusSuccessBright,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = payment.paymentType.displayName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                    Text(
                        text = DateUtils.formatRelativeDateTime(payment.paymentDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+ ${CurrencyUtils.formatAmount(payment.amount, currencyCode)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = StatusSuccessBright
                )
                Text(
                    text = payment.paymentMethod.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        }
    }
}
