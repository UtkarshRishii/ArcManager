package com.arcmanager.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.arcmanager.presentation.theme.*

// ──────────────────────────────────────────────
// 1. Liquid Glass Modifier
// ──────────────────────────────────────────────
fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(18.dp),
    backgroundColor: Color = GlassSurfaceLight,
    borderBrush: Brush = LiquidGlassCardBorder,
    borderWidth: Dp = 1.dp,
): Modifier = this
    .clip(shape)
    .background(backgroundColor, shape)
    .border(borderWidth, borderBrush, shape)

// ──────────────────────────────────────────────
// 2. Liquid Glass Card Composable
// ──────────────────────────────────────────────
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    backgroundColor: Color = Color(0x1A1E1E2E),
    borderBrush: Brush = LiquidGlassCardBorder,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "card_press_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(backgroundColor, shape)
            .border(borderWidth, borderBrush, shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(Dimens.CardPadding)
    ) {
        Column(content = content)
    }
}

// ──────────────────────────────────────────────
// 3. Ambient Liquid Mesh Background
// ──────────────────────────────────────────────
@Composable
fun LiquidMeshBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    // Subtle animated ambient pulsing for living liquid depth
    val infiniteTransition = rememberInfiniteTransition(label = "mesh_pulse")
    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_1"
    )
    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 1.15f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(7500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_2"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Ambient Mesh Orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Top-left Electric Violet glow orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x358B5CF6), Color(0x158B5CF6), Color.Transparent),
                    center = Offset(width * 0.15f, height * 0.12f),
                    radius = width * 0.75f * pulse1
                ),
                center = Offset(width * 0.15f, height * 0.12f),
                radius = width * 0.75f * pulse1
            )

            // Right-center Cyan/Indigo glow orb
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x2206B6D4), Color(0x0E6366F1), Color.Transparent),
                    center = Offset(width * 0.88f, height * 0.45f),
                    radius = width * 0.65f * pulse2
                ),
                center = Offset(width * 0.88f, height * 0.45f),
                radius = width * 0.65f * pulse2
            )

            // Bottom-left deep purple ambient puddle
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x207C3AED), Color(0x087C3AED), Color.Transparent),
                    center = Offset(width * 0.3f, height * 0.85f),
                    radius = width * 0.7f * pulse1
                ),
                center = Offset(width * 0.3f, height * 0.85f),
                radius = width * 0.7f * pulse1
            )
        }

        content()
    }
}

// ──────────────────────────────────────────────
// 4. Liquid Glass Button
// ──────────────────────────────────────────────
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradientColors: List<Color> = GradientLiquidViolet,
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "btn_scale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .height(Dimens.ButtonHeight)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = if (enabled) Brush.horizontalGradient(gradientColors) else Brush.horizontalGradient(listOf(DarkSurfaceElevated, DarkSurface)),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                brush = if (enabled) Brush.verticalGradient(listOf(Color(0x80FFFFFF), Color(0x20FFFFFF), Color.Transparent)) else Brush.verticalGradient(listOf(BorderSubtle, BorderSubtle)),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}
