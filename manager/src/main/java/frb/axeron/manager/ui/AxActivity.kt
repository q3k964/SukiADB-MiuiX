package frb.axeron.manager.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.effects.lens
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.ActivateScreenDestination
import com.ramcosta.composedestinations.generated.destinations.FlashScreenDestination
import com.ramcosta.composedestinations.generated.destinations.HomeScreenDestination
import com.ramcosta.composedestinations.generated.destinations.QuickShellScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import frb.axeron.api.AxeronInfo
import frb.axeron.manager.adb.KeepAlive
import frb.axeron.manager.R
import frb.axeron.manager.ui.component.FloatingBarFabHost
import frb.axeron.manager.ui.component.FloatingBarFabSlot
import frb.axeron.manager.ui.component.liquid.LiquidBottomTab
import frb.axeron.manager.ui.component.liquid.LiquidBottomTabs
import frb.axeron.manager.ui.component.LocalFloatingBarFabHost
import frb.axeron.manager.ui.navigation.BottomBarDestination
import frb.axeron.manager.ui.screen.FlashIt
import frb.axeron.manager.ui.theme.AxManagerTheme
import frb.axeron.manager.ui.util.LocalFloatingBarInset
import frb.axeron.manager.ui.util.LocalSnackbarHost
import frb.axeron.manager.ui.util.LocaleHelper
import frb.axeron.manager.ui.viewmodel.ActivateViewModel
import frb.axeron.manager.ui.viewmodel.AppsViewModel
import frb.axeron.manager.ui.viewmodel.PluginViewModel
import frb.axeron.manager.ui.viewmodel.PrivilegeViewModel
import frb.axeron.manager.ui.viewmodel.SettingsViewModel
import frb.axeron.manager.ui.viewmodel.ViewModelGlobal
import frb.axeron.server.PluginInstaller
import kotlin.math.abs
import kotlinx.coroutines.delay

class AxActivity : ComponentActivity() {

