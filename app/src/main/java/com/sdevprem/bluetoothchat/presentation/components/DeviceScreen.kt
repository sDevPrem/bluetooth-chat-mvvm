package com.sdevprem.bluetoothchat.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sdevprem.bluetoothchat.domain.chat.BTDevice
import com.sdevprem.bluetoothchat.presentation.BTUiState

@Composable
fun DeviceScreen(
    state: BTUiState,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onDeviceClick: (BTDevice) -> Unit,
    onStartServer: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        BTDeviceList(
            pairedDevices = state.pairedDevices,
            scannedDevices = state.scannedDevices,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onDeviceClick = onDeviceClick
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = onStartScan) {
                Text(text = "Start scan")
            }
            Button(onClick = onStopScan) {
                Text(text = "Stop scan")
            }
            Button(onClick = onStartServer) {
                Text(text = "Start server")
            }
        }
    }
}

@Composable
private fun BTDeviceList(
    pairedDevices: List<BTDevice>,
    scannedDevices: List<BTDevice>,
    modifier: Modifier,
    onDeviceClick: (BTDevice) -> Unit
) {

    LazyColumn(modifier = modifier) {
        item {
            Text(
                text = "Paired Devices",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
        }

        items(pairedDevices) {
            Text(
                text = it.name ?: "(No name)",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDeviceClick(it) }
                    .padding(16.dp)
            )
        }
        item {
            Text(
                text = "Scanned Devices",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
        }

        items(scannedDevices) {
            Text(
                text = it.name ?: "(No name)",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDeviceClick(it) }
                    .padding(16.dp)
            )
        }
    }

}