package com.arcmanager.presentation.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arcmanager.core.util.CurrencyUtils
import com.arcmanager.presentation.components.FinancialCard
import com.arcmanager.presentation.components.LiquidMeshBackground
import com.arcmanager.presentation.components.LoadingState
import com.arcmanager.presentation.theme.*
import com.arcmanager.presentation.viewmodel.DashboardViewModel
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val overview = uiState.overview
    val currency = uiState.user?.defaultCurrency ?: "INR"

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    LiquidMeshBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Analytics & Cash Flow",
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
            if (uiState.isLoading && overview.totalReceived == BigDecimal.ZERO) {
                LoadingState(message = "Computing analytics...")
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = Dimens.ScreenPadding)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Summary Financial Cards
                    FinancialCard(
                        title = "Total Collected",
                        amount = overview.totalReceived,
                        currencyCode = currency,
                        highlightColor = StatusSuccessBright
                    )

                    FinancialCard(
                        title = "Total Pending Pipeline",
                        amount = overview.totalPending,
                        currencyCode = currency,
                        highlightColor = StatusWarningBright
                    )

                    FinancialCard(
                        title = "Received This Month",
                        amount = overview.receivedThisMonth,
                        currencyCode = currency,
                        highlightColor = PrimaryVioletBright
                    )

                    // Collection Efficiency Ratio
                    val totalBilled = overview.totalReceived.add(overview.totalPending)
                    val collectionPercentage = if (totalBilled > BigDecimal.ZERO) {
                        overview.totalReceived.multiply(BigDecimal(100)).divide(totalBilled, 1, java.math.RoundingMode.HALF_UP).toFloat()
                    } else 0f

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = PrimaryViolet.copy(alpha = 0.2f))
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x1C1C1C2E))
                            .border(1.dp, Brush.verticalGradient(listOf(Color(0x35FFFFFF), Color(0x0CFFFFFF))), RoundedCornerShape(20.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "COLLECTION EFFICIENCY",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = TextTertiary,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "$collectionPercentage%",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryVioletBright
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { (collectionPercentage / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = PrimaryVioletBright,
                                trackColor = DarkSurfaceElevated
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
