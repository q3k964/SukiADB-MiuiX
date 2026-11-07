package frb.axeron.manager.ui.screen

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import java.security.MessageDigest
import android.os.SystemClock
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.foundation.border
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Text
import frb.axeron.manager.ui.component.AxTopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import frb.axeron.manager.ui.component.SettingsDivider
import frb.axeron.api.Axeron
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.ActivateScreenDestination
import com.ramcosta.composedestinations.generated.destinations.PluginScreenDestination
import com.ramcosta.composedestinations.generated.destinations.PrivilegeScreenDestination
import com.ramcosta.composedestinations.generated.destinations.QuickShellScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import frb.axeron.manager.BuildConfig
import frb.axeron.manager.R
import frb.axeron.manager.ui.component.AxCard
import frb.axeron.manager.ui.component.AxCardShape
import frb.axeron.manager.ui.component.AxPanelShape
import frb.axeron.manager.ui.component.SectionHeader
import frb.axeron.manager.ui.component.IconBadge
import frb.axeron.manager.ui.component.StatusPill
import frb.axeron.manager.data.QuickSettingsStore
import frb.axeron.manager.data.SettingsAccess
import androidx.compose.material.icons.outlined.Bolt
import frb.axeron.manager.ui.component.NeedMoreStepDialog
import frb.axeron.manager.ui.component.PowerDialog
import frb.axeron.manager.ui.component.rememberConfirmDialog
import frb.axeron.manager.ui.component.rememberReigniteSheet
import frb.axeron.manager.ui.util.ClipboardUtil
import frb.axeron.manager.ui.util.LocalFloatingBarInset
import frb.axeron.manager.ui.component.FloatingBarFab
import frb.axeron.manager.ui.util.checkNewVersion
import frb.axeron.manager.ui.util.module.LatestVersionInfo
import frb.axeron.manager.ui.viewmodel.ActivateViewModel
import frb.axeron.manager.ui.viewmodel.ViewModelGlobal
import frb.axeron.server.ServerInfo
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import frb.axeron.shared.AxeronApiConstant.server.VERSION_CODE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>(start = true)
@Composable
fun HomeScreen(navigator: DestinationsNavigator, viewModelGlobal: ViewModelGlobal) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val pluginViewModel = viewModelGlobal.pluginViewModel
    val privilegeViewModel = viewModelGlobal.privilegeViewModel
    val activateViewModel = viewModelGlobal.activateViewModel

    val isRunning = activateViewModel.activateStatus is ActivateViewModel.ActivateStatus.Running

    val verification = rememberServerVerification()
    var showSignature by remember { mutableStateOf(false) }
    if (showSignature && isRunning) {
        SignatureSheet(
            verification = verification,
            serverInfo = activateViewModel.axeronInfo.serverInfo,
            onDismiss = { showSignature = false }
        )
    }

    val reigniteSheet = rememberReigniteSheet()
    val shutdownTitle = stringResource(R.string.server_shutdown)
    val restartTitle = stringResource(R.string.server_restart)
    val scope = rememberCoroutineScope()
    var showPowerDialog by remember { mutableStateOf(false) }

    if (showPowerDialog) {
        PowerDialog(
            onDismiss = { showPowerDialog = false },
            onReignite = {
                scope.launch {
                    val success = reigniteSheet.reignite()

                    if (success) {
                        pluginViewModel.fetchModuleList()
                    }
                }
            },
            onShutdown = {
                scope.launch {
                    reigniteSheet.run(shutdownTitle) { activateViewModel.shutdownServer(it) }
                }
            },
            onRestart = {
                scope.launch {
                    val success = reigniteSheet.run(restartTitle) {
                        activateViewModel.restartServer(it)
                    }
                    if (success) {
                        pluginViewModel.fetchModuleList()
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            AxTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Medium,
                            )
                            Text(
                                text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                },
                actions = {
                    if (isRunning) {
                        SignatureBadge(verification, activateViewModel.axeronInfo.serverInfo) {
                            showSignature = true
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { innerPadding ->
        if (isRunning) {
            FloatingBarFab {
                FloatingActionButton(
                    shape = CircleShape,
                    onClick = {
                        navigator.navigate(QuickShellScreenDestination)
                    }
                ) {
                    Icon(Icons.Filled.Terminal, null)
                }
            }
        }

        Column(
            modifier = Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                // Padding inside the scroll lets content pass under the fading top bar.
                .padding(
                    top = innerPadding.calculateTopPadding() + 12.dp,
                    bottom = innerPadding.calculateBottomPadding() + LocalFloatingBarInset.current + 16.dp
                )
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // KernelSU's home is a 2-column mosaic: a large tinted status tile on the left,
            // two stacked white counters on the right.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatusCard(
                    activateViewModel = activateViewModel,
                    onOpenActivation = { navigator.navigate(ActivateScreenDestination) },
                    onPowerClick = { showPowerDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                // Plain `if` rather than AnimatedVisibility: the mosaic relies on the row's
                // intrinsic height so both columns match, and AnimatedVisibility breaks it.
                if (isRunning) {
                    val pluginCount = pluginViewModel.plugins.size
                    val privilegeCount = privilegeViewModel.privilegedCount
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CounterTile(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            label = stringResource(
                                if (privilegeCount <= 1) R.string.privilege else R.string.privilege_plural
                            ),
                            value = privilegeCount,
                            onClick = { navigator.navigate(PrivilegeScreenDestination) }
                        )
                        CounterTile(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            label = stringResource(
                                if (pluginCount <= 1) R.string.plugin else R.string.plugin_plural
                            ),
                            value = pluginCount,
                            onClick = { navigator.navigate(PluginScreenDestination) }
                        )
                    }
                }
            }

            // With the server off, Quick Settings still works through the settings permissions
            // the server granted earlier; offer it while both are held.
            val context = LocalContext.current
            val quickSettingsOffline = remember(isRunning) {
                !isRunning && SettingsAccess.canWriteWithoutServer(context)
            }
            var showQuickSettings by remember { mutableStateOf(false) }
            if (showQuickSettings) {
                QuickSettingsSheet(onDismissRequest = { showQuickSettings = false })
            }
            AnimatedVisibility(visible = quickSettingsOffline) {
                QuickSettingsCard(
                    count = QuickSettingsStore.items.size,
                    onClick = { showQuickSettings = true }
                )
            }

            UpdateCard()

            HomeSectionHeader(stringResource(R.string.home_section_device))
            InfoCard(activateViewModel)

            HomeSectionHeader(stringResource(R.string.home_section_help))
            LinksCard()
        }
    }
}

/**
 * KernelSU's status tile: a big tinted block, a bold state line, a mode caption and a large
 * watermark glyph. The colour carries the state — pale blue-green while the server is up,
 * pale red while it is down — which is exactly how KernelSU reads "工作中 <LKM>" at a glance.
 */
@Composable
fun StatusCard(
    activateViewModel: ActivateViewModel,
    onOpenActivation: () -> Unit,
    onPowerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val axeronInfo = activateViewModel.axeronInfo
    val context = LocalContext.current
    val isRunning = activateViewModel.activateStatus is ActivateViewModel.ActivateStatus.Running
    val isUpdating = activateViewModel.activateStatus is ActivateViewModel.ActivateStatus.Updating
    val isNeedExtraStep = activateViewModel.activateStatus is ActivateViewModel.ActivateStatus.NeedExtraStep

    val containerColor by animateColorAsState(
        targetValue = when {
            isUpdating -> MaterialTheme.colorScheme.primaryContainer
            isNeedExtraStep -> MaterialTheme.colorScheme.errorContainer
            isRunning -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.errorContainer
        },
        animationSpec = tween(400),
        label = "statusContainer"
    )

    val contentColor = if (isRunning || isUpdating) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onErrorContainer
    }

    val updating = stringResource(R.string.updating)
    var showNeedMoreStepDialog by remember { mutableStateOf(false) }

    if (showNeedMoreStepDialog) {
        NeedMoreStepDialog(onDismiss = { showNeedMoreStepDialog = false })
    }

    val watermark = when {
        isUpdating -> rememberVectorPainter(Icons.Outlined.Update)
        isNeedExtraStep -> rememberVectorPainter(Icons.Outlined.Build)
        isRunning -> painterResource(R.drawable.ic_axeron)
        else -> rememberVectorPainter(Icons.Outlined.Cancel)
    }

    Box(
        modifier = modifier
            .clip(AxCardShape)
            .background(containerColor)
            // The activation screen is always reachable from here, except mid-update.
            .clickable {
                if (isUpdating) {
                    Toast.makeText(context, updating, Toast.LENGTH_SHORT).show()
                } else {
                    onOpenActivation()
                }
            }
            .padding(16.dp)
    ) {
        // Watermark glyph, clipped by the tile like KernelSU's oversized status icon.
        Icon(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(96.dp)
                .offset(x = 12.dp, y = 12.dp),
            painter = watermark,
            contentDescription = null,
            tint = contentColor.copy(alpha = 0.18f)
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = when {
                    isUpdating -> updating
                    isNeedExtraStep -> stringResource(R.string.home_need_fix)
                    isRunning -> stringResource(R.string.home_running)
                    else -> stringResource(R.string.home_not_running)
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )

            Text(
                text = when {
                    isRunning -> stringResource(R.string.server_version) + " " + axeronInfo.getVersionCode()
                    isUpdating -> "v${axeronInfo.getVersionCode()} → v$VERSION_CODE"
                    isNeedExtraStep -> stringResource(R.string.home_badge_action)
                    else -> stringResource(R.string.home_badge_stopped)
                },
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.75f)
            )

            Spacer(Modifier.height(4.dp))

            // The running tile keeps its uptime line; KernelSU shows the version there instead.
            if (isRunning) {
                var time by remember { mutableLongStateOf(0) }

                LaunchedEffect(Unit) {
                    while (true) {
                        time = SystemClock.elapsedRealtime() - axeronInfo.serverInfo.starting
                        delay(1000)
                    }
                }

                Text(
                    text = formatUptime(
                        time,
                        stringResource(R.string.day_singular),
                        stringResource(R.string.day_plural)
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.65f)
                )
            } else if (isNeedExtraStep) {
                Text(
                    text = stringResource(R.string.home_need_fix),
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.65f)
                )
            }
        }
    }
}

/**
 * The white counter tiles to the right of the status block: a small grey caption over a large
 * bold number, exactly the "超级用户 / 0" and "模块 / 0" pair in KernelSU's home mosaic.
 */
@Composable
private fun CounterTile(
    label: String,
    value: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AxCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$value",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}



/**
 * Two-part pill: a tinted "UID" label segment, a hairline, then the uid itself in bold
 * monospace, so the number reads as the value and the label as its caption.
 */
@Composable
private fun UidBadge(uid: Int, container: Color, content: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .clip(CircleShape)
            .background(container)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .background(content.copy(alpha = 0.16f))
                .padding(start = 10.dp, end = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "UID",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.8.sp,
                color = content.copy(alpha = 0.8f),
                maxLines = 1
            )
        }
        Box(
            modifier = Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(content.copy(alpha = 0.35f))
        )
        Text(
            text = uid.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            color = content,
            maxLines = 1,
            modifier = Modifier.padding(start = 7.dp, end = 10.dp, top = 3.dp, bottom = 3.dp)
        )
    }
}








/** Section title between Home cards, matching the Settings section headers. */
@Composable
private fun HomeSectionHeader(text: String) {
    SectionHeader(
        text = text,
        modifier = Modifier.padding(top = 8.dp)
    )
}

private fun formatUptime(millis: Long, daySingular: String, dayPlural: String): String {
    val totalSeconds = millis / 1000
    val days = totalSeconds / 86400
    val hours = (totalSeconds % 86400) / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    val dayPart = when {
        days == 1L -> "1 $daySingular "
        days > 1 -> "$days $dayPlural "
        else -> ""
    }

    return "$dayPart%02d:%02d:%02d".format(hours, minutes, seconds)
}


/** What the server's callback proved so far; it lands after the screen is composed. */
private data class ServerVerification(val verified: Boolean, val callbackUid: Int)

// Plain fields in Axeron, not state: the info read right after the callback is equal to the one
// before, so nothing else would redraw.
@Composable
private fun rememberServerVerification(): ServerVerification {
    var verification by remember {
        mutableStateOf(ServerVerification(Axeron.isServerVerified(), Axeron.serverCallbackUid()))
    }
    DisposableEffect(Unit) {
        val listener = Axeron.OnBinderReceivedListener {
            verification = ServerVerification(Axeron.isServerVerified(), Axeron.serverCallbackUid())
        }
        Axeron.addBinderReceivedListenerSticky(listener, Handler(Looper.getMainLooper()))
        onDispose { Axeron.removeBinderReceivedListener(listener) }
    }
    return verification
}

/**
 * Whether this server was started by a manager signed like this one, as with root managers, and
 * whether the binder behind that answer is the server's at all: icon, verdict, colour, and what
 * the verdict means.
 */
@Composable
private fun signatureVerdict(
    verification: ServerVerification,
    serverInfo: ServerInfo
): Quad<ImageVector, Int, Color, Int> {
    val colors = MaterialTheme.colorScheme
    return when {
        !verification.verified -> Quad(
            Icons.Outlined.Warning, R.string.home_signature_unverified, colors.error,
            R.string.home_signature_unverified_msg
        )
        serverInfo.callerIsManager -> Quad(
            Icons.Outlined.VerifiedUser, R.string.home_signature_match, colors.primary,
            R.string.home_signature_match_msg
        )
        else -> Quad(
            Icons.Outlined.Warning, R.string.home_signature_mismatch, colors.error,
            R.string.home_signature_mismatch_msg
        )
    }
}

/** The verdict as a pill: tinted, not filled, with a thin edge so it still reads on any background. */
@Composable
private fun SignaturePill(
    icon: ImageVector,
    text: String,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.14f))
            .border(1.dp, tint.copy(alpha = 0.35f), CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(modifier = Modifier.size(16.dp), imageVector = icon, contentDescription = null, tint = tint)
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = tint,
            maxLines = 1
        )
    }
}

