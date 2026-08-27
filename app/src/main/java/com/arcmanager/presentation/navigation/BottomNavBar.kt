package com.arcmanager.presentation.navigation

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.arcmanager.presentation.theme.*

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
)

val bottomNavItems = listOf(
    BottomNavItem("Home", Icons.Outlined.Home, Screen.Dashboard.route),
    BottomNavItem("Clients", Icons.Outlined.People, Screen.Clients.route),
    BottomNavItem("Payments", Icons.Outlined.Payments, Screen.Payments.route),
    BottomNavItem("More", Icons.Outlined.Menu, Screen.More.route),
)

// ──────────────────────────────────────────────
// Floating Liquid Glass Dock
// ──────────────────────────────────────────────
@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onFabClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Floating Glass Dock Capsule
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(26.dp),
                    spotColor = PrimaryViolet.copy(alpha = 0.35f),
                    ambientColor = Color.Black
                )
                .clip(RoundedCornerShape(26.dp))
                .background(GlassSurfaceDock)
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x66FFFFFF),
                            Color(0x1AFFFFFF),
                            Color(0x338B5CF6)
                        )
                    ),
                    shape = RoundedCornerShape(26.dp)
                )
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Left 2 Navigation Items
                bottomNavItems.take(2).forEach { item ->
                    LiquidDockItemView(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = { onNavigate(item.route) }
                    )
                }

                // Central Floating Action Button spacer & anchor
                LiquidDockCenterFab(onClick = onFabClick)

                // Right 2 Navigation Items
                bottomNavItems.drop(2).forEach { item ->
                    LiquidDockItemView(
                        item = item,
                        isSelected = currentRoute == item.route,
                        onClick = { onNavigate(item.route) }
                    )
                }
            }
        }
    }
}

@Composable
private fun LiquidDockItemView(
    item: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.88f
            isSelected -> 1.06f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "dock_item_scale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) PrimaryVioletBright else TextTertiary,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "dock_icon_color"
    )

    val pillAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "dock_pill_alpha"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                color = if (isSelected) PrimaryViolet.copy(alpha = 0.18f * pillAlpha) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                brush = if (isSelected) Brush.verticalGradient(
                    listOf(PrimaryVioletLight.copy(alpha = 0.5f * pillAlpha), Color.Transparent)
                ) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = iconColor,
                letterSpacing = 0.3.sp
            )
        }
    }
}

@Composable
private fun LiquidDockCenterFab(
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "fab_scale"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .size(48.dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                spotColor = PrimaryViolet,
                ambientColor = PrimaryViolet
            )
            .clip(CircleShape)
            .background(Brush.radialGradient(GradientLiquidViolet))
            .border(
                width = 1.5.dp,
                brush = Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.8f), Color.Transparent)
                ),
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Quick Add",
            tint = Color.White,
            modifier = Modifier.size(26.dp)
        )
    }
}
