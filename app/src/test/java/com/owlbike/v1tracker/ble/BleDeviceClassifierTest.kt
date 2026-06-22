package com.owlbike.v1tracker.ble

import org.junit.Assert.assertEquals
import org.junit.Test

class BleDeviceClassifierTest {
    @Test
    fun `classifies fitness machine service as trainer`() {
        val type = BleDeviceClassifier.classify(
            name = null,
            serviceUuids = listOf(BleUuids.FITNESS_MACHINE_SERVICE.toString()),
        )

        assertEquals(BleDeviceType.Trainer, type)
    }

    @Test
    fun `classifies cycling services as cycling device`() {
        assertEquals(
            BleDeviceType.CyclingDevice,
            BleDeviceClassifier.classify(
                name = null,
                serviceUuids = listOf(BleUuids.CYCLING_SPEED_AND_CADENCE_SERVICE.toString()),
            ),
        )
        assertEquals(
            BleDeviceType.CyclingDevice,
            BleDeviceClassifier.classify(
                name = null,
                serviceUuids = listOf(BleUuids.CYCLING_POWER_SERVICE.toString()),
            ),
        )
    }

    @Test
    fun `keeps heart rate only devices as other`() {
        val type = BleDeviceClassifier.classify(
            name = null,
            serviceUuids = listOf(BleUuids.HEART_RATE_SERVICE.toString()),
        )

        assertEquals(BleDeviceType.Other, type)
    }

    @Test
    fun `classifies trainer-like names as trainer`() {
        listOf("YESOUL", "YSV100637", "Bike V1", "Indoor Cycle").forEach { name ->
            assertEquals(
                BleDeviceType.Trainer,
                BleDeviceClassifier.classify(name = name, serviceUuids = emptyList()),
            )
        }
    }

    @Test
    fun `keeps address only devices as other`() {
        val type = BleDeviceClassifier.classify(
            name = null,
            serviceUuids = emptyList(),
        )

        assertEquals(BleDeviceType.Other, type)
    }

    @Test
    fun `keeps huawei watch names as other`() {
        val type = BleDeviceClassifier.classify(
            name = "HUAWEI WATCH FIT 3-75F",
            serviceUuids = emptyList(),
        )

        assertEquals(BleDeviceType.Other, type)
    }

    @Test
    fun `keeps device information only devices as other`() {
        val type = BleDeviceClassifier.classify(
            name = "HUAWEI WATCH FIT 3-75F",
            serviceUuids = listOf(BleUuids.DEVICE_INFORMATION_SERVICE.toString()),
        )

        assertEquals(BleDeviceType.Other, type)
    }

    @Test
    fun `keeps huawei heart rate only devices as other`() {
        val type = BleDeviceClassifier.classify(
            name = "HUAWEI WATCH FIT 3-75F",
            serviceUuids = listOf(BleUuids.HEART_RATE_SERVICE.toString()),
        )

        assertEquals(BleDeviceType.Other, type)
    }

    @Test
    fun `classifies remembered yesoul names as trainer candidates`() {
        val isTrainer = RememberedEquipmentClassifier.isLikelyTrainer(
            name = "YSV100637",
            serviceUuidsText = null,
        )

        assertEquals(true, isTrainer)
    }

    @Test
    fun `keeps remembered huawei watch as non trainer`() {
        val isTrainer = RememberedEquipmentClassifier.isLikelyTrainer(
            name = "HUAWEI WATCH FIT 3-75F",
            serviceUuidsText = BleUuids.HEART_RATE_SERVICE.toString(),
        )

        assertEquals(false, isTrainer)
    }

    @Test
    fun `remembers only discovered indoor bike connections`() {
        assertEquals(
            false,
            RememberedEquipmentClassifier.canRememberConnection(
                BleConnectionState(
                    isConnected = true,
                    servicesDiscovered = false,
                    deviceAddress = "AA:BB:CC:DD:EE:FF",
                    capabilities = BleCapabilities(hasIndoorBikeData = true),
                ),
            ),
        )
        assertEquals(
            false,
            RememberedEquipmentClassifier.canRememberConnection(
                BleConnectionState(
                    isConnected = true,
                    servicesDiscovered = true,
                    deviceAddress = "AA:BB:CC:DD:EE:FF",
                    capabilities = BleCapabilities(hasHeartRate = true),
                ),
            ),
        )
        assertEquals(
            true,
            RememberedEquipmentClassifier.canRememberConnection(
                BleConnectionState(
                    isConnected = true,
                    servicesDiscovered = true,
                    deviceAddress = "AA:BB:CC:DD:EE:FF",
                    capabilities = BleCapabilities(
                        hasFitnessMachineService = true,
                        hasIndoorBikeData = true,
                    ),
                ),
            ),
        )
    }
}
