package com.arcmanager.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.arcmanager.core.util.CurrencyUtils
import com.arcmanager.presentation.theme.*
import java.math.BigDecimal

// ──────────────────────────────────────────────
// 1. Liquid Glass Search Bar
// ──────────────────────────────────────────────
@Composable
fun ArcSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search clients, projects, payments...",
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector = Icons.Outlined.Search,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = TextTertiary
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = "Search",
                tint = if (query.isNotEmpty()) PrimaryVioletLight else TextTertiary,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryViolet,
            unfocusedBorderColor = Color(0x22FFFFFF),
            cursorColor = PrimaryViolet,
            focusedContainerColor = Color(0x18FFFFFF),
            unfocusedContainerColor = Color(0x0CFFFFFF),
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
    )
}

// ──────────────────────────────────────────────
// 2. Liquid Glass Filter Chips
// ──────────────────────────────────────────────
@Composable
fun <T> ArcFilterChips(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    labelProvider: (T) -> String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            val isSelected = item == selectedItem
            val label = labelProvider(item)

            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.92f else 1f,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "chip_scale"
            )

            Box(
                modifier = Modifier
                    .scale(scale)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        color = if (isSelected) PrimaryViolet.copy(alpha = 0.22f) else Color(0x0EFFFFFF),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        brush = if (isSelected) Brush.verticalGradient(
                            listOf(PrimaryVioletLight.copy(alpha = 0.8f), PrimaryVioletDark.copy(alpha = 0.4f))
                        ) else Brush.verticalGradient(
                            listOf(Color(0x20FFFFFF), Color(0x08FFFFFF))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onItemSelected(item) }
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) PrimaryVioletBright else TextSecondary
                )
            }
        }
    }
}

// ──────────────────────────────────────────────
// 3. Liquid Glass Status Badges
// ──────────────────────────────────────────────
@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, borderColor, textColor, text) = when (status.lowercase()) {
        "received", "paid", "fully paid", "completed" -> Quadruple(StatusSuccessSubtle, StatusSuccess.copy(alpha = 0.4f), StatusSuccessBright, status.uppercase())
        "partially paid", "partially_paid" -> Quadruple(StatusInfoSubtle, StatusInfo.copy(alpha = 0.4f), SecondaryBlueLight, "PARTIALLY PAID")
        "pending", "active" -> Quadruple(PrimaryVioletSubtle, PrimaryViolet.copy(alpha = 0.4f), PrimaryVioletBright, status.uppercase())
        "overdue" -> Quadruple(StatusDangerSubtle, StatusDanger.copy(alpha = 0.5f), StatusDangerBright, "OVERDUE")
        "cancelled", "failed", "on_hold" -> Quadruple(Color(0x15FFFFFF), Color(0x20FFFFFF), TextTertiary, status.uppercase().replace("_", " "))
        else -> Quadruple(Color(0x15FFFFFF), Color(0x20FFFFFF), TextSecondary, status.uppercase())
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = textColor
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

// ──────────────────────────────────────────────
// 4. Liquid Glass Financial Summary Card
// ──────────────────────────────────────────────
@Composable
fun FinancialCard(
    title: String,
    amount: BigDecimal,
    currencyCode: String = "INR",
    subtitle: String? = null,
    trendText: String? = null,
    isPositiveTrend: Boolean = true,
    highlightColor: Color = PrimaryVioletLight,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "fin_card_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = highlightColor.copy(alpha = 0.12f),
                ambientColor = Color.Black
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x18181828))
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        highlightColor.copy(alpha = 0.4f),
                        Color(0x18FFFFFF),
                        Color(0x06FFFFFF)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(18.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = TextTertiary,
                    letterSpacing = 1.2.sp
                )
                if (trendText != null) {
                    Text(
                        text = trendText,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPositiveTrend) StatusSuccess else StatusDanger
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = CurrencyUtils.formatAmount(amount, currencyCode),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = highlightColor
            )

            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}

// ──────────────────────────────────────────────
// 5. Liquid Glass Empty State
// ──────────────────────────────────────────────
@Composable
fun EmptyState(
    title: String,
    description: String,
    icon: ImageVector,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0x10FFFFFF))
            .border(1.dp, Color(0x18FFFFFF), RoundedCornerShape(24.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(PrimaryVioletSubtle)
                .border(1.dp, PrimaryViolet.copy(alpha = 0.4f), RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryVioletBright,
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(20.dp))
            LiquidGlassButton(
                onClick = onActionClick,
                gradientColors = GradientLiquidViolet
            ) {
                Text(actionLabel, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
            }
        }
    }
}

// ──────────────────────────────────────────────
// 6. Loading State with Liquid Shimmer
// ──────────────────────────────────────────────
@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
    message: String = "Loading...",
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = PrimaryVioletBright,
                strokeWidth = 3.dp,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(Dimens.SpacingMedium))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

// ──────────────────────────────────────────────
// 7. Error State
// ──────────────────────────────────────────────
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(StatusDangerSubtle)
            .border(1.dp, StatusDanger.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(Dimens.ScreenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Something went wrong",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = StatusDangerBright
        )
        Spacer(modifier = Modifier.height(Dimens.SpacingSmall))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
        OutlinedButton(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryVioletBright)
        ) {
            Text("Retry")
        }
    }
}

// ──────────────────────────────────────────────
// 8. Frosted Liquid Confirmation Dialog
// ──────────────────────────────────────────────
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TextPrimary) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium, color = TextSecondary) },
        containerColor = Color(0xF0141422),
        shape = RoundedCornerShape(24.dp),
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) StatusDanger else PrimaryViolet,
                    contentColor = TextOnPrimary
                )
            ) {
                Text(confirmText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText, color = TextSecondary)
            }
        }
    )
}
