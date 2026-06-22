package com.owlbike.v1tracker.ble

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

object BleSessionManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private lateinit var appContext: Context
    private lateinit var client: YesoulBleClient
    private var initialized = false
    private var manualDisconnect = true
    private var reconnectJob: Job? = null
    private var lastDevice: BleDeviceItem? = null

    var keepAliveActive: Boolean = false
        private set

    val devices: StateFlow<List<BleDeviceItem>>
        get() = client.devices
    val connection: StateFlow<BleConnectionState>
        get() = client.connection
    val metrics: SharedFlow<BikeMetrics>
        get() = client.metrics
    val diagnostics: SharedFlow<GattDiagnosticSnapshot>
        get() = client.diagnostics
    val controlMessages: SharedFlow<String>
        get() = client.controlMessages

    fun get(context: Context): BleSessionManager {
        ensureInitialized(context.applicationContext)
        return this
    }

    fun requiredPermissions(): Array<String> = client.requiredPermissions()

    fun hasRequiredPermissions(): Boolean = client.hasRequiredPermissions()

    fun startScan() = client.startScan()

    fun stopScan() = client.stopScan()

    fun connect(device: BleDeviceItem) {
        val connection = client.connection.value
        if (
            connection.deviceAddress != device.address &&
            (connection.isConnecting || connection.isConnected)
        ) {
            return
        }
        manualDisconnect = false
        keepAliveActive = true
        lastDevice = device
        if (client.hasRequiredPermissions()) {
            startKeepAliveService()
        }
        client.connect(device)
    }

    fun disconnect() {
        manualDisconnect = true
        keepAliveActive = false
        reconnectJob?.cancel()
        reconnectJob = null
        client.disconnect()
        stopKeepAliveService()
    }

    private fun ensureInitialized(context: Context) {
        if (initialized) return
        appContext = context
        client = YesoulBleClient(context)
        initialized = true
        observeConnection()
    }

    private fun observeConnection() {
        scope.launch {
            client.connection.collect { connection ->
                if (connection.isConnected || connection.isConnecting) {
                    reconnectJob?.cancel()
                    reconnectJob = null
                } else if (keepAliveActive && !manualDisconnect && lastDevice != null) {
                    scheduleReconnect()
                }
            }
        }
    }

    private fun scheduleReconnect() {
        if (reconnectJob?.isActive == true) return
        reconnectJob = scope.launch {
            delay(RECONNECT_DELAY_MS)
            val device = lastDevice ?: return@launch
            if (keepAliveActive && !manualDisconnect) {
                client.connect(device, autoConnect = true)
            }
        }
    }

    private fun startKeepAliveService() {
        val intent = Intent(appContext, BleConnectionService::class.java)
            .setAction(BleConnectionService.ACTION_START)
        runCatching {
            ContextCompat.startForegroundService(appContext, intent)
        }
    }

    private fun stopKeepAliveService() {
        val intent = Intent(appContext, BleConnectionService::class.java)
        appContext.stopService(intent)
    }

    private const val RECONNECT_DELAY_MS = 2_500L
}
