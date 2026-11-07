package frb.axeron.manager.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

val GREEN = Color(0xFF4CAF50)             // Green
val RED = Color(0xFFF44336)               // Red
val YELLOW = Color(0xFFFFEB3B)            // Yellow
val ORANGE = Color(0xFFFF9800)            // Orange

fun Color.blend(other: Color, ratio: Float): Color {
    val inv = 1f - ratio
    return Color(
        red = red * inv + other.red * ratio,
        green = green * inv + other.green * ratio,
        blue = blue * inv + other.blue * ratio,
        alpha = alpha
    )
}


fun Color.saturate(factor: Float): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(this.toArgb(), hsl)

    hsl[1] = (hsl[1] * factor).coerceIn(0f, 10f)

    return Color(ColorUtils.HSLToColor(hsl))
}

fun Color.adjust(
    hueDelta: Float = 0f,
    satMul: Float = 1f,
    lightMul: Float = 1f
): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(this.toArgb(), hsl)
    hsl[0] = (hsl[0] + hueDelta) % 360f
    hsl[1] = (hsl[1] * satMul).coerceIn(0f, 1f)
    hsl[2] = (hsl[2] * lightMul).coerceIn(0f, 1f)
    return Color(ColorUtils.HSLToColor(hsl))
}


fun Color.toHexString(includeAlpha: Boolean = true): String {
    val argb = toArgb()
    return if (includeAlpha) {
        String.format("#%08X", argb)
    } else {
        String.format("#%06X", argb and 0xFFFFFF)
    }
}


val AMOLED_BLACK: Color = Color(0xFF000000)

/**
 * KernelSU / MiuiX 风格：默认强调色是系统蓝，而不是原来的珊瑚粉。
 * 用户仍可在「外观」里自定义，这里的值只是默认种子。
 */
val basePrimaryDefault = Color(0xFF2196F3)

// Flat MiuiX surfaces. Cards are pure white on a very light grey page; the page itself is
// the "background", cards are "surfaceContainerHigh". No tonal tinting anywhere.
private val baseError = Color(0xFFE5484D)

private val lightPage = Color(0xFFF5F5F5)
private val lightCard = Color(0xFFFFFFFF)
private val lightInk = Color(0xFF1A1A1A)
private val lightInkVariant = Color(0xFF8A8A8A)
private val lightLine = Color(0xFFE6E6E6)
private val lightSearchField = Color(0xFFEDEDED)

private val darkPage = Color(0xFF121212)
private val darkCard = Color(0xFF202020)
private val darkInk = Color(0xFFEDEDED)
private val darkInkVariant = Color(0xFF9A9A9A)
private val darkLine = Color(0xFF2E2E2E)
private val darkSearchField = Color(0xFF2A2A2A)

/**
 * Flat light scheme. The primary container doubles as the "working" status tint on Home,
 * which is the pale green/blue block KernelSU shows for a healthy state.
 */
fun getVortexLightColorScheme(
    customColor: Color = basePrimaryDefault,
): ColorScheme {
    val primary = customColor
    val onPrimary = Color.White
    val primaryContainer = primary.blend(Color.White, 0.88f)
    val onPrimaryContainer = primary.blend(Color.Black, 0.55f)
    val secondary = primary.blend(Color.White, 0.35f)
    val tertiary = primary.adjust(hueDelta = 30f, satMul = 0.8f)
    val errorContainer = baseError.blend(Color.White, 0.88f)

    return lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        inversePrimary = primary.blend(Color.White, 0.40f),

        secondary = secondary,
        onSecondary = Color.White,
        secondaryContainer = secondary.blend(Color.White, 0.75f),
        onSecondaryContainer = Color(0xFF1A1A1A),

        tertiary = tertiary,
        onTertiary = Color.White,
        tertiaryContainer = tertiary.blend(Color.White, 0.85f),
        onTertiaryContainer = Color(0xFF1A1A1A),

        background = lightPage,
        onBackground = lightInk,

        surface = lightPage,
        onSurface = lightInk,

        surfaceVariant = lightSearchField,
        onSurfaceVariant = lightInkVariant,

        surfaceTint = primary,

        inverseSurface = Color(0xFF2A2A2A),
        inverseOnSurface = Color(0xFFF5F5F5),

        error = baseError,
        onError = Color.White,
        errorContainer = errorContainer,
        onErrorContainer = baseError.blend(Color.Black, 0.45f),

        outline = Color(0xFFD9D9D9),
        outlineVariant = lightLine,

        scrim = Color(0xFF000000),

        // Cards sit on surfaceContainerHigh == pure white; the page is flat grey.
        surfaceBright = lightCard,
        surfaceDim = lightPage,
        surfaceContainerLowest = lightCard,
        surfaceContainerLow = lightCard,
        surfaceContainer = lightPage,
        surfaceContainerHigh = lightCard,
        surfaceContainerHighest = lightSearchField,

        primaryFixed = primary,
        primaryFixedDim = primary.blend(Color.Black, 0.20f),
        onPrimaryFixed = Color.White,
        onPrimaryFixedVariant = Color.White.copy(alpha = 0.8f),

        secondaryFixed = secondary,
        secondaryFixedDim = secondary.blend(Color.Black, 0.20f),
        onSecondaryFixed = Color.White,
        onSecondaryFixedVariant = Color.White.copy(alpha = 0.7f),

        tertiaryFixed = tertiary,
        tertiaryFixedDim = tertiary.blend(Color.Black, 0.20f),
        onTertiaryFixed = Color.White,
        onTertiaryFixedVariant = Color.White.copy(alpha = 0.7f)
    )
}

