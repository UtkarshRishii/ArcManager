package com.arcmanager.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ──────────────────────────────────────────────
// Liquid Glass & Material 3 Dark Palette
// Ultra-premium, translucent, glowing fintech
// ──────────────────────────────────────────────

// Deep Ambient Backgrounds
val DarkBackground = Color(0xFF07070B)
val DarkBackgroundMesh1 = Color(0xFF0F0B1E)
val DarkBackgroundMesh2 = Color(0xFF090D1A)
val DarkBackgroundMesh3 = Color(0xFF14081E)

// Liquid Glass Translucent Surfaces (Frosted Acrylic)
val GlassSurfaceUltraLight = Color(0x24FFFFFF) // 14% white for highlights
val GlassSurfaceLight = Color(0x18FFFFFF)      // 9% white for cards
val GlassSurfaceDefault = Color(0x0EFFFFFF)    // 5.5% white default glass
val GlassSurfaceElevated = Color(0x15FFFFFF)   // 8% white
val GlassSurfaceDark = Color(0xCC0E0E17)       // 80% dark for dense backdrops
val GlassSurfaceDock = Color(0xB310101C)       // 70% dark for floating dock

// Glass Border Gradients (Gives specular edge shine)
val GlassBorderTop = Color(0x55FFFFFF)
val GlassBorderMiddle = Color(0x1AFFFFFF)
val GlassBorderBottom = Color(0x08FFFFFF)
val GlassBorderHighlight = Color(0x808B5CF6)   // Glowing violet border

// Accent Colors — Electric Violet, Neon Cyan, Liquid Indigo
val PrimaryViolet = Color(0xFF8B5CF6)
val PrimaryVioletLight = Color(0xFFA78BFA)
val PrimaryVioletBright = Color(0xFFC4B5FD)
val PrimaryVioletDark = Color(0xFF6D28D9)
val PrimaryVioletSubtle = Color(0x288B5CF6)    // Glowing violet aura

val SecondaryBlue = Color(0xFF6366F1)
val SecondaryBlueLight = Color(0xFF818CF8)
val SecondaryCyan = Color(0xFF06B6D4)
val SecondaryCyanGlow = Color(0x3306B6D4)

// Liquid Status Colors (Luminous & Restrained)
val StatusSuccess = Color(0xFF10B981)
val StatusSuccessBright = Color(0xFF34D399)
val StatusSuccessSubtle = Color(0x2610B981)
val StatusSuccessGlow = Color(0x4010B981)

val StatusWarning = Color(0xFFF59E0B)
val StatusWarningBright = Color(0xFFFBBF24)
val StatusWarningSubtle = Color(0x26F59E0B)

val StatusDanger = Color(0xFFEF4444)
val StatusDangerBright = Color(0xFFF87171)
val StatusDangerSubtle = Color(0x26EF4444)

val StatusInfo = Color(0xFF3B82F6)
val StatusInfoSubtle = Color(0x263B82F6)

// Text Colors with high readability
val TextPrimary = Color(0xFFF9FAFB)
val TextSecondary = Color(0xFF9CA3AF)
val TextTertiary = Color(0xFF6B7280)
val TextOnPrimary = Color(0xFFFFFFFF)
val TextMuted = Color(0xFF4B5563)

// Solid Fallback Surfaces
val DarkSurface = Color(0xFF11111A)
val DarkSurfaceElevated = Color(0xFF171725)
val DarkSurfaceCard = Color(0xFF1A1A28)
val BorderDefault = Color(0xFF28283C)
val BorderSubtle = Color(0xFF1E1E2E)

// ──────────────────────────────────────────────
// Liquid Glass Specular & Glow Brushes
// ──────────────────────────────────────────────
val LiquidGlassCardBorder = Brush.verticalGradient(
    colors = listOf(
        Color(0x4DFFFFFF),
        Color(0x1AFFFFFF),
        Color(0x0AFFFFFF),
        Color(0x268B5CF6)
    )
)

val LiquidGlassDockBorder = Brush.horizontalGradient(
    colors = listOf(
        Color(0x338B5CF6),
        Color(0x66FFFFFF),
        Color(0x336366F1),
        Color(0x66FFFFFF),
        Color(0x338B5CF6)
    )
)

val LiquidVioletGlow = Brush.radialGradient(
    colors = listOf(
        Color(0x558B5CF6),
        Color(0x208B5CF6),
        Color.Transparent
    )
)

val LiquidCyanGlow = Brush.radialGradient(
    colors = listOf(
        Color(0x4006B6D4),
        Color(0x1506B6D4),
        Color.Transparent
    )
)

val GradientLiquidViolet = listOf(Color(0xFF9061F9), Color(0xFF6366F1), Color(0xFF4F46E5))
val GradientLiquidSuccess = listOf(Color(0xFF34D399), Color(0xFF10B981), Color(0xFF059669))
val GradientLiquidWarning = listOf(Color(0xFFFBBF24), Color(0xFFF59E0B), Color(0xFFD97706))
val GradientLiquidDanger = listOf(Color(0xFFF87171), Color(0xFFEF4444), Color(0xFFDC2626))
