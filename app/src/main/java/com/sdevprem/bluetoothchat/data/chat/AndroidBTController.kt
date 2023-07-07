package com.sdevprem.bluetoothchat.data.chat

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import com.sdevprem.bluetoothchat.domain.chat.BTController
import com.sdevprem.bluetoothchat.domain.chat.BTDevice
import com.sdevprem.bluetoothchat.domain.chat.BTMsg
import com.sdevprem.bluetoothchat.domain.chat.ConnectionResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

@SuppressLint("MissingPermission")
class AndroidBTController @Inject constructor(
    @ApplicationContext private val context: Context,
) : BTController {

    private val btManager by lazy {
        context.getSystemService(BluetoothManager::class.java)
    }
    private val btAdapter by lazy {
        btManager?.adapter
    }

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean>
        get() = _isConnected.asStateFlow()

    private val _scannedDevices = MutableStateFlow<List<BTDevice>>(emptyList())
    override val scannedDevices: StateFlow<List<BTDevice>>
        get() = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BTDevice>>(emptyList())
    override val pairedDevices: StateFlow<List<BTDevice>>
        get() = _pairedDevices.asStateFlow()

    private val _errors = MutableSharedFlow<String>()
    override val errors: SharedFlow<String>
        get() = _errors.asSharedFlow()

    private val deviceFoundReceiver = DeviceFoundReceiver { device ->
        val newDevice = device.toBTDevice()
        _scannedDevices.update {
            if (newDevice in it)
                it
            else it + newDevice
        }
    }

    private val btStateReceiver = BTStateReceiver { isConnected, btDevice ->
        if (btAdapter?.bondedDevices?.contains(btDevice) == true) {
            _isConnected.update { isConnected }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                _errors.emit("Can't connect to a non-paired device")
            }
        }
    }

    private var currentServerSocket: BluetoothServerSocket? = null
    private var currentClientSocket: BluetoothSocket? = null

    private var dataTransferService: BTDataTransferService? = null

    init {
        updatePairedDevices()
        context.registerReceiver(
            btStateReceiver,
            IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
                addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            }
        )
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

    override fun startBTServer(): Flow<ConnectionResult> = flow {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT))
            throw SecurityException("NO BLUETOOTH CONNECT permission granted")

        currentServerSocket = btAdapter?.listenUsingRfcommWithServiceRecord(
            "chat_service",
            UUID.fromString(SERVICE_UUID)
        )
        var shouldLoop = true
        while (shouldLoop) {
            currentClientSocket = try {
                currentServerSocket?.accept()
            } catch (e: IOException) {
                shouldLoop = false
                null
            }
            emit(ConnectionResult.ConnectionEstablished)
            currentClientSocket?.let {
                currentServerSocket?.close()
                val service = BTDataTransferService(it)
                dataTransferService = service
                emitAll(
                    service
                        .listenForIncomingMessage()
                        .map { msg ->
                            ConnectionResult.TransferSucceeded(msg)
                        }
                )
            }
        }
    }.onCompletion {
        closeConnection()
    }.flowOn(Dispatchers.IO)

    override fun connectToDevice(device: BTDevice): Flow<ConnectionResult> = flow {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT))
            throw SecurityException("NO BLUETOOTH CONNECT permission granted")

        val bluetoothDevice = btAdapter?.getRemoteDevice(device.address)

        currentClientSocket = btAdapter
            ?.getRemoteDevice(device.address)
            ?.createRfcommSocketToServiceRecord(UUID.fromString(SERVICE_UUID))

        stopDiscovery()

        currentClientSocket?.let { socket ->
            try {
                socket.connect()
                emit(ConnectionResult.ConnectionEstablished)

                BTDataTransferService(socket = socket).also {
                    dataTransferService = it
                    emitAll(
                        it.listenForIncomingMessage()
                            .map { msg ->
                                ConnectionResult.TransferSucceeded(msg)
                            }
                    )
                }
            } catch (e: IOException) {
                socket.close()
                currentServerSocket = null
                emit(ConnectionResult.ConnectionError("Connection was interrupted"))
            }
        }
    }.onCompletion {
        closeConnection()
    }.flowOn(Dispatchers.IO)

    override suspend fun trySendMsg(msg: String): BTMsg? {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT) || dataTransferService == null)
            return null

        val btMsg = BTMsg(
            msg = msg,
            senderName = btAdapter?.name ?: "Unknown name",
            isFromLocalUser = true
        )

        dataTransferService?.sendMsg(btMsg.toByteArray())
        return btMsg
    }

    override fun closeConnection() {
        currentServerSocket?.close()
        currentClientSocket?.close()
        currentServerSocket = null
        currentClientSocket = null
    }

    override fun release() {
        try {
            context.unregisterReceiver(deviceFoundReceiver)
            context.unregisterReceiver(btStateReceiver)
        } catch (_: IllegalArgumentException) {
        }
        stopDiscovery()
        closeConnection()
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

    companion object {
        const val SERVICE_UUID = "835066b9-8d8f-4709-b336-ccce01116625"
    }

}