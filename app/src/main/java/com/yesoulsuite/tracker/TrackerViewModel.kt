package com.yesoulsuite.tracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yesoulsuite.tracker.ble.BikeMetrics
import com.yesoulsuite.tracker.ble.BleConnectionState
import com.yesoulsuite.tracker.ble.BleDeviceItem
import com.yesoulsuite.tracker.ble.YesoulBleClient
import com.yesoulsuite.tracker.data.DiagnosticSnapshotEntity
import com.yesoulsuite.tracker.data.WorkoutDatabase
import com.yesoulsuite.tracker.data.WorkoutRepository
import com.yesoulsuite.tracker.data.WorkoutSampleEntity
import com.yesoulsuite.tracker.data.WorkoutSessionEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class TrackerViewModel(application: Application) : AndroidViewModel(application) {
    private val bleClient = YesoulBleClient(application.applicationContext)
    private val repository = WorkoutRepository(WorkoutDatabase.get(application).workoutDao())

    private val _uiState = MutableStateFlow(TrackerUiState())
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()

    private var activeSession: WorkoutSessionEntity? = null
    private var samplesJob: Job? = null

    init {
        refreshPermissions()
        viewModelScope.launch {
            bleClient.devices.collect { devices ->
                _uiState.update { it.copy(devices = devices) }
            }
        }
        viewModelScope.launch {
            bleClient.connection.collect { connection ->
                _uiState.update { it.copy(connection = connection) }
            }
        }
        viewModelScope.launch {
            bleClient.metrics.collect(::handleMetrics)
        }
        viewModelScope.launch {
            bleClient.diagnostics.collect { snapshot ->
                repository.saveDiagnostic(
                    DiagnosticSnapshotEntity(
                        createdAtMillis = snapshot.createdAtMillis,
                        deviceName = snapshot.deviceName,
                        deviceAddress = snapshot.deviceAddress,
                        servicesText = snapshot.text,
                    ),
                )
            }
        }
        viewModelScope.launch {
            bleClient.controlMessages.collect { message ->
                _uiState.update { state ->
                    state.copy(controlLog = (listOf(message) + state.controlLog).take(100))
                }
            }
        }
        viewModelScope.launch {
            repository.observeSessions().collect { sessions ->
                _uiState.update { it.copy(sessions = sessions) }
            }
        }
        viewModelScope.launch {
            repository.observeLatestDiagnostic().collect { diagnostic ->
                _uiState.update { it.copy(latestDiagnostic = diagnostic) }
            }
        }
    }

    fun requiredPermissions(): Array<String> = bleClient.requiredPermissions()

    fun refreshPermissions() {
        _uiState.update { it.copy(permissionsGranted = bleClient.hasRequiredPermissions()) }
    }

    fun selectScreen(screen: AppScreen) {
        _uiState.update { it.copy(screen = screen) }
    }

    fun startScan() {
        refreshPermissions()
        bleClient.startScan()
    }

    fun stopScan() = bleClient.stopScan()

    fun connect(device: BleDeviceItem) = bleClient.connect(device)

    fun disconnect() = bleClient.disconnect()

    fun startWorkout() {
        if (activeSession != null) return
        val now = System.currentTimeMillis()
        val connection = _uiState.value.connection
        val session = WorkoutSessionEntity(
            id = UUID.randomUUID().toString(),
            startTimeMillis = now,
            endTimeMillis = null,
            state = "running",
            deviceName = connection.deviceName,
            deviceAddress = connection.deviceAddress,
            totalDistanceMeters = null,
            totalCalories = null,
            averagePowerWatts = null,
            averageCadenceRpm = null,
            averageHeartRateBpm = null,
            sampleCount = 0,
        )
        activeSession = session
        viewModelScope.launch {
            repository.createSession(session)
            _uiState.update {
                it.copy(
                    isWorkoutRunning = true,
                    isWorkoutPaused = false,
                    activeSessionId = session.id,
                )
            }
        }
    }

    fun pauseWorkout() {
        val session = activeSession ?: return
        val paused = session.copy(state = "paused")
        activeSession = paused
        viewModelScope.launch {
            repository.updateSession(paused)
            _uiState.update { it.copy(isWorkoutPaused = true) }
        }
    }

    fun resumeWorkout() {
        val session = activeSession ?: return
        val running = session.copy(state = "running")
        activeSession = running
        viewModelScope.launch {
            repository.updateSession(running)
            _uiState.update { it.copy(isWorkoutPaused = false) }
        }
    }

    fun finishWorkout() {
        val session = activeSession ?: return
        activeSession = null
        viewModelScope.launch {
            repository.finishSession(session, System.currentTimeMillis())
            _uiState.update {
                it.copy(
                    isWorkoutRunning = false,
                    isWorkoutPaused = false,
                    activeSessionId = null,
                )
            }
        }
    }

    fun selectSession(session: WorkoutSessionEntity) {
        samplesJob?.cancel()
        _uiState.update { it.copy(selectedSession = session, selectedSamples = emptyList()) }
        samplesJob = viewModelScope.launch {
            repository.observeSamples(session.id).collect { samples ->
                _uiState.update { it.copy(selectedSamples = samples) }
            }
        }
    }

    fun clearSelectedSession() {
        samplesJob?.cancel()
        samplesJob = null
        _uiState.update { it.copy(selectedSession = null, selectedSamples = emptyList()) }
    }

    override fun onCleared() {
        bleClient.disconnect()
        super.onCleared()
    }

    private fun handleMetrics(metrics: BikeMetrics) {
        val state = _uiState.value
        val merged = state.currentMetrics.merge(metrics)
        _uiState.update { it.copy(currentMetrics = merged) }
        val session = activeSession ?: return
        if (!state.isWorkoutRunning || state.isWorkoutPaused) return

        val sample = WorkoutSampleEntity(
            sessionId = session.id,
            timestampMillis = metrics.timestampMillis,
            elapsedSeconds = ((metrics.timestampMillis - session.startTimeMillis) / 1000).coerceAtLeast(0),
            speedKmh = merged.speedKmh,
            cadenceRpm = merged.cadenceRpm,
            powerWatts = merged.powerWatts,
            heartRateBpm = merged.heartRateBpm,
            resistanceLevel = merged.resistanceLevel,
            distanceMeters = merged.distanceMeters,
            calories = merged.calories,
        )
        val updatedSession = session.copy(
            totalDistanceMeters = merged.distanceMeters ?: session.totalDistanceMeters,
            totalCalories = merged.calories ?: session.totalCalories,
            sampleCount = session.sampleCount + 1,
        )
        activeSession = updatedSession
        viewModelScope.launch {
            repository.saveSample(sample)
            repository.updateSession(updatedSession)
        }
    }
}

enum class AppScreen {
    Connect,
    Workout,
    History,
    Diagnostics,
}

data class TrackerUiState(
    val screen: AppScreen = AppScreen.Connect,
    val permissionsGranted: Boolean = false,
    val devices: List<BleDeviceItem> = emptyList(),
    val connection: BleConnectionState = BleConnectionState(),
    val currentMetrics: BikeMetrics = BikeMetrics(source = null),
    val isWorkoutRunning: Boolean = false,
    val isWorkoutPaused: Boolean = false,
    val activeSessionId: String? = null,
    val sessions: List<WorkoutSessionEntity> = emptyList(),
    val selectedSession: WorkoutSessionEntity? = null,
    val selectedSamples: List<WorkoutSampleEntity> = emptyList(),
    val latestDiagnostic: DiagnosticSnapshotEntity? = null,
    val controlLog: List<String> = emptyList(),
)
