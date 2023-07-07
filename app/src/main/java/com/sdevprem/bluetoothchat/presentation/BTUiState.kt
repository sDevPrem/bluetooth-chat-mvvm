package com.sdevprem.bluetoothchat.presentation

import com.sdevprem.bluetoothchat.domain.chat.BTDevice
import com.sdevprem.bluetoothchat.domain.chat.BTMsg

data class BTUiState(
    val scannedDevices: List<BTDevice> = emptyList(),
    val pairedDevices: List<BTDevice> = emptyList(),
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val errorMsg: String? = null,
    val messages: List<BTMsg> = emptyList()
)