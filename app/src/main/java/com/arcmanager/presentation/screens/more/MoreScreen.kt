package com.arcmanager.presentation.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.arcmanager.domain.model.User
import com.arcmanager.presentation.components.ConfirmDialog
import com.arcmanager.presentation.navigation.Screen
import com.arcmanager.presentation.theme.*
import com.arcmanager.presentation.viewmodel.AuthViewModel

@Composable
fun MoreScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val authState by authViewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showSecurityInfo by remember { mutableStateOf(false) }
    var showRetainersInfo by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        authViewModel.checkSession()
    }

    if (showLogoutConfirm) {
        ConfirmDialog(
            title = "Sign Out",
            message = "Are you sure you want to sign out of ArcManager?",
            confirmText = "Sign Out",
            isDestructive = true,
            onConfirm = {
                showLogoutConfirm = false
                authViewModel.logout()
                navController.navigate(Screen.Login.route) {
                    popUpTo(Screen.Dashboard.route) { inclusive = true }
                }
            },
            onDismiss = { showLogoutConfirm = false }
        )
    }

    if (showSecurityInfo) {
        AlertDialog(
            onDismissRequest = { showSecurityInfo = false },
            title = { Text("Security & Data Protection", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary) },
            text = {
                Text(
                    "• Bank account numbers and IFSC details are encrypted using Android Keystore AES-GCM.\n• All database rows are strictly scoped by Supabase Row-Level Security.\n• Only the last 4 digits of accounts are displayed on screen.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSecurityInfo = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet)
                ) {
                    Text("OK", color = Color.White)
                }
            },
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showRetainersInfo) {
        AlertDialog(
            onDismissRequest = { showRetainersInfo = false },
            title = { Text("Recurring Retainers", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary) },
            text = {
                Text(
                    "You can create monthly recurring projects when creating a new project. Select 'Monthly Recurring' under Payment Plan Model to generate recurring milestone billing.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRetainersInfo = false
                        navController.navigate(Screen.CreateProject.createRoute())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryViolet)
                ) {
                    Text("+ Create Recurring Project", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRetainersInfo = false }) {
                    Text("Close", color = TextSecondary)
                }
            },
            containerColor = DarkSurfaceElevated,
            shape = RoundedCornerShape(20.dp)
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = Dimens.ScreenPadding,
            end = Dimens.ScreenPadding,
            top = 20.dp,
            bottom = 110.dp // Floating dock clearance
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        item {
            Text(
                text = "Account & Tools",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }

        // Profile Card with Liquid Glass Aura (Clickable)
        item {
            UserProfileLiquidCard(
                user = authState.user,
                onClick = { navController.navigate(Screen.Profile.route) }
            )
        }

        // Hub Navigation Items Capsule
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0x1818182A))
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color(0x35FFFFFF), Color(0x0CFFFFFF))
                        ),
                        shape = RoundedCornerShape(22.dp)
                    )
            ) {
                Column {
                    MoreMenuItem(
                        icon = Icons.Outlined.AccountBalance,
                        title = "Bank Accounts",
                        subtitle = "Track personal bank receiving accounts",
                        onClick = { navController.navigate(Screen.BankAccounts.route) }
                    )
                    HorizontalDivider(color = Color(0x12FFFFFF), modifier = Modifier.padding(horizontal = 16.dp))
                    MoreMenuItem(
                        icon = Icons.Outlined.Repeat,
                        title = "Recurring Retainers",
                        subtitle = "Monthly and periodic recurring billing",
                        onClick = { showRetainersInfo = true }
                    )
                    HorizontalDivider(color = Color(0x12FFFFFF), modifier = Modifier.padding(horizontal = 16.dp))
                    MoreMenuItem(
                        icon = Icons.Outlined.BarChart,
                        title = "Analytics & Cash Flow",
                        subtitle = "Financial breakdown & collection efficiency",
                        onClick = { navController.navigate(Screen.Analytics.route) }
                    )
                    HorizontalDivider(color = Color(0x12FFFFFF), modifier = Modifier.padding(horizontal = 16.dp))
                    MoreMenuItem(
                        icon = Icons.Outlined.CalendarMonth,
                        title = "Payment Calendar",
                        subtitle = "Monthly due dates & milestone schedule",
                        onClick = { navController.navigate(Screen.Calendar.route) }
                    )
                    HorizontalDivider(color = Color(0x12FFFFFF), modifier = Modifier.padding(horizontal = 16.dp))
                    MoreMenuItem(
                        icon = Icons.Outlined.Security,
                        title = "Security & Encryption",
                        subtitle = "Keystore AES-GCM & Row-Level Security",
                        onClick = { showSecurityInfo = true }
                    )
                }
            }
        }

        // Sign out Liquid Button
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(StatusDangerSubtle)
                    .border(1.dp, StatusDanger.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                    .clickable { showLogoutConfirm = true }
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = "Sign Out",
                            tint = StatusDangerBright,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Sign Out",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = StatusDangerBright
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserProfileLiquidCard(
    user: User?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(22.dp), spotColor = PrimaryViolet.copy(alpha = 0.2f))
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0x221A1A2E))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    listOf(PrimaryVioletLight.copy(alpha = 0.5f), Color(0x10FFFFFF))
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(PrimaryVioletSubtle)
                        .border(2.dp, PrimaryVioletBright, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val initial = user?.fullName?.take(1)?.uppercase() ?: "U"
                    Text(
                        text = initial,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryVioletBright
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column {
                    Text(
                        text = user?.fullName ?: "User Profile",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = user?.email ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Currency: ${user?.defaultCurrency ?: "INR"} • Tap to Edit",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = PrimaryVioletBright
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
                contentDescription = "Edit Profile",
                tint = TextTertiary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun MoreMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x18FFFFFF))
                    .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryVioletBright,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Outlined.ArrowForwardIos,
            contentDescription = "Navigate",
            tint = TextTertiary,
            modifier = Modifier.size(14.dp)
        )
    }
}
