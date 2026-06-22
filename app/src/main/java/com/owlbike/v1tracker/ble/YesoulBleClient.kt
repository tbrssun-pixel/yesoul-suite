package com.owlbike.v1tracker.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.ArrayDeque
import java.util.Locale
import java.util.UUID

class YesoulBleClient(private val context: Context) {
    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val adapter: BluetoothAdapter? = bluetoothManager?.adapter
    private val cscParser = CscMeasurementParser()

    private val _devices = MutableStateFlow<List<BleDeviceItem>>(emptyList())
    val devices: StateFlow<List<BleDeviceItem>> = _devices.asStateFlow()

    private val _connection = MutableStateFlow(BleConnectionState())
    val connection: StateFlow<BleConnectionState> = _connection.asStateFlow()

    private val _metrics = MutableSharedFlow<BikeMetrics>(extraBufferCapacity = 64)
    val metrics: SharedFlow<BikeMetrics> = _metrics.asSharedFlow()

    private val _diagnostics = MutableSharedFlow<GattDiagnosticSnapshot>(extraBufferCapacity = 4)
    val diagnostics: SharedFlow<GattDiagnosticSnapshot> = _diagnostics.asSharedFlow()

    private val _controlMessages = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val controlMessages: SharedFlow<String> = _controlMessages.asSharedFlow()

    private var gatt: BluetoothGatt? = null
    private var controlPoint: BluetoothGattCharacteristic? = null
    private val operationQueue = ArrayDeque<GattOperation>()
    private var operationInProgress = false

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            handleScanResult(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            results.forEach(::handleScanResult)
        }

        override fun onScanFailed(errorCode: Int) {
            _connection.value = _connection.value.copy(
                isScanning = false,
                status = "Scan failed: $errorCode",
            )
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                _connection.value = _connection.value.copy(
                    isConnected = true,
                    isConnecting = false,
                    servicesDiscovered = false,
                    status = "Connected; discovering services",
                )
                if (hasConnectPermission()) {
                    gatt.discoverServices()
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                _connection.value = _connection.value.copy(
                    isConnected = false,
                    isConnecting = false,
                    servicesDiscovered = false,
                    status = "Disconnected: $status",
                    capabilities = BleCapabilities(),
                )
                controlPoint = null
                operationQueue.clear()
                operationInProgress = false
                safeCloseGatt()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                _connection.value = _connection.value.copy(
                    servicesDiscovered = false,
                    status = "Service discovery failed: $status",
                )
                return
            }
            val capabilities = inspectCapabilities(gatt)
            controlPoint = gatt
                .getService(BleUuids.FITNESS_MACHINE_SERVICE)
                ?.getCharacteristic(BleUuids.FITNESS_MACHINE_CONTROL_POINT)
            _connection.value = _connection.value.copy(
                servicesDiscovered = true,
                status = "Services discovered",
                capabilities = capabilities,
            )
            _diagnostics.tryEmit(
                GattDiagnosticSnapshot(
                    createdAtMillis = System.currentTimeMillis(),
                    deviceName = _connection.value.deviceName,
                    deviceAddress = _connection.value.deviceAddress,
                    text = formatServices(gatt),
                ),
            )
            subscribeDiagnosticsCharacteristics(gatt)
            enqueueDiagnosticReads(gatt)
        }

