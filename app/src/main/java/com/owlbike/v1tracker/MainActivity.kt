package com.owlbike.v1tracker

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.owlbike.v1tracker.ble.BleDeviceItem
import com.owlbike.v1tracker.ble.BleDeviceType
import com.owlbike.v1tracker.ble.BleConnectionState
import com.owlbike.v1tracker.history.buildHistoryWeekGroups
import com.owlbike.v1tracker.data.RememberedDeviceEntity
import com.owlbike.v1tracker.data.WorkoutExportFormat
import com.owlbike.v1tracker.data.WorkoutExporters
import com.owlbike.v1tracker.data.WorkoutSampleEntity
import com.owlbike.v1tracker.data.WorkoutSessionEntity
import com.owlbike.v1tracker.race.GoalSource
import com.owlbike.v1tracker.race.GoalType
import com.owlbike.v1tracker.race.GoalInputError
import com.owlbike.v1tracker.race.GoalInputParser
import com.owlbike.v1tracker.race.GhostRaceState
import com.owlbike.v1tracker.race.PersonalBaseline
import com.owlbike.v1tracker.race.RaceCalculator
import com.owlbike.v1tracker.race.RaceZone
import com.owlbike.v1tracker.race.WorkoutGoal
import com.owlbike.v1tracker.settings.AppSettings
import com.owlbike.v1tracker.settings.LanguageMode
import com.owlbike.v1tracker.settings.MeasurementFormatter
import com.owlbike.v1tracker.settings.ThemeMode
import com.owlbike.v1tracker.settings.UnitSystem
import com.owlbike.v1tracker.ui.HomePrimaryAction
import com.owlbike.v1tracker.ui.resolveHomePrimaryAction
import java.io.File
import java.text.DateFormat
import java.util.Date
import kotlin.math.abs
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: TrackerViewModel = viewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                viewModel.setLanguageMode(currentLanguageMode())
            }

            OwlBikeTheme(state.settings.themeMode) {
                OwlBikeApp(
                    viewModel = viewModel,
                    state = state,
                    onLanguageModeChange = { mode ->
                        viewModel.setLanguageMode(mode)
                        applyLanguageMode(mode)
                    },
                )
            }
        }
    }
}

private data class AppPalette(
    val background: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val ink: Color,
    val muted: Color,
    val border: Color,
    val accent: Color,
    val accentSoft: Color,
    val cyan: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val gold: Color,
    val invertedText: Color,
    val invertedMuted: Color,
)

private val DarkPalette = AppPalette(
    background = Color(0xFF071012),
    surface = Color(0xFF101A1D),
    surfaceMuted = Color(0xFF172327),
    ink = Color(0xFFF2F7F6),
    muted = Color(0xFF8EA0A5),
    border = Color(0xFF26363A),
    accent = Color(0xFF36D399),
    accentSoft = Color(0xFF12352D),
    cyan = Color(0xFF49C7F5),
    success = Color(0xFF3BE38D),
    warning = Color(0xFFE7A443),
    danger = Color(0xFFFF5A67),
    gold = Color(0xFFF6D365),
    invertedText = Color(0xFF071012),
    invertedMuted = Color(0xFF4E6268),
)

private val LightPalette = AppPalette(
    background = Color(0xFFF3F7F5),
    surface = Color(0xFFFFFFFF),
    surfaceMuted = Color(0xFFE8EFEC),
    ink = Color(0xFF071012),
    muted = Color(0xFF54676B),
    border = Color(0xFFD4DEDA),
    accent = Color(0xFF0F8F64),
    accentSoft = Color(0xFFDDF4EA),
    cyan = Color(0xFF167FA2),
    success = Color(0xFF0F8F64),
    warning = Color(0xFFB46A12),
    danger = Color(0xFFD73846),
    gold = Color(0xFF9E7426),
    invertedText = Color(0xFFF2F7F6),
    invertedMuted = Color(0xFFC9D6D2),
)

private val LocalAppPalette = staticCompositionLocalOf { DarkPalette }

private val AppBackground: Color
    @Composable get() = LocalAppPalette.current.background
private val AppSurface: Color
    @Composable get() = LocalAppPalette.current.surface
private val AppSurfaceMuted: Color
    @Composable get() = LocalAppPalette.current.surfaceMuted
private val AppInk: Color
    @Composable get() = LocalAppPalette.current.ink
private val AppMuted: Color
    @Composable get() = LocalAppPalette.current.muted
private val AppBorder: Color
    @Composable get() = LocalAppPalette.current.border
private val AppAccent: Color
    @Composable get() = LocalAppPalette.current.accent
private val AppAccentSoft: Color
    @Composable get() = LocalAppPalette.current.accentSoft
private val AppCyan: Color
    @Composable get() = LocalAppPalette.current.cyan
private val AppSuccess: Color
    @Composable get() = LocalAppPalette.current.success
private val AppWarning: Color
    @Composable get() = LocalAppPalette.current.warning
private val AppDanger: Color
    @Composable get() = LocalAppPalette.current.danger
private val AppGold: Color
    @Composable get() = LocalAppPalette.current.gold
private val AppLightCardText: Color
    @Composable get() = LocalAppPalette.current.invertedText
private val AppLightCardMuted: Color
    @Composable get() = LocalAppPalette.current.invertedMuted
private const val EQUIPMENT_MODEL_NAME = "Bike V1 / YS-003"

@Composable
private fun OwlBikeTheme(themeMode: ThemeMode, content: @Composable () -> Unit) {
    val palette = if (themeMode == ThemeMode.Light) LightPalette else DarkPalette
    val colorScheme = if (themeMode == ThemeMode.Light) {
        lightColorScheme(
            primary = palette.accent,
            secondary = palette.ink,
            tertiary = palette.warning,
            surface = palette.surface,
            background = palette.background,
            onSurface = palette.ink,
            onSurfaceVariant = palette.muted,
            primaryContainer = palette.accentSoft,
            outline = palette.border,
            error = palette.danger,
        )
    } else {
        darkColorScheme(
            primary = palette.accent,
            secondary = palette.ink,
            tertiary = palette.warning,
            surface = palette.surface,
            background = palette.background,
            onSurface = palette.ink,
            onSurfaceVariant = palette.muted,
            primaryContainer = palette.accentSoft,
            outline = palette.border,
            error = palette.danger,
        )
    }

    CompositionLocalProvider(LocalAppPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}

private fun currentLanguageMode(): LanguageMode {
    return LanguageMode.fromLocaleTags(AppCompatDelegate.getApplicationLocales().toLanguageTags())
}

private fun applyLanguageMode(mode: LanguageMode) {
    val locales = mode.localeTag
        ?.let(LocaleListCompat::forLanguageTags)
        ?: LocaleListCompat.getEmptyLocaleList()
    AppCompatDelegate.setApplicationLocales(locales)
}

private data class BottomNavItem(
    val screen: AppScreen,
    val labelRes: Int,
    val iconRes: Int,
)

private val BottomNavItems = listOf(
    BottomNavItem(AppScreen.Home, R.string.tab_home, R.drawable.ic_nav_home),
    BottomNavItem(AppScreen.Ride, R.string.tab_ride, R.drawable.ic_nav_ride),
    BottomNavItem(AppScreen.History, R.string.tab_history, R.drawable.ic_nav_history),
    BottomNavItem(AppScreen.Profile, R.string.tab_profile, R.drawable.ic_nav_profile),
)

@Composable
private fun OwlBottomNavigation(selected: AppScreen, onSelect: (AppScreen) -> Unit) {
    NavigationBar(
        containerColor = AppSurface,
        contentColor = AppInk,
        tonalElevation = 0.dp,
    ) {
        BottomNavItems.forEach { item ->
            val selectedItem = selected == item.screen
            NavigationBarItem(
                selected = selectedItem,
                onClick = { onSelect(item.screen) },
                icon = {
                    Icon(
                        painter = painterResource(item.iconRes),
                        contentDescription = null,
                    )
                },
                label = {
                    Text(
                        stringResource(item.labelRes),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}

@Composable
private fun OwlBikeApp(
    viewModel: TrackerViewModel,
    state: TrackerUiState,
    onLanguageModeChange: (LanguageMode) -> Unit,
) {
    var showSplash by rememberSaveable { mutableStateOf(true) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        viewModel.refreshPermissions()
    }

    LaunchedEffect(Unit) {
        viewModel.refreshPermissions()
    }

    LaunchedEffect(showSplash) {
        if (showSplash) {
            delay(3_000)
            showSplash = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        if (showSplash) {
            SplashScreen(
                versionName = BuildConfig.VERSION_NAME,
                onContinue = { showSplash = false },
            )
            return@Surface
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = AppBackground,
            bottomBar = {
                OwlBottomNavigation(
                    selected = state.screen,
                    onSelect = viewModel::selectScreen,
                )
            },
        ) { innerPadding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (state.connectEntry != null) {
                    ConnectFlowHeader(
                        entry = state.connectEntry,
                        onClose = viewModel::closeConnect,
                    )
                    ConnectScreen(
                        state = state,
                        onGrantPermissions = { permissionLauncher.launch(viewModel.requiredPermissions()) },
                        onScan = viewModel::startScan,
                        onStopScan = viewModel::stopScan,
                        onConnect = viewModel::connect,
                        onConnectRemembered = viewModel::connectRememberedDevice,
                        onDisconnect = viewModel::disconnect,
                        onForgetEquipment = viewModel::forgetRememberedDevice,
                        onOpenRide = viewModel::openRidePlanning,
                    )
                } else {
                    PageTitleBar(stringResource(pageTitleRes(state)))
                    when (state.screen) {
                        AppScreen.Home -> HomeScreen(
                            state = state,
                            onOpenRide = viewModel::openRidePlanning,
                            onReconnectLast = viewModel::reconnectLastRememberedDevice,
                            onConnectFirst = { viewModel.openConnect(ConnectEntry.FirstSetup) },
                            onConnectOther = { viewModel.openConnect(ConnectEntry.ConnectOther) },
                            onOpenHistory = { viewModel.selectScreen(AppScreen.History) },
                        )
                        AppScreen.Ride -> RideScreen(
                        state = state,
                        onStart = viewModel::startPlannedRide,
                        onPause = viewModel::pauseWorkout,
                        onResume = viewModel::resumeWorkout,
                        onRequestFinish = viewModel::requestFinishWorkout,
                        onDismissFinish = viewModel::dismissFinishConfirmation,
                        onConfirmFinish = viewModel::finishWorkout,
                        onConfirmGoalOverride = viewModel::confirmGoalOverride,
                        onClearFinishedRide = viewModel::clearFinishedRide,
                        onOpenFinishedRide = viewModel::openFinishedRideInHistory,
                    )
                        AppScreen.History -> HistoryScreen(
                        state = state,
                        onSelect = viewModel::openHistorySession,
                        onBack = viewModel::clearSelectedSession,
                        onToggleWeek = viewModel::toggleHistoryWeek,
                        onRequestDelete = viewModel::requestDeleteSession,
                        onCancelDelete = viewModel::cancelDeleteSession,
                        onConfirmDelete = viewModel::confirmDeleteSession,
                    )
                        AppScreen.Profile -> ProfileScreen(
                        state = state,
                        onThemeModeChange = viewModel::setThemeMode,
                        onUnitSystemChange = viewModel::setUnitSystem,
                        onLanguageModeChange = onLanguageModeChange,
                    )
                    }
                }
            }
        }
    }
}

@Composable
private fun SplashScreen(versionName: String, onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Spacer(Modifier.height(24.dp))
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Surface(
                modifier = Modifier.size(132.dp),
                shape = CircleShape,
                color = AppAccentSoft,
                border = BorderStroke(1.dp, AppAccent.copy(alpha = 0.30f)),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = stringResource(R.string.app_name),
                        modifier = Modifier.size(108.dp),
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineSmall,
                color = AppInk,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.splash_version, versionName),
                style = MaterialTheme.typography.bodyMedium,
                color = AppMuted,
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.splash_privacy_note),
                modifier = Modifier.fillMaxWidth(0.88f),
                style = MaterialTheme.typography.bodyMedium,
                color = AppMuted,
                textAlign = TextAlign.Center,
            )
        }
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AppAccent),
        ) {
            Text(
                text = stringResource(R.string.splash_continue),
                color = AppLightCardText,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun PageTitleBar(title: String) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .background(AppBackground)
            .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 6.dp)
            .semantics { heading() },
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = AppInk,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

private fun pageTitleRes(state: TrackerUiState): Int {
    return when (state.screen) {
        AppScreen.Home -> R.string.tab_home
        AppScreen.Ride -> when {
            state.rideStage == RideStage.Results && state.lastFinishedSession != null && !state.isWorkoutRunning -> R.string.ride_saved_title
            state.isWorkoutRunning -> R.string.live_ride
            else -> R.string.ride_plan_title
        }
        AppScreen.History -> R.string.tab_history
        AppScreen.Profile -> R.string.profile_settings
    }
}

@Composable
private fun StatusPill(state: TrackerUiState) {
    val statusColor = when {
        state.connection.isConnected -> AppSuccess
        state.connection.isConnecting || state.connection.isScanning -> AppWarning
        else -> AppMuted
    }
    val label = when {
        state.connection.isConnected -> stringResource(R.string.connected)
        state.connection.isScanning -> stringResource(R.string.scan)
        state.connection.isConnecting -> state.connection.status
        else -> stringResource(R.string.not_connected)
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = statusColor.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, statusColor.copy(alpha = 0.18f)),
    ) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                Modifier
                    .height(8.dp)
                    .width(8.dp)
                    .background(statusColor, RoundedCornerShape(50)),
            )
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = statusColor,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun HomeScreen(
    state: TrackerUiState,
    onOpenRide: () -> Unit,
    onReconnectLast: () -> Unit,
    onConnectFirst: () -> Unit,
    onConnectOther: () -> Unit,
    onOpenHistory: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            HomeEquipmentPanel(
                state = state,
                onOpenRide = onOpenRide,
                onReconnectLast = onReconnectLast,
                onConnectFirst = onConnectFirst,
                onConnectOther = onConnectOther,
            )
        }
        item { HomeAchievementsPanel(state) }
        item { HomeHistoryPanel(state, onOpenHistory) }
    }
}