/** Flat dark scheme: cards are a slightly lifted charcoal, never tinted by the accent. */
fun getVortexDarkColorScheme(
    customColor: Color = basePrimaryDefault,
): ColorScheme {
    val primary = customColor.blend(Color.White, 0.35f)
    val onPrimary = Color(0xFF06213A)
    val primaryContainer = primary.blend(darkPage, 0.78f)
    val onPrimaryContainer = primary.blend(Color.White, 0.85f)
    val secondary = primary.blend(darkPage, 0.35f)
    val tertiary = primary.adjust(hueDelta = 30f, satMul = 0.8f).blend(Color.White, 0.15f)
    val errorContainer = baseError.blend(darkPage, 0.35f)

    return darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        inversePrimary = customColor,

        secondary = secondary,
        onSecondary = Color(0xFF111111),
        secondaryContainer = secondary.blend(darkCard, 0.70f),
        onSecondaryContainer = darkInk,

        tertiary = tertiary,
        onTertiary = Color(0xFF111111),
        tertiaryContainer = tertiary.blend(darkCard, 0.70f),
        onTertiaryContainer = darkInk,

        background = darkPage,
        onBackground = darkInk,

        surface = darkPage,
        onSurface = darkInk,

        surfaceVariant = darkSearchField,
        onSurfaceVariant = darkInkVariant,

        surfaceTint = primary,

        inverseSurface = Color(0xFFEDEDED),
        inverseOnSurface = Color(0xFF121212),

        error = baseError.blend(Color.White, 0.15f),
        onError = Color(0xFF2A0004),
        errorContainer = errorContainer,
        onErrorContainer = baseError.blend(Color.White, 0.80f),

        outline = Color(0xFF3A3A3A),
        outlineVariant = darkLine,

        scrim = Color(0xFF000000),

        surfaceBright = darkCard.blend(Color.White, 0.08f),
        surfaceDim = darkPage,
        surfaceContainerLowest = Color(0xFF0D0D0D),
        surfaceContainerLow = Color(0xFF181818),
        surfaceContainer = darkPage,
        surfaceContainerHigh = darkCard,
        surfaceContainerHighest = darkSearchField,

        primaryFixed = primary,
        primaryFixedDim = primary.blend(Color.Black, 0.25f),
        onPrimaryFixed = Color(0xFF111111),
        onPrimaryFixedVariant = Color(0xFF111111).copy(alpha = 0.7f),

        secondaryFixed = secondary,
        secondaryFixedDim = secondary.blend(Color.Black, 0.25f),
        onSecondaryFixed = Color(0xFF111111),
        onSecondaryFixedVariant = Color(0xFF111111).copy(alpha = 0.7f),

        tertiaryFixed = tertiary,
        tertiaryFixedDim = tertiary.blend(Color.Black, 0.25f),
        onTertiaryFixed = Color(0xFF111111),
        onTertiaryFixedVariant = Color(0xFF111111).copy(alpha = 0.7f)
    )
}
