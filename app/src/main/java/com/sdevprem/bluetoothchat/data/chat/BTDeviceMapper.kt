package com.sdevprem.bluetoothchat.data.chat

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import com.sdevprem.bluetoothchat.domain.chat.BTDevice

@SuppressLint("MissingPermission")
fun BluetoothDevice.toBTDevice() = BTDevice(
    name = this.name,
    address = this.address
)