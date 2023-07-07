package com.sdevprem.bluetoothchat.domain.chat

data class BTMsg(
    val msg: String,
    val senderName: String,
    val isFromLocalUser: Boolean
)