package com.sdevprem.bluetoothchat.domain.chat

import kotlinx.coroutines.flow.StateFlow

interface BTController {
    val scannedDevices: StateFlow<List<BTDevice>>
    val pairedDevices: StateFlow<List<BTDevice>>

    fun startDiscovery()
    fun stopDiscovery()

    fun release()
}