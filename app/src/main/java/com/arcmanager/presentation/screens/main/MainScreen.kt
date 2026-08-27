package com.arcmanager.presentation.screens.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AddCard
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.arcmanager.presentation.components.LiquidMeshBackground
import com.arcmanager.presentation.navigation.BottomNavBar
import com.arcmanager.presentation.navigation.Screen
import com.arcmanager.presentation.screens.clients.ClientsScreen
import com.arcmanager.presentation.screens.dashboard.DashboardScreen
import com.arcmanager.presentation.screens.more.MoreScreen
import com.arcmanager.presentation.screens.payments.PaymentsScreen
import com.arcmanager.presentation.theme.*

@Composable
fun MainScreen(
    navController: NavController,
    currentRoute: String,
) {
    var showQuickAdd by remember { mutableStateOf(false) }

    LiquidMeshBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            // Content Screen Container
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                when (currentRoute) {
                    Screen.Dashboard.route -> DashboardScreen(navController = navController)
                    Screen.Clients.route -> ClientsScreen(
                        onNavigateToAddClient = { navController.navigate(Screen.AddClient.route) },
                        onNavigateToClientDetail = { clientId ->
                            navController.navigate(Screen.ClientDetail.createRoute(clientId))
                        }
                    )
                    Screen.Payments.route -> PaymentsScreen(
                        onNavigateToAddPayment = { navController.navigate(Screen.AddPayment.createRoute()) },
                        onNavigateToPaymentDetail = { paymentId ->
                            navController.navigate(Screen.PaymentDetail.createRoute(paymentId))
                        }
                    )
                    Screen.More.route -> MoreScreen(navController = navController)
                }
            }

            // Suspended Floating Liquid Glass Dock
            BottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    if (route != currentRoute) {
                        navController.navigate(route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onFabClick = { showQuickAdd = !showQuickAdd },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // Frosted Quick Add Overlay
            AnimatedVisibility(
                visible = showQuickAdd,
                enter = fadeIn(tween(250)) + slideInVertically(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                    initialOffsetY = { it / 2 }
                ),
                exit = fadeOut(tween(200)) + slideOutVertically(
                    animationSpec = tween(200),
                    targetOffsetY = { it / 2 }
                ),
            ) {
                QuickAddLiquidOverlay(
                    onDismiss = { showQuickAdd = false },
                    onAddPayment = {
                        showQuickAdd = false
                        navController.navigate(Screen.AddPayment.createRoute())
                    },
                    onAddClient = {
                        showQuickAdd = false
                        navController.navigate(Screen.AddClient.route)
                    },
                    onAddProject = {
                        showQuickAdd = false
                        navController.navigate(Screen.CreateProject.createRoute())
                    },
                    onAddBankAccount = {
                        showQuickAdd = false
                        navController.navigate(Screen.AddBankAccount.route)
                    }
                )
            }
        }
    }
}

@Composable
private fun QuickAddLiquidOverlay(
    onDismiss: () -> Unit,
    onAddPayment: () -> Unit,
    onAddClient: () -> Unit,
    onAddProject: () -> Unit,
    onAddBankAccount: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC07070B))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.ScreenPadding)
                .padding(bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Dismiss handle / close button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GlassSurfaceLight)
                        .border(1.dp, Color(0x33FFFFFF), CircleShape)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            QuickAddLiquidButton(
                icon = Icons.Outlined.AddCard,
                title = "Record Payment",
                subtitle = "Log money received from a client",
                iconColor = StatusSuccess,
                glowColor = StatusSuccessSubtle,
                onClick = onAddPayment
            )
            QuickAddLiquidButton(
                icon = Icons.Outlined.PersonAdd,
                title = "Add Client",
                subtitle = "Register a new client or company",
                iconColor = PrimaryVioletLight,
                glowColor = PrimaryVioletSubtle,
                onClick = onAddClient
            )
            QuickAddLiquidButton(
                icon = Icons.Outlined.FolderOpen,
                title = "Create Project",
                subtitle = "Set up milestones & payment plans",
                iconColor = SecondaryBlueLight,
                glowColor = StatusInfoSubtle,
                onClick = onAddProject
            )
            QuickAddLiquidButton(
                icon = Icons.Outlined.AccountBalance,
                title = "Track Bank Account",
                subtitle = "Add a personal receiving account",
                iconColor = StatusWarningBright,
                glowColor = StatusWarningSubtle,
                onClick = onAddBankAccount
            )
        }
    }
}

@Composable
private fun QuickAddLiquidButton(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    glowColor: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp), spotColor = iconColor.copy(alpha = 0.2f))
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xE6151524))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        iconColor.copy(alpha = 0.5f),
                        Color(0x20FFFFFF),
                        Color(0x0AFFFFFF)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(glowColor)
                    .border(1.dp, iconColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
