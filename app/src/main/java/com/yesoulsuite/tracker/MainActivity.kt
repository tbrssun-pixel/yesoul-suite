package com.yesoulsuite.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yesoulsuite.tracker.ble.BleDeviceItem
import com.yesoulsuite.tracker.data.WorkoutSampleEntity
import com.yesoulsuite.tracker.data.WorkoutSessionEntity
import java.text.DateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: TrackerViewModel = viewModel()
            YesoulTheme {
                YesoulApp(viewModel)
            }
        }
    }
}

@Composable
private fun YesoulTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF25635A),
            secondary = Color(0xFF6B5B2A),
            tertiary = Color(0xFF7C4D57),
            surface = Color(0xFFF7F8F6),
            background = Color(0xFFF7F8F6),
        ),
        content = content,
    )
}

@Composable
private fun YesoulApp(viewModel: TrackerViewModel) {
    val state by viewModel.uiState.collectAsState()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        viewModel.refreshPermissions()
    }

    LaunchedEffect(Unit) {
        viewModel.refreshPermissions()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(Modifier.fillMaxSize()) {
            Header(state)
            AppTabs(state.screen, viewModel::selectScreen)
            when (state.screen) {
                AppScreen.Connect -> ConnectScreen(
                    state = state,
                    onGrantPermissions = { permissionLauncher.launch(viewModel.requiredPermissions()) },
                    onScan = viewModel::startScan,
                    onStopScan = viewModel::stopScan,
                    onConnect = viewModel::connect,
                    onDisconnect = viewModel::disconnect,
                )
                AppScreen.Workout -> WorkoutScreen(
                    state = state,
                    onStart = viewModel::startWorkout,
                    onPause = viewModel::pauseWorkout,
                    onResume = viewModel::resumeWorkout,
                    onFinish = viewModel::finishWorkout,
                )
                AppScreen.History -> HistoryScreen(
                    state = state,
                    onSelect = viewModel::selectSession,
                    onBack = viewModel::clearSelectedSession,
                )
                AppScreen.Diagnostics -> DiagnosticsScreen(state)
            }
        }
    }
}

@Composable
private fun Header(state: TrackerUiState) {
    Column(Modifier.padding(16.dp, 14.dp, 16.dp, 8.dp)) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "${stringResource(R.string.status)}: ${state.connection.status}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AppTabs(selected: AppScreen, onSelect: (AppScreen) -> Unit) {
    val tabs = listOf(
        AppScreen.Connect to stringResource(R.string.tab_connect),
        AppScreen.Workout to stringResource(R.string.tab_workout),
        AppScreen.History to stringResource(R.string.tab_history),
        AppScreen.Diagnostics to stringResource(R.string.tab_diagnostics),
    )
    TabRow(selectedTabIndex = tabs.indexOfFirst { it.first == selected }.coerceAtLeast(0)) {
        tabs.forEach { (screen, label) ->
            Tab(
                selected = screen == selected,
                onClick = { onSelect(screen) },
                text = { Text(label, maxLines = 1, style = MaterialTheme.typography.labelMedium) },
            )
        }
    }
}

@Composable
private fun ConnectScreen(
    state: TrackerUiState,
    onGrantPermissions: () -> Unit,
    onScan: () -> Unit,
    onStopScan: () -> Unit,
    onConnect: (BleDeviceItem) -> Unit,
    onDisconnect: () -> Unit,
) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        if (!state.permissionsGranted) {
            Panel {
                Text(stringResource(R.string.permissions_needed), fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Button(onClick = onGrantPermissions) {
                    Text(stringResource(R.string.grant_permissions))
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = if (state.connection.isScanning) onStopScan else onScan,
                enabled = state.permissionsGranted,
            ) {
                Text(if (state.connection.isScanning) stringResource(R.string.stop_scan) else stringResource(R.string.scan))
            }
            OutlinedButton(onClick = onDisconnect, enabled = state.connection.isConnected || state.connection.isConnecting) {
                Text(stringResource(R.string.disconnect))
            }
        }

        Spacer(Modifier.height(12.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            items(state.devices, key = { it.address }) { device ->
                DeviceRow(device, onConnect)
            }
        }
    }
}

@Composable
private fun DeviceRow(device: BleDeviceItem, onConnect: (BleDeviceItem) -> Unit) {
    Panel {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(Modifier.weight(1f)) {
                Text(device.name ?: stringResource(R.string.unknown_device), fontWeight = FontWeight.SemiBold)
                Text(device.address, style = MaterialTheme.typography.bodySmall)
                Text("RSSI ${device.rssi}", style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = { onConnect(device) }) {
                Text(stringResource(R.string.connect))
            }
        }
    }
}

@Composable
private fun WorkoutScreen(
    state: TrackerUiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onFinish: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MetricsGrid(state)
        WorkoutControls(state, onStart, onPause, onResume, onFinish)
        ManualResistanceInfo()
    }
}