@Composable
private fun HomeEquipmentPanel(
    state: TrackerUiState,
    onOpenRide: () -> Unit,
    onReconnectLast: () -> Unit,
    onConnectFirst: () -> Unit,
    onConnectOther: () -> Unit,
) {
    val lastDevice = state.rememberedDevices.firstOrNull()
    val action = resolveHomePrimaryAction(
        isConnected = state.connection.isConnected,
        isConnecting = state.connection.isConnecting,
        hasRememberedDevice = lastDevice != null,
    )
    val displayName = when {
        state.connection.isConnected -> state.connection.deviceName
            ?: EQUIPMENT_MODEL_NAME
        lastDevice != null -> lastDevice.name ?: EQUIPMENT_MODEL_NAME
        else -> stringResource(R.string.home_no_trainer)
    }
    val statusText = when {
        state.connection.isConnected -> stringResource(R.string.connected)
        state.connection.isConnecting -> stringResource(R.string.connect_title_connecting)
        state.connection.isScanning -> stringResource(R.string.connect_title_scanning)
        else -> stringResource(R.string.not_connected)
    }
    val statusColor = when {
        state.connection.isConnected -> AppSuccess
        state.connection.isConnecting || state.connection.isScanning -> AppWarning
        else -> AppMuted
    }
    val primaryLabel = when {
        state.isWorkoutRunning -> stringResource(R.string.continue_ride)
        action == HomePrimaryAction.OpenRide -> stringResource(R.string.open_ride)
        action == HomePrimaryAction.ReconnectLast -> stringResource(R.string.reconnect_last_trainer)
        action == HomePrimaryAction.WaitForConnection -> stringResource(R.string.connect_title_connecting)
        else -> stringResource(R.string.connect_trainer)
    }
    val primaryAction: () -> Unit = when {
        state.isWorkoutRunning -> onOpenRide
        action == HomePrimaryAction.OpenRide -> onOpenRide
        action == HomePrimaryAction.ReconnectLast -> onReconnectLast
        action == HomePrimaryAction.OpenFirstSetupConnect -> onConnectFirst
        else -> ({})
    }

    Panel {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EquipmentThumbnail(Modifier.size(74.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.home_last_trainer),
                    style = MaterialTheme.typography.labelMedium,
                    color = AppMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = AppInk,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                MiniChip(statusText, statusColor)
            }
        }
        Spacer(Modifier.height(14.dp))
        Button(
            onClick = primaryAction,
            enabled = action != HomePrimaryAction.WaitForConnection || state.isWorkoutRunning,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppAccent,
                disabledContainerColor = AppSurfaceMuted,
                disabledContentColor = AppMuted,
            ),
        ) {
            if (action == HomePrimaryAction.WaitForConnection && !state.isWorkoutRunning) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = AppMuted,
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                primaryLabel,
                color = if (action == HomePrimaryAction.WaitForConnection && !state.isWorkoutRunning) AppMuted else AppLightCardText,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onConnectOther,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppAccent),
        ) {
            Text(stringResource(R.string.connect_other_trainer), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun HomeAchievementsPanel(state: TrackerUiState) {
    val unitSystem = state.settings.unitSystem
    val finishedSessions = state.sessions.filter { it.state == "finished" }
    val totalDistance = finishedSessions.mapNotNull { it.totalDistanceMeters }.sum().takeIf { it > 0.0 }
    val baseline = state.personalBaseline
    val medianDuration = baseline.medianDistanceDurationSeconds ?: baseline.medianCaloriesDurationSeconds
    Panel {
        Text(
            stringResource(R.string.home_achievements),
            style = MaterialTheme.typography.titleMedium,
            color = AppInk,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryStat(stringResource(R.string.days_streak), baseline.currentStreakDays.toString(), Modifier.weight(1f))
            SummaryStat(stringResource(R.string.total_distance), MeasurementFormatter.distance(totalDistance, unitSystem), Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryStat(
                stringResource(R.string.median_distance),
                MeasurementFormatter.distance(baseline.medianDistanceMeters, unitSystem),
                Modifier.weight(1f),
            )
            SummaryStat(stringResource(R.string.median_calories), baseline.medianCalories?.toString() ?: "-", Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryStat(
                stringResource(R.string.median_duration),
                medianDuration?.let(::formatDurationSeconds) ?: "-",
                Modifier.weight(1f),
            )
            SummaryStat(stringResource(R.string.sessions), finishedSessions.size.toString(), Modifier.weight(1f))
        }
    }
}

@Composable
private fun HomeHistoryPanel(state: TrackerUiState, onOpenHistory: () -> Unit) {
    Panel(
        modifier = Modifier.clickable(role = Role.Button, onClick = onOpenHistory),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.home_history_total, state.sessions.count { it.state == "finished" }),
                    style = MaterialTheme.typography.titleSmall,
                    color = AppInk,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    stringResource(R.string.home_history_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppMuted,
                )
            }
            Text(stringResource(R.string.view_history), color = AppAccent, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ConnectFlowHeader(entry: ConnectEntry, onClose: () -> Unit) {
    val title = when (entry) {
        ConnectEntry.ReconnectLast -> stringResource(R.string.reconnect_last_trainer)
        ConnectEntry.ConnectOther -> stringResource(R.string.connect_other_trainer)
        ConnectEntry.FirstSetup -> stringResource(R.string.connect_trainer)
    }
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            modifier = Modifier.weight(1f).semantics { heading() },
            style = MaterialTheme.typography.titleSmall,
            color = AppInk,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        TextButton(onClick = onClose) {
            Text(stringResource(R.string.back), color = AppAccent)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConnectScreen(
    state: TrackerUiState,
    onGrantPermissions: () -> Unit,
    onScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnect: (BleDeviceItem) -> Unit,
    onConnectRemembered: (RememberedDeviceEntity) -> Unit,
    onDisconnect: () -> Unit,
    onForgetEquipment: (String) -> Unit,
    onOpenRide: () -> Unit,
) {
    var showOnlyTrainerDevices by rememberSaveable { mutableStateOf(true) }
    var selectedEquipmentAddress by rememberSaveable { mutableStateOf<String?>(null) }
    var unpairCandidateAddress by rememberSaveable { mutableStateOf<String?>(null) }
    val activeDeviceAddress = state.connection.deviceAddress
    val nearbyByAddress = state.devices.associateBy { it.address }
    val rememberedAddresses = state.rememberedDevices.map { it.address }.toSet()
    val selectedEquipment = state.rememberedDevices.firstOrNull { it.address == selectedEquipmentAddress }
    val unpairCandidate = state.rememberedDevices.firstOrNull { it.address == unpairCandidateAddress }
    val equipmentSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val unrememberedNearbyDevices = state.devices.filterNot { it.address in rememberedAddresses }
    val visibleNearbyDevices = if (showOnlyTrainerDevices) {
        unrememberedNearbyDevices.filter { it.type != BleDeviceType.Other || it.address == activeDeviceAddress }
    } else {
        unrememberedNearbyDevices
    }
    val hiddenOtherDeviceCount = if (showOnlyTrainerDevices) {
        unrememberedNearbyDevices.count { it.type == BleDeviceType.Other && it.address != activeDeviceAddress }
    } else {
        0
    }
    val showNearbySection = visibleNearbyDevices.isNotEmpty() ||
        hiddenOtherDeviceCount > 0 ||
        (state.connection.isScanning && unrememberedNearbyDevices.isEmpty())

    LaunchedEffect(selectedEquipmentAddress, selectedEquipment) {
        if (selectedEquipmentAddress != null && selectedEquipment == null) {
            selectedEquipmentAddress = null
        }
    }
    LaunchedEffect(unpairCandidateAddress, unpairCandidate) {
        if (unpairCandidateAddress != null && unpairCandidate == null) {
            unpairCandidateAddress = null
        }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
        ) {
            item { ConnectHero(state) }
            item {
                ConnectPrimaryActions(
                    state = state,
                    onGrantPermissions = onGrantPermissions,
                    onScan = onScan,
                    onStopScan = onStopScan,
                    onOpenRide = onOpenRide,
                )
            }
            if (state.permissionsGranted && state.rememberedDevices.isNotEmpty()) {
                item {
                    SectionTitle(
                        title = stringResource(R.string.my_equipment),
                        meta = pluralStringResource(
                            R.plurals.saved_equipment_count,
                            state.rememberedDevices.size,
                            state.rememberedDevices.size,
                        ),
                    )
                }
                items(state.rememberedDevices, key = { it.address }) { device ->
                    EquipmentCard(
                        device = device,
                        nearbyDevice = nearbyByAddress[device.address],
                        connection = state.connection,
                        onOpenDetails = { selectedEquipmentAddress = device.address },
                        onConnect = onConnect,
                        onConnectRemembered = onConnectRemembered,
                        onDisconnect = onDisconnect,
                    )
                }
            }
            if (state.permissionsGranted && showNearbySection) {
                item {
                    NearbyDevicesHeader(
                        visibleCount = visibleNearbyDevices.size,
                        showOnlyTrainerDevices = showOnlyTrainerDevices,
                        onShowOnlyTrainerDevicesChange = { showOnlyTrainerDevices = it },
                    )
                }
                if (hiddenOtherDeviceCount > 0) {
                    item { HiddenDevicesHint(hiddenOtherDeviceCount) }
                }
                if (visibleNearbyDevices.isEmpty()) {
                    item {
                        EmptyStatePanel(
                            if (showOnlyTrainerDevices && hiddenOtherDeviceCount > 0) {
                                stringResource(R.string.nearby_trainers_empty)
                            } else {
                                stringResource(R.string.nearby_devices_hint)
                            },
                        )
                    }
                } else {
                    items(visibleNearbyDevices, key = { it.address }) { device ->
                        DeviceRow(
                            device = device,
                            connection = state.connection,
                            onConnect = onConnect,
                            onDisconnect = onDisconnect,
                        )
                    }
                }
            }
        }

        if (selectedEquipment != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedEquipmentAddress = null },
                sheetState = equipmentSheetState,
                containerColor = AppBackground,
            ) {
                EquipmentDetailSheet(
                    device = selectedEquipment,
                    nearbyDevice = nearbyByAddress[selectedEquipment.address],
                    connection = state.connection,
                    onConnect = onConnect,
                    onConnectRemembered = onConnectRemembered,
                    onDisconnect = onDisconnect,
                    onRequestUnpair = { unpairCandidateAddress = selectedEquipment.address },
                )
            }
        }

        if (unpairCandidate != null) {
            val displayName = equipmentDisplayName(unpairCandidate, nearbyByAddress[unpairCandidate.address])
            AlertDialog(
                onDismissRequest = { unpairCandidateAddress = null },
                title = {
                    Text(
                        stringResource(R.string.unpair_confirm_title),
                        color = AppInk,
                    )
                },
                text = {
                    Text(
                        stringResource(R.string.unpair_confirm_body, displayName),
                        color = AppMuted,
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val address = unpairCandidate.address
                            if (selectedEquipmentAddress == address) {
                                selectedEquipmentAddress = null
                            }
                            unpairCandidateAddress = null
                            onForgetEquipment(address)
                        },
                    ) {
                        Text(
                            stringResource(R.string.unpair_confirm_action),
                            color = AppDanger,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { unpairCandidateAddress = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                containerColor = AppSurface,
                titleContentColor = AppInk,
                textContentColor = AppMuted,
            )
        }
    }
}

@Composable
private fun NearbyDevicesHeader(
    visibleCount: Int,
    showOnlyTrainerDevices: Boolean,
    onShowOnlyTrainerDevicesChange: (Boolean) -> Unit,
) {
    val onlyTrainersLabel = stringResource(R.string.only_trainers)

    Row(
        Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(R.string.nearby_devices),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AppInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (visibleCount > 0) {
                Text(
                    pluralStringResource(R.plurals.nearby_devices_count, visibleCount, visibleCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppMuted,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                onlyTrainersLabel,
                style = MaterialTheme.typography.labelMedium,
                color = AppMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Switch(
                modifier = Modifier.semantics {
                    contentDescription = onlyTrainersLabel
                },
                checked = showOnlyTrainerDevices,
                onCheckedChange = onShowOnlyTrainerDevicesChange,
            )
        }
    }
}

@Composable
private fun HiddenDevicesHint(hiddenCount: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = AppSurfaceMuted,
        border = BorderStroke(1.dp, AppBorder),
    ) {
        Text(
            pluralStringResource(R.plurals.hidden_ble_devices, hiddenCount, hiddenCount),
            modifier = Modifier.padding(10.dp),
            style = MaterialTheme.typography.bodySmall,
            color = AppMuted,
        )
    }
}

@Composable
private fun ConnectHero(state: TrackerUiState) {
    val deviceLabel = state.connection.deviceName
        ?: EQUIPMENT_MODEL_NAME
    val title = when {
        !state.permissionsGranted -> stringResource(R.string.connect_title_permission)
        state.connection.isConnected -> stringResource(R.string.connect_title_connected, deviceLabel)
        state.connection.isConnecting -> stringResource(R.string.connect_title_connecting)
        state.connection.isScanning -> stringResource(R.string.connect_title_scanning)
        else -> stringResource(R.string.connect_title_disconnected)
    }
    val subtitle = when {
        !state.permissionsGranted -> stringResource(R.string.connect_subtitle_permission)
        state.connection.isConnected -> stringResource(R.string.connect_subtitle_connected)
        state.connection.isConnecting -> stringResource(R.string.connect_subtitle_connecting)
        state.connection.isScanning -> stringResource(R.string.connect_subtitle_scanning)
        else -> stringResource(R.string.connect_subtitle_disconnected)
    }
    Panel {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppInk,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppMuted,
                )
            }
            StatusPill(state)
        }
    }
}

@Composable
private fun ConnectPrimaryActions(
    state: TrackerUiState,
    onGrantPermissions: () -> Unit,
    onScan: () -> Unit,
    onStopScan: () -> Unit,
    onOpenRide: () -> Unit,
) {
    when {
        !state.permissionsGranted -> {
            Button(
                onClick = onGrantPermissions,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppAccent),
            ) {
                Text(
                    stringResource(R.string.grant_permissions),
                    color = AppLightCardText,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        state.connection.isConnected -> {
            Button(
                onClick = onOpenRide,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppAccent),
            ) {
                Text(
                    stringResource(R.string.open_ride),
                    color = AppLightCardText,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        state.connection.isConnecting -> {
            Button(
                onClick = {},
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = AppSurfaceMuted,
                    disabledContentColor = AppMuted,
                ),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = AppMuted,
                    )
                    Text(stringResource(R.string.connect_title_connecting))
                }
            }
        }

        else -> {
            Button(
                onClick = if (state.connection.isScanning) onStopScan else onScan,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppAccent),
            ) {
                Text(
                    if (state.connection.isScanning) {
                        stringResource(R.string.stop_scan)
                    } else {
                        stringResource(R.string.scan)
                    },
                    color = AppLightCardText,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun EquipmentCard(
    device: RememberedDeviceEntity,
    nearbyDevice: BleDeviceItem?,
    connection: BleConnectionState,
    onOpenDetails: () -> Unit,
    onConnect: (BleDeviceItem) -> Unit,
    onConnectRemembered: (RememberedDeviceEntity) -> Unit,
    onDisconnect: () -> Unit,
) {
    val displayName = equipmentDisplayName(device, nearbyDevice)
    val statusLabel = equipmentStatusText(device.address, connection)
    val statusColor = equipmentStatusColor(device.address, connection)
    val connectEquipment = {
        if (nearbyDevice != null) {
            onConnect(nearbyDevice)
        } else {
            onConnectRemembered(device)
        }
    }

    Panel(
        modifier = Modifier.clickable(
            role = Role.Button,
            onClick = onOpenDetails,
        ),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            EquipmentThumbnail(Modifier.size(82.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    EQUIPMENT_MODEL_NAME,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AppInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    statusLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = statusColor,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        DeviceActionButton(
            address = device.address,
            connection = connection,
            onConnect = connectEquipment,
            onDisconnect = onDisconnect,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            connectLabel = stringResource(R.string.connect_equipment),
        )
    }
}

@Composable
private fun EquipmentDetailSheet(
    device: RememberedDeviceEntity,
    nearbyDevice: BleDeviceItem?,
    connection: BleConnectionState,
    onConnect: (BleDeviceItem) -> Unit,
    onConnectRemembered: (RememberedDeviceEntity) -> Unit,
    onDisconnect: () -> Unit,
    onRequestUnpair: () -> Unit,
) {
    val displayName = equipmentDisplayName(device, nearbyDevice)
    val bluetoothName = nearbyDevice?.name ?: device.name ?: stringResource(R.string.unknown_device)
    val statusLabel = equipmentStatusText(device.address, connection)
    val statusColor = equipmentStatusColor(device.address, connection)
    val connectEquipment = {
        if (nearbyDevice != null) {
            onConnect(nearbyDevice)
        } else {
            onConnectRemembered(device)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .padding(bottom = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.equipment_details),
            style = MaterialTheme.typography.titleMedium,
            color = AppInk,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(18.dp))
        EquipmentThumbnail(Modifier.size(184.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            EQUIPMENT_MODEL_NAME,
            style = MaterialTheme.typography.headlineSmall,
            color = AppInk,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = AppMuted,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(14.dp))
        Text(
            statusLabel,
            style = MaterialTheme.typography.titleMedium,
            color = statusColor,
            fontWeight = FontWeight.Medium,
        )
        Spacer(Modifier.height(18.dp))
        DeviceActionButton(
            address = device.address,
            connection = connection,
            onConnect = connectEquipment,
            onDisconnect = onDisconnect,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            connectLabel = stringResource(R.string.connect_equipment),
        )
        Spacer(Modifier.height(22.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = AppSurface,
            border = BorderStroke(1.dp, AppBorder),
        ) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                EquipmentDetailRow(
                    label = stringResource(R.string.bluetooth_name),
                    value = bluetoothName,
                )
                HorizontalDivider(color = AppBorder)
                EquipmentDetailRow(
                    label = stringResource(R.string.equipment_model),
                    value = EQUIPMENT_MODEL_NAME,
                )
                HorizontalDivider(color = AppBorder)
                EquipmentDetailRow(
                    label = stringResource(R.string.device_id),
                    value = device.address,
                )
                HorizontalDivider(color = AppBorder)
                if (nearbyDevice != null) {
                    EquipmentDetailRow(
                        label = stringResource(R.string.nearby_now),
                        value = stringResource(R.string.rssi_value, nearbyDevice.rssi),
                    )
                    HorizontalDivider(color = AppBorder)
                }
                EquipmentDetailRow(
                    label = stringResource(R.string.last_connected),
                    value = formatDate(device.lastConnectedMillis),
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        TextButton(onClick = onRequestUnpair) {
            Text(
                stringResource(R.string.unpair),
                color = AppDanger,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun EquipmentDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = AppInk,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            value,
            modifier = Modifier.weight(1.2f),
            style = MaterialTheme.typography.bodyMedium,
            color = AppMuted,
            textAlign = TextAlign.End,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun EquipmentThumbnail(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = AppSurfaceMuted,
        border = BorderStroke(1.dp, AppBorder),
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

private fun equipmentDisplayName(
    device: RememberedDeviceEntity,
    nearbyDevice: BleDeviceItem?,
): String = nearbyDevice?.name ?: device.name ?: EQUIPMENT_MODEL_NAME

@Composable
private fun equipmentStatusText(address: String, connection: BleConnectionState): String {
    val isActiveDevice = connection.deviceAddress == address
    return when {
        isActiveDevice && connection.isConnected -> stringResource(R.string.connected)
        isActiveDevice && connection.isConnecting -> stringResource(R.string.connect_title_connecting)
        else -> stringResource(R.string.not_connected)
    }
}

@Composable
private fun equipmentStatusColor(address: String, connection: BleConnectionState): Color {
    val isActiveDevice = connection.deviceAddress == address
    return when {
        isActiveDevice && connection.isConnected -> AppSuccess
        isActiveDevice && connection.isConnecting -> AppWarning
        else -> AppMuted
    }
}

@Composable
private fun DeviceRow(
    device: BleDeviceItem,
    connection: BleConnectionState,
    onConnect: (BleDeviceItem) -> Unit,
    onDisconnect: () -> Unit,
) {
    val statusLabel = equipmentStatusText(device.address, connection)
    val statusColor = equipmentStatusColor(device.address, connection)
    Panel {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    device.name ?: stringResource(R.string.unknown_device),
                    fontWeight = FontWeight.SemiBold,
                    color = AppInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    statusLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    deviceTypeLabel(device.type),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            DeviceActionButton(
                address = device.address,
                connection = connection,
                onConnect = { onConnect(device) },
                onDisconnect = onDisconnect,
            )
        }
    }
}

@Composable
private fun DeviceActionButton(
    address: String,
    connection: BleConnectionState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier.width(136.dp).heightIn(min = 48.dp),
    connectLabel: String? = null,
) {
    val isActiveDevice = connection.deviceAddress == address
    val isConnectingDevice = connection.isConnecting && isActiveDevice
    val isConnectedDevice = connection.isConnected && isActiveDevice
    val disableInactiveDevice = (connection.isConnecting || connection.isConnected) && !isActiveDevice
    val idleLabel = connectLabel ?: stringResource(R.string.connect)

    when {
        isConnectingDevice -> {
            OutlinedButton(
                onClick = onDisconnect,
                modifier = modifier,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppAccent),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = AppAccent,
                    )
                    Text(
                        stringResource(R.string.cancel),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        isConnectedDevice -> {
            OutlinedButton(
                onClick = onDisconnect,
                modifier = modifier,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppAccent),
            ) {
                Text(
                    stringResource(R.string.disconnect),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        else -> {
            val enabled = !disableInactiveDevice
            Button(
                onClick = onConnect,
                enabled = enabled,
                modifier = modifier,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppAccent,
                    disabledContainerColor = AppSurfaceMuted,
                    disabledContentColor = AppMuted,
                ),
            ) {
                Text(
                    idleLabel,
                    color = if (enabled) AppLightCardText else AppMuted,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun deviceTypeLabel(type: BleDeviceType): String = when (type) {
    BleDeviceType.Trainer -> stringResource(R.string.device_type_trainer)
    BleDeviceType.CyclingDevice -> stringResource(R.string.device_type_cycling)
    BleDeviceType.Other -> stringResource(R.string.device_type_other)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RideScreen(
    state: TrackerUiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRequestFinish: () -> Unit,
    onDismissFinish: () -> Unit,
    onConfirmFinish: () -> Unit,
    onConfirmGoalOverride: (WorkoutGoal) -> Unit,
    onClearFinishedRide: () -> Unit,
    onOpenFinishedRide: () -> Unit,
) {
    var showGoalSheet by remember { mutableStateOf(false) }
    var allowGoalSheetDismiss by remember { mutableStateOf(false) }
    val goalSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { targetValue ->
            targetValue != SheetValue.Hidden || allowGoalSheetDismiss
        },
    )
    val goalSheetScope = rememberCoroutineScope()

    fun dismissGoalSheet() {
        allowGoalSheetDismiss = true
        goalSheetScope.launch {
            goalSheetState.hide()
            showGoalSheet = false
            allowGoalSheetDismiss = false
        }
    }

    if (state.rideStage == RideStage.Results && state.lastFinishedSession != null && !state.isWorkoutRunning) {
        TriumphScreen(
            session = state.lastFinishedSession,
            samples = state.lastFinishedSamples,
            baseline = state.personalBaseline,
            unitSystem = state.settings.unitSystem,
            onDone = onClearFinishedRide,
            onOpenHistory = onOpenFinishedRide,
        )
        return
    }

    if (state.isWorkoutRunning) {
        ActiveRideScreen(
            state = state,
            onPause = onPause,
            onResume = onResume,
            onRequestFinish = onRequestFinish,
            onDismissFinish = onDismissFinish,
            onConfirmFinish = onConfirmFinish,
        )
    } else {
        RidePlanningScreen(
            state = state,
            onStart = onStart,
            onEditGoal = { showGoalSheet = true },
        )
    }

    if (showGoalSheet) {
        ModalBottomSheet(
            onDismissRequest = { dismissGoalSheet() },
            sheetState = goalSheetState,
            dragHandle = null,
            containerColor = AppSurface,
            contentColor = AppInk,
        ) {
            GoalOverrideSheet(
                state = state,
                onDismiss = { dismissGoalSheet() },
                onConfirmGoal = { goal ->
                    onConfirmGoalOverride(goal)
                    dismissGoalSheet()
                },
            )
        }
    }
}

@Composable
private fun RidePlanningScreen(
    state: TrackerUiState,
    onStart: () -> Unit,
    onEditGoal: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RidePlanningHero(state, onStart)
        PlannedGoalPanel(state, onEditGoal)
        BaselineMediansPanel(state)
    }
}

@Composable
private fun ActiveRideScreen(
    state: TrackerUiState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRequestFinish: () -> Unit,
    onDismissFinish: () -> Unit,
    onConfirmFinish: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        WorkoutContextStrip(state)
        RaceTrackPanel(state, onEditGoal = {}, showEditGoal = false)
        PrimaryMetricCard(state, onEditGoal = {}, showEditGoal = false)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HeartRateCard(state, Modifier.weight(1f))
            PaceCard(state, Modifier.weight(1f))
        }
        ResistanceControlPanel(state)
        ActiveRideControls(
            state = state,
            onPause = onPause,
            onResume = onResume,
            onRequestFinish = onRequestFinish,
            onDismissFinish = onDismissFinish,
            onConfirmFinish = onConfirmFinish,
        )
    }
}

@Composable
private fun RidePlanningHero(state: TrackerUiState, onStart: () -> Unit) {
    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = onStart,
            enabled = state.connection.isConnected,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppAccent,
                disabledContainerColor = AppSurfaceMuted,
                disabledContentColor = AppMuted,
            ),
        ) {
            Text(
                stringResource(R.string.start_ride),
                color = if (state.connection.isConnected) AppLightCardText else AppMuted,
                fontWeight = FontWeight.SemiBold,
            )
        }
        if (!state.connection.isConnected) {
            Text(
                stringResource(R.string.ride_plan_connect_required),
                style = MaterialTheme.typography.bodySmall,
                color = AppWarning,
            )
        }
    }
}

@Composable
private fun PlannedGoalPanel(state: TrackerUiState, onEditGoal: () -> Unit) {
    val goal = state.workoutGoal
    val unitSystem = state.settings.unitSystem
    val target = goal.targetValue?.let { formatRaceMetric(goal.type, it, unitSystem) }
        ?: stringResource(R.string.goal_free_ride)
    val source = when {
        !goal.isActive -> stringResource(R.string.goal_not_set)
        goal.source == GoalSource.Manual -> stringResource(R.string.goal_manual)
        else -> stringResource(R.string.goal_from_median)
    }
    Panel {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.goal),
                    style = MaterialTheme.typography.titleSmall,
                    color = AppInk,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    source,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppMuted,
                )
            }
            TextButton(onClick = onEditGoal) {
                Text(stringResource(R.string.edit_goal), color = AppAccent)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            target,
            style = MaterialTheme.typography.headlineSmall,
            color = AppCyan,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun BaselineMediansPanel(state: TrackerUiState) {
    val baseline = state.personalBaseline
    val unitSystem = state.settings.unitSystem
    Panel {
        Text(
            stringResource(R.string.baseline_medians),
            style = MaterialTheme.typography.titleSmall,
            color = AppInk,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryStat(
                stringResource(R.string.median_distance),
                MeasurementFormatter.distance(baseline.medianDistanceMeters, unitSystem),
                Modifier.weight(1f),
            )
            SummaryStat(stringResource(R.string.median_calories), baseline.medianCalories?.toString() ?: "-", Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryStat(
                stringResource(R.string.median_duration),
                baseline.medianDistanceDurationSeconds?.let(::formatDurationSeconds)
                    ?: baseline.medianCaloriesDurationSeconds?.let(::formatDurationSeconds)
                    ?: "-",
                Modifier.weight(1f),
            )
            SummaryStat(stringResource(R.string.days_streak), baseline.currentStreakDays.toString(), Modifier.weight(1f))
        }
    }
}

@Composable
private fun WorkoutTopActionStrip(
    state: TrackerUiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
) {
    when {
        !state.isWorkoutRunning -> {
            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppAccent),
            ) {
                Text(stringResource(R.string.start_ride), color = AppLightCardText, fontWeight = FontWeight.SemiBold)
            }
        }
        state.isWorkoutPaused -> {
            Button(
                onClick = onResume,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppAccent),
            ) {
                Text(stringResource(R.string.resume), color = AppLightCardText, fontWeight = FontWeight.SemiBold)
            }
        }
        else -> {
            OutlinedButton(
                onClick = onPause,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppAccent),
            ) {
                Text(stringResource(R.string.pause), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun WorkoutContextStrip(state: TrackerUiState) {
    val goalProgress = RaceCalculator.goalProgressState(
        goal = state.workoutGoal,
        currentDistanceMeters = state.currentMetrics.distanceMeters,
        currentCalories = state.currentMetrics.calories,
    )
    val statusText = when {
        state.isWorkoutPaused -> stringResource(R.string.paused)
        state.isWorkoutRunning -> stringResource(R.string.recording)
        else -> stringResource(R.string.not_recording)
    }
    val statusColor = when {
        state.isWorkoutPaused -> AppWarning
        state.isWorkoutRunning -> AppSuccess
        else -> AppMuted
    }
    val baseline = state.personalBaseline
    val paceText = when {
        goalProgress.completed -> stringResource(R.string.race_goal_reached)
        state.ghostRace.isActive && state.ghostRace.zone == RaceZone.Ahead -> stringResource(R.string.race_on_pace)
        state.ghostRace.isActive && state.ghostRace.zone == RaceZone.Behind -> stringResource(R.string.race_behind)
        state.ghostRace.isActive -> stringResource(R.string.race_steady)
        else -> stringResource(R.string.race_need_more_rides)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurfaceMuted),
        border = BorderStroke(1.dp, AppBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MiniChip(statusText, statusColor)
            MiniChip(
                stringResource(R.string.days_streak_value, baseline.currentStreakDays),
                if (baseline.currentStreakDays > 0) AppGold else AppMuted,
            )
            MiniChip(stringResource(R.string.week_rides_value, baseline.ridesThisWeek), AppCyan)
            MiniChip(paceText, raceColor(state.ghostRace, goalProgress.completed), Modifier.weight(1f))
        }
    }
}

@Composable
private fun MiniChip(text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.heightIn(min = 30.dp),
        shape = RoundedCornerShape(50),
        color = color.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.22f)),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun RaceTrackPanel(
    state: TrackerUiState,
    onEditGoal: () -> Unit,
    showEditGoal: Boolean = true,
) {
    val race = state.ghostRace
    val goal = state.workoutGoal
    val goalProgress = RaceCalculator.goalProgressState(
        goal = goal,
        currentDistanceMeters = state.currentMetrics.distanceMeters,
        currentCalories = state.currentMetrics.calories,
    )
    val color = raceColor(race, goalProgress.completed)
    val raceAlpha = if (isHrDanger(state)) 0.42f else 1f
    val unitSystem = state.settings.unitSystem
    val targetText = goal.targetValue?.let { formatRaceMetric(goal.type, it, unitSystem) }
        ?: stringResource(R.string.no_goal)

    Card(
        modifier = Modifier.fillMaxWidth().alpha(raceAlpha),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.24f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.race_with_shadow),
                        style = MaterialTheme.typography.titleMedium,
                        color = AppInk,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        stringResource(R.string.race_shadow_explainer),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "${stringResource(R.string.goal)}: $targetText",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (showEditGoal) {
                    TextButton(onClick = onEditGoal) {
                        Text(stringResource(R.string.edit_goal), color = AppAccent)
                    }
                }
            }
            RaceProgressLine(
                label = stringResource(R.string.you_today),
                value = when {
                    goalProgress.isActive -> formatRaceMetric(goal.type, goalProgress.userValue, unitSystem)
                    goal.type == GoalType.None -> formatRaceMetric(GoalType.Distance, goalProgress.userValue, unitSystem)
                    else -> "-"
                },
                progress = goalProgress.progress,
                color = color,
            )
            RaceProgressLine(
                label = stringResource(R.string.your_median),
                value = if (race.isActive) {
                    formatRaceMetric(goal.type, race.ghostValue, unitSystem)
                } else {
                    stringResource(R.string.need_more_rides)
                },
                progress = race.ghostProgress,
                color = AppMuted,
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = color.copy(alpha = 0.11f),
                border = BorderStroke(1.dp, color.copy(alpha = 0.20f)),
            ) {
                Text(
                raceDeltaText(state),
                    modifier = Modifier.padding(10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = color,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun RaceProgressLine(
    label: String,
    value: String,
    progress: Float,
    color: Color,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = AppMuted)
            Text(value, style = MaterialTheme.typography.labelMedium, color = AppInk, fontWeight = FontWeight.SemiBold)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(50))
                .background(AppSurfaceMuted),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(14.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color),
            )
        }
    }
}

@Composable
private fun PrimaryMetricCard(
    state: TrackerUiState,
    onEditGoal: () -> Unit,
    showEditGoal: Boolean = true,
) {
    val goal = state.workoutGoal
    val unitSystem = state.settings.unitSystem
    val value = when (goal.type) {
        GoalType.Distance -> state.currentMetrics.distanceMeters
            ?.let { MeasurementFormatter.distanceValue(it, unitSystem).format(2) }
            ?: "-"
        GoalType.Calories -> state.currentMetrics.calories?.toString() ?: "-"
        GoalType.None -> state.currentMetrics.distanceMeters
            ?.let { MeasurementFormatter.distanceValue(it, unitSystem).format(2) }
            ?: "-"
    }
    val unit = when (goal.type) {
        GoalType.Distance, GoalType.None -> MeasurementFormatter.distanceUnit(unitSystem)
        GoalType.Calories -> "kcal"
    }
    val label = when (goal.type) {
        GoalType.Distance -> stringResource(R.string.distance_goal)
        GoalType.Calories -> stringResource(R.string.calories_goal)
        GoalType.None -> stringResource(R.string.no_goal)
    }
    val source = when {
        !goal.isActive -> stringResource(R.string.goal_not_set)
        goal.source == GoalSource.Manual -> stringResource(R.string.goal_manual)
        else -> stringResource(R.string.goal_from_median)
    }
    val accent = if (goal.isActive) AppAccent else AppCyan

    Card(
        modifier = Modifier.fillMaxWidth().heightIn(min = 132.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurfaceMuted),
        border = BorderStroke(1.dp, accent.copy(alpha = if (goal.isActive) 0.42f else 0.24f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.labelMedium, color = AppMuted)
                    Surface(
                        modifier = Modifier.padding(top = 6.dp),
                        shape = RoundedCornerShape(50),
                        color = accent.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.22f)),
                    ) {
                        Text(
                            source,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (showEditGoal) {
                    TextButton(onClick = onEditGoal) {
                        Text(stringResource(R.string.edit_goal), color = AppAccent)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.SemiBold, color = AppInk)
                Spacer(Modifier.width(8.dp))
                Text(unit, style = MaterialTheme.typography.titleMedium, color = accent, modifier = Modifier.padding(bottom = 8.dp))
            }
        }
    }
}

@Composable
private fun HeartRateCard(state: TrackerUiState, modifier: Modifier = Modifier) {
    val heartRate = state.currentMetrics.heartRateBpm
    val danger = isHrDanger(state)
    val transition = rememberInfiniteTransition(label = "hr-alert")
    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (danger) 1.035f else 1f,
        animationSpec = infiniteRepeatable(tween(650), RepeatMode.Reverse),
        label = "hr-alert-scale",
    )
    val color = when {
        danger -> AppDanger
        heartRate != null && heartRate >= 155 -> AppWarning
        heartRate != null -> AppSuccess
        else -> AppMuted
    }
    val status = when {
        danger -> stringResource(R.string.hr_safety_alert)
        heartRate != null && heartRate >= 155 -> stringResource(R.string.hr_high)
        heartRate != null -> stringResource(R.string.hr_steady)
        else -> stringResource(R.string.no_hr)
    }

    CockpitMetricCard(
        label = stringResource(R.string.heart_rate),
        value = heartRate?.toString() ?: "-",
        unit = "bpm",
        status = status,
        color = color,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
    )
}

@Composable
private fun PaceCard(state: TrackerUiState, modifier: Modifier = Modifier) {
    val metrics = state.currentMetrics
    val hasPower = metrics.powerWatts != null
    val value = if (hasPower) metrics.powerWatts?.toString() else metrics.cadenceRpm?.format(0)
    val label = if (hasPower) stringResource(R.string.power) else stringResource(R.string.cadence)
    val unit = if (hasPower) "W" else "rpm"
    val status = when {
        state.ghostRace.isActive && state.ghostRace.zone == RaceZone.Ahead -> stringResource(R.string.pace_above_typical)
        state.ghostRace.isActive && state.ghostRace.zone == RaceZone.Behind -> stringResource(R.string.pace_below_typical)
        else -> stringResource(R.string.pace_steady)
    }
    CockpitMetricCard(
        label = label,
        value = value ?: "-",
        unit = unit,
        status = status,
        color = AppCyan,
        modifier = modifier,
    )
}

@Composable
private fun CockpitMetricCard(
    label: String,
    value: String,
    unit: String,
    status: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.heightIn(min = 112.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.22f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = AppMuted, maxLines = 1)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, style = MaterialTheme.typography.headlineMedium, color = AppInk, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(6.dp))
                Text(unit, style = MaterialTheme.typography.bodySmall, color = color, modifier = Modifier.padding(bottom = 5.dp))
            }
            Text(status, style = MaterialTheme.typography.bodySmall, color = color, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ResistanceControlPanel(state: TrackerUiState) {
    val current = state.currentMetrics.resistanceLevel
    val canSet = state.connection.capabilities.canSetResistance
    Panel {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.manual_resistance),
                    style = MaterialTheme.typography.titleSmall,
                    color = AppInk,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    stringResource(R.string.telemetry_resistance),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppMuted,
                )
            }
            MiniChip(stringResource(R.string.manual_only), AppCyan)
        }
        Spacer(Modifier.height(12.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = AppSurfaceMuted,
        ) {
            Row(
                Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.current_load_value, current?.format(1) ?: "-"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppInk,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    stringResource(R.string.manual_only),
                    style = MaterialTheme.typography.labelSmall,
                    color = AppMuted,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            if (canSet) stringResource(R.string.remote_control_pending) else stringResource(R.string.remote_control_unavailable),
            style = MaterialTheme.typography.bodySmall,
            color = AppMuted,
        )
    }
}

@Composable
private fun ActiveRideControls(
    state: TrackerUiState,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRequestFinish: () -> Unit,
    onDismissFinish: () -> Unit,
    onConfirmFinish: () -> Unit,
) {
    Panel {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (state.isWorkoutPaused) {
                    Button(
                        onClick = onResume,
                        enabled = state.isWorkoutRunning,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppAccent),
                    ) {
                        Text(stringResource(R.string.resume), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                } else {
                    OutlinedButton(
                        onClick = onPause,
                        enabled = state.isWorkoutRunning,
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppAccent),
                    ) {
                        Text(stringResource(R.string.pause), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                OutlinedButton(
                    onClick = onRequestFinish,
                    enabled = state.isWorkoutRunning,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppAccent),
                ) {
                    Text(stringResource(R.string.finish), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (state.showFinishConfirmation) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = AppAccentSoft,
                    border = BorderStroke(1.dp, AppAccent.copy(alpha = 0.22f)),
                ) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(R.string.finish_confirmation),
                            style = MaterialTheme.typography.bodySmall,
                            color = AppInk,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onDismissFinish,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppMuted),
                            ) {
                                Text(stringResource(R.string.cancel))
                            }
                            Button(
                                onClick = onConfirmFinish,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = AppAccent),
                            ) {
                                Text(stringResource(R.string.confirm_finish))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutControls(
    state: TrackerUiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRequestFinish: () -> Unit,
    onDismissFinish: () -> Unit,
    onConfirmFinish: () -> Unit,
) {
    Panel {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onStart,
                enabled = !state.isWorkoutRunning,
                modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppAccent),
            ) {
                Text(stringResource(R.string.start))
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (state.isWorkoutPaused) {
                    Button(
                        onClick = onResume,
                        enabled = state.isWorkoutRunning,
                        modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AppAccent),
                    ) {
                        Text(stringResource(R.string.resume), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                } else {
                    OutlinedButton(
                        onClick = onPause,
                        enabled = state.isWorkoutRunning,
                        modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppAccent),
                    ) {
                        Text(stringResource(R.string.pause), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                OutlinedButton(
                    onClick = onRequestFinish,
                    enabled = state.isWorkoutRunning,
                    modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppAccent),
                ) {
                    Text(stringResource(R.string.finish), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (state.showFinishConfirmation) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = AppAccentSoft,
                    border = BorderStroke(1.dp, AppAccent.copy(alpha = 0.22f)),
                ) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(R.string.finish_confirmation),
                            style = MaterialTheme.typography.bodySmall,
                            color = AppInk,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onDismissFinish,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppMuted),
                            ) {
                                Text(stringResource(R.string.cancel))
                            }
                            Button(
                                onClick = onConfirmFinish,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = AppAccent),
                            ) {
                                Text(stringResource(R.string.confirm_finish))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalOverrideSheet(
    state: TrackerUiState,
    onDismiss: () -> Unit,
    onConfirmGoal: (WorkoutGoal) -> Unit,
) {
    val hapticView = LocalView.current
    val unitSystem = state.settings.unitSystem
    var selectedType by remember {
        mutableStateOf(state.workoutGoal.type.takeIf { it != GoalType.None } ?: GoalType.Distance)
    }
    var draftGoal by remember { mutableStateOf(state.workoutGoal) }
    var customExpanded by remember { mutableStateOf(false) }
    var customInput by remember { mutableStateOf("") }
    var customError by remember { mutableStateOf<GoalInputError?>(null) }

    fun withHaptic(action: () -> Unit) {
        hapticView.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        action()
    }

    fun resetCustomInput() {
        customExpanded = false
        customInput = ""
        customError = null
    }

    fun updateCustomInput(input: String) {
        customInput = input
        val parsed = GoalInputParser.parse(selectedType, input, unitSystem)
        if (parsed.isValid) {
            customError = null
            draftGoal = parsed.goal ?: draftGoal
        } else {
            customError = parsed.error
        }
    }

    fun expandCustomInput() {
        customExpanded = true
        updateCustomInput(GoalInputParser.inputText(draftGoal, selectedType, unitSystem))
    }

    val medianGoal = medianGoalForType(selectedType, state.personalBaseline)
    val medianSubtitle = medianGoal?.targetValue?.let {
        stringResource(R.string.goal_reset_to, formatRaceMetric(selectedType, it, unitSystem))
    } ?: stringResource(R.string.need_more_rides)
    val customErrorMessage = customError?.let { stringResource(goalInputErrorStringRes(it)) }
    val confirmEnabled = customError == null && (draftGoal.isActive || draftGoal.type == GoalType.None)
    val stepOneTitle = when (selectedType) {
        GoalType.Distance -> stringResource(
            R.string.add_distance_unit,
            "1",
            MeasurementFormatter.distanceUnit(unitSystem),
        )
        GoalType.Calories -> stringResource(R.string.add_fifty_kcal)
        GoalType.None -> stringResource(
            R.string.add_distance_unit,
            "1",
            MeasurementFormatter.distanceUnit(unitSystem),
        )
    }
    val stepTwoTitle = when (selectedType) {
        GoalType.Distance -> stringResource(
            R.string.add_distance_unit,
            "2",
            MeasurementFormatter.distanceUnit(unitSystem),
        )
        GoalType.Calories -> stringResource(R.string.add_hundred_kcal)
        GoalType.None -> stringResource(
            R.string.add_distance_unit,
            "2",
            MeasurementFormatter.distanceUnit(unitSystem),
        )
    }
    val stepOneSubtitle = when (selectedType) {
        GoalType.Distance -> stringResource(R.string.goal_step_small_distance)
        GoalType.Calories -> stringResource(R.string.goal_step_small_calories)
        GoalType.None -> ""
    }
    val stepTwoSubtitle = when (selectedType) {
        GoalType.Distance -> stringResource(R.string.goal_step_large_distance)
        GoalType.Calories -> stringResource(R.string.goal_step_large_calories)
        GoalType.None -> ""
    }

    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.68f)
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GoalSheetHandle(onDismiss = onDismiss)
        Text(
            stringResource(R.string.edit_goal),
            style = MaterialTheme.typography.titleLarge,
            color = AppInk,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            goalSheetSummary(state.workoutGoal, unitSystem),
            style = MaterialTheme.typography.bodySmall,
            color = AppMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            stringResource(R.string.goal_sheet_new, goalSheetValue(draftGoal, unitSystem)),
            style = MaterialTheme.typography.bodyMedium,
            color = AppCyan,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        GoalCustomValueControl(
            type = selectedType,
            unitSystem = unitSystem,
            expanded = customExpanded,
            input = customInput,
            errorMessage = customErrorMessage,
            onExpand = {
                withHaptic { expandCustomInput() }
            },
            onInputChange = { updateCustomInput(it) },
        )

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                GoalTypeTab(
                    text = stringResource(R.string.distance),
                    selected = selectedType == GoalType.Distance,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        withHaptic {
                            selectedType = GoalType.Distance
                            draftGoal = medianGoalForType(GoalType.Distance, state.personalBaseline)
                                ?: WorkoutGoal.none(GoalSource.Manual)
                            resetCustomInput()
                        }
                    },
                )
                GoalTypeTab(
                    text = stringResource(R.string.calories),
                    selected = selectedType == GoalType.Calories,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        withHaptic {
                            selectedType = GoalType.Calories
                            draftGoal = medianGoalForType(GoalType.Calories, state.personalBaseline)
                                ?: WorkoutGoal.none(GoalSource.Manual)
                            resetCustomInput()
                        }
                    },
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    GoalActionTile(
                        title = stepOneTitle,
                        subtitle = stepOneSubtitle,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            withHaptic {
                                draftGoal = incrementDraftGoal(
                                    draftGoal = draftGoal,
                                    state = state,
                                    type = selectedType,
                                    distanceAmount = 1.0,
                                    calories = 50,
                                )
                                resetCustomInput()
                            }
                        },
                    )
                    GoalActionTile(
                        title = stringResource(R.string.use_median),
                        subtitle = medianSubtitle,
                        enabled = medianGoal != null,
                        emphasized = medianGoal != null,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            medianGoal?.let { goal ->
                                withHaptic {
                                    draftGoal = goal
                                    resetCustomInput()
                                }
                            }
                        },
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    GoalActionTile(
                        title = stepTwoTitle,
                        subtitle = stepTwoSubtitle,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            withHaptic {
                                draftGoal = incrementDraftGoal(
                                    draftGoal = draftGoal,
                                    state = state,
                                    type = selectedType,
                                    distanceAmount = 2.0,
                                    calories = 100,
                                )
                                resetCustomInput()
                            }
                        },
                    )
                    GoalActionTile(
                        title = stringResource(R.string.no_goal),
                        subtitle = stringResource(R.string.goal_free_ride),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            withHaptic {
                                draftGoal = WorkoutGoal.none(GoalSource.Manual)
                                resetCustomInput()
                            }
                        },
                    )
                }
            }
        }

        val confirmNewGoalText = stringResource(R.string.confirm_new_goal)

        Button(
            onClick = {
                withHaptic { onConfirmGoal(draftGoal) }
            },
            enabled = confirmEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .semantics { contentDescription = confirmNewGoalText },
            colors = ButtonDefaults.buttonColors(containerColor = AppAccent),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(
                confirmNewGoalText,
                color = AppBackground,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun GoalSheetHandle(onDismiss: () -> Unit) {
    val handleDescription = stringResource(R.string.goal_sheet_drag_handle)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .semantics {
                contentDescription = handleDescription
                role = Role.Button
            }
            .pointerInput(onDismiss) {
                var dragDistance = 0f
                detectVerticalDragGestures(
                    onDragStart = { dragDistance = 0f },
                    onVerticalDrag = { _, dragAmount ->
                        if (dragAmount > 0f) {
                            dragDistance += dragAmount
                        }
                        if (dragDistance > 48f) {
                            dragDistance = 0f
                            onDismiss()
                        }
                    },
                    onDragEnd = { dragDistance = 0f },
                    onDragCancel = { dragDistance = 0f },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(width = 46.dp, height = 5.dp)
                .clip(RoundedCornerShape(50))
                .background(AppMuted.copy(alpha = 0.62f)),
        )
    }
}

@Composable
private fun GoalTypeTab(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val stateText = stringResource(
        if (selected) R.string.goal_type_selected else R.string.goal_type_not_selected,
    )
    Surface(
        modifier = modifier
            .height(56.dp)
            .semantics {
                contentDescription = text
                stateDescription = stateText
                role = Role.Tab
            }
            .clickable(role = Role.Tab, onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) AppAccentSoft else AppSurfaceMuted,
        border = BorderStroke(1.dp, if (selected) AppAccent.copy(alpha = 0.72f) else AppBorder),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text,
                color = if (selected) AppAccent else AppMuted,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun GoalActionTile(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    emphasized: Boolean = false,
    onClick: () -> Unit,
) {
    val tileDescription = if (subtitle.isBlank()) title else "$title. $subtitle"
    Surface(
        modifier = modifier
            .height(80.dp)
            .semantics {
                contentDescription = tileDescription
                role = Role.Button
            }
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = when {
            !enabled -> AppSurfaceMuted.copy(alpha = 0.44f)
            emphasized -> AppAccentSoft
            else -> AppSurfaceMuted
        },
        border = BorderStroke(
            1.dp,
            when {
                !enabled -> AppBorder.copy(alpha = 0.52f)
                emphasized -> AppAccent.copy(alpha = 0.34f)
                else -> AppBorder
            },
        ),
    ) {
        Column(
            Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                title,
                color = if (enabled) AppInk else AppMuted.copy(alpha = 0.70f),
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle.isNotBlank()) {
                Text(
                    subtitle,
                    color = if (enabled) AppMuted else AppMuted.copy(alpha = 0.62f),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GoalCustomValueControl(
    type: GoalType,
    unitSystem: UnitSystem,
    expanded: Boolean,
    input: String,
    errorMessage: String?,
    onExpand: () -> Unit,
    onInputChange: (String) -> Unit,
) {
    val customValueText = stringResource(R.string.goal_custom_value)
    val customValueHint = stringResource(R.string.goal_custom_value_hint)
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(expanded) {
        if (expanded) {
            focusRequester.requestFocus()
            bringIntoViewRequester.bringIntoView()
            keyboard?.show()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .semantics {
                contentDescription = customValueText
                role = Role.Button
            }
            .clickable(enabled = !expanded, role = Role.Button, onClick = onExpand),
        shape = RoundedCornerShape(8.dp),
        color = AppSurfaceMuted,
        border = BorderStroke(1.dp, AppBorder),
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    customValueText,
                    color = AppInk,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    customValueHint,
                    color = AppMuted,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                when (type) {
                    GoalType.Distance -> MeasurementFormatter.distanceUnit(unitSystem)
                    GoalType.Calories -> "kcal"
                    GoalType.None -> ""
                },
                color = AppCyan,
                fontWeight = FontWeight.Bold,
            )
        }
    }

    if (expanded) {
        OutlinedTextField(
            value = input,
            onValueChange = onInputChange,
            modifier = Modifier
                .fillMaxWidth()
                .bringIntoViewRequester(bringIntoViewRequester)
                .focusRequester(focusRequester)
                .semantics {
                    errorMessage?.let { contentDescription = it }
                },
            label = {
                Text(
                    when (type) {
                        GoalType.Distance -> stringResource(R.string.distance_goal)
                        GoalType.Calories -> stringResource(R.string.calories_goal)
                        GoalType.None -> stringResource(R.string.goal)
                    },
                )
            },
            isError = errorMessage != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (type == GoalType.Distance) KeyboardType.Decimal else KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            supportingText = errorMessage?.let { message ->
                { Text(message, color = AppDanger) }
            },
        )
    }
}

private fun goalInputErrorStringRes(error: GoalInputError): Int {
    return when (error) {
        GoalInputError.Distance -> R.string.goal_custom_invalid_distance
        GoalInputError.Calories -> R.string.goal_custom_invalid_calories
    }
}

private fun medianGoalForType(type: GoalType, baseline: PersonalBaseline): WorkoutGoal? {
    return when (type) {
        GoalType.Distance -> if (baseline.hasDistanceBaseline) {
            WorkoutGoal.distance(baseline.medianDistanceMeters ?: 0.0, GoalSource.Median)
        } else {
            null
        }
        GoalType.Calories -> if (baseline.hasCaloriesBaseline) {
            WorkoutGoal.calories(baseline.medianCalories ?: 0, GoalSource.Median)
        } else {
            null
        }
        GoalType.None -> null
    }
}

private fun incrementDraftGoal(
    draftGoal: WorkoutGoal,
    state: TrackerUiState,
    type: GoalType,
    distanceAmount: Double,
    calories: Int,
): WorkoutGoal {
    return when (type) {
        GoalType.Distance -> {
            val base = when {
                draftGoal.type == GoalType.Distance -> draftGoal.targetDistanceMeters
                state.currentMetrics.distanceMeters != null -> state.currentMetrics.distanceMeters
                else -> state.personalBaseline.medianDistanceMeters
            } ?: 0.0
            WorkoutGoal.distance(
                base + MeasurementFormatter.distanceInputToMeters(distanceAmount, state.settings.unitSystem),
                GoalSource.Manual,
            )
        }
        GoalType.Calories -> {
            val base = when {
                draftGoal.type == GoalType.Calories -> draftGoal.targetCalories
                state.currentMetrics.calories != null -> state.currentMetrics.calories
                else -> state.personalBaseline.medianCalories
            } ?: 0
            WorkoutGoal.calories(base + calories, GoalSource.Manual)
        }
        GoalType.None -> draftGoal
    }
}

@Composable
private fun TriumphScreen(
    session: WorkoutSessionEntity,
    samples: List<WorkoutSampleEntity>,
    baseline: com.owlbike.v1tracker.race.PersonalBaseline,
    unitSystem: UnitSystem,
    onDone: () -> Unit,
    onOpenHistory: () -> Unit,
) {
    val context = LocalContext.current
    val goalType = GoalType.fromStorage(session.goalType)
    val actual = sessionActualValue(session, goalType)
    val median = sessionMedianValue(session, goalType)
    val target = sessionTargetValue(session, goalType)
    val beatMedian = actual != null && median != null && actual >= median
    val reachedGoal = actual != null && target != null && actual >= target
    val title = when {
        beatMedian -> stringResource(R.string.median_defeated)
        reachedGoal -> stringResource(R.string.goal_reached_title)
        else -> stringResource(R.string.ride_saved_title)
    }
    val accent = when {
        beatMedian || reachedGoal -> AppGold
        else -> AppCyan
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = AppSurface),
            border = BorderStroke(1.dp, accent.copy(alpha = 0.34f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    Modifier
                        .height(58.dp)
                        .width(58.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_success_check),
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(32.dp),
                    )
                }
                Text(title, style = MaterialTheme.typography.headlineSmall, color = AppInk, fontWeight = FontWeight.SemiBold)
                Text(
                    finishDeltaText(goalType, actual, median, unitSystem),
                    style = MaterialTheme.typography.bodyMedium,
                    color = accent,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    stringResource(R.string.next_ride_deadline),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppMuted,
                )
            }
        }
        Panel {
            Text(
                stringResource(R.string.habit_momentum),
                style = MaterialTheme.typography.titleSmall,
                color = AppInk,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryStat(stringResource(R.string.days_streak), baseline.currentStreakDays.toString(), Modifier.weight(1f))
                SummaryStat(stringResource(R.string.best_streak), baseline.bestStreakDays.toString(), Modifier.weight(1f))
            }
        }
        Panel {
            Text(
                stringResource(R.string.session_summary),
                style = MaterialTheme.typography.titleSmall,
                color = AppInk,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryStat(stringResource(R.string.duration), formatDuration(session.startTimeMillis, session.endTimeMillis), Modifier.weight(1f))
                SummaryStat(
                    stringResource(R.string.distance),
                    MeasurementFormatter.distance(session.totalDistanceMeters, unitSystem),
                    Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryStat(stringResource(R.string.calories), (session.totalCalories ?: "-").toString(), Modifier.weight(1f))
                SummaryStat(
                    "${stringResource(R.string.heart_rate)} ${stringResource(R.string.average_short)}",
                    "${session.averageHeartRateBpm?.format(0) ?: "-"} bpm",
                    Modifier.weight(1f),
                )
            }
        }
        Panel {
            Text(
                stringResource(R.string.export_workout),
                style = MaterialTheme.typography.titleSmall,
                color = AppInk,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.export_note),
                style = MaterialTheme.typography.bodySmall,
                color = AppMuted,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { shareWorkoutExport(context, session, samples, WorkoutExportFormat.Csv) },
                    enabled = samples.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AppAccent),
                ) {
                    Text(stringResource(R.string.export_csv), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                OutlinedButton(
                    onClick = { shareWorkoutExport(context, session, samples, WorkoutExportFormat.Tcx) },
                    enabled = samples.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppAccent),
                ) {
                    Text(stringResource(R.string.export_tcx), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onDone,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AppMuted),
            ) {
                Text(stringResource(R.string.done))
            }
            Button(
                onClick = onOpenHistory,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppAccent),
            ) {
                Text(stringResource(R.string.open_details_export))
            }
        }
    }
}

@Composable
private fun HistoryScreen(
    state: TrackerUiState,
    onSelect: (WorkoutSessionEntity) -> Unit,
    onBack: () -> Unit,
    onToggleWeek: (Long) -> Unit,
    onRequestDelete: (WorkoutSessionEntity) -> Unit,
    onCancelDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    val pendingDelete = state.pendingDeleteSession
    if (pendingDelete != null) {
        AlertDialog(
            onDismissRequest = onCancelDelete,
            title = {
                Text(
                    stringResource(R.string.delete_session_confirm_title),
                    color = AppInk,
                )
            },
            text = {
                Text(
                    stringResource(R.string.delete_session_confirm_body, formatDate(pendingDelete.startTimeMillis)),
                    color = AppMuted,
                )
            },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text(
                        stringResource(R.string.delete_session_confirm_action),
                        color = AppDanger,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelDelete) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = AppSurface,
            titleContentColor = AppInk,
            textContentColor = AppMuted,
        )
    }

    val selected = state.selectedSession
    if (selected != null) {
        SessionDetail(selected, state.selectedSamples, state.settings.unitSystem, onBack)
        return
    }
    val groups = buildHistoryWeekGroups(state.sessions, state.expandedHistoryWeekStarts)
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
    ) {
        if (state.sessions.isNotEmpty()) {
            item { WeeklySummaryPanel(state.sessions, state.settings.unitSystem) }
        }
        if (state.sessions.isEmpty()) {
            item { EmptyStatePanel(stringResource(R.string.history_empty)) }
        }
        groups.forEach { group ->
            item(key = "week-${group.weekStartMillis}") {
                HistoryWeekHeader(group, onToggleWeek)
            }
            if (group.isExpanded) {
                items(group.sessions, key = { it.id }) { session ->
                    SessionRow(
                        session = session,
                        onSelect = onSelect,
                        onRequestDelete = onRequestDelete,
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryWeekHeader(
    group: com.owlbike.v1tracker.history.HistoryWeekGroup,
    onToggleWeek: (Long) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onToggleWeek(group.weekStartMillis) },
        shape = RoundedCornerShape(8.dp),
        color = AppSurfaceMuted,
        border = BorderStroke(1.dp, AppBorder),
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    "${formatDateOnly(group.weekStartMillis)} - ${formatDateOnly(group.weekEndMillis)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = AppInk,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    stringResource(R.string.week_sessions_count, group.sessions.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppMuted,
                )
            }
            Text(
                if (group.isExpanded) stringResource(R.string.collapse_week) else stringResource(R.string.expand_week),
                color = AppAccent,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun SessionRow(
    session: WorkoutSessionEntity,
    onSelect: (WorkoutSessionEntity) -> Unit,
    onRequestDelete: (WorkoutSessionEntity) -> Unit,
) {
    var actionsExpanded by remember { mutableStateOf(false) }
    Panel {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    formatDate(session.startTimeMillis),
                    fontWeight = FontWeight.SemiBold,
                    color = AppInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "${stringResource(R.string.duration)}: ${formatDuration(session.startTimeMillis, session.endTimeMillis)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppMuted,
                )
                Text(
                    "${stringResource(R.string.samples)}: ${session.sampleCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppMuted,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { onSelect(session) }) {
                    Text(stringResource(R.string.open_session), color = AppAccent)
                }
                Box {
                    IconButton(onClick = { actionsExpanded = true }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_more_vert),
                            contentDescription = stringResource(R.string.more_actions),
                            tint = AppMuted,
                        )
                    }
                    DropdownMenu(
                        expanded = actionsExpanded,
                        onDismissRequest = { actionsExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete_session)) },
                            onClick = {
                                actionsExpanded = false
                                onRequestDelete(session)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklySummaryPanel(sessions: List<WorkoutSessionEntity>, unitSystem: UnitSystem) {
    val summary = weeklySummary(sessions)
    Panel {
        Text(
            stringResource(R.string.weekly_summary),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppInk,
        )
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryStat(stringResource(R.string.sessions), summary.sessionCount.toString(), Modifier.weight(1f))
            SummaryStat(stringResource(R.string.total_time), formatDurationSeconds(summary.durationSeconds), Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SummaryStat(
                stringResource(R.string.distance),
                MeasurementFormatter.distance(summary.distanceMeters, unitSystem),
                Modifier.weight(1f),
            )
            SummaryStat(stringResource(R.string.calories), (summary.calories ?: "-").toString(), Modifier.weight(1f))
        }
    }
}

@Composable
private fun SessionDetail(
    session: WorkoutSessionEntity,
    samples: List<WorkoutSampleEntity>,
    unitSystem: UnitSystem,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(
            onClick = onBack,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AppAccent),
        ) {
            Text(stringResource(R.string.back))
        }
        Panel {
            Text(
                stringResource(R.string.session_detail),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AppInk,
            )
            Text(
                formatDate(session.startTimeMillis),
                style = MaterialTheme.typography.bodySmall,
                color = AppMuted,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryStat(
                    stringResource(R.string.duration),
                    formatDuration(session.startTimeMillis, session.endTimeMillis),
                    Modifier.weight(1f),
                )
                SummaryStat(stringResource(R.string.samples), session.sampleCount.toString(), Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryStat(
                    stringResource(R.string.distance),
                    MeasurementFormatter.distance(session.totalDistanceMeters, unitSystem),
                    Modifier.weight(1f),
                )
                SummaryStat(stringResource(R.string.calories), (session.totalCalories ?: "-").toString(), Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryStat(
                    "${stringResource(R.string.power)} ${stringResource(R.string.average_short)}",
                    "${session.averagePowerWatts?.format(0) ?: "-"} W",
                    Modifier.weight(1f),
                )
                SummaryStat(
                    "${stringResource(R.string.cadence)} ${stringResource(R.string.average_short)}",
                    "${session.averageCadenceRpm?.format(0) ?: "-"} rpm",
                    Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(8.dp))
            SummaryStat(
                "${stringResource(R.string.heart_rate)} ${stringResource(R.string.average_short)}",
                "${session.averageHeartRateBpm?.format(0) ?: "-"} bpm",
                Modifier.fillMaxWidth(),
            )
        }
        Panel {
            Text(
                stringResource(R.string.export_workout),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AppInk,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.export_note),
                style = MaterialTheme.typography.bodySmall,
                color = AppMuted,
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { shareWorkoutExport(context, session, samples, WorkoutExportFormat.Csv) },
                    enabled = samples.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AppAccent),
                ) {
                    Text(stringResource(R.string.export_csv), maxLines = 1)
                }
                OutlinedButton(
                    onClick = { shareWorkoutExport(context, session, samples, WorkoutExportFormat.Tcx) },
                    enabled = samples.isNotEmpty(),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppAccent),
                ) {
                    Text(stringResource(R.string.export_tcx), maxLines = 1)
                }
            }
        }
        Panel {
            Text(
                "${stringResource(R.string.last_samples)}: ${samples.takeLast(20).size}/${samples.size}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = AppInk,
            )
            Spacer(Modifier.height(8.dp))
            if (samples.isEmpty()) {
                Text(
                    stringResource(R.string.latest_samples_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppMuted,
                )
            } else {
                samples.takeLast(20).forEach { sample ->
                    SampleLine(sample, unitSystem)
                }
            }
        }
    }
}

@Composable
private fun SampleLine(sample: WorkoutSampleEntity, unitSystem: UnitSystem) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        shape = RoundedCornerShape(8.dp),
        color = AppSurfaceMuted,
        border = BorderStroke(1.dp, AppBorder),
    ) {
        Text(
            "${sample.elapsedSeconds}s  " +
                "${MeasurementFormatter.speed(sample.speedKmh, unitSystem)}  " +
                "${sample.cadenceRpm?.format(0) ?: "-"} rpm  " +
                "${sample.powerWatts ?: "-"} W  " +
                "${sample.heartRateBpm ?: "-"} bpm  " +
                "L${sample.resistanceLevel?.format(1) ?: "-"}",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = AppInk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private data class SettingsChoice<T>(
    val value: T,
    val labelRes: Int,
)

@Composable
private fun ProfileScreen(
    state: TrackerUiState,
    onThemeModeChange: (ThemeMode) -> Unit,
    onUnitSystemChange: (UnitSystem) -> Unit,
    onLanguageModeChange: (LanguageMode) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SettingsPanel(
                settings = state.settings,
                onThemeModeChange = onThemeModeChange,
                onUnitSystemChange = onUnitSystemChange,
                onLanguageModeChange = onLanguageModeChange,
            )
        }
        item { SectionTitle(stringResource(R.string.profile_diagnostics)) }
        item { DiagnosticsActionsPanel(state) }
        item { CompatibilityPanel(state) }
        item { DiagnosticServicesPanel(state) }
        item { LatestDiagnosticPanel(state) }
        item { RawBleLogPanel(state) }
        item { SectionTitle(stringResource(R.string.profile_about)) }
        item { AboutContent() }
    }
}

@Composable
private fun SettingsPanel(
    settings: AppSettings,
    onThemeModeChange: (ThemeMode) -> Unit,
    onUnitSystemChange: (UnitSystem) -> Unit,
    onLanguageModeChange: (LanguageMode) -> Unit,
) {
    Panel {
        SettingsChoiceRow(
            title = stringResource(R.string.setting_language),
            selected = settings.languageMode,
            choices = listOf(
                SettingsChoice(LanguageMode.System, R.string.language_system),
                SettingsChoice(LanguageMode.Russian, R.string.language_russian),
                SettingsChoice(LanguageMode.English, R.string.language_english),
            ),
            onSelect = onLanguageModeChange,
        )
        HorizontalDivider(Modifier.padding(vertical = 12.dp), color = AppBorder)
        SettingsChoiceRow(
            title = stringResource(R.string.setting_theme),
            selected = settings.themeMode,
            choices = listOf(
                SettingsChoice(ThemeMode.Dark, R.string.theme_dark),
                SettingsChoice(ThemeMode.Light, R.string.theme_light),
            ),
            onSelect = onThemeModeChange,
        )
        HorizontalDivider(Modifier.padding(vertical = 12.dp), color = AppBorder)
        SettingsChoiceRow(
            title = stringResource(R.string.setting_units),
            selected = settings.unitSystem,
            choices = listOf(
                SettingsChoice(UnitSystem.Metric, R.string.units_metric),
                SettingsChoice(UnitSystem.Imperial, R.string.units_imperial),
            ),
            onSelect = onUnitSystemChange,
        )
    }
}

@Composable
private fun <T> SettingsChoiceRow(
    title: String,
    selected: T,
    choices: List<SettingsChoice<T>>,
    onSelect: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            color = AppInk,
            fontWeight = FontWeight.SemiBold,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            choices.forEach { choice ->
                val isSelected = choice.value == selected
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSelect(choice.value) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) AppAccentSoft else AppSurfaceMuted,
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) AppAccent.copy(alpha = 0.72f) else AppBorder,
                    ),
                ) {
                    Box(
                        Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            stringResource(choice.labelRes),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) AppAccent else AppMuted,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiagnosticsScreen(state: TrackerUiState) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DiagnosticsActionsPanel(state)
        CompatibilityPanel(state)
        DiagnosticServicesPanel(state)
        LatestDiagnosticPanel(state)
        RawBleLogPanel(state)
    }
}

@Composable
private fun DiagnosticsActionsPanel(state: TrackerUiState) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    val rawBleLogTitle = stringResource(R.string.raw_ble_log)
    val diagnosticsText = diagnosticsText(state, rawBleLogTitle)

    Panel {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    clipboard.setText(AnnotatedString(diagnosticsText))
                    copied = true
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (copied) stringResource(R.string.copied) else stringResource(R.string.copy_diagnostics))
            }
            OutlinedButton(
                onClick = { shareDiagnostics(context, diagnosticsText) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.share_diagnostics))
            }
        }
    }
}

@Composable
private fun DiagnosticServicesPanel(state: TrackerUiState) {
    Panel {
        val caps = state.connection.capabilities
        Text(stringResource(R.string.detected_services), fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text("FTMS: ${caps.hasFitnessMachineService}")
        Text("Indoor Bike Data: ${caps.hasIndoorBikeData}")
        Text("${stringResource(R.string.control_point_present)}: ${caps.hasFitnessMachineControlPoint}")
        Text("CSC: ${caps.hasCyclingSpeedCadence}")
        Text("HRS: ${caps.hasHeartRate}")
        Text("${stringResource(R.string.physical_control)}: ${stringResource(R.string.manual_only)}")
    }
}

@Composable
private fun LatestDiagnosticPanel(state: TrackerUiState) {
    Panel {
        Text(stringResource(R.string.latest_diagnostic), fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        SelectionContainer {
            Text(
                state.latestDiagnostic?.servicesText ?: stringResource(R.string.no_data),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun RawBleLogPanel(state: TrackerUiState) {
    Panel {
        Text(stringResource(R.string.raw_ble_log), fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        SelectionContainer {
            Column {
                state.controlLog.forEach {
                    Text(it, style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun AboutScreen() {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AboutContent()
    }
}

@Composable
private fun AboutContent() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AboutTextPanel(
            title = stringResource(R.string.about_title),
            body = stringResource(R.string.about_positioning),
            secondaryBody = stringResource(R.string.not_affiliated),
        )
        AboutTextPanel(
            title = stringResource(R.string.privacy_title),
            body = stringResource(R.string.privacy_body),
        )
        AboutTextPanel(
            title = stringResource(R.string.permissions_title),
            body = stringResource(R.string.permissions_body),
        )
        AboutTextPanel(
            title = stringResource(R.string.compatibility_title),
            body = stringResource(R.string.model_scope_note),
            secondaryBody = stringResource(R.string.connect_help),
        )
        AboutTextPanel(
            title = stringResource(R.string.release_scope_title),
            body = stringResource(R.string.release_scope_body),
        )
    }
}

@Composable
private fun AboutTextPanel(title: String, body: String, secondaryBody: String? = null) {
    Panel {
        Text(title, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            body,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (secondaryBody != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                secondaryBody,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CompatibilityPanel(state: TrackerUiState) {
    val caps = state.connection.capabilities
    val metrics = state.currentMetrics
    Panel {
        Text(stringResource(R.string.compatibility_title), fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(R.string.compatibility_intro),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.metric_availability), fontWeight = FontWeight.Medium)
        CapabilityLine(
            stringResource(R.string.speed),
            availabilityStatus(metrics.speedKmh != null, caps.hasIndoorBikeData || caps.hasCyclingSpeedCadence),
        )
        CapabilityLine(
            stringResource(R.string.cadence),
            availabilityStatus(metrics.cadenceRpm != null, caps.hasIndoorBikeData || caps.hasCyclingSpeedCadence),
        )
        CapabilityLine(
            stringResource(R.string.power),
            availabilityStatus(metrics.powerWatts != null, caps.hasIndoorBikeData),
        )
        CapabilityLine(
            stringResource(R.string.heart_rate),
            availabilityStatus(metrics.heartRateBpm != null, caps.hasHeartRate),
        )
        CapabilityLine(
            stringResource(R.string.telemetry_resistance),
            availabilityStatus(metrics.resistanceLevel != null, caps.hasFitnessMachineService),
        )
        Spacer(Modifier.height(8.dp))
        Text(stringResource(R.string.known_limits), fontWeight = FontWeight.Medium)
        Text(
            stringResource(R.string.manual_resistance_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.model_scope_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.not_affiliated),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CapabilityLine(label: String, statusRes: Int) {
    Text("$label: ${stringResource(statusRes)}")
}

private fun availabilityStatus(hasValue: Boolean, capabilityPresent: Boolean): Int {
    return when {
        hasValue -> R.string.status_detected
        capabilityPresent -> R.string.status_waiting
        else -> R.string.status_not_seen
    }
}

private fun diagnosticsText(state: TrackerUiState, rawBleLogTitle: String): String {
    val caps = state.connection.capabilities
    return buildString {
        appendLine("Owl Bike V1 Tracker diagnostics")
        appendLine("Status: ${state.connection.status}")
        appendLine("Device: ${state.connection.deviceName ?: "-"}")
        appendLine("Device address: hidden")
        appendLine("Connected: ${state.connection.isConnected}")
        appendLine("FTMS: ${caps.hasFitnessMachineService}")
        appendLine("Indoor Bike Data: ${caps.hasIndoorBikeData}")
        appendLine("FTMS Control Point present: ${caps.hasFitnessMachineControlPoint}")
        appendLine("CSC: ${caps.hasCyclingSpeedCadence}")
        appendLine("HRS: ${caps.hasHeartRate}")
        appendLine("Can set resistance: ${caps.canSetResistance}")
        appendLine("Physical load control: manual only")
        appendLine("Compatible device scope: YESOUL Bike V1 / YS-003")
        appendLine("Affiliation: not official YESOUL app; not affiliated with YESOUL")
        appendLine()
        appendLine("Latest GATT snapshot")
        appendLine(state.latestDiagnostic?.servicesText ?: "No data")
        appendLine()
        appendLine(rawBleLogTitle)
        state.controlLog.forEach { appendLine(it) }
    }
}

private fun shareWorkoutExport(
    context: Context,
    session: WorkoutSessionEntity,
    samples: List<WorkoutSampleEntity>,
    format: WorkoutExportFormat,
) {
    if (samples.isEmpty()) return

    val exportsDir = File(context.cacheDir, "exports").apply { mkdirs() }
    val file = File(exportsDir, WorkoutExporters.fileName(session, format))
    file.writeText(WorkoutExporters.export(session, samples, format), Charsets.UTF_8)

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = format.mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, file.name)
        clipData = ClipData.newUri(context.contentResolver, file.name, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_export_chooser)))
}

private fun shareDiagnostics(context: Context, diagnosticsText: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Owl Bike V1 Tracker diagnostics")
        putExtra(Intent.EXTRA_TEXT, diagnosticsText)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_diagnostics_chooser)))
}

private data class WeeklySummary(
    val sessionCount: Int,
    val durationSeconds: Long,
    val distanceMeters: Double?,
    val calories: Int?,
)

private fun weeklySummary(
    sessions: List<WorkoutSessionEntity>,
    nowMillis: Long = System.currentTimeMillis(),
): WeeklySummary {
    val weekStartMillis = nowMillis - 7L * 24L * 60L * 60L * 1000L
    val recent = sessions.filter { it.startTimeMillis >= weekStartMillis }
    val distanceValues = recent.mapNotNull { it.totalDistanceMeters }
    val calorieValues = recent.mapNotNull { it.totalCalories }
    return WeeklySummary(
        sessionCount = recent.size,
        durationSeconds = recent.sumOf { session ->
            val end = session.endTimeMillis ?: nowMillis
            ((end - session.startTimeMillis) / 1000).coerceAtLeast(0)
        },
        distanceMeters = distanceValues.takeIf { it.isNotEmpty() }?.sum(),
        calories = calorieValues.takeIf { it.isNotEmpty() }?.sum(),
    )
}

@Composable
private fun raceColor(race: GhostRaceState, goalCompleted: Boolean = false): Color {
    return when {
        goalCompleted || race.completed -> AppGold
        race.isActive && race.zone == RaceZone.Ahead -> AppSuccess
        race.isActive && race.zone == RaceZone.Behind -> AppWarning
        race.isActive -> AppCyan
        else -> AppMuted
    }
}

@Composable
private fun raceDeltaText(state: TrackerUiState): String {
    val race = state.ghostRace
    val goal = state.workoutGoal
    val unitSystem = state.settings.unitSystem
    val goalProgress = RaceCalculator.goalProgressState(
        goal = goal,
        currentDistanceMeters = state.currentMetrics.distanceMeters,
        currentCalories = state.currentMetrics.calories,
    )
    if (!goal.isActive) return stringResource(R.string.goal_riding_without_goal)
    if (!race.isActive) {
        return if (goalProgress.completed) {
            stringResource(
                R.string.race_goal_without_shadow,
                formatRaceMetric(goal.type, goalProgress.targetValue, unitSystem),
            )
        } else {
            stringResource(
                R.string.race_to_goal_without_shadow,
                formatRaceMetric(goal.type, goalProgress.remainingValue, unitSystem),
            )
        }
    }
    if (goalProgress.completed) {
        return stringResource(
            R.string.race_goal_completed,
            formatRaceMetric(goal.type, goalProgress.targetValue, unitSystem),
        )
    }
    val delta = formatRaceMetric(goal.type, abs(race.deltaValue), unitSystem)
    return if (race.deltaValue >= 0.0) {
        stringResource(R.string.race_ahead_by, delta)
    } else {
        stringResource(R.string.race_behind_by, delta)
    }
}

@Composable
private fun goalSheetSummary(goal: WorkoutGoal, unitSystem: UnitSystem): String {
    return if (!goal.isActive) {
        stringResource(R.string.goal_not_set)
    } else {
        stringResource(R.string.goal_sheet_current, goalSheetValue(goal, unitSystem))
    }
}

@Composable
private fun goalSheetValue(goal: WorkoutGoal, unitSystem: UnitSystem): String {
    return if (!goal.isActive) {
        stringResource(R.string.goal_not_set)
    } else {
        formatRaceMetric(goal.type, goal.targetValue ?: 0.0, unitSystem)
    }
}

private fun formatRaceMetric(
    type: GoalType,
    value: Double,
    unitSystem: UnitSystem,
): String {
    return when (type) {
        GoalType.Distance -> MeasurementFormatter.distance(value, unitSystem, 2)
        GoalType.Calories -> "${value.format(0)} kcal"
        GoalType.None -> value.format(0)
    }
}

private fun isHrDanger(state: TrackerUiState): Boolean {
    return (state.currentMetrics.heartRateBpm ?: 0) >= 180
}

private fun sessionActualValue(session: WorkoutSessionEntity, type: GoalType): Double? {
    return when (type) {
        GoalType.Distance -> session.totalDistanceMeters
        GoalType.Calories -> session.totalCalories?.toDouble()
        GoalType.None -> null
    }
}

private fun sessionMedianValue(session: WorkoutSessionEntity, type: GoalType): Double? {
    return when (type) {
        GoalType.Distance -> session.baselineMedianDistanceMeters
        GoalType.Calories -> session.baselineMedianCalories?.toDouble()
        GoalType.None -> null
    }
}

private fun sessionTargetValue(session: WorkoutSessionEntity, type: GoalType): Double? {
    return when (type) {
        GoalType.Distance -> session.goalTargetDistanceMeters
        GoalType.Calories -> session.goalTargetCalories?.toDouble()
        GoalType.None -> null
    }
}

@Composable
private fun finishDeltaText(
    type: GoalType,
    actual: Double?,
    median: Double?,
    unitSystem: UnitSystem,
): String {
    if (type == GoalType.None || actual == null || median == null) {
        return stringResource(R.string.ride_saved_body)
    }
    val delta = actual - median
    val formatted = formatRaceMetric(type, abs(delta), unitSystem)
    return if (delta >= 0.0) {
        stringResource(R.string.finished_ahead_by, formatted)
    } else {
        stringResource(R.string.finished_behind_by, formatted)
    }
}

@Composable
private fun SectionTitle(title: String, meta: String? = null) {
    Row(
        Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = AppInk,
        )
        if (meta != null) {
            Text(
                meta,
                style = MaterialTheme.typography.labelSmall,
                color = AppMuted,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun EmptyStatePanel(message: String) {
    Panel {
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = AppMuted,
        )
    }
}

@Composable
private fun NoticePanel(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, AppAccent.copy(alpha = 0.20f)),
        colors = CardDefaults.cardColors(containerColor = AppAccentSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(14.dp), content = content)
    }
}

@Composable
private fun SummaryStat(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.heightIn(min = 64.dp),
        shape = RoundedCornerShape(8.dp),
        color = AppSurfaceMuted,
    ) {
        Column(Modifier.padding(10.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = AppMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                color = AppInk,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun Panel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, AppBorder),
        colors = CardDefaults.cardColors(containerColor = AppSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(14.dp), content = content)
    }
}

private fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)

private fun formatDurationSeconds(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}

private fun formatDate(millis: Long): String {
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(millis))
}

private fun formatDateOnly(millis: Long): String {
    return DateFormat.getDateInstance(DateFormat.SHORT).format(Date(millis))
}

private fun formatDuration(startMillis: Long, endMillis: Long?): String {
    val end = endMillis ?: System.currentTimeMillis()
    val total = ((end - startMillis) / 1000).coerceAtLeast(0)
    val minutes = total / 60
    val seconds = total % 60
    return "%02d:%02d".format(minutes, seconds)
}
