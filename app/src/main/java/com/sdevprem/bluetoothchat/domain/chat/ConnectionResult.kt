package com.sdevprem.bluetoothchat.domain.chat

interface ConnectionResult {
    object ConnectionEstablished : ConnectionResult
    class ConnectionError(val msg: String) : ConnectionResult

    data class TransferSucceeded(val msg: BTMsg) : ConnectionResult
}