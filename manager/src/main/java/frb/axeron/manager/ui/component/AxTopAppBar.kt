package frb.axeron.manager.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Icon row height on screens that have a back button or trailing actions. */
private val IconRowHeight = 48.dp

/** Horizontal gutter shared by the icon row and the title, so both line up. */
val HeaderTitleStart = 16.dp

/**
 * KernelSU / MiuiX header.
 *
 * MiuiX splits what Material's TopAppBar puts on one line into two stacked bands:
 *   1. a short row of plain icon buttons (back arrow on the left, actions on the right), and
 *   2. a very large, left-aligned page title underneath it.
 * Screens with neither a back button nor actions show the title alone, which is why the icon row
 * is conditional rather than always reserving 48dp.
 *
 * [title] call sites pass `headlineLarge`; the header does not force a style, because a few
 * screens (Home, Flash) compose a two-line title block of their own.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AxTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val background = MaterialTheme.colorScheme.background
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to background,
                    0.7f to background,
                    1f to background.copy(alpha = 0f)
                )
            )
    ) {
        if (navigationIcon != null || actions != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IconRowHeight)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(contentAlignment = Alignment.CenterStart) {
                    navigationIcon?.invoke()
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    content = { actions?.invoke(this) }
                )
            }
        }

        Box(
            modifier = Modifier.padding(
                start = HeaderTitleStart,
                end = HeaderTitleStart,
                top = if (navigationIcon != null || actions != null) 0.dp else 12.dp,
                bottom = 10.dp
            )
        ) {
            title()
        }
    }
}

/**
 * Pull-to-refresh for lists that scroll under [AxTopAppBar]: the box spans the whole screen,
 * so the indicator is pushed down by [topInset] to appear below the bar instead of behind it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AxPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    topInset: Dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val state = rememberPullToRefreshState()
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        state = state,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = state,
                isRefreshing = isRefreshing,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = topInset)
            )
        },
        content = content
    )
}

/**
 * MiuiX has no floating nav pill: the bottom bar is an opaque full-width strip flush with the
 * system nav bar, so content simply stops above it and needs no fade.
 */
fun bottomBarFadeBrush(background: Color): Brush = Brush.verticalGradient(
    0f to background,
    1f to background
)