/** Top right of Home while the server runs: the verdict at a glance, the details on a tap. */
@Composable
private fun SignatureBadge(
    verification: ServerVerification,
    serverInfo: ServerInfo,
    onClick: () -> Unit
) {
    val (icon, verdict, tint, _) = signatureVerdict(verification, serverInfo)
    SignaturePill(
        icon = icon,
        text = stringResource(verdict),
        tint = tint,
        onClick = onClick,
        modifier = Modifier
            .padding(end = 8.dp)
    )
}

/**
 * What the badge means, and what it was read from, as it is read, so a "no" says why: who
 * called back, and both certificates to compare by eye (a tap copies one).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SignatureSheet(
    verification: ServerVerification,
    serverInfo: ServerInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val copied = stringResource(R.string.copied)
    val colors = MaterialTheme.colorScheme
    val own = remember { ownCertDigest(context) }
    val (icon, verdict, tint, explanation) = signatureVerdict(verification, serverInfo)

    fun copy(value: String) {
        if (ClipboardUtil.put(context, value) && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(context, copied, Toast.LENGTH_SHORT).show()
        }
    }

    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surfaceContainerLowest,
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.home_signature),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                SignaturePill(icon = icon, text = stringResource(verdict), tint = tint)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(explanation),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.home_signature_about),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant
                )
            }

            Surface(
                shape = AxPanelShape,
                color = colors.surfaceContainerHigh,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                    SignatureRow(
                        label = stringResource(R.string.home_signature_binder),
                        value = when {
                            verification.verified -> stringResource(
                                R.string.home_signature_called_back, uidName(verification.callbackUid)
                            )
                            verification.callbackUid >= 0 -> stringResource(
                                R.string.home_signature_called_back_wrong, uidName(verification.callbackUid)
                            )
                            else -> stringResource(R.string.home_signature_no_callback)
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 14.dp),
                        color = colors.outline.copy(alpha = 0.2f)
                    )
                    SignatureBlock(
                        label = stringResource(R.string.home_signature_server),
                        value = serverInfo.managerCert,
                        onCopy = ::copy
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 14.dp),
                        color = colors.outline.copy(alpha = 0.2f)
                    )
                    SignatureBlock(
                        label = stringResource(R.string.home_signature_this),
                        value = own.orEmpty(),
                        onCopy = ::copy
                    )
                }
            }

            Text(
                text = stringResource(R.string.home_signature_copy_hint),
                style = MaterialTheme.typography.bodySmall,
                color = colors.outline
            )
        }
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

/** A label and its short value on one line. */
@Composable
private fun SignatureRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value.ifEmpty { "—" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End
        )
    }
}

