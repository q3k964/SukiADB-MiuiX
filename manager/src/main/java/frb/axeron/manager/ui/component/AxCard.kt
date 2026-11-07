package frb.axeron.manager.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// KernelSU / MiuiX card language: pure flat white, large radius, no border, no shadow,
// no gradient, no glass. Separation comes from the grey page behind it, not from elevation.
val AxCardShape: Shape = RoundedCornerShape(20.dp)
val AxPanelShape: Shape = RoundedCornerShape(14.dp)

/** A panel that remains square along the bottom edge (for modal bottom sheets). */
val AxTopPanelShape: Shape = RoundedCornerShape(
    topStart = 20.dp,
    topEnd = 20.dp,
    bottomEnd = 0.dp,
    bottomStart = 0.dp
)

/** Keep the scrollbar bubble's asymmetric corners. */
val AxScrollbarBubbleShape: Shape = RoundedCornerShape(
    topStart = 25.dp,
    topEnd = 25.dp,
    bottomEnd = 4.dp,
    bottomStart = 25.dp
)

/**
 * The one card every screen uses.
 *
 * MiuiX flat variant: `surfaceContainerHigh` is pure white in light mode (charcoal in dark),
 * and there is deliberately no `border` and no `tonalElevation`, because KernelSU's cards are
 * perfectly flat — the only depth cue is the page background showing between them.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AxCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    /** A long press; the card then handles both itself (Surface's onClick has no long press). */
    onLongClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    shape: Shape = AxCardShape,
    content: @Composable ColumnScope.() -> Unit
) {
    val color = MaterialTheme.colorScheme.surfaceContainerHigh
    if (onLongClick != null) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .combinedClickable(
                    enabled = enabled,
                    onClick = onClick ?: {},
                    onLongClick = onLongClick
                ),
            shape = shape,
            color = color,
            tonalElevation = 0.dp
        ) {
            Column(content = content)
        }
    } else if (onClick != null) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            onClick = onClick,
            enabled = enabled,
            shape = shape,
            color = color,
            tonalElevation = 0.dp
        ) {
            Column(content = content)
        }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = color,
            tonalElevation = 0.dp
        ) {
            Column(content = content)
        }
    }
}

/** Tinted panel used inside an [AxCard] to group a row of values. */
fun Modifier.axPanel(color: Color): Modifier = this
    .clip(AxPanelShape)
    .background(color.copy(alpha = 0.6f))

/**
 * The square icon tile inside a list row. MiuiX rows use a plain monochrome glyph with no
 * tinted backing plate, so this renders the icon directly at [size] with no background box.
 */
@Composable
fun IconBadge(
    painter: Painter,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    size: Dp = 36.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            modifier = Modifier.size(size * 0.62f),
            painter = painter,
            contentDescription = null,
            tint = tint
        )
    }
}

@Composable
fun IconBadge(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    size: Dp = 36.dp
) = IconBadge(rememberVectorPainter(imageVector), modifier, tint, size)

@Composable
fun StatusPill(text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.14f),
        shape = CircleShape
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun InlineDivider(height: Dp) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(height)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

/**
 * MiuiX has no coloured section headers: groups are separated by whitespace alone. Kept as a
 * no-op-shaped composable so call sites stay intact, but it now renders the small grey caption
 * KernelSU uses above a group when a label is genuinely needed.
 */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Normal
    )
}