    companion object {
        const val OPEN_QUICK_SHELL = "AxManager.QUICK_SHELL"
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.applyLanguage(newBase))
    }

    var zipUri by mutableStateOf<List<PluginInstaller>?>(emptyList())

    val shortcut by lazy {
        ShortcutInfoCompat.Builder(this, "quick-shell")
            .setShortLabel(getString(R.string.quick_shell_short))
            .setLongLabel(getString(R.string.quick_shell))
            .setDisabledMessage(getString(R.string.quick_shell_not_supported))
            .setIcon(IconCompat.createWithResource(this, R.drawable.terminal))
            .setIntent(
                Intent(this, AxActivity::class.java).apply {
                    action = OPEN_QUICK_SHELL
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
            )
            .build()
    }

    private var intentState: Intent? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ShortcutManagerCompat.pushDynamicShortcut(this@AxActivity, shortcut)

        // Only a fresh start: after a recreate (rotation, theme change) the intent was already
        // handled, and handling a VIEW again would flash the same zip a second time.
        if (savedInstanceState == null) intentState = intent

        // 每次打开界面都对齐一次保活服务：
        // 用户在设置里关掉了就不启，开着但被系统杀掉过就重新拉起来。
        KeepAlive.sync(this)

        setContent {
            AxManagerTheme {
                MainScreen(it)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intentState = intent
        Log.i("AxActivity", "onNewIntent")
    }

    @Composable
    fun MainScreen(settingsViewModel: SettingsViewModel) {
        val snackBarHostState = remember { SnackbarHostState() }
        val navController = rememberNavController()
        val navigator = navController.rememberDestinationsNavigator()
        val currentDestination = navController.currentBackStackEntryAsState().value?.destination

        val activateViewModel: ActivateViewModel = viewModel<ActivateViewModel>()

        val appsViewModel: AppsViewModel = viewModel<AppsViewModel>()
        val privilegeViewModel: PrivilegeViewModel = viewModel<PrivilegeViewModel>()
        val pluginViewModel: PluginViewModel = viewModel<PluginViewModel>()

        val axeronInfo = activateViewModel.axeronInfo

        LaunchedEffect(intentState) {
            if (intentState == null) return@LaunchedEffect
            Log.i("AxActivity", "intent: $intentState")
            when (intentState!!.action) {
                OPEN_QUICK_SHELL -> {
                    if (axeronInfo.isRunning()) {
                        navigator.navigate(HomeScreenDestination()) {
                            popUpTo(NavGraphs.root) {
                                saveState = true
                            }
                        }
                        if (navController.currentDestination?.route == HomeScreenDestination.route) {
                            navigator.navigate(QuickShellScreenDestination())
                        }
                    }
                }

                Intent.ACTION_VIEW -> {
                    // The intent being handled: with singleTask a VIEW often arrives through
                    // onNewIntent, while `intent` is still the one that first created the activity.
                    val view = intentState!!
                    if (view.data?.scheme == "content") {
                        zipUri =
                            view.data?.let { uri ->
                                arrayListOf(PluginInstaller(uri))
                            } ?: run {
                                val uris: ArrayList<Uri>? =
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        view.getParcelableArrayListExtra("uris", Uri::class.java)
                                    } else {
                                        @Suppress("DEPRECATION")
                                        view.getParcelableArrayListExtra("uris")
                                    }

                                uris
                                    ?.map { PluginInstaller(it) }
                                    ?.toCollection(arrayListOf())
                                    ?: arrayListOf()
                            }
                        if (!zipUri.isNullOrEmpty()) {
                            navigator.navigate(
                                FlashScreenDestination(
                                    flashIt = FlashIt.FlashPlugins(zipUri!!)
                                )
                            )
                            zipUri = null
                        }
                    }
                }
            }

            intentState = null
        }

        // Reload only when the server process changes, not on every AxeronInfo emission.
        val serverProcess = axeronInfo.serverInfo.uid to axeronInfo.serverInfo.pid
        LaunchedEffect(serverProcess) {
            if (axeronInfo.isRunning()) {
                // pluginViewModel is prioritized because of BottomBar badge
                pluginViewModel.fetchModuleList()

                // Stagger other loads to avoid UI stutter during startup/navigation
                delay(500)
                appsViewModel.loadInstalledApps()

                delay(500)
                privilegeViewModel.loadInstalledApps()
            }
        }

        val viewModelGlobal = remember {
            ViewModelGlobal(
                settingsViewModel = settingsViewModel,
                appsViewModel = appsViewModel,
                activateViewModel = activateViewModel,
                pluginViewModel = pluginViewModel,
                privilegeViewModel = privilegeViewModel
            )
        }

        val showBottomBar = when (currentDestination?.route) {
            ActivateScreenDestination.route -> false // Hide for Activate
            FlashScreenDestination.route -> false // Hide for Flash and plugin actions
            else -> true
        }

        val density = LocalDensity.current
        var bottomBarHeight by remember { mutableStateOf(0.dp) }
        val fabHost = remember { FloatingBarFabHost() }

        // Liquid Glass sampling source: the NavHost content is recorded into this layer and the
        // bottom bar refracts those pixels. Only the bar samples it, so cost is one layer.
        val glassBackdrop = rememberLayerBackdrop()

        Box {
            Scaffold(
                contentWindowInsets = WindowInsets()
            ) { innerPadding ->
                CompositionLocalProvider(
                    LocalSnackbarHost provides snackBarHostState,
                    LocalFloatingBarInset provides if (showBottomBar) bottomBarHeight else 0.dp,
                    LocalFloatingBarFabHost provides fabHost,
                ) {
                    DestinationsNavHost(
                        // Full-bleed content; the floating bar overlays it and each screen adds its own bottom inset.
                        modifier = Modifier
                            .padding(innerPadding)
                            .layerBackdrop(glassBackdrop)
                            .tabSwipeGesture(
                                navController = navController,
                                navigator = navigator,
                                serverRunning = activateViewModel.axeronInfo.isRunning()
                            ),
                        navGraph = NavGraphs.root,
                        navController = navController,
                        dependenciesContainerBuilder = {
                            dependency(viewModelGlobal)
                        },
                        defaultTransitions = object : NavHostAnimatedDestinationStyle() {
                            // iOS 导航曲线 cubic-bezier(0.32, 0.72, 0, 1)：
                            // 起手快、收尾带阻尼，比 Material 的线性 tween 更跟手。
                            val iosCurve = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
                            val iosMs = 380

                            override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition
                                get() = {
                                    val fromRoute = initialState.destination.route
                                    val toRoute = targetState.destination.route
                                    val fromIdx = BottomBarDestination.entries.find { it.direction.route == fromRoute }?.ordinal ?: -1
                                    val toIdx = BottomBarDestination.entries.find { it.direction.route == toRoute }?.ordinal ?: -1

                                    if (fromIdx != -1 && toIdx != -1) {
                                        val forward = toIdx > fromIdx
                                        slideInHorizontally(
                                            initialOffsetX = { if (forward) it else -it },
                                            animationSpec = tween(iosMs, easing = iosCurve)
                                        ) + fadeIn(tween(iosMs / 2))
                                    } else {
                                        // 详情页从右侧推入
                                        slideInHorizontally(
                                            initialOffsetX = { it },
                                            animationSpec = tween(iosMs, easing = iosCurve)
                                        ) + fadeIn(tween(220))
                                    }
                                }

                            override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition
                                get() = {
                                    val fromRoute = initialState.destination.route
                                    val toRoute = targetState.destination.route
                                    val fromIdx = BottomBarDestination.entries.find { it.direction.route == fromRoute }?.ordinal ?: -1
                                    val toIdx = BottomBarDestination.entries.find { it.direction.route == toRoute }?.ordinal ?: -1

                                    if (fromIdx != -1 && toIdx != -1) {
                                        val forward = toIdx > fromIdx
                                        slideOutHorizontally(
                                            targetOffsetX = { if (forward) -it else it },
                                            animationSpec = tween(iosMs, easing = iosCurve)
                                        ) + fadeOut(tween(iosMs / 2))
                                    } else {
                                        // iOS 视差：退出的页面只左移 1/3，制造纵深
                                        slideOutHorizontally(
                                            targetOffsetX = { -it / 3 },
                                            animationSpec = tween(iosMs, easing = iosCurve)
                                        ) + fadeOut(tween(iosMs))
                                    }
                                }
                        }
                    )
                }
            }

            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                CompositionLocalProvider(LocalSnackbarHost provides snackBarHostState) {
                    // Floating Liquid Glass tab bar (ported from the AndroidLiquidGlass catalog).
                    //
                    // It samples the NavHost layer through `glassBackdrop`, so content scrolling
                    // underneath shows through refracted rather than being covered. The lens pill
                    // over the selected tab magnifies the tinted row beneath it, which is what
                    // gives the bar its "water droplet" look.
                    val tabs = BottomBarDestination.entries
                        .filter { activateViewModel.axeronInfo.isRunning() || !it.needAxeron }
                    val currentTabIndex = tabs.indexOfFirst { destination ->
                        navController.currentDestination?.route == destination.direction.route
                    }.takeIf { it >= 0 } ?: 0

                    Column(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .onSizeChanged {
                                bottomBarHeight = with(density) { it.height.toDp() } + 20.dp
                            }
                    ) {
                        LiquidBottomTabs(
                            selectedTabIndex = { currentTabIndex },
                            onTabSelected = { index ->
                                val destination = tabs.getOrNull(index) ?: return@LiquidBottomTabs
                                navigator.navigate(destination.direction) {
                                    popUpTo(NavGraphs.root) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            backdrop = glassBackdrop,
                            tabsCount = tabs.size,
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tabs.forEach { destination ->
                                val selected = destination.direction.route ==
                                        navController.currentDestination?.route
                                val contentColor = if (selected) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }

                                LiquidBottomTab(
                                    onClick = {
                                        navigator.navigate(destination.direction) {
                                            popUpTo(NavGraphs.root) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                ) {
                                    if (destination == BottomBarDestination.Plugin &&
                                        pluginViewModel.pluginUpdateCount > 0
                                    ) {
                                        BadgedBox(
                                            badge = {
                                                Badge {
                                                    Text(pluginViewModel.pluginUpdateCount.toString())
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (selected) destination.iconSelected
                                                else destination.iconNotSelected,
                                                contentDescription = stringResource(destination.labelId),
                                                tint = contentColor
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = if (selected) destination.iconSelected
                                            else destination.iconNotSelected,
                                            contentDescription = stringResource(destination.labelId),
                                            tint = contentColor
                                        )
                                    }
                                    Text(
                                        text = stringResource(destination.labelId),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (selected) FontWeight.Medium
                                        else FontWeight.Normal,
                                        color = contentColor,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Page FABs float above the bottom bar's right edge, as in KernelSU's module list.
            // They live outside the bar so they can overlap list content, and the slot is sized
            // from bottomBarHeight so it always clears the bar as it animates in.
            if (showBottomBar) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = bottomBarHeight)
                        .padding(bottom = 12.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    FloatingBarFabSlot(fabHost)
                }
            }
        }
    }


}

/**
 * Swipe left/right on content to move between the bottom-bar tabs.
 *
 * The gesture only fires when the pointer travels further horizontally than vertically, so a
 * list's vertical scroll and a horizontally scrolling widget (the terminal's output, a code
 * block) keep working. `awaitEachGesture` is used rather than `detectHorizontalDragGestures`
 * because we need to see the initial movement direction before claiming the gesture at all.
 *
 * Only the four [BottomBarDestination] routes participate: detail screens (an app's privilege
 * page, the terminal, activation) are pushed on top and must not be swiped away sideways.
 */
private fun Modifier.tabSwipeGesture(
    navController: NavHostController,
    navigator: DestinationsNavigator,
    serverRunning: Boolean
): Modifier = this.pointerInput(serverRunning) {
    var totalDx = 0f
    var totalDy = 0f
    var claimed = false

    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        totalDx = 0f
        totalDy = 0f
        claimed = false

        // Watch the drag until it either qualifies as a tab swipe or is released.
        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == down.id } ?: break
            if (change.changedToUpIgnoreConsumed()) break

            val delta = change.positionChange()
            totalDx += delta.x
            totalDy += delta.y

            if (!claimed) {
                // Decide once the finger has clearly committed to a direction.
                val threshold = 24.dp.toPx()
                when {
                    abs(totalDx) > threshold && abs(totalDx) > abs(totalDy) * 1.5f -> {
                        // Horizontal intent: take the gesture so the content stops scrolling.
                        claimed = true
                        change.consume()
                    }
                    abs(totalDy) > threshold -> {
                        // Vertical intent: let the list have it.
                        break
                    }
                }
            } else {
                change.consume()
            }
        }

        if (claimed) {
            val committed = abs(totalDx) > 80.dp.toPx()
            if (committed) {
                val current = navController.currentDestination?.route
                val tabs = BottomBarDestination.entries
                    .filter { serverRunning || !it.needAxeron }
                val index = tabs.indexOfFirst { it.direction.route == current }
                if (index >= 0) {
                    val target = if (totalDx < 0) index + 1 else index - 1
                    tabs.getOrNull(target)?.let { destination ->
                        navigator.navigate(destination.direction) {
                            popUpTo(NavGraphs.root) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            }
        }
    }
}