@Composable
private fun MetricsGrid(state: TrackerUiState) {
    val metrics = state.currentMetrics
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricTile(stringResource(R.string.speed), metrics.speedKmh?.format(1) ?: "-", "km/h", Modifier.weight(1f))
            MetricTile(stringResource(R.string.cadence), metrics.cadenceRpm?.format(0) ?: "-", "rpm", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricTile(stringResource(R.string.power), metrics.powerWatts?.toString() ?: "-", "W", Modifier.weight(1f))
            MetricTile(stringResource(R.string.heart_rate), metrics.heartRateBpm?.toString() ?: "-", "bpm", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricTile(stringResource(R.string.distance), metrics.distanceMeters?.let { (it / 1000.0).format(2) } ?: "-", "km", Modifier.weight(1f))
            MetricTile(stringResource(R.string.calories), metrics.calories?.toString() ?: "-", "kcal", Modifier.weight(1f))
        }
        MetricTile(
            stringResource(R.string.telemetry_resistance),
            metrics.resistanceLevel?.format(1) ?: "-",
            "level",
            Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun MetricTile(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text(unit, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun WorkoutControls(
    state: TrackerUiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onFinish: () -> Unit,
) {
    Panel {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onStart, enabled = !state.isWorkoutRunning) {
                Text(stringResource(R.string.start))
            }
            if (state.isWorkoutPaused) {
                Button(onClick = onResume, enabled = state.isWorkoutRunning) {
                    Text(stringResource(R.string.resume))
                }
            } else {
                OutlinedButton(onClick = onPause, enabled = state.isWorkoutRunning) {
                    Text(stringResource(R.string.pause))
                }
            }
            OutlinedButton(onClick = onFinish, enabled = state.isWorkoutRunning) {
                Text(stringResource(R.string.finish))
            }
        }
    }
}

@Composable
private fun ManualResistanceInfo() {
    Panel {
        Text(stringResource(R.string.manual_resistance), fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(R.string.manual_resistance_note),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HistoryScreen(
    state: TrackerUiState,
    onSelect: (WorkoutSessionEntity) -> Unit,
    onBack: () -> Unit,
) {
    val selected = state.selectedSession
    if (selected != null) {
        SessionDetail(selected, state.selectedSamples, onBack)
        return
    }
    LazyColumn(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (state.sessions.isEmpty()) {
            item { Text(stringResource(R.string.history_empty)) }
        }
        items(state.sessions, key = { it.id }) { session ->
            Panel {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text(formatDate(session.startTimeMillis), fontWeight = FontWeight.SemiBold)
                        Text("${stringResource(R.string.duration)}: ${formatDuration(session.startTimeMillis, session.endTimeMillis)}")
                        Text("${stringResource(R.string.samples)}: ${session.sampleCount}")
                    }
                    TextButton(onClick = { onSelect(session) }) {
                        Text(">")
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionDetail(
    session: WorkoutSessionEntity,
    samples: List<WorkoutSampleEntity>,
    onBack: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(onClick = onBack) { Text(stringResource(R.string.back)) }
        Panel {
            Text(formatDate(session.startTimeMillis), fontWeight = FontWeight.SemiBold)
            Text("${stringResource(R.string.duration)}: ${formatDuration(session.startTimeMillis, session.endTimeMillis)}")
            Text("${stringResource(R.string.device)}: ${session.deviceName ?: "-"}")
            Text("${stringResource(R.string.distance)}: ${session.totalDistanceMeters?.let { (it / 1000.0).format(2) } ?: "-"} km")
            Text("${stringResource(R.string.calories)}: ${session.totalCalories ?: "-"}")
            Text("${stringResource(R.string.power)} ${stringResource(R.string.average_short)}: ${session.averagePowerWatts?.format(0) ?: "-"} W")
            Text("${stringResource(R.string.cadence)} ${stringResource(R.string.average_short)}: ${session.averageCadenceRpm?.format(0) ?: "-"} rpm")
            Text("${stringResource(R.string.heart_rate)} ${stringResource(R.string.average_short)}: ${session.averageHeartRateBpm?.format(0) ?: "-"} bpm")
        }
        Panel {
            Text("${stringResource(R.string.samples)}: ${samples.size}", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            samples.takeLast(20).forEach { sample ->
                Text(
                    "${sample.elapsedSeconds}s  " +
                        "${sample.speedKmh?.format(1) ?: "-"} km/h  " +
                        "${sample.cadenceRpm?.format(0) ?: "-"} rpm  " +
                        "${sample.powerWatts ?: "-"} W  " +
                        "${sample.heartRateBpm ?: "-"} bpm",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun DiagnosticsScreen(state: TrackerUiState) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    val rawBleLogTitle = stringResource(R.string.raw_ble_log)
    val diagnosticsText = diagnosticsText(state, rawBleLogTitle)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Button(
            onClick = {
                clipboard.setText(AnnotatedString(diagnosticsText))
                copied = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (copied) stringResource(R.string.copied) else stringResource(R.string.copy_diagnostics))
        }
        Panel {
            val caps = state.connection.capabilities
            Text("FTMS: ${caps.hasFitnessMachineService}")
            Text("Indoor Bike Data: ${caps.hasIndoorBikeData}")
            Text("${stringResource(R.string.control_point_present)}: ${caps.hasFitnessMachineControlPoint}")
            Text("CSC: ${caps.hasCyclingSpeedCadence}")
            Text("HRS: ${caps.hasHeartRate}")
            Text("${stringResource(R.string.physical_control)}: ${stringResource(R.string.manual_only)}")
        }
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
}

private fun diagnosticsText(state: TrackerUiState, rawBleLogTitle: String): String {
    val caps = state.connection.capabilities
    return buildString {
        appendLine("YESOUL Tracker diagnostics")
        appendLine("Status: ${state.connection.status}")
        appendLine("Device: ${state.connection.deviceName ?: "-"} ${state.connection.deviceAddress ?: "-"}")
        appendLine("Connected: ${state.connection.isConnected}")
        appendLine("FTMS: ${caps.hasFitnessMachineService}")
        appendLine("Indoor Bike Data: ${caps.hasIndoorBikeData}")
        appendLine("FTMS Control Point present: ${caps.hasFitnessMachineControlPoint}")
        appendLine("CSC: ${caps.hasCyclingSpeedCadence}")
        appendLine("HRS: ${caps.hasHeartRate}")
        appendLine("Physical load control: manual only")
        appendLine()
        appendLine("Latest GATT snapshot")
        appendLine(state.latestDiagnostic?.servicesText ?: "No data")
        appendLine()
        appendLine(rawBleLogTitle)
        state.controlLog.forEach { appendLine(it) }
    }
}

@Composable
private fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(Modifier.padding(12.dp), content = content)
    }
}

private fun Double.format(decimals: Int): String = "%.${decimals}f".format(this)

private fun formatDate(millis: Long): String {
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(millis))
}

private fun formatDuration(startMillis: Long, endMillis: Long?): String {
    val end = endMillis ?: System.currentTimeMillis()
    val total = ((end - startMillis) / 1000).coerceAtLeast(0)
    val minutes = total / 60
    val seconds = total % 60
    return "%02d:%02d".format(minutes, seconds)
}