        @Deprecated("Deprecated on API 33")
        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
        ) {
            handleCharacteristicChanged(characteristic.uuid, characteristic.value ?: return)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
        ) {
            handleCharacteristicChanged(characteristic.uuid, value)
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int,
        ) {
            operationInProgress = false
            writeNext()
        }

        @Deprecated("Deprecated on API 33")
        @Suppress("DEPRECATION")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int,
        ) {
            handleCharacteristicRead(characteristic.uuid, characteristic.value ?: byteArrayOf(), status)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int,
        ) {
            handleCharacteristicRead(characteristic.uuid, value, status)
        }

    }

    fun requiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun hasRequiredPermissions(): Boolean {
        return requiredPermissions().all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (!hasRequiredPermissions()) {
            _connection.value = _connection.value.copy(status = "Missing Bluetooth permissions")
            return
        }
        val scanner = adapter?.bluetoothLeScanner
        if (adapter == null || scanner == null || adapter.isEnabled != true) {
            _connection.value = _connection.value.copy(status = "Bluetooth is off")
            return
        }
        _devices.value = emptyList()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        scanner.startScan(null, settings, scanCallback)
        _connection.value = _connection.value.copy(isScanning = true, status = "Scanning")
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (hasScanPermission()) {
            adapter?.bluetoothLeScanner?.stopScan(scanCallback)
        }
        _connection.value = _connection.value.copy(isScanning = false)
    }

    @SuppressLint("MissingPermission")
    fun connect(device: BleDeviceItem, autoConnect: Boolean = false) {
        stopScan()
        if (!hasConnectPermission()) {
            _connection.value = _connection.value.copy(status = "Missing Bluetooth connect permission")
            return
        }
        safeCloseGatt()
        val bluetoothDevice = adapter?.getRemoteDevice(device.address)
        if (bluetoothDevice == null) {
            _connection.value = _connection.value.copy(status = "Device not found")
            return
        }
        _connection.value = BleConnectionState(
            isConnecting = true,
            deviceName = device.name,
            deviceAddress = device.address,
            status = if (autoConnect) "Reconnecting" else "Connecting",
        )
        gatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothDevice.connectGatt(context, autoConnect, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            bluetoothDevice.connectGatt(context, autoConnect, gattCallback)
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        if (hasConnectPermission()) {
            gatt?.disconnect()
        }
        safeCloseGatt()
        _connection.value = BleConnectionState(status = "Disconnected")
    }

    private fun handleScanResult(result: ScanResult) {
        val recordUuids = result.scanRecord?.serviceUuids.orEmpty().map { it.uuid.toString() }
        val item = BleDeviceItem(
            name = result.device.name ?: result.scanRecord?.deviceName,
            address = result.device.address,
            rssi = result.rssi,
            serviceUuids = recordUuids,
            type = BleDeviceClassifier.classify(result.device.name ?: result.scanRecord?.deviceName, recordUuids),
            lastSeenMillis = System.currentTimeMillis(),
        )
        val merged = (_devices.value.filterNot { it.address == item.address } + item)
            .sortedWith(compareBy<BleDeviceItem> { it.type.scanSortRank }.thenByDescending { it.rssi })
        _devices.value = merged
    }

    private fun inspectCapabilities(gatt: BluetoothGatt): BleCapabilities {
        val ftms = gatt.getService(BleUuids.FITNESS_MACHINE_SERVICE)
        val csc = gatt.getService(BleUuids.CYCLING_SPEED_AND_CADENCE_SERVICE)
        val hrs = gatt.getService(BleUuids.HEART_RATE_SERVICE)
        val control = ftms?.getCharacteristic(BleUuids.FITNESS_MACHINE_CONTROL_POINT)
        return BleCapabilities(
            hasFitnessMachineService = ftms != null,
            hasIndoorBikeData = ftms?.getCharacteristic(BleUuids.INDOOR_BIKE_DATA) != null,
            hasFitnessMachineControlPoint = control != null,
            hasCyclingSpeedCadence = csc?.getCharacteristic(BleUuids.CSC_MEASUREMENT) != null,
            hasHeartRate = hrs?.getCharacteristic(BleUuids.HEART_RATE_MEASUREMENT) != null,
            canSetResistance = false,
        )
    }

    private fun enqueueDiagnosticReads(gatt: BluetoothGatt) {
        gatt.services
            .flatMap { it.characteristics }
            .filter { it.properties.hasProperty(BluetoothGattCharacteristic.PROPERTY_READ) }
            .forEach { enqueueOperation(GattOperation.CharacteristicRead(it)) }
    }

    private fun subscribeDiagnosticsCharacteristics(gatt: BluetoothGatt) {
        val characteristics = gatt.services
            .flatMap { it.characteristics }
            .filter {
                it.properties.hasProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY) ||
                    it.properties.hasProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)
            }
        characteristics.forEach { enableUpdates(gatt, it) }
    }

    @SuppressLint("MissingPermission")
    private fun enableUpdates(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        if (!hasConnectPermission()) return
        val canNotify = characteristic.properties.hasProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)
        val canIndicate = characteristic.properties.hasProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)
        if (!canNotify && !canIndicate) return
        gatt.setCharacteristicNotification(characteristic, true)
        val descriptor = characteristic.getDescriptor(BleUuids.CLIENT_CHARACTERISTIC_CONFIG) ?: return
        val value = if (canIndicate) {
            BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
        } else {
            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        }
        enqueueOperation(GattOperation.DescriptorWrite(descriptor, value))
    }

    private fun handleCharacteristicChanged(uuid: UUID, value: ByteArray) {
        _controlMessages.tryEmit("Notify ${uuid.shortLabel()}: ${value.toHex()}")
        when (uuid) {
            BleUuids.INDOOR_BIKE_DATA -> FtmsIndoorBikeParser.parse(value)?.let {
                _controlMessages.tryEmit("Decoded Indoor Bike Data: ${it.shortSummary()}")
                _metrics.tryEmit(it)
            }
            BleUuids.CSC_MEASUREMENT -> cscParser.parse(value)?.let(_metrics::tryEmit)
            BleUuids.HEART_RATE_MEASUREMENT -> HeartRateMeasurementParser.parse(value)?.let(_metrics::tryEmit)
            BleUuids.FITNESS_MACHINE_CONTROL_POINT -> handleControlPointResponse(value)
            BleUuids.FITNESS_MACHINE_STATUS -> {
                _controlMessages.tryEmit("FTMS status: ${value.toHex()}")
                FtmsStatusParser.parse(value)?.let {
                    _controlMessages.tryEmit("Decoded FTMS Status: ${it.shortSummary()}")
                    _metrics.tryEmit(it)
                }
            }
        }
    }

    private fun handleCharacteristicRead(uuid: UUID, value: ByteArray, status: Int) {
        operationInProgress = false
        if (uuid == BleUuids.FITNESS_MACHINE_FEATURE && status == BluetoothGatt.GATT_SUCCESS) {
            val supportsResistance = supportsFtmsResistanceTargetSetting(value)
            val current = _connection.value
            val control = controlPoint
            val controlProps = control?.properties ?: 0
            val canWrite = controlProps.hasProperty(BluetoothGattCharacteristic.PROPERTY_WRITE) ||
                controlProps.hasProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)
            _connection.value = current.copy(
                capabilities = current.capabilities.copy(
                    canSetResistance = supportsResistance && control != null && canWrite,
                ),
                status = if (supportsResistance) current.status else "Manual resistance only",
            )
            _controlMessages.tryEmit(
                "Read FTMS feature: ${value.toHex()} | resistance target: " +
                    if (supportsResistance) "yes" else "no",
            )
        } else if (status == BluetoothGatt.GATT_SUCCESS) {
            _controlMessages.tryEmit("Read ${uuid.shortLabel()}: ${value.toHex()}")
        } else {
            _controlMessages.tryEmit("Read ${uuid.shortLabel()} failed: $status")
        }
        writeNext()
    }

    private fun handleControlPointResponse(value: ByteArray) {
        _controlMessages.tryEmit("FTMS response: ${value.toHex()}")
        if (value.size < 3 || value[0] != 0x80.toByte()) return
        val requestOpCode = value[1].toInt() and 0xFF
        val resultCode = value[2].toInt() and 0xFF
        if (requestOpCode == 0x00) {
            _controlMessages.tryEmit(
                "FTMS Request Control result: " +
                    if (resultCode == 0x01) "success" else "failed code=$resultCode",
            )
        }
    }

    private fun enqueueOperation(operation: GattOperation) {
        operationQueue.add(operation)
        writeNext()
    }

    @SuppressLint("MissingPermission")
    private fun writeNext() {
        val currentGatt = gatt ?: return
        if (operationInProgress || !hasConnectPermission()) return
        val next = operationQueue.poll() ?: return
        operationInProgress = true
        val started = when (next) {
            is GattOperation.DescriptorWrite -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    currentGatt.writeDescriptor(next.descriptor, next.value) == BluetoothGatt.GATT_SUCCESS
                } else {
                    @Suppress("DEPRECATION")
                    next.descriptor.value = next.value
                    @Suppress("DEPRECATION")
                    currentGatt.writeDescriptor(next.descriptor)
                }
            }
            is GattOperation.CharacteristicRead -> {
                currentGatt.readCharacteristic(next.characteristic)
            }
        }
        if (!started) {
            operationInProgress = false
            writeNext()
        }
    }

    private fun formatServices(gatt: BluetoothGatt): String {
        return buildString {
            gatt.services.forEach { service ->
                appendLine("Service ${service.uuid.shortLabel()} ${service.uuid}")
                service.characteristics.forEach { characteristic ->
                    appendLine(
                        "  Characteristic ${characteristic.uuid.shortLabel()} ${characteristic.uuid} " +
                            propertiesLabel(characteristic.properties),
                    )
                    characteristic.descriptors.forEach { descriptor ->
                        appendLine("    Descriptor ${descriptor.uuid.shortLabel()} ${descriptor.uuid}")
                    }
                }
            }
        }
    }

    private fun safeCloseGatt() {
        try {
            if (hasConnectPermission()) {
                gatt?.close()
            }
        } catch (_: SecurityException) {
        } finally {
            gatt = null
        }
    }

    private fun hasScanPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun hasConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
        }
    }
}

