package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AppThemePreset(
    val id: String,
    val title: String,
    val subtitle: String,
    val primaryLight: Color,
    val primaryContainerLight: Color,
    val onPrimaryContainerLight: Color,
    val secondaryLight: Color,
    val secondaryContainerLight: Color,
    val primaryDark: Color,
    val primaryContainerDark: Color,
    val onPrimaryContainerDark: Color,
    val secondaryDark: Color
) {
    WECHAT_GREEN(
        id = "wechat_green",
        title = "微信经典绿",
        subtitle = "小程序与生态亲和风格，温润护眼",
        primaryLight = Color(0xFF07C160),
        primaryContainerLight = Color(0xFFE8F8EE),
        onPrimaryContainerLight = Color(0xFF024B24),
        secondaryLight = Color(0xFF10B981),
        secondaryContainerLight = Color(0xFFD1FAE5),
        primaryDark = Color(0xFF34D399),
        primaryContainerDark = Color(0xFF064E3B),
        onPrimaryContainerDark = Color(0xFFA7F3D0),
        secondaryDark = Color(0xFF6EE7B7)
    ),
    MODERN_INDIGO(
        id = "modern_indigo",
        title = "极简黛蓝",
        subtitle = "现代高质感科技色调，专注高效",
        primaryLight = Color(0xFF4F46E5),
        primaryContainerLight = Color(0xFFEEF2FF),
        onPrimaryContainerLight = Color(0xFF1E1B4B),
        secondaryLight = Color(0xFF6366F1),
        secondaryContainerLight = Color(0xFFE0E7FF),
        primaryDark = Color(0xFF818CF8),
        primaryContainerDark = Color(0xFF312E81),
        onPrimaryContainerDark = Color(0xFFC7D2FE),
        secondaryDark = Color(0xFFA5B4FC)
    ),
    ROYAL_PURPLE(
        id = "royal_purple",
        title = "典雅紫晶",
        subtitle = "高密度收纳质感，层次分明",
        primaryLight = Color(0xFF6750A4),
        primaryContainerLight = Color(0xFFEADDFF),
        onPrimaryContainerLight = Color(0xFF21005D),
        secondaryLight = Color(0xFF625B71),
        secondaryContainerLight = Color(0xFFE8DEF8),
        primaryDark = Color(0xFFD0BCFF),
        primaryContainerDark = Color(0xFF4F378B),
        onPrimaryContainerDark = Color(0xFFEADDFF),
        secondaryDark = Color(0xFFCCC2DC)
    ),
    WARM_AMBER(
        id = "warm_amber",
        title = "琥珀暖橙",
        subtitle = "居家温馨暖色调，活力收纳",
        primaryLight = Color(0xFFD97706),
        primaryContainerLight = Color(0xFFFEF3C7),
        onPrimaryContainerLight = Color(0xFF451A03),
        secondaryLight = Color(0xFFF59E0B),
        secondaryContainerLight = Color(0xFFFDE68A),
        primaryDark = Color(0xFFFBBF24),
        primaryContainerDark = Color(0xFF78350F),
        onPrimaryContainerDark = Color(0xFFFEF3C7),
        secondaryDark = Color(0xFFFCD34D)
    ),
    DEEP_OCEAN(
        id = "deep_ocean",
        title = "深海湛蓝",
        subtitle = "沉静商务风，清爽利落",
        primaryLight = Color(0xFF0284C7),
        primaryContainerLight = Color(0xFFE0F2FE),
        onPrimaryContainerLight = Color(0xFF082F49),
        secondaryLight = Color(0xFF0EA5E9),
        secondaryContainerLight = Color(0xFFBAE6FD),
        primaryDark = Color(0xFF38BDF8),
        primaryContainerDark = Color(0xFF075985),
        onPrimaryContainerDark = Color(0xFFE0F2FE),
        secondaryDark = Color(0xFF7DD3FC)
    ),
    EMERALD_FOREST(
        id = "emerald_forest",
        title = "翡翠森林",
        subtitle = "自然生机绿意，宁静舒心",
        primaryLight = Color(0xFF059669),
        primaryContainerLight = Color(0xFFD1FAE5),
        onPrimaryContainerLight = Color(0xFF064E3B),
        secondaryLight = Color(0xFF10B981),
        secondaryContainerLight = Color(0xFFA7F3D0),
        primaryDark = Color(0xFF34D399),
        primaryContainerDark = Color(0xFF064E3B),
        onPrimaryContainerDark = Color(0xFFD1FAE5),
        secondaryDark = Color(0xFF6EE7B7)
    ),
    TITANIUM_SLATE(
        id = "titanium_slate",
        title = "钛金冷灰",
        subtitle = "克制极简工业风，低调沉稳",
        primaryLight = Color(0xFF475569),
        primaryContainerLight = Color(0xFFF1F5F9),
        onPrimaryContainerLight = Color(0xFF0F172A),
        secondaryLight = Color(0xFF64748B),
        secondaryContainerLight = Color(0xFFE2E8F0),
        primaryDark = Color(0xFF94A3B8),
        primaryContainerDark = Color(0xFF334155),
        onPrimaryContainerDark = Color(0xFFF1F5F9),
        secondaryDark = Color(0xFFCBD5E1)
    ),
    CRIMSON_ROSE(
        id = "crimson_rose",
        title = "绯红热情",
        subtitle = "精致高贵色泽，醒目悦目",
        primaryLight = Color(0xFFE11D48),
        primaryContainerLight = Color(0xFFFFE4E6),
        onPrimaryContainerLight = Color(0xFF4C0519),
        secondaryLight = Color(0xFFF43F5E),
        secondaryContainerLight = Color(0xFFFECDD3),
        primaryDark = Color(0xFFFB7185),
        primaryContainerDark = Color(0xFF881337),
        onPrimaryContainerDark = Color(0xFFFFE4E6),
        secondaryDark = Color(0xFFFDA4AF)
    );

    fun buildColorScheme(darkTheme: Boolean): ColorScheme {
        return if (darkTheme) {
            darkColorScheme(
                primary = primaryDark,
                onPrimary = Color(0xFF0F172A),
                primaryContainer = primaryContainerDark,
                onPrimaryContainer = onPrimaryContainerDark,
                secondary = secondaryDark,
                onSecondary = Color(0xFF0F172A),
                secondaryContainer = Color(0xFF1E293B),
                onSecondaryContainer = Color(0xFFE2E8F0),
                tertiary = AlertAmber,
                background = BackgroundDark,
                surface = SurfaceDark,
                onBackground = Color(0xFFF1F5F9),
                onSurface = Color(0xFFF1F5F9),
                surfaceVariant = Color(0xFF1E293B),
                onSurfaceVariant = Color(0xFF94A3B8),
                outline = CardBorderDark
            )
        } else {
            lightColorScheme(
                primary = primaryLight,
                onPrimary = Color.White,
                primaryContainer = primaryContainerLight,
                onPrimaryContainer = onPrimaryContainerLight,
                secondary = secondaryLight,
                onSecondary = Color.White,
                secondaryContainer = secondaryContainerLight,
                onSecondaryContainer = onPrimaryContainerLight,
                tertiary = AlertAmber,
                background = BackgroundLight,
                surface = SurfaceLight,
                onBackground = Color(0xFF1C1B1F),
                onSurface = Color(0xFF1C1B1F),
                surfaceVariant = Color(0xFFF8FAFC),
                onSurfaceVariant = Color(0xFF49454F),
                outline = Color(0xFFE2E8F0),
                outlineVariant = Color(0xFFF1F5F9)
            )
        }
    }
}

@Composable
fun StorageProTheme(
    preset: AppThemePreset = AppThemePreset.ROYAL_PURPLE,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> preset.buildColorScheme(darkTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
