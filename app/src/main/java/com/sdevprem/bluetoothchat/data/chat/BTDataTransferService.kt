package com.sdevprem.bluetoothchat.data.chat

import android.bluetooth.BluetoothSocket
import com.sdevprem.bluetoothchat.domain.chat.BTMsg
import com.sdevprem.bluetoothchat.domain.chat.TransferFailedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException

class BTDataTransferService(
    private val socket: BluetoothSocket
) {
    fun listenForIncomingMessage(): Flow<BTMsg> = flow {
        if (!socket.isConnected)
            return@flow
        val buffer = ByteArray(1024)
        while (true) {
            val byteCount = try {
                socket.inputStream.read(buffer)
            } catch (e: IOException) {
                throw TransferFailedException
            }

            emit(
                buffer
                    .decodeToString(endIndex = byteCount)
                    .toBTMsg(isFromLocalUser = false)
            )

        }
    }.flowOn(Dispatchers.IO)

    suspend fun sendMsg(bytes: ByteArray): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            socket.outputStream.write(bytes)
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }
}

