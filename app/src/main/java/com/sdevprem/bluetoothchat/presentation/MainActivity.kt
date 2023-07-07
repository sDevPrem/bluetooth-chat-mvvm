package com.sdevprem.bluetoothchat.presentation

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.sdevprem.bluetoothchat.presentation.components.ChatScreen
import com.sdevprem.bluetoothchat.presentation.components.DeviceScreen
import com.sdevprem.bluetoothchat.ui.theme.BluetoothChatTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val btManager by lazy {
        this.getSystemService(BluetoothManager::class.java)
    }
    private val btAdapter by lazy {
        btManager?.adapter
    }

    private val isBTEnabled: Boolean
        get() = btAdapter?.isEnabled == true

    private val enableBluetoothDevice = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {/*no needed*/ }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        var canEnableBluetooth = true

        requiredPermissions.forEach {
            if (perms[it] == false)
                canEnableBluetooth = false
        }

        if (canEnableBluetooth && !isBTEnabled) {
            enableBluetoothDevice.launch(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            )
        }
    }

    private val requiredPermissions = ArrayList<String>().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }.toTypedArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasBTPermission()) {
            permissionLauncher.launch(requiredPermissions)
        } else if (!isBTEnabled)
            enableBluetoothDevice.launch(
                Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            )

        setContent {
            BluetoothChatTheme {
                val viewModel = hiltViewModel<BTViewModel>()
                val state by viewModel.state.collectAsState()

                LaunchedEffect(key1 = state.errorMsg) {
                    state.errorMsg?.let {
                        Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                    }
                }

                LaunchedEffect(key1 = state.isConnected) {
                    if (state.isConnected) {
                        Toast.makeText(
                            this@MainActivity.applicationContext,
                            "You're connected",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        state.isConnecting -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                                Text(text = "Connecting...")
                            }
                        }

                        state.isConnected -> {
                            ChatScreen(
                                state = state,
                                onDisconnect = viewModel::disconnectFromDevice,
                                onSendMsg = viewModel::sendMsg
                            )
                        }

                        else -> DeviceScreen(
                            state = state,
                            onStartScan = viewModel::startScan,
                            onStopScan = viewModel::stopScan,
                            onDeviceClick = viewModel::connectToDevice,
                            onStartServer = viewModel::waitForIncomingConnection
                        )
                    }
                }
            }
        }
    }

    private fun hasBTPermission(): Boolean {
        var hasPermission = true
        requiredPermissions.forEach {
            if (ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED)
                hasPermission = false
        }
        return hasPermission
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BluetoothChatTheme {
        Greeting("Android")
    }
}