private sealed interface GattOperation {
    data class DescriptorWrite(val descriptor: BluetoothGattDescriptor, val value: ByteArray) : GattOperation
    data class CharacteristicRead(val characteristic: BluetoothGattCharacteristic) : GattOperation
}

private fun Int.hasProperty(property: Int): Boolean = this and property != 0

private fun UUID.shortLabel(): String = when (this) {
    BleUuids.FITNESS_MACHINE_SERVICE -> "FTMS"
    BleUuids.FITNESS_MACHINE_FEATURE -> "Fitness Machine Feature"
    BleUuids.INDOOR_BIKE_DATA -> "Indoor Bike Data"
    BleUuids.TRAINING_STATUS -> "Training Status"
    BleUuids.SUPPORTED_SPEED_RANGE -> "Supported Speed Range"
    BleUuids.SUPPORTED_RESISTANCE_LEVEL_RANGE -> "Supported Resistance Range"
    BleUuids.SUPPORTED_HEART_RATE_RANGE -> "Supported HR Range"
    BleUuids.SUPPORTED_POWER_RANGE -> "Supported Power Range"
    BleUuids.FITNESS_MACHINE_CONTROL_POINT -> "FTMS Control Point"
    BleUuids.FITNESS_MACHINE_STATUS -> "FTMS Status"
    BleUuids.YESOUL_VENDOR_WRITE -> "YESOUL Vendor Write"
    BleUuids.VENDOR_FFF0_SERVICE -> "FFF0"
    BleUuids.VENDOR_FFF1 -> "FFF1"
    BleUuids.VENDOR_FFF2 -> "FFF2"
    BleUuids.VENDOR_FFF3 -> "FFF3"
    BleUuids.VENDOR_FFF4 -> "FFF4"
    BleUuids.VENDOR_FFF5 -> "FFF5"
    BleUuids.VENDOR_FAB0_SERVICE -> "FAB0"
    BleUuids.VENDOR_FAB1 -> "FAB1"
    BleUuids.VENDOR_FAB2 -> "FAB2"
    BleUuids.VENDOR_FAB3 -> "FAB3"
    BleUuids.CYCLING_SPEED_AND_CADENCE_SERVICE -> "CSC"
    BleUuids.CSC_MEASUREMENT -> "CSC Measurement"
    BleUuids.HEART_RATE_SERVICE -> "Heart Rate"
    BleUuids.HEART_RATE_MEASUREMENT -> "Heart Rate Measurement"
    BleUuids.CLIENT_CHARACTERISTIC_CONFIG -> "CCCD"
    BleUuids.DEVICE_INFORMATION_SERVICE -> "Device Information"
    else -> toString().substring(4, 8).uppercase(Locale.US)
}

private fun propertiesLabel(properties: Int): String {
    val labels = buildList {
        if (properties.hasProperty(BluetoothGattCharacteristic.PROPERTY_READ)) add("read")
        if (properties.hasProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)) add("write")
        if (properties.hasProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)) add("write_no_response")
        if (properties.hasProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)) add("notify")
        if (properties.hasProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)) add("indicate")
    }
    return labels.joinToString(prefix = "[", postfix = "]")
}

private fun ByteArray.toHex(): String = joinToString(" ") { "%02X".format(it) }

private fun BikeMetrics.shortSummary(): String {
    return buildList {
        speedKmh?.let { add("speed=${"%.1f".format(it)}km/h") }
        cadenceRpm?.let { add("cadence=${"%.0f".format(it)}rpm") }
        powerWatts?.let { add("power=${it}W") }
        heartRateBpm?.let { add("hr=${it}bpm") }
        resistanceLevel?.let { add("resistance=${"%.1f".format(it)}") }
        distanceMeters?.let { add("distance=${"%.0f".format(it)}m") }
        calories?.let { add("calories=$it") }
        source?.let { add("source=$it") }
    }.joinToString(", ")
}
