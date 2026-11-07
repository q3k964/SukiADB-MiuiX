package frb.axeron.manager.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * The phone mock-up KernelSU shows at the top of its theme screen.
 *
 * A miniature of this app's own layout, drawn with shapes rather than a screenshot: a tinted
 * hero tile, two stacked counters, a wide card, and a four-slot nav bar. Because it is drawn
 * from the live [MaterialTheme] colours, it previews whatever accent and light/dark mode is
 * currently selected instead of a hard-coded picture.
 */
@Composable
fun ThemePreviewCard(
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    label: String = "MiuiX"
) {
    val page = MaterialTheme.colorScheme.background
    val card = MaterialTheme.colorScheme.surfaceContainerHigh
    val hairline = MaterialTheme.colorScheme.outlineVariant

    Column(modifier = modifier) {
        Text(
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // The "device": an outlined rounded rect holding a scaled-down app screen.
        Box(
            modifier = Modifier
                .width(210.dp)
                .aspectRatio(0.52f)
                .clip(RoundedCornerShape(26.dp))
                .background(page)
                .border(1.dp, hairline, RoundedCornerShape(26.dp))
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Hero tile + two counters, the home mosaic in miniature.
                Row(
                    modifier = Modifier.height(74.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(12.dp))
                            .background(accent.copy(alpha = 0.18f))
                    ) {
                        // A check mark stands in for KernelSU's status glyph.
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(accent.copy(alpha = 0.55f))
                        )
                    }
                    Column(
                        modifier = Modifier.weight(0.8f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MiniCard(card, Modifier.weight(1f))
                        MiniCard(card, Modifier.weight(1f))
                    }
                }

                // A wide content card.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(card)
                )

                // The bottom navigation strip, with four dots and the first one accented.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == 0) 9.dp else 8.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (index == 0) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniCard(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color)
    )
}

/**
 * The segmented light/dark selector KernelSU puts under its preview: three equal pills in a
 * track, the selected one lifted onto a white surface.
 */
@Composable
fun ThemeModeSelector(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, text ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (selected) MaterialTheme.colorScheme.surfaceContainerHigh
                        else Color.Transparent
                    )
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                    color = if (selected) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