/**
 * A certificate digest under its label, in groups of 8 characters, 4 groups to a line, so two
 * of them can be compared line by line. More than one signer gets a gap between them. A tap
 * copies the digest as one string.
 */
@Composable
private fun SignatureBlock(label: String, value: String, onCopy: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (value.isNotEmpty()) Modifier.clickable { onCopy(value) } else Modifier),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = colors.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            if (value.isNotEmpty()) {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = null,
                    tint = colors.outline
                )
            }
        }
        Text(
            text = value.split(',').filter { it.isNotEmpty() }.joinToString("\n\n") { digest ->
                digest.chunked(8).chunked(4).joinToString("\n") { it.joinToString(" ") }
            }.ifEmpty { "—" },
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp,
            lineHeight = 20.sp
        )
    }
}

/** root and shell are what a real server calls back as; any other uid is shown as a number. */
private fun uidName(uid: Int): String = when (uid) {
    0 -> "root (0)"
    2000 -> "shell (2000)"
    else -> "uid $uid"
}

/**
 * SHA-256 of this app's signing certificate(s), computed as the server computes the one it
 * trusts, to show next to it. Display only: whether they match is the server's call.
 */
private fun ownCertDigest(context: Context): String? = runCatching {
    val pm = context.packageManager
    val signers = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            .signingInfo?.apkContentsSigners
    } else {
        @Suppress("DEPRECATION")
        pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES).signatures
    }
    signers?.joinToString(",") { signer ->
        MessageDigest.getInstance("SHA-256").digest(signer.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
}.getOrNull()


@Composable
fun UpdateCard() {
    val latestVersionInfo = LatestVersionInfo()
    val newVersion by produceState(initialValue = latestVersionInfo) {
        value = withContext(Dispatchers.IO) {
            checkNewVersion()
        }
    }

    val currentVersionCode = BuildConfig.VERSION_CODE
    val newVersionCode = newVersion.versionCode
    val newVersionUrl = newVersion.downloadUrl
    val changelog = newVersion.changelog

    val uriHandler = LocalUriHandler.current
    val title = stringResource(R.string.changelog)
    val updateText = stringResource(R.string.update)
    val newVersionAvailable = stringResource(R.string.new_version_available)

    AnimatedVisibility(
        visible = newVersionCode > currentVersionCode,
        enter = fadeIn() + expandVertically(),
        exit = shrinkVertically() + fadeOut()
    ) {
        val updateDialog = rememberConfirmDialog(onConfirm = { uriHandler.openUri(newVersionUrl) })
        WarningCard(
            message = newVersionAvailable.format(newVersionCode),
            color = MaterialTheme.colorScheme.primary,
            icon = Icons.Outlined.Update
        ) {
            if (changelog.isEmpty()) {
                uriHandler.openUri(newVersionUrl)
            } else {
                updateDialog.showConfirm(
                    title = title,
                    content = changelog,
                    markdown = true,
                    confirm = updateText
                )
            }
        }
    }
}

/** Entry to Quick Settings while the server is off. */
@Composable
private fun QuickSettingsCard(count: Int, onClick: () -> Unit) {
    AxCard(onClick = onClick) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconBadge(imageVector = Icons.Outlined.Bolt)
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.quick_settings),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.quick_settings_offline_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (count > 0) {
                StatusPill(text = count.toString(), color = MaterialTheme.colorScheme.primary)
            }
            Chevron()
        }
    }
}

