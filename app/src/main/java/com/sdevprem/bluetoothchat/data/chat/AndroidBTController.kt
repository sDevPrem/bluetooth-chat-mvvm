package com.sdevprem.bluetoothchat.data.chat

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import com.sdevprem.bluetoothchat.domain.chat.BTController
import com.sdevprem.bluetoothchat.domain.chat.BTDevice
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@SuppressLint("MissingPermission")
class AndroidBTController @Inject constructor(
    @ApplicationContext private val context: Context
) : BTController {

    private val btManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val btAdapter by lazy {
        btManager?.adapter
    }

    private val _scannedDevices = MutableStateFlow<List<BTDevice>>(emptyList())
    override val scannedDevices: StateFlow<List<BTDevice>>
        get() = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BTDevice>>(emptyList())
    override val pairedDevices: StateFlow<List<BTDevice>>
        get() = _pairedDevices.asStateFlow()

    private val deviceFoundReceiver = DeviceFoundReceiver { device ->
        val newDevice = device.toBTDevice()
        _scannedDevices.update {
            if (newDevice in it)
                it
            else it + newDevice
        }
    }

    init {
        updatePairedDevices()
    }

    /*TODO : Register a broadcast receiver for ACTION_DISCOVERY_STARTED
       and ACTION_DISCOVERY_FINISHED intents to determine exactly when
       the discovery starts and completes. And expose a state for this
       to show a progress bar in the ui.
       */
    override fun startDiscovery() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN))
            return
        context.registerReceiver(
            deviceFoundReceiver,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )
        updatePairedDevices()
        btAdapter?.startDiscovery()
    }

    override fun stopDiscovery() {
        if (hasPermission(Manifest.permission.BLUETOOTH_SCAN))
            btAdapter?.cancelDiscovery()
    }

    override fun release() {
        context.unregisterReceiver(deviceFoundReceiver)
        stopDiscovery()
    }

    private fun updatePairedDevices() {
        if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT))
            btAdapter
                ?.bondedDevices
                ?.map { it.toBTDevice() }
                ?.also { devices -> _pairedDevices.update { devices } }
    }

    private fun hasPermission(permission: String) =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
            true
        else
            context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

}