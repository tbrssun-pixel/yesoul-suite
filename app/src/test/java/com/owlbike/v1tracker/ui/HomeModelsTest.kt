package com.owlbike.v1tracker.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class HomeModelsTest {
    @Test
    fun connectedTrainerOpensRide() {
        assertEquals(
            HomePrimaryAction.OpenRide,
            resolveHomePrimaryAction(
                isConnected = true,
                isConnecting = false,
                hasRememberedDevice = false,
            ),
        )
    }

    @Test
    fun rememberedTrainerReconnectsLast() {
        assertEquals(
            HomePrimaryAction.ReconnectLast,
            resolveHomePrimaryAction(
                isConnected = false,
                isConnecting = false,
                hasRememberedDevice = true,
            ),
        )
    }

    @Test
    fun noTrainerStartsFirstSetupConnect() {
        assertEquals(
            HomePrimaryAction.OpenFirstSetupConnect,
            resolveHomePrimaryAction(
                isConnected = false,
                isConnecting = false,
                hasRememberedDevice = false,
            ),
        )
    }

    @Test
    fun connectingTrainerWaitsForConnection() {
        assertEquals(
            HomePrimaryAction.WaitForConnection,
            resolveHomePrimaryAction(
                isConnected = false,
                isConnecting = true,
                hasRememberedDevice = true,
            ),
        )
    }
}