@Composable
fun WarningCard(
    message: String,
    color: Color = MaterialTheme.colorScheme.error,
    icon: ImageVector = Icons.Outlined.Cancel,
    onClick: (() -> Unit)? = null
) {
    AxCard(onClick = onClick) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconBadge(imageVector = icon, tint = color)
            Text(
                modifier = Modifier.weight(1f),
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
            if (onClick != null) {
                Chevron()
            }
        }
    }
}

/** A device fact: [shown] is the short form for the grid, [full] is what a tap copies. */
private data class DeviceFact(val label: String, val shown: String, val full: String = shown)

/**
 * Device facts as a calm 2-column grid: no icon badges, one line per value, long values
 * shortened (kernel version number, primary ABI). Tapping a cell copies its full value.
 * Server UID/PID/version live in the status card strip.
 */
@Composable
fun InfoCard(activateViewModel: ActivateViewModel) {
    val axeronInfo = activateViewModel.axeronInfo
    val kernel = System.getProperty("os.version").orEmpty()

    val facts = listOf(
        DeviceFact(
            stringResource(R.string.device_model),
            "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
        ),
        DeviceFact(
            stringResource(R.string.android_version),
            "${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})"
        ),
        DeviceFact(stringResource(R.string.security_patch), Build.VERSION.SECURITY_PATCH),
        DeviceFact(stringResource(R.string.kernel_version), kernel.substringBefore('-'), kernel),
        DeviceFact(
            stringResource(R.string.abi_supported),
            Build.SUPPORTED_ABIS.firstOrNull().orEmpty(),
            Build.SUPPORTED_ABIS.joinToString(", ")
        ),
        DeviceFact(stringResource(R.string.selinux_context), axeronInfo.serverInfo.selinuxContext)
    )

    AxCard {
        facts.chunked(2).forEachIndexed { index, row ->
            if (index > 0) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                )
            }
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                DeviceFactCell(row[0], Modifier.weight(1f))
                VerticalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                )
                row.getOrNull(1)?.let { DeviceFactCell(it, Modifier.weight(1f)) }
                    ?: Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DeviceFactCell(fact: DeviceFact, modifier: Modifier) {
    val context = LocalContext.current
    val copied = stringResource(R.string.copied)

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable {
                // Android 13+ shows its own clipboard confirmation.
                if (ClipboardUtil.put(context, fact.full) &&
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                ) {
                    Toast.makeText(context, copied, Toast.LENGTH_SHORT).show()
                }
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = fact.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = fact.shown.ifEmpty { "—" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Support, docs and issue links in one card instead of three separate ones. */
@Composable
fun LinksCard() {
    val uriHandler = LocalUriHandler.current
    val githubFahrez182 = "https://github.com/q3k964/SukiADB"
    val learnAxManager = "https://github.com/q3k964/SukiADB#readme"
    val githubIssueUrl = "https://github.com/q3k964/SukiADB/issues"

    AxCard {
        LinkRow(
            icon = Icons.Outlined.FavoriteBorder,
            title = stringResource(R.string.support_me),
            message = stringResource(R.string.support_me_msg),
            onClick = { uriHandler.openUri(githubFahrez182) }
        ) {
            LinkTrailingIcon()
        }
        InfoRowDivider()
        LinkRow(
            icon = Icons.AutoMirrored.Outlined.MenuBook,
            title = stringResource(R.string.learn_more),
            message = stringResource(R.string.learn_more_msg),
            onClick = { uriHandler.openUri(learnAxManager) }
        ) {
            LinkTrailingIcon()
        }
        InfoRowDivider()
        LinkRow(
            icon = Icons.Outlined.BugReport,
            title = stringResource(R.string.report_issue),
            message = "${stringResource(R.string.report_issue_msg)} ${stringResource(R.string.report_issue_msg2)}",
            onClick = null,
            // IconButtons carry 12dp of their own padding; trim the row's so the icons line up
            // with the ↗ icons above (16dp from the edge).
            endPadding = 4.dp
        ) {
            IconButton(onClick = { uriHandler.openUri(githubIssueUrl) }) {
                Icon(
                    painter = painterResource(R.drawable.ic_github),
                    contentDescription = "Report to github",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LinkRow(
    icon: ImageVector,
    title: String,
    message: String,
    onClick: (() -> Unit)?,
    endPadding: Dp = 16.dp,
    trailing: @Composable () -> Unit
) {
    // Same row metrics as Settings: 16dp gutter, 36dp badge, 12dp gap.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(start = 16.dp, end = endPadding, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(imageVector = icon)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        trailing()
    }
}

@Composable
private fun LinkTrailingIcon() {
    Icon(
        modifier = Modifier.size(20.dp),
        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )
}

@Composable
private fun Chevron() {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    )
}

@Composable
private fun InfoRowDivider() {
    // Same inset as Settings rows: lines up with the text after the badge.
    SettingsDivider()
}

