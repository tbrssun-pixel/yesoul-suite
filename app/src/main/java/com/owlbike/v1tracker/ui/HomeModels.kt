package com.owlbike.v1tracker.ui

enum class HomePrimaryAction {
    OpenRide,
    ReconnectLast,
    OpenFirstSetupConnect,
    WaitForConnection,
}

fun resolveHomePrimaryAction(
    isConnected: Boolean,
    isConnecting: Boolean,
    hasRememberedDevice: Boolean,
): HomePrimaryAction {
    return when {
        isConnected -> HomePrimaryAction.OpenRide
        isConnecting -> HomePrimaryAction.WaitForConnection
        hasRememberedDevice -> HomePrimaryAction.ReconnectLast
        else -> HomePrimaryAction.OpenFirstSetupConnect
    }
}